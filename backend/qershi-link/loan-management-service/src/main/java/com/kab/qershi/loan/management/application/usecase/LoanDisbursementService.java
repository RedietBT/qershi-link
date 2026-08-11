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
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Business logic service managing Loan Account Activation & Amortization Schedule Generation upon Disbursement.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class LoanDisbursementService implements LoanDisbursementUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoanDisbursementService.class);

    private final LoanAccountRepositoryPort accountRepository;
    private final RepaymentScheduleRepositoryPort scheduleRepository;
    private final NotificationGrpcClientAdapter notificationAdapter;
    private final AmortizationEngine amortizationEngine;

    public LoanDisbursementService(LoanAccountRepositoryPort accountRepository,
                                   RepaymentScheduleRepositoryPort scheduleRepository,
                                   NotificationGrpcClientAdapter notificationAdapter) {
        this.accountRepository = accountRepository;
        this.scheduleRepository = scheduleRepository;
        this.notificationAdapter = notificationAdapter;
        this.amortizationEngine = new AmortizationEngine();
    }

    @Override
    @Transactional
    public LoanAccount disburseLoan(DisburseCommand command) {
        log.info("Processing loan disbursement for application ID: {}, User ID: {}, Amount: {}",
                command.applicationId(), command.userId(), command.amount());

        // 1. Guard against duplicate disbursement
        accountRepository.findByApplicationId(command.applicationId())
                .ifPresent(existing -> {
                    throw new IllegalStateException("Loan application " + command.applicationId() + " is already disbursed under Account No: " + existing.getAccountNo());
                });

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
}
