package com.kab.qershi.loan.management.application.usecase;

import com.kab.qershi.loan.management.domain.engine.PaymentWaterfallEngine;
import com.kab.qershi.loan.management.domain.model.*;
import com.kab.qershi.loan.management.domain.port.in.LoanRepaymentUseCase;
import com.kab.qershi.loan.management.domain.port.out.LoanAccountRepositoryPort;
import com.kab.qershi.loan.management.domain.port.out.LoanRepaymentRepositoryPort;
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
 * Business logic service managing Loan Repayment processing via Payment Waterfall rules.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class LoanRepaymentService implements LoanRepaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoanRepaymentService.class);

    private final LoanAccountRepositoryPort accountRepository;
    private final RepaymentScheduleRepositoryPort scheduleRepository;
    private final LoanRepaymentRepositoryPort repaymentRepository;
    private final NotificationGrpcClientAdapter notificationAdapter;
    private final PaymentWaterfallEngine waterfallEngine;

    public LoanRepaymentService(LoanAccountRepositoryPort accountRepository,
                                RepaymentScheduleRepositoryPort scheduleRepository,
                                LoanRepaymentRepositoryPort repaymentRepository,
                                NotificationGrpcClientAdapter notificationAdapter) {
        this.accountRepository = accountRepository;
        this.scheduleRepository = scheduleRepository;
        this.repaymentRepository = repaymentRepository;
        this.notificationAdapter = notificationAdapter;
        this.waterfallEngine = new PaymentWaterfallEngine();
    }

    @Override
    @Transactional
    public LoanRepayment processRepayment(RepaymentCommand command) {
        log.info("Processing loan repayment for account ID: {}, Amount: {}, Channel: {}",
                command.accountId(), command.amount(), command.paymentChannel());

        LoanAccount account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new IllegalArgumentException("Loan account not found with ID: " + command.accountId()));

        if (account.getStatus() == LoanStatus.CLOSED) {
            throw new IllegalStateException("Loan account " + account.getAccountNo() + " is already fully paid and CLOSED.");
        }

        List<RepaymentSchedule> schedules = scheduleRepository.findByAccountIdOrderByInstallmentNoAsc(command.accountId());

        // Allocate payment across Penalties, Interest, and Principal
        PaymentWaterfallEngine.AllocationResult allocation = waterfallEngine.allocatePayment(
                command.amount(), BigDecimal.ZERO, schedules
        );

        // Update schedule records
        scheduleRepository.saveAll(schedules);

        // Generate unique transaction reference
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomSuffix = ThreadLocalRandom.current().nextInt(10000, 99999);
        String txRef = "TXN-PMT-" + datePrefix + "-" + randomSuffix;

        OffsetDateTime now = OffsetDateTime.now();

        LoanRepayment repayment = new LoanRepayment(
                null,
                account.getAccountId(),
                txRef,
                command.amount(),
                allocation.getPrincipalAllocated(),
                allocation.getInterestAllocated(),
                allocation.getPenaltyAllocated(),
                now,
                command.paymentChannel(),
                command.remarks(),
                now
        );

        LoanRepayment savedRepayment = repaymentRepository.save(repayment);

        // Check if all installments are fully paid; if so, close the loan account
        boolean allPaid = schedules.stream().allMatch(s -> s.getStatus() == ScheduleStatus.PAID);
        if (allPaid) {
            account.setStatus(LoanStatus.CLOSED);
            account.setUpdatedAt(now);
            accountRepository.save(account);
            log.info("Loan Account {} is now fully paid and CLOSED", account.getAccountNo());
        } else if (account.getStatus() == LoanStatus.DISBURSED) {
            account.setStatus(LoanStatus.ACTIVE);
            account.setUpdatedAt(now);
            accountRepository.save(account);
        }

        // Trigger SMS Confirmation Notification — send to the actual member's phone
        if (command.memberPhone() != null && !command.memberPhone().isBlank()) {
            notificationAdapter.sendNotification(
                    command.memberPhone(),
                    "LOAN_REPAYMENT_CONFIRMATION",
                    Map.of(
                            "accountNo", account.getAccountNo(),
                            "amount", command.amount().toPlainString(),
                            "txRef", txRef
                    )
            );
        } else {
            log.warn("Repayment SMS skipped: memberPhone not provided for account {}", account.getAccountNo());
        }

        return savedRepayment;
    }
}
