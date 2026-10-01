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
    private final com.kab.qershi.loan.management.domain.port.out.AccountClientPort accountClientPort;
    private final com.kab.qershi.loan.management.infrastructure.persistence.repository.SpringDataPenaltyRuleRepository penaltyRuleRepository;
    private final com.kab.qershi.loan.management.infrastructure.persistence.repository.SpringDataLoanAccountGuarantorRepository guarantorRepository;

    public LoanRepaymentService(LoanAccountRepositoryPort accountRepository,
                                RepaymentScheduleRepositoryPort scheduleRepository,
                                LoanRepaymentRepositoryPort repaymentRepository,
                                NotificationGrpcClientAdapter notificationAdapter,
                                com.kab.qershi.loan.management.domain.port.out.AccountClientPort accountClientPort,
                                com.kab.qershi.loan.management.infrastructure.persistence.repository.SpringDataPenaltyRuleRepository penaltyRuleRepository,
                                com.kab.qershi.loan.management.infrastructure.persistence.repository.SpringDataLoanAccountGuarantorRepository guarantorRepository) {
        this.accountRepository = accountRepository;
        this.scheduleRepository = scheduleRepository;
        this.repaymentRepository = repaymentRepository;
        this.notificationAdapter = notificationAdapter;
        this.accountClientPort = accountClientPort;
        this.penaltyRuleRepository = penaltyRuleRepository;
        this.guarantorRepository = guarantorRepository;
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

        // 1. Debit member savings account if source account is specified
        if (command.sourceAccountNo() != null && !command.sourceAccountNo().isBlank()) {
            log.info("Debiting savings account {} for loan repayment amount {} via gRPC", command.sourceAccountNo(), command.amount());
            com.kab.qershi.loan.management.domain.port.out.AccountClientPort.ValidationResult debitValidation =
                    accountClientPort.validateDebit(command.sourceAccountNo(), command.amount());
            if (!debitValidation.isValid()) {
                throw new IllegalArgumentException("Loan repayment rejected: " + debitValidation.message());
            }

            boolean debitOk = accountClientPort.postTransaction(command.sourceAccountNo(), command.amount(), "DEBIT");
            if (!debitOk) {
                throw new RuntimeException("Failed to debit savings account " + command.sourceAccountNo() + " for loan repayment.");
            }
        }

        List<RepaymentSchedule> schedules = scheduleRepository.findByAccountIdOrderByInstallmentNoAsc(command.accountId());

        // 2. Compute dynamic overdue penalty based on active penalty policies
        BigDecimal penaltyOwed = calculateOverduePenalty(schedules);

        // 3. Allocate payment across Penalties, Interest, and Principal
        PaymentWaterfallEngine.AllocationResult allocation = waterfallEngine.allocatePayment(
                command.amount(), penaltyOwed, schedules
        );

        // Update schedule records
        scheduleRepository.saveAll(schedules);

        // Generate unique transaction reference
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String txRef = "TXN-PMT-" + datePrefix + "-" + uniqueSuffix;

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

            // Release all active peer guarantor lien holds
            try {
                List<com.kab.qershi.loan.management.infrastructure.persistence.entity.LoanAccountGuarantorEntity> heldGuarantors =
                        guarantorRepository.findByAccountIdAndStatus(account.getAccountId(), "HELD");
                for (var g : heldGuarantors) {
                    if (g.getLienId() != null) {
                        log.info("Releasing lien hold {} on guarantor account {} for closed loan {}",
                                g.getLienId(), g.getSavingsAccountNo(), account.getAccountNo());
                        boolean released = accountClientPort.releaseLien(g.getLienId().toString(), "SYSTEM_LOAN_CLOSURE");
                        if (released) {
                            g.setStatus("RELEASED");
                            g.setUpdatedAt(now);
                            guarantorRepository.save(g);
                            log.info("Successfully released lien for guarantor account {}", g.getSavingsAccountNo());
                        } else {
                            log.warn("Remote ReleaseLien RPC returned false for lienId {}", g.getLienId());
                        }
                    }
                }
            } catch (Exception ex) {
                log.error("Failed releasing guarantor liens for loan {}: {}", account.getAccountNo(), ex.getMessage());
            }
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

    private BigDecimal calculateOverduePenalty(List<RepaymentSchedule> schedules) {
        BigDecimal penaltyRate = new BigDecimal("2.00");
        int gracePeriodDays = 5;
        try {
            List<com.kab.qershi.loan.management.infrastructure.persistence.entity.PenaltyRuleEntity> rules = penaltyRuleRepository.findByActiveTrue();
            if (!rules.isEmpty()) {
                penaltyRate = rules.get(0).getPenaltyRatePct();
                gracePeriodDays = rules.get(0).getGracePeriodDays();
            }
        } catch (Exception ex) {
            log.warn("Using default penalty rule (2% rate, 5 days grace): {}", ex.getMessage());
        }

        LocalDate today = LocalDate.now();
        BigDecimal totalPenalty = BigDecimal.ZERO;
        for (RepaymentSchedule s : schedules) {
            if (s.getStatus() != ScheduleStatus.PAID && s.getDueDate() != null) {
                LocalDate graceEnd = s.getDueDate().plusDays(gracePeriodDays);
                if (today.isAfter(graceEnd)) {
                    BigDecimal overdueDue = s.getTotalDue().subtract(s.getAmountPaid()).max(BigDecimal.ZERO);
                    if (overdueDue.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal penalty = overdueDue.multiply(penaltyRate)
                                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                        totalPenalty = totalPenalty.add(penalty);
                    }
                }
            }
        }
        return totalPenalty;
    }
}
