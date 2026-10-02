package com.kab.qershi.loan.management.domain.model;

import java.math.BigDecimal;

/**
 * Pure Domain Record representing the result of Portfolio at Risk (PAR) aging evaluation.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
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
