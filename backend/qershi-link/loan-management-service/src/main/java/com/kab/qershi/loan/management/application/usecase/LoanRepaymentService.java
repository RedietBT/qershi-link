package com.kab.qershi.loan.management.application.usecase;

import com.kab.qershi.loan.management.domain.engine.PaymentWaterfallEngine;
import com.kab.qershi.loan.management.domain.model.*;
import com.kab.qershi.loan.management.domain.port.in.LoanRepaymentUseCase;
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
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Business logic service managing Loan Repayment processing via Payment Waterfall rules.
 * Follows strict Hexagonal Architecture DDD principles.
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
    private final NotificationClientPort notificationClientPort;
    private final PaymentWaterfallEngine waterfallEngine;
    private final AccountClientPort accountClientPort;
    private final PenaltyRuleRepositoryPort penaltyRuleRepository;
    private final LoanGuarantorRepositoryPort guarantorRepository;

    public LoanRepaymentService(LoanAccountRepositoryPort accountRepository,
                                RepaymentScheduleRepositoryPort scheduleRepository,
                                LoanRepaymentRepositoryPort repaymentRepository,
                                NotificationClientPort notificationClientPort,
                                AccountClientPort accountClientPort,
                                PenaltyRuleRepositoryPort penaltyRuleRepository,
                                LoanGuarantorRepositoryPort guarantorRepository) {
        this.accountRepository = accountRepository;
        this.scheduleRepository = scheduleRepository;
        this.repaymentRepository = repaymentRepository;
        this.notificationClientPort = notificationClientPort;
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
            throw new IllegalStateException("Cannot process repayment on closed loan account: " + account.getAccountNo());
        }

        // Fetch unpaid amortization schedules
        List<RepaymentSchedule> schedules = scheduleRepository.findByAccountIdOrderByInstallmentNoAsc(command.accountId());
        if (schedules.isEmpty()) {
            throw new IllegalStateException("No repayment schedules found for loan account: " + account.getAccountNo());
        }

        // 1. Calculate accrued overdue penalties if any installments are overdue
        BigDecimal calculatedPenalty = calculateOverduePenalty(schedules);

        // 1.5. If payment channel is SAVINGS_INTERNAL, auto-debit the member's savings account via gRPC
        if ("SAVINGS_INTERNAL".equalsIgnoreCase(command.paymentChannel()) && command.sourceAccountNo() != null) {
            log.info("Auto-debiting {} ETB from member savings account {} for loan repayment {}",
                    command.amount(), command.sourceAccountNo(), account.getAccountNo());
            boolean debitSuccess = accountClientPort.postTransaction(
                    command.sourceAccountNo(),
                    command.amount(),
                    "DEBIT"
            );
            if (!debitSuccess) {
                log.error("Failed to auto-debit savings account {} for loan repayment", command.sourceAccountNo());
                throw new IllegalStateException("Auto-debit from savings account failed. Repayment cannot proceed.");
            }
        }

        // 2. Allocate payment using Payment Waterfall Engine
        PaymentWaterfallEngine.AllocationResult allocation = waterfallEngine.allocatePayment(
                command.amount(),
                calculatedPenalty,
                schedules
        );

        log.info("Payment waterfall allocated: Principal={}, Interest={}, Penalty={}, Excess={}",
                allocation.getPrincipalAllocated(), allocation.getInterestAllocated(), allocation.getPenaltyAllocated(), allocation.getUnallocatedAmount());

        // 3. Persist updated schedule installments
        scheduleRepository.saveAll(schedules);

        // 4. Generate unique transaction reference: LRP-{YYYYMMDD}-{UUID.substr}
        String txDatePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String txRandomPart = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        String txRef = "LRP-" + txDatePart + "-" + txRandomPart;

        OffsetDateTime now = OffsetDateTime.now();

        // 5. Create & persist Loan Repayment record
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
                List<LoanAccountGuarantor> heldGuarantors =
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
            notificationClientPort.sendNotification(
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
            List<PenaltyRule> rules = penaltyRuleRepository.findByActiveTrue();
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
                if (today.isAfter(s.getDueDate().plusDays(gracePeriodDays))) {
                    BigDecimal remainingPrincipal = s.getPrincipalDue() != null ? s.getPrincipalDue() : BigDecimal.ZERO;
                    BigDecimal penalty = remainingPrincipal.multiply(penaltyRate).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                    totalPenalty = totalPenalty.add(penalty);
                }
            }
        }
        return totalPenalty;
    }
}
