package com.kab.qershi.loan.management.domain.engine;

import com.kab.qershi.loan.management.domain.model.RepaymentSchedule;
import com.kab.qershi.loan.management.domain.model.ScheduleStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Pure Domain Amortization Engine calculating repayment schedules dynamically for
 * REDUCING_BALANCE, FLAT_RATE, and BULLET interest strategies across all frequency units
 * (Tier-1 Core Banking Standards).
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class AmortizationEngine {

    public List<RepaymentSchedule> generateSchedule(UUID accountId, BigDecimal principal, BigDecimal annualInterestRatePct,
                                                     int termMonths, String frequency, String interestType,
                                                     LocalDate startDate) {
        List<RepaymentSchedule> schedules = new ArrayList<>();
        OffsetDateTime now = OffsetDateTime.now();

        String normalizedFreq = (frequency != null) ? frequency.trim().toUpperCase() : "MONTHLY";
        String normalizedType = (interestType != null) ? interestType.trim().toUpperCase() : "REDUCING_BALANCE";

        if ("BULLET".equals(normalizedFreq)) {
            // BULLET: Single installment at maturity containing 100% Principal + Total Interest
            BigDecimal totalInterest = principal.multiply(annualInterestRatePct)
                    .multiply(BigDecimal.valueOf(termMonths))
                    .divide(BigDecimal.valueOf(1200), 2, RoundingMode.HALF_UP);
            BigDecimal totalDue = principal.add(totalInterest);

            RepaymentSchedule bulletSchedule = new RepaymentSchedule(
                    null, accountId, 1, startDate.plusMonths(termMonths),
                    principal, totalInterest, totalDue, BigDecimal.ZERO,
                    ScheduleStatus.PENDING, now, now
            );
            schedules.add(bulletSchedule);
            return schedules;
        }

        // Determine month step based on frequency string
        int monthStep = switch (normalizedFreq) {
            case "QUARTERLY" -> 3;
            case "SEMI_ANNUAL", "HALF_YEARLY" -> 6;
            case "ANNUAL", "YEARLY" -> 12;
            default -> 1;
        };

        int numberOfInstallments = Math.max(1, termMonths / monthStep);
        BigDecimal periodicRate = annualInterestRatePct.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(monthStep))
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        if ("FLAT_RATE".equals(normalizedType) || "FLAT".equals(normalizedType)) {
            BigDecimal totalInterest = principal.multiply(annualInterestRatePct)
                    .multiply(BigDecimal.valueOf(termMonths))
                    .divide(BigDecimal.valueOf(1200), 2, RoundingMode.HALF_UP);

            BigDecimal principalPerInstallment = principal.divide(BigDecimal.valueOf(numberOfInstallments), 2, RoundingMode.HALF_UP);
            BigDecimal interestPerInstallment = totalInterest.divide(BigDecimal.valueOf(numberOfInstallments), 2, RoundingMode.HALF_UP);
            BigDecimal totalInstallment = principalPerInstallment.add(interestPerInstallment);

            LocalDate currentDueDate = startDate;
            for (int i = 1; i <= numberOfInstallments; i++) {
                currentDueDate = currentDueDate.plusMonths(monthStep);

                // Adjustment for final installment rounding difference
                if (i == numberOfInstallments) {
                    BigDecimal sumPrincipal = principalPerInstallment.multiply(BigDecimal.valueOf(numberOfInstallments - 1));
                    principalPerInstallment = principal.subtract(sumPrincipal);
                    totalInstallment = principalPerInstallment.add(interestPerInstallment);
                }

                schedules.add(new RepaymentSchedule(
                        null, accountId, i, currentDueDate,
                        principalPerInstallment, interestPerInstallment, totalInstallment,
                        BigDecimal.ZERO, ScheduleStatus.PENDING, now, now
                ));
            }
        } else {
            // REDUCING_BALANCE (Equated Periodic Installment)
            BigDecimal numerator = periodicRate.add(BigDecimal.ONE).pow(numberOfInstallments);
            BigDecimal denominator = numerator.subtract(BigDecimal.ONE);

            BigDecimal emi;
            if (denominator.compareTo(BigDecimal.ZERO) == 0) {
                emi = principal.divide(BigDecimal.valueOf(numberOfInstallments), 2, RoundingMode.HALF_UP);
            } else {
                emi = principal.multiply(periodicRate).multiply(numerator)
                        .divide(denominator, 2, RoundingMode.HALF_UP);
            }

            BigDecimal remainingBalance = principal;
            LocalDate currentDueDate = startDate;

            for (int i = 1; i <= numberOfInstallments; i++) {
                currentDueDate = currentDueDate.plusMonths(monthStep);
                BigDecimal interestDue = remainingBalance.multiply(periodicRate).setScale(2, RoundingMode.HALF_UP);
                BigDecimal principalDue = emi.subtract(interestDue);

                if (i == numberOfInstallments || principalDue.compareTo(remainingBalance) > 0) {
                    principalDue = remainingBalance;
                    emi = principalDue.add(interestDue);
                }

                remainingBalance = remainingBalance.subtract(principalDue);

                schedules.add(new RepaymentSchedule(
                        null, accountId, i, currentDueDate,
                        principalDue, interestDue, emi,
                        BigDecimal.ZERO, ScheduleStatus.PENDING, now, now
                ));
            }
        }

        return schedules;
    }
}
