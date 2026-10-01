package com.kab.qershi.loan.management.application.usecase;

import com.kab.qershi.loan.management.domain.engine.AmortizationEngine;
import com.kab.qershi.loan.management.domain.model.LoanAccount;
import com.kab.qershi.loan.management.domain.model.LoanStatus;
import com.kab.qershi.loan.management.domain.model.RepaymentSchedule;
import com.kab.qershi.loan.management.domain.port.in.LoanDisbursementUseCase;
import com.kab.qershi.loan.management.domain.port.out.LoanAccountRepositoryPort;
import com.kab.qershi.loan.management.domain.port.out.RepaymentScheduleRepositoryPort;
import com.kab.qershi.loan.management.infrastructure.adapters.NotificationGrpcClientAdapter;
import com.kab.qershi.loan.management.infrastructure.persistence.entity.LoanAuditLogEntity;
import com.kab.qershi.loan.management.infrastructure.persistence.repository.SpringDataLoanAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Business logic service managing Loan Disbursement & Account Activation.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class LoanDisbursementService implements LoanDisbursementUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoanDisbursementService.class);

    private final LoanAccountRepositoryPort accountRepository;
    private final RepaymentScheduleRepositoryPort scheduleRepository;
    private final AmortizationEngine amortizationEngine;
    private final NotificationGrpcClientAdapter notificationAdapter;
    private final SpringDataLoanAuditLogRepository auditLogRepository;
    private final com.kab.qershi.loan.management.domain.port.out.AccountClientPort accountClientPort;
    private final com.kab.qershi.loan.management.infrastructure.persistence.repository.SpringDataLoanAccountGuarantorRepository guarantorRepository;

    public LoanDisbursementService(LoanAccountRepositoryPort accountRepository,
                                   RepaymentScheduleRepositoryPort scheduleRepository,
                                   AmortizationEngine amortizationEngine,
                                   NotificationGrpcClientAdapter notificationAdapter,
                                   SpringDataLoanAuditLogRepository auditLogRepository,
                                   com.kab.qershi.loan.management.domain.port.out.AccountClientPort accountClientPort,
                                   com.kab.qershi.loan.management.infrastructure.persistence.repository.SpringDataLoanAccountGuarantorRepository guarantorRepository) {
        this.accountRepository = accountRepository;
        this.scheduleRepository = scheduleRepository;
        this.amortizationEngine = amortizationEngine;
        this.notificationAdapter = notificationAdapter;
        this.auditLogRepository = auditLogRepository;
        this.accountClientPort = accountClientPort;
        this.guarantorRepository = guarantorRepository;
    }

    @Override
    @Transactional
    public LoanAccount disburseLoan(DisburseCommand command) {
        log.info("Processing loan disbursement for application ID: {}, User ID: {}, Amount: {}, IdempotencyKey: {}",
                command.applicationId(), command.userId(), command.amount(), command.idempotencyKey());

        // 1. Idempotency Check: Return existing account if application was already disbursed
        Optional<LoanAccount> existing = accountRepository.findByApplicationId(command.applicationId());
        if (existing.isPresent()) {
            log.info("Idempotent disbursement request detected for application ID {}. Returning existing account {}",
                    command.applicationId(), existing.get().getAccountNo());
            return existing.get();
        }

        // 2. Generate unique loan account number: LN-YYYYMMDD-XXXXXXXX
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String accountNo = "LN-" + datePrefix + "-" + uniqueSuffix;

        OffsetDateTime now = OffsetDateTime.now();

        // 3. Create Loan Account aggregate
        LoanAccount account = new LoanAccount(
                null,
                accountNo,
                command.applicationId(),
                command.userId(),
                command.productId(),
                command.amount(),
                command.interestRatePct(),
                command.termMonths(),
                command.repaymentFrequency(),
                command.interestType(),
                now,
                LoanStatus.DISBURSED,
                now,
                now
        );

        LoanAccount savedAccount = accountRepository.save(account);

        try {
            auditLogRepository.save(new LoanAuditLogEntity(
                    null,
                    savedAccount.getAccountNo(),
                    savedAccount.getUserId(),
                    command.userId(),
                    "LOAN_DISBURSEMENT_INITIATED",
                    "status",
                    null,
                    savedAccount.getStatus().name(),
                    OffsetDateTime.now()
            ));
        } catch (Exception ex) {
            log.warn("Failed writing loan disbursement audit log: {}", ex.getMessage());
        }

        // 4. Generate & persist Amortization Repayment Schedule
        List<RepaymentSchedule> schedules = amortizationEngine.generateSchedule(
                savedAccount.getAccountId(),
                savedAccount.getPrincipalAmount(),
                savedAccount.getInterestRatePct(),
                savedAccount.getTermMonths(),
                savedAccount.getRepaymentFrequency(),
                savedAccount.getInterestType(),
                LocalDate.now()
        );

        scheduleRepository.saveAll(schedules);

        log.info("Disbursed Loan Account {} with {} repayment installments", savedAccount.getAccountNo(), schedules.size());

        // 4.5. Credit the disbursed funds to the borrower's savings account in account-management-service
        if (command.targetSavingsAccountId() != null) {
            String targetAccount = command.targetSavingsAccountId().toString();
            log.info("Crediting loan disbursement amount {} to savings account {} via gRPC", command.amount(), targetAccount);
            boolean creditOk = accountClientPort.postTransaction(targetAccount, command.amount(), "CREDIT");
            if (!creditOk) {
                log.error("Failed to credit savings account {} for disbursed loan {}", targetAccount, savedAccount.getAccountNo());
                throw new RuntimeException("Failed to credit disbursed funds to borrower savings account: " + targetAccount);
            }
        }

        // 4.6. Place monetary lien holds on member peer guarantors' savings accounts via gRPC
        if (command.guarantors() != null && !command.guarantors().isEmpty()) {
            for (GuarantorDisbursementInput g : command.guarantors()) {
                log.info("Placing lien hold of {} ETB on guarantor savings account {} for loan {}",
                        g.guaranteedAmount(), g.savingsAccountNo(), savedAccount.getAccountNo());
                UUID lienId = null;
                String status = "PENDING";
                try {
                    com.kab.qershi.loan.management.domain.port.out.AccountClientPort.LienResult lienResult =
                            accountClientPort.placeLien(
                                    g.savingsAccountNo(),
                                    g.guaranteedAmount(),
                                    "Peer Guarantor Pledge for Loan " + savedAccount.getAccountNo(),
                                    savedAccount.getAccountNo(),
                                    command.userId() != null ? command.userId().toString() : ""
                            );
                    if (lienResult.isSuccess() && lienResult.lienId() != null && !lienResult.lienId().isBlank()) {
                        lienId = UUID.fromString(lienResult.lienId());
                        status = "HELD";
                        log.info("Successfully placed lien {} on guarantor account {}", lienId, g.savingsAccountNo());
                    } else {
                        log.warn("Lien hold placement rejected or failed for guarantor account {}: {}", g.savingsAccountNo(), lienResult.message());
                        status = "FAILED";
                    }
                } catch (Exception ex) {
                    log.error("Error calling placeLien on account {}: {}", g.savingsAccountNo(), ex.getMessage());
                    status = "FAILED";
                }

                guarantorRepository.save(new com.kab.qershi.loan.management.infrastructure.persistence.entity.LoanAccountGuarantorEntity(
                        null,
                        savedAccount.getAccountId(),
                        command.applicationId(),
                        g.guarantorUserId(),
                        g.guarantorName(),
                        g.guarantorPhone(),
                        g.savingsAccountNo(),
                        g.guaranteedAmount(),
                        lienId,
                        status,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                ));
            }
        }

        // 5. Trigger SMS Notification via gRPC — send to the actual member's phone
        if (command.memberPhone() != null && !command.memberPhone().isBlank()) {
            notificationAdapter.sendNotification(
                    command.memberPhone(),
                    "LOAN_DISBURSED",
                    Map.of(
                            "accountNo", savedAccount.getAccountNo(),
                            "amount", savedAccount.getPrincipalAmount().toPlainString()
                    )
            );
        } else {
            log.warn("Loan disbursement SMS skipped: memberPhone not provided for account {}", savedAccount.getAccountNo());
        }

        return savedAccount;
    }

    @Override
    @Transactional
    public LoanAccount approveDisbursement(UUID accountId, UUID checkerUserId) {
        log.info("Executing Maker-Checker dual control approval for loan account ID: {} by checker: {}", accountId, checkerUserId);

        LoanAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Loan account not found with ID: " + accountId));

        if (account.getStatus() == LoanStatus.DISBURSED || account.getStatus() == LoanStatus.ACTIVE) {
            log.info("Loan account {} is already in status {}. Returning account.", account.getAccountNo(), account.getStatus());
            return account;
        }

        // Maker-Checker Self-Approval Security Guard
        if (checkerUserId != null && checkerUserId.equals(account.getUserId())) {
            throw new IllegalArgumentException("Maker-Checker Guard Violation: Operator who initiated disbursement cannot self-approve disbursement.");
        }

        OffsetDateTime now = OffsetDateTime.now();
        account.setStatus(LoanStatus.DISBURSED);
        account.setDisbursementDate(now);
        account.setUpdatedAt(now);

        LoanAccount savedAccount = accountRepository.save(account);

        try {
            auditLogRepository.save(new LoanAuditLogEntity(
                    null,
                    savedAccount.getAccountNo(),
                    savedAccount.getUserId(),
                    checkerUserId != null ? checkerUserId : savedAccount.getUserId(),
                    "LOAN_DISBURSEMENT_APPROVED",
                    "status",
                    LoanStatus.PENDING_DISBURSEMENT.name(),
                    LoanStatus.DISBURSED.name(),
                    OffsetDateTime.now()
            ));
        } catch (Exception ex) {
            log.warn("Failed writing loan approval audit log: {}", ex.getMessage());
        }

        // Generate & persist Amortization Repayment Schedule on Checker Approval if not present
        List<RepaymentSchedule> existingSchedules = scheduleRepository.findByAccountIdOrderByInstallmentNoAsc(savedAccount.getAccountId());
        if (existingSchedules.isEmpty()) {
            List<RepaymentSchedule> schedules = amortizationEngine.generateSchedule(
                    savedAccount.getAccountId(),
                    savedAccount.getPrincipalAmount(),
                    savedAccount.getInterestRatePct(),
                    savedAccount.getTermMonths(),
                    savedAccount.getRepaymentFrequency(),
                    savedAccount.getInterestType(),
                    LocalDate.now()
            );

            scheduleRepository.saveAll(schedules);
            log.info("Generated {} schedule installments for account {}", schedules.size(), savedAccount.getAccountNo());
        }

        log.info("Maker-Checker APPROVED disbursement for Loan Account {}. Status: DISBURSED", savedAccount.getAccountNo());
        return savedAccount;
    }
}
