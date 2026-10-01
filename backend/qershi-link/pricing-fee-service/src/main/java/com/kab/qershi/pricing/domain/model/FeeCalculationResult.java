package com.kab.qershi.pricing.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Domain value object for calculated fee assessment.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record FeeCalculationResult(
        boolean feeApplicable,
        String tariffCode,
        String tariffName,
        String transactionType,
        String feeType,
        BigDecimal rateOrFlatValue,
        BigDecimal calculatedFee,
        BigDecimal minFee,
        BigDecimal maxFee,
        String feeGlCode,
        BigDecimal totalDebitRequired,
        String matchedSlabDetails
) {
    public FeeCalculationResult(
            boolean feeApplicable,
            String tariffCode,
            String tariffName,
            String transactionType,
            String feeType,
            BigDecimal rateOrFlatValue,
            BigDecimal calculatedFee,
            BigDecimal minFee,
            BigDecimal maxFee,
            String feeGlCode,
            BigDecimal totalDebitRequired) {
        this(feeApplicable, tariffCode, tariffName, transactionType, feeType,
                rateOrFlatValue, calculatedFee, minFee, maxFee, feeGlCode, totalDebitRequired, null);
    }

    public static FeeCalculationResult zero(String transactionType, BigDecimal amount) {
        BigDecimal amt = amount != null ? amount : BigDecimal.ZERO;
        return new FeeCalculationResult(
                false,
                null,
                "No Active Tariff Found",
                transactionType,
                "NONE",
                BigDecimal.ZERO,
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                null,
                null,
                "4020",
                amt.setScale(2, RoundingMode.HALF_UP),
                null
        );
    }
}
