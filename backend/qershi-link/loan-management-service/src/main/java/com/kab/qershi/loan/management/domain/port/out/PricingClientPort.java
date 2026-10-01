package com.kab.qershi.loan.management.domain.port.out;

import java.math.BigDecimal;

/**
 * Outbound port interface for dynamic loan processing fee calculation via pricing-fee-service.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface PricingClientPort {

    FeeAssessment calculateFee(String transactionType, BigDecimal amount, String customerTier, String currency);

    record FeeAssessment(
            boolean feeApplicable,
            String tariffCode,
            String tariffName,
            BigDecimal feeAmount,
            String feeGlCode,
            BigDecimal totalDebitAmount
    ) {}
}
