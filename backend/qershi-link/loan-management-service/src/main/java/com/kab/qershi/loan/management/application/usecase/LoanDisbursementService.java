package com.kab.qershi.loan.management.application.usecase;

import com.kab.qershi.loan.management.domain.engine.AmortizationEngine;
import com.kab.qershi.loan.management.domain.model.LoanAccount;
import com.kab.qershi.loan.management.domain.model.LoanAccountGuarantor;
import com.kab.qershi.loan.management.domain.model.LoanAuditLog;
import com.kab.qershi.loan.management.domain.model.LoanStatus;
import com.kab.qershi.loan.management.domain.model.RepaymentSchedule;
import com.kab.qershi.loan.management.domain.port.in.LoanDisbursementUseCase;
import com.kab.qershi.loan.management.domain.port.out.*;
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
 * Follows strict Hexagonal Architecture DDD principles.
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
    private final NotificationClientPort notificationClientPort;
    private final LoanAuditLogRepositoryPort auditLogRepository;
    private final AccountClientPort accountClientPort;
    private final LoanGuarantorRepositoryPort guarantorRepository;
    private final PricingClientPort pricingClientPort;

    public LoanDisbursementService(LoanAccountRepositoryPort accountRepository,
                                   RepaymentScheduleRepositoryPort scheduleRepository,
                                   AmortizationEngine amortizationEngine,
                                   NotificationClientPort notificationClientPort,
                                   LoanAuditLogRepositoryPort auditLogRepository,
                                   AccountClientPort accountClientPort,
                                   LoanGuarantorRepositoryPort guarantorRepository,
                                   PricingClientPort pricingClientPort) {
        this.accountRepository = accountRepository;
        this.scheduleRepository = scheduleRepository;
        this.amortizationEngine = amortizationEngine;
        this.notificationClientPort = notificationClientPort;
        this.auditLogRepository = auditLogRepository;
        this.accountClientPort = accountClientPort;
        this.guarantorRepository = guarantorRepository;
        this.pricingClientPort = pricingClientPort;
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

        // 2. Generate Unique Loan Account Number: LN-{YYYYMM}-{UUID.substr}
        String timestampPart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String generatedAccountNo = "LN-" + timestampPart + "-" + randomPart;

        OffsetDateTime now = OffsetDateTime.now();

        // 3. Create Loan Account in PENDING_DISBURSEMENT state for Maker-Checker Dual Authorization
        LoanAccount account = new LoanAccount(
                null,
                generatedAccountNo,
                command.applicationId(),
                command.userId(),
                command.productId(),
                command.amount(),
                command.interestRatePct(),
                command.termMonths(),
                command.repaymentFrequency(),
                command.interestType(),
                now,
                LoanStatus.PENDING_DISBURSEMENT,
                now,
                now
        );

        LoanAccount savedAccount = accountRepository.save(account);

        // 4. Audit Log entry for Maker step
        try {
            auditLogRepository.save(new LoanAuditLog(
                    null,
                    savedAccount.getAccountNo(),
                    savedAccount.getUserId(),
                    command.userId(),
                    "LOAN_DISBURSEMENT_INITIATED",
                    "status",
                    null,
                    LoanStatus.PENDING_DISBURSEMENT.name(),
                    now
            ));
        } catch (Exception ex) {
            log.warn("Failed writing loan initiation audit log: {}", ex.getMessage());
        }

        // 4.0. Query Dynamic Tariff & Fee Configuration via pricing-fee-service
        BigDecimal grossAmount = command.amount();
        BigDecimal processingFee = BigDecimal.ZERO;
        String feeGlCode = "4021"; // Default Loan Processing Fee Income
        String tariffCode = "NONE";

        if (pricingClientPort != null) {
            try {
                var feeResult = pricingClientPort.calculateFee("LOAN_PROCESSING", grossAmount, "STANDARD", "ETB");
                if (feeResult.feeApplicable() && feeResult.feeAmount() != null && feeResult.feeAmount().compareTo(BigDecimal.ZERO) > 0) {
                    processingFee = feeResult.feeAmount();
                    feeGlCode = feeResult.feeGlCode() != null && !feeResult.feeGlCode().isBlank() ? feeResult.feeGlCode() : "4021";
                    tariffCode = feeResult.tariffCode();
                    log.info("Assessed loan processing fee: {} ETB (Tariff: {}, Fee Income GL: {}) for loan application {}",
                            processingFee, tariffCode, feeGlCode, command.applicationId());
                }
            } catch (Exception ex) {
                log.warn("Pricing fee assessment failed or unavailable, proceeding with zero processing fee: {}", ex.getMessage());
            }
        }

        BigDecimal netDisbursedAmount = grossAmount.subtract(processingFee);
        if (netDisbursedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Net disbursement amount non-positive (Gross: {}, Fee: {}), defaulting to gross", grossAmount, processingFee);
            netDisbursedAmount = grossAmount;
            processingFee = BigDecimal.ZERO;
        }

        try {
            auditLogRepository.save(new LoanAuditLog(
                    null,
                    savedAccount.getAccountNo(),
                    savedAccount.getUserId(),
                    command.userId(),
                    "LOAN_DISBURSEMENT_INITIATED",
                    "disbursement_net_calculation",
                    null,
                    "Gross: " + grossAmount + " ETB | Processing Fee Deducted: " + processingFee + " ETB (GL " + feeGlCode + ") | Net Disbursed: " + netDisbursedAmount + " ETB",
                    OffsetDateTime.now()
            ));
        } catch (Exception ex) {
            log.warn("Failed writing loan disbursement audit log: {}", ex.getMessage());
        }

        // 4.1. Generate & persist Amortization Repayment Schedule (based on full principal gross amount)
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

        // 4.5. Credit the NET disbursed funds to the borrower's savings account in account-management-service
        if (command.targetSavingsAccountId() != null) {
            String targetAccount = command.targetSavingsAccountId().toString();
            log.info("Crediting net loan disbursement amount {} (Gross: {}, Fee: {}) to savings account {} via gRPC",
                    netDisbursedAmount, grossAmount, processingFee, targetAccount);
            boolean creditOk = accountClientPort.postTransaction(targetAccount, netDisbursedAmount, "CREDIT");
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
                    var lienResult = accountClientPort.placeLien(
                            g.savingsAccountNo(),
                            g.guaranteedAmount(),
                            "PEER_GUARANTEE_HOLD",
                            "Collateral lien hold for Loan " + savedAccount.getAccountNo(),
                            savedAccount.getUserId() != null ? savedAccount.getUserId().toString() : "SYSTEM"
                    );
                    if (lienResult.isSuccess() && lienResult.lienId() != null) {
                        try {
                            lienId = UUID.fromString(lienResult.lienId());
                        } catch (Exception ignored) {}
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

                guarantorRepository.save(new LoanAccountGuarantor(
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
            notificationClientPort.sendNotification(
                    command.memberPhone(),
                    "LOAN_DISBURSED",
                    Map.of(
                            "accountNo", savedAccount.getAccountNo(),
                            "amount", savedAccount.getPrincipalAmount().toPlainString(),
                            "netDisbursed", netDisbursedAmount.toPlainString(),
                            "processingFee", processingFee.toPlainString()
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
            auditLogRepository.save(new LoanAuditLog(
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
