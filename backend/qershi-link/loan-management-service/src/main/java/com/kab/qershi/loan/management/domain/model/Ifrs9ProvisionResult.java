package com.kab.qershi.loan.management.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Pure Domain Record representing the result of an IFRS 9 / NBE month-end impairment run.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record Ifrs9ProvisionResult(
        UUID runId,
        LocalDate businessDate,
        int totalLoansEvaluated,
        BigDecimal totalPortfolioBalance,
        BigDecimal totalProvisionRequired,
        String glPostingRef,
        String status
) {}
