package com.kab.qershi.loan.management.domain.engine;

import com.kab.qershi.loan.management.domain.model.RepaymentSchedule;
import com.kab.qershi.loan.management.domain.model.ScheduleStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Pure Domain Payment Waterfall Engine executing standard Core Banking payment allocation:
 * 1. Penalties & Overdue Fees
 * 2. Accrued Interest
 * 3. Principal Debt
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class PaymentWaterfallEngine {

    public static class AllocationResult {
        private final BigDecimal principalAllocated;
        private final BigDecimal interestAllocated;
        private final BigDecimal penaltyAllocated;
        private final BigDecimal unallocatedAmount;

        public AllocationResult(BigDecimal principalAllocated, BigDecimal interestAllocated,
                                BigDecimal penaltyAllocated, BigDecimal unallocatedAmount) {
            this.principalAllocated = principalAllocated;
            this.interestAllocated = interestAllocated;
            this.penaltyAllocated = penaltyAllocated;
            this.unallocatedAmount = unallocatedAmount;
        }

        public BigDecimal getPrincipalAllocated() {
            return principalAllocated;
        }

        public BigDecimal getInterestAllocated() {
            return interestAllocated;
        }

        public BigDecimal getPenaltyAllocated() {
            return penaltyAllocated;
        }

        public BigDecimal getUnallocatedAmount() {
            return unallocatedAmount;
        }
    }

    public AllocationResult allocatePayment(BigDecimal paymentAmount, BigDecimal penaltyOwed, List<RepaymentSchedule> schedules) {
        BigDecimal remainingPayment = paymentAmount;

        // 1. Allocate to Penalty first
        BigDecimal penaltyAllocated = BigDecimal.ZERO;
        if (penaltyOwed != null && penaltyOwed.compareTo(BigDecimal.ZERO) > 0) {
            penaltyAllocated = remainingPayment.min(penaltyOwed);
            remainingPayment = remainingPayment.subtract(penaltyAllocated);
        }

        BigDecimal totalInterestAllocated = BigDecimal.ZERO;
        BigDecimal totalPrincipalAllocated = BigDecimal.ZERO;

        OffsetDateTime now = OffsetDateTime.now();

        // 2 & 3. Allocate remaining payment across unpaid schedules (Interest first, then Principal)
        for (RepaymentSchedule schedule : schedules) {
            if (remainingPayment.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            if (schedule.getStatus() == ScheduleStatus.PAID) {
                continue;
            }

            BigDecimal outstandingDue = schedule.getTotalDue().subtract(schedule.getAmountPaid());
            if (outstandingDue.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal interestOutstanding = schedule.getInterestDue();
            BigDecimal interestPaidSoFar = schedule.getAmountPaid().min(schedule.getInterestDue());
            BigDecimal remainingInterestDue = interestOutstanding.subtract(interestPaidSoFar);

            // Allocate Interest
            BigDecimal interestPaidThisTxn = BigDecimal.ZERO;
            if (remainingInterestDue.compareTo(BigDecimal.ZERO) > 0) {
                interestPaidThisTxn = remainingPayment.min(remainingInterestDue);
                totalInterestAllocated = totalInterestAllocated.add(interestPaidThisTxn);
                remainingPayment = remainingPayment.subtract(interestPaidThisTxn);
            }

            // Allocate Principal
            BigDecimal principalOutstanding = schedule.getPrincipalDue();
            BigDecimal principalPaidSoFar = schedule.getAmountPaid().subtract(interestPaidSoFar).max(BigDecimal.ZERO);
            BigDecimal remainingPrincipalDue = principalOutstanding.subtract(principalPaidSoFar);

            BigDecimal principalPaidThisTxn = BigDecimal.ZERO;
            if (remainingPayment.compareTo(BigDecimal.ZERO) > 0 && remainingPrincipalDue.compareTo(BigDecimal.ZERO) > 0) {
                principalPaidThisTxn = remainingPayment.min(remainingPrincipalDue);
                totalPrincipalAllocated = totalPrincipalAllocated.add(principalPaidThisTxn);
                remainingPayment = remainingPayment.subtract(principalPaidThisTxn);
            }

            BigDecimal totalScheduleAllocated = interestPaidThisTxn.add(principalPaidThisTxn);
            BigDecimal newAmountPaid = schedule.getAmountPaid().add(totalScheduleAllocated);
            schedule.setAmountPaid(newAmountPaid);
            schedule.setUpdatedAt(now);

            if (newAmountPaid.compareTo(schedule.getTotalDue()) >= 0) {
                schedule.setStatus(ScheduleStatus.PAID);
            } else {
                schedule.setStatus(ScheduleStatus.PARTIALLY_PAID);
            }
        }

        return new AllocationResult(totalPrincipalAllocated, totalInterestAllocated, penaltyAllocated, remainingPayment);
    }
}
