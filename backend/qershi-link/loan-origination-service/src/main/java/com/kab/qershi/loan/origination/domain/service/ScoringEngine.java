package com.kab.qershi.loan.origination.domain.service;

import com.kab.qershi.loan.origination.domain.model.Collateral;
import com.kab.qershi.loan.origination.domain.model.CreditScoring;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Pure Domain Service calculating multi-factor pre-eligibility credit scores.
 * Dynamically handles strategy codes (COLLATERAL, SAVINGS, AGRI_PRODUCTIVITY, etc.).
 *
 * @author KAB Digital Solution PLC
 * @version 1.1.0
 */
@Service
public class ScoringEngine {

    private static final BigDecimal PASSING_THRESHOLD = new BigDecimal("60.00");
    private static final BigDecimal MIN_COLLATERAL_RATIO = new BigDecimal("1.20"); // 120% collateral coverage ratio

    public CreditScoring evaluateEligibility(UUID applicationId,
                                              String scoringType,
                                              BigDecimal amountRequested,
                                              BigDecimal savingsConsistency,
                                              BigDecimal historicalYield,
                                              BigDecimal projectedYield,
                                              BigDecimal landSizeHectares,
                                              List<Collateral> collaterals) {

        BigDecimal calculatedScore = BigDecimal.ZERO;
        boolean passed = false;

        String strategy = scoringType != null ? scoringType.toUpperCase().trim() : "SAVINGS";

        switch (strategy) {
            case "AGRI_PRODUCTIVITY":
                if (landSizeHectares != null && landSizeHectares.compareTo(BigDecimal.ZERO) > 0 && projectedYield != null) {
                    BigDecimal yieldPerHectare = projectedYield.divide(landSizeHectares, 2, RoundingMode.HALF_UP);
                    calculatedScore = yieldPerHectare.multiply(new BigDecimal("10.00")).min(new BigDecimal("100.00"));
                } else {
                    calculatedScore = BigDecimal.ZERO;
                }
                passed = calculatedScore.compareTo(PASSING_THRESHOLD) >= 0;
                break;

            case "COLLATERAL":
                BigDecimal totalCollateralValue = collaterals != null
                        ? collaterals.stream()
                        .map(Collateral::getEstimatedValue)
                        .filter(v -> v != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        : BigDecimal.ZERO;

                if (amountRequested != null && amountRequested.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal requiredCollateral = amountRequested.multiply(MIN_COLLATERAL_RATIO);
                    if (totalCollateralValue.compareTo(requiredCollateral) >= 0) {
                        calculatedScore = new BigDecimal("100.00");
                        passed = true;
                    } else {
                        calculatedScore = totalCollateralValue
                                .divide(requiredCollateral, 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100.00"))
                                .setScale(2, RoundingMode.HALF_UP);
                        passed = false;
                    }
                }
                break;

            case "SAVINGS":
            default:
                calculatedScore = savingsConsistency != null ? savingsConsistency : BigDecimal.ZERO;
                passed = calculatedScore.compareTo(PASSING_THRESHOLD) >= 0;
                break;
        }

        return new CreditScoring(
                UUID.randomUUID(),
                applicationId,
                savingsConsistency,
                historicalYield,
                projectedYield,
                landSizeHectares,
                calculatedScore,
                passed,
                Instant.now()
        );
    }
}
