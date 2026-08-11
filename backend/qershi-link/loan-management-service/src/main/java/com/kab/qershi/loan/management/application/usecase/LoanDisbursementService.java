package com.kab.qershi.loan.management.application.usecase;

import com.kab.qershi.loan.management.domain.engine.AmortizationEngine;
import com.kab.qershi.loan.management.domain.model.LoanAccount;
import com.kab.qershi.loan.management.domain.model.LoanStatus;
import com.kab.qershi.loan.management.domain.model.RepaymentSchedule;
import com.kab.qershi.loan.management.domain.port.in.LoanDisbursementUseCase;
import com.kab.qershi.loan.management.domain.port.out.LoanAccountRepositoryPort;
import com.kab.qershi.loan.management.domain.port.out.RepaymentScheduleRepositoryPort;
import com.kab.qershi.loan.management.infrastructure.adapters.NotificationGrpcClientAdapter;
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

    public LoanDisbursementService(LoanAccountRepositoryPort accountRepository,
                                   RepaymentScheduleRepositoryPort scheduleRepository,
                                   AmortizationEngine amortizationEngine,
                                   NotificationGrpcClientAdapter notificationAdapter) {
        this.accountRepository = accountRepository;
        this.scheduleRepository = scheduleRepository;
        this.amortizationEngine = amortizationEngine;
        this.notificationAdapter = notificationAdapter;
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
