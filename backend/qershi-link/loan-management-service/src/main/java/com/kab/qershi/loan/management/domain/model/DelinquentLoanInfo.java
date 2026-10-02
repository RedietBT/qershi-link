package com.kab.qershi.loan.management.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Pure Domain Record representing delinquent loan account details.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record DelinquentLoanInfo(
        UUID snapshotId,
        UUID accountId,
        String accountNo,
        UUID userId,
        BigDecimal principalAmount,
        Integer daysPastDue,
        BigDecimal overduePrincipal,
        BigDecimal overdueInterest,
        BigDecimal totalOverdue,
        String parBucket,
        BigDecimal provisionRatePct,
        BigDecimal provisionAmount,
        LocalDate businessDate
) {}
