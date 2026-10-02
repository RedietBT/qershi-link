package com.kab.qershi.loan.management.application.usecase;

import com.kab.qershi.loan.management.domain.model.*;
import com.kab.qershi.loan.management.domain.port.in.LoanDelinquencyUseCase;
import com.kab.qershi.loan.management.domain.port.out.LoanAccountRepositoryPort;
import com.kab.qershi.loan.management.domain.port.out.LoanDelinquencyRepositoryPort;
import com.kab.qershi.loan.management.domain.port.out.RepaymentScheduleRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Portfolio at Risk (PAR) Aging and Regulatory Delinquency Provisioning Engine.
 * Evaluates DPD (Days Past Due) across repayment schedules and categorizes loans into standard buckets.
 * Follows strict Hexagonal Architecture DDD principles.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class LoanDelinquencyService implements LoanDelinquencyUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoanDelinquencyService.class);

    private final LoanAccountRepositoryPort loanAccountRepository;
    private final RepaymentScheduleRepositoryPort scheduleRepository;
    private final LoanDelinquencyRepositoryPort snapshotRepository;

    public LoanDelinquencyService(LoanAccountRepositoryPort loanAccountRepository,
                                  RepaymentScheduleRepositoryPort scheduleRepository,
                                  LoanDelinquencyRepositoryPort snapshotRepository) {
        this.loanAccountRepository = loanAccountRepository;
        this.scheduleRepository = scheduleRepository;
        this.snapshotRepository = snapshotRepository;
    }

    @Override
    @Transactional
    public ParAgingResult evaluateParAging(LocalDate businessDate) {
        log.info("Beginning Loan Portfolio at Risk (PAR) aging evaluation for business date: {}", businessDate);

        List<LoanAccount> activeLoans = loanAccountRepository.findByStatusIn(
                List.of(LoanStatus.ACTIVE, LoanStatus.DISBURSED)
        );

        int currentCount = 0;
        int par30Count = 0;
        int par60Count = 0;
        int par90Count = 0;
        int lossCount = 0;
        BigDecimal totalOverdue = BigDecimal.ZERO;
        BigDecimal totalProvisions = BigDecimal.ZERO;

        List<LoanDelinquencySnapshot> snapshots = new ArrayList<>();

        for (LoanAccount loan : activeLoans) {
            List<RepaymentSchedule> unpaidSchedules = scheduleRepository.findByAccountIdAndStatusNot(
                    loan.getAccountId(), ScheduleStatus.PAID
            );

            LocalDate earliestOverdueDueDate = null;
            BigDecimal overduePrincipal = BigDecimal.ZERO;
            BigDecimal overdueInterest = BigDecimal.ZERO;

            for (RepaymentSchedule schedule : unpaidSchedules) {
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

            // Update loan account domain aggregate
            loan.setDaysPastDue(daysPastDue);
            loan.setParBucket(parBucket);
            loan.setProvisionRatePct(provisionRatePct);
            loan.setProvisionAmount(provisionAmount);
            loan.setLastParEvaluationDate(businessDate);

            // Create delinquency snapshot domain aggregate
            LoanDelinquencySnapshot snapshot = new LoanDelinquencySnapshot(
                    null,
                    loan.getAccountId(),
                    businessDate,
                    daysPastDue,
                    overduePrincipal,
                    overdueInterest,
                    loanTotalOverdue,
                    parBucket,
                    provisionRatePct,
                    provisionAmount,
                    OffsetDateTime.now()
            );
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

    @Override
    @Transactional(readOnly = true)
    public ParSummary getParSummary() {
        List<LoanAccount> loans = loanAccountRepository.findByStatusIn(
                List.of(LoanStatus.ACTIVE, LoanStatus.DISBURSED)
        );

        BigDecimal totalPrincipal = BigDecimal.ZERO;
        int currentCount = 0;
        BigDecimal currentAmount = BigDecimal.ZERO;
        int par30Count = 0;
        BigDecimal par30Amount = BigDecimal.ZERO;
        int par60Count = 0;
        BigDecimal par60Amount = BigDecimal.ZERO;
        int par90Count = 0;
        BigDecimal par90Amount = BigDecimal.ZERO;
        int lossCount = 0;
        BigDecimal lossAmount = BigDecimal.ZERO;
        BigDecimal totalProvisions = BigDecimal.ZERO;

        for (LoanAccount loan : loans) {
            BigDecimal principal = loan.getPrincipalAmount() != null ? loan.getPrincipalAmount() : BigDecimal.ZERO;
            totalPrincipal = totalPrincipal.add(principal);

            BigDecimal provision = loan.getProvisionAmount() != null ? loan.getProvisionAmount() : BigDecimal.ZERO;
            totalProvisions = totalProvisions.add(provision);

            String bucket = loan.getParBucket() != null ? loan.getParBucket() : "CURRENT";
            switch (bucket) {
                case "WATCHLIST_PAR_30" -> {
                    par30Count++;
                    par30Amount = par30Amount.add(principal);
                }
                case "SUBSTANDARD_PAR_60" -> {
                    par60Count++;
                    par60Amount = par60Amount.add(principal);
                }
                case "DOUBTFUL_PAR_90" -> {
                    par90Count++;
                    par90Amount = par90Amount.add(principal);
                }
                case "LOSS_PAR_90_PLUS" -> {
                    lossCount++;
                    lossAmount = lossAmount.add(principal);
                }
                default -> {
                    currentCount++;
                    currentAmount = currentAmount.add(principal);
                }
            }
        }

        BigDecimal nplRatio = BigDecimal.ZERO;
        if (totalPrincipal.compareTo(BigDecimal.ZERO) > 0) {
            nplRatio = lossAmount.multiply(new BigDecimal("100")).divide(totalPrincipal, 2, RoundingMode.HALF_UP);
        }

        return new ParSummary(
                loans.size(),
                totalPrincipal,
                currentCount,
                currentAmount,
                par30Count,
                par30Amount,
                par60Count,
                par60Amount,
                par90Count,
                par90Amount,
                lossCount,
                lossAmount,
                nplRatio,
                totalProvisions
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<DelinquentLoanInfo> getDelinquentLoans(String bucket) {
        List<LoanDelinquencySnapshot> snapshots = (bucket != null && !bucket.isBlank())
                ? snapshotRepository.findLatestSnapshots().stream()
                    .filter(s -> bucket.equalsIgnoreCase(s.getParBucket()))
                    .toList()
                : snapshotRepository.findLatestSnapshots();

        Map<UUID, LoanAccount> loanMap = loanAccountRepository.findAll().stream()
                .collect(Collectors.toMap(LoanAccount::getAccountId, l -> l, (l1, l2) -> l1));

        List<DelinquentLoanInfo> result = new ArrayList<>();
        for (LoanDelinquencySnapshot s : snapshots) {
            LoanAccount loan = loanMap.get(s.getAccountId());
            result.add(new DelinquentLoanInfo(
                    s.getSnapshotId(),
                    s.getAccountId(),
                    loan != null ? loan.getAccountNo() : "ACC-" + s.getAccountId().toString().substring(0, 8),
                    loan != null ? loan.getUserId() : null,
                    loan != null ? loan.getPrincipalAmount() : BigDecimal.ZERO,
                    s.getDaysPastDue(),
                    s.getOverduePrincipal(),
                    s.getOverdueInterest(),
                    s.getTotalOverdue(),
                    s.getParBucket(),
                    s.getProvisionRatePct(),
                    s.getProvisionAmount(),
                    s.getBusinessDate()
            ));
        }

        return result;
    }
}
