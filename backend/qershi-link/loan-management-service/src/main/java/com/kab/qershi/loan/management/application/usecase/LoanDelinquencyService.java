package com.kab.qershi.loan.management.application.usecase;

import com.kab.qershi.loan.management.domain.model.LoanStatus;
import com.kab.qershi.loan.management.domain.model.ScheduleStatus;
import com.kab.qershi.loan.management.infrastructure.persistence.entity.LoanAccountEntity;
import com.kab.qershi.loan.management.infrastructure.persistence.entity.LoanDelinquencySnapshotEntity;
import com.kab.qershi.loan.management.infrastructure.persistence.entity.RepaymentScheduleEntity;
import com.kab.qershi.loan.management.infrastructure.persistence.repository.SpringDataLoanAccountRepository;
import com.kab.qershi.loan.management.infrastructure.persistence.repository.SpringDataLoanDelinquencySnapshotRepository;
import com.kab.qershi.loan.management.infrastructure.persistence.repository.SpringDataRepaymentScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Portfolio at Risk (PAR) Aging and Regulatory Delinquency Provisioning Engine.
 * Evaluates DPD (Days Past Due) across repayment schedules and categorizes loans into standard buckets.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class LoanDelinquencyService {

    private static final Logger log = LoggerFactory.getLogger(LoanDelinquencyService.class);

    private final SpringDataLoanAccountRepository loanAccountRepository;
    private final SpringDataRepaymentScheduleRepository scheduleRepository;
    private final SpringDataLoanDelinquencySnapshotRepository snapshotRepository;

    public LoanDelinquencyService(SpringDataLoanAccountRepository loanAccountRepository,
                                  SpringDataRepaymentScheduleRepository scheduleRepository,
                                  SpringDataLoanDelinquencySnapshotRepository snapshotRepository) {
        this.loanAccountRepository = loanAccountRepository;
        this.scheduleRepository = scheduleRepository;
        this.snapshotRepository = snapshotRepository;
    }

    public record ParAgingResult(
            int totalLoansEvaluated,
            int currentCount,
            int par30Count,
            int par60Count,
            int par90Count,
            int lossCount,
            BigDecimal totalOverdueAmount,
            BigDecimal totalProvisionReserve
    ) {}

    @Transactional
    public ParAgingResult evaluateParAging(LocalDate businessDate) {
        log.info("Beginning Loan Portfolio at Risk (PAR) aging evaluation for business date: {}", businessDate);

        List<LoanAccountEntity> activeLoans = loanAccountRepository.findByStatusIn(
                List.of(LoanStatus.ACTIVE, LoanStatus.DISBURSED)
        );

        int currentCount = 0;
        int par30Count = 0;
        int par60Count = 0;
        int par90Count = 0;
        int lossCount = 0;
        BigDecimal totalOverdue = BigDecimal.ZERO;
        BigDecimal totalProvisions = BigDecimal.ZERO;

        List<LoanDelinquencySnapshotEntity> snapshots = new ArrayList<>();

        for (LoanAccountEntity loan : activeLoans) {
            List<RepaymentScheduleEntity> unpaidSchedules = scheduleRepository.findByAccountIdAndStatusNot(
                    loan.getAccountId(), ScheduleStatus.PAID
            );

            LocalDate earliestOverdueDueDate = null;
            BigDecimal overduePrincipal = BigDecimal.ZERO;
            BigDecimal overdueInterest = BigDecimal.ZERO;

            for (RepaymentScheduleEntity schedule : unpaidSchedules) {
                if (schedule.getDueDate().isBefore(businessDate)) {
                    BigDecimal totalDue = schedule.getTotalDue() != null ? schedule.getTotalDue() : BigDecimal.ZERO;
                    BigDecimal amountPaid = schedule.getAmountPaid() != null ? schedule.getAmountPaid() : BigDecimal.ZERO;
                    BigDecimal installmentDue = totalDue.subtract(amountPaid);

                    if (installmentDue.compareTo(BigDecimal.ZERO) > 0) {
                        if (earliestOverdueDueDate == null || schedule.getDueDate().isBefore(earliestOverdueDueDate)) {
                            earliestOverdueDueDate = schedule.getDueDate();
                        }
                        BigDecimal princDue = schedule.getPrincipalDue() != null ? schedule.getPrincipalDue() : BigDecimal.ZERO;
                        overduePrincipal = overduePrincipal.add(princDue.max(BigDecimal.ZERO));

                        BigDecimal intDue = schedule.getInterestDue() != null ? schedule.getInterestDue() : BigDecimal.ZERO;
                        overdueInterest = overdueInterest.add(intDue.max(BigDecimal.ZERO));
                    }
                }
            }

            int daysPastDue = 0;
            if (earliestOverdueDueDate != null) {
                daysPastDue = (int) ChronoUnit.DAYS.between(earliestOverdueDueDate, businessDate);
            }

            String parBucket;
            BigDecimal provisionRatePct;

            if (daysPastDue <= 0) {
                parBucket = "CURRENT";
                provisionRatePct = new BigDecimal("1.00"); // 1% general provision
                currentCount++;
            } else if (daysPastDue <= 30) {
                parBucket = "WATCHLIST_PAR_30";
                provisionRatePct = new BigDecimal("5.00"); // 5% watchlist provision
                par30Count++;
            } else if (daysPastDue <= 60) {
                parBucket = "SUBSTANDARD_PAR_60";
                provisionRatePct = new BigDecimal("25.00"); // 25% substandard provision
                par60Count++;
            } else if (daysPastDue <= 90) {
                parBucket = "DOUBTFUL_PAR_90";
                provisionRatePct = new BigDecimal("50.00"); // 50% doubtful provision
                par90Count++;
            } else {
                parBucket = "LOSS_PAR_90_PLUS";
                provisionRatePct = new BigDecimal("100.00"); // 100% loss provision
                lossCount++;
            }

            BigDecimal principal = loan.getPrincipalAmount() != null ? loan.getPrincipalAmount() : BigDecimal.ZERO;
            BigDecimal provisionAmount = principal.multiply(provisionRatePct)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            BigDecimal loanTotalOverdue = overduePrincipal.add(overdueInterest);
            totalOverdue = totalOverdue.add(loanTotalOverdue);
            totalProvisions = totalProvisions.add(provisionAmount);

            // Update loan account entity
            loan.setDaysPastDue(daysPastDue);
            loan.setParBucket(parBucket);
            loan.setProvisionRatePct(provisionRatePct);
            loan.setProvisionAmount(provisionAmount);
            loan.setLastParEvaluationDate(businessDate);

            // Create delinquency snapshot
            LoanDelinquencySnapshotEntity snapshot = new LoanDelinquencySnapshotEntity();
            snapshot.setAccountId(loan.getAccountId());
            snapshot.setBusinessDate(businessDate);
            snapshot.setDaysPastDue(daysPastDue);
            snapshot.setOverduePrincipal(overduePrincipal);
            snapshot.setOverdueInterest(overdueInterest);
            snapshot.setTotalOverdue(loanTotalOverdue);
            snapshot.setParBucket(parBucket);
            snapshot.setProvisionRatePct(provisionRatePct);
            snapshot.setProvisionAmount(provisionAmount);
            snapshots.add(snapshot);
        }

        loanAccountRepository.saveAll(activeLoans);
        snapshotRepository.saveAll(snapshots);

        log.info("PAR Aging Evaluation Complete. Evaluated: {}, Current: {}, PAR 30: {}, PAR 60: {}, PAR 90: {}, Loss: {}, Total Overdue: {} ETB, Provision Reserve: {} ETB",
                activeLoans.size(), currentCount, par30Count, par60Count, par90Count, lossCount, totalOverdue, totalProvisions);

        return new ParAgingResult(
                activeLoans.size(),
                currentCount,
                par30Count,
                par60Count,
                par90Count,
                lossCount,
                totalOverdue,
                totalProvisions
        );
    }
}
