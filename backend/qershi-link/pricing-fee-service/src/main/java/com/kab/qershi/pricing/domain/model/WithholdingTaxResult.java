package com.kab.qershi.pricing.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Domain value object for statutory 5% Withholding Tax deduction.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record WithholdingTaxResult(
        String accountNo,
        LocalDate businessDate,
        BigDecimal grossInterest,
        BigDecimal taxRatePct,
        BigDecimal taxWithheld,
        BigDecimal netInterest,
        String whtGlCode
) {}
