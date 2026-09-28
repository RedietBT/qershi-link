package com.kab.qershi.loan.management.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Detailed delinquency and PAR status for an individual overdue loan account.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Schema(description = "Delinquent loan account detail and aging classification")
public record DelinquentLoanResponse(
        @Schema(description = "Snapshot record ID")
        UUID snapshotId,

        @Schema(description = "Loan account identifier")
        UUID accountId,

        @Schema(description = "Loan account human-readable number")
        String accountNo,

        @Schema(description = "Borrower member UUID")
        UUID userId,

        @Schema(description = "Original disbursed loan principal (ETB)")
        BigDecimal principalAmount,

        @Schema(description = "Days Past Due (DPD)")
        Integer daysPastDue,

        @Schema(description = "Overdue principal portion (ETB)")
        BigDecimal overduePrincipal,

        @Schema(description = "Overdue interest portion (ETB)")
        BigDecimal overdueInterest,

        @Schema(description = "Total overdue amount (ETB)")
        BigDecimal totalOverdue,

        @Schema(description = "Regulatory PAR aging bucket")
        String parBucket,

        @Schema(description = "Regulatory provision reserve percentage")
        BigDecimal provisionRatePct,

        @Schema(description = "Regulatory loan loss provision amount (ETB)")
        BigDecimal provisionAmount,

        @Schema(description = "Business date when evaluated")
        LocalDate businessDate
) {}
