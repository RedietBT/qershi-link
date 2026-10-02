package com.kab.qershi.loan.management.domain.model;

import java.math.BigDecimal;

/**
 * Pure Domain Record representing aggregate Portfolio at Risk summary statistics.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record ParSummary(
        int totalLoans,
        BigDecimal totalPrincipal,
        int currentCount,
        BigDecimal currentAmount,
        int par30Count,
        BigDecimal par30Amount,
        int par60Count,
        BigDecimal par60Amount,
        int par90Count,
        BigDecimal par90Amount,
        int lossCount,
        BigDecimal lossAmount,
        BigDecimal nplRatio,
        BigDecimal totalProvisions
) {}
