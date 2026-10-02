package com.kab.qershi.loan.management.infrastructure.rest.dto;

import com.kab.qershi.loan.management.domain.model.LoanImpairmentProvisionRun;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO for an IFRS 9 / NBE monthly loan impairment provision run summary.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Schema(description = "IFRS 9 / NBE Month-End Loan Impairment Provision Run Summary")
public record Ifrs9ProvisionRunResponse(

        @Schema(description = "Unique provision run identifier")
        UUID runId,

        @Schema(description = "Business date of the month-end run")
        LocalDate businessDate,

        @Schema(description = "Run type: MONTH_END or MANUAL")
        String runType,

        @Schema(description = "Run status: COMPLETED, IN_PROGRESS, FAILED")
        String status,

        @Schema(description = "Total number of active loans evaluated")
        int totalLoansEvaluated,

        @Schema(description = "Total outstanding portfolio principal (ETB)")
        BigDecimal totalPortfolioBalance,

        // ── Per-bucket balances ────────────────────────────────────────────────
        @Schema(description = "Pass bucket outstanding balance (0–29 DPD) [1% reserve]")
        BigDecimal passBalance,

        @Schema(description = "Special Mention outstanding balance (30–89 DPD) [5% reserve]")
        BigDecimal specialMentionBalance,

        @Schema(description = "Substandard outstanding balance (90–179 DPD) [20% reserve]")
        BigDecimal substandardBalance,

        @Schema(description = "Doubtful outstanding balance (180–359 DPD) [50% reserve]")
        BigDecimal doubtfulBalance,

        @Schema(description = "Loss outstanding balance (360+ DPD) [100% reserve]")
        BigDecimal lossBalance,

        // ── Per-bucket provisions ──────────────────────────────────────────────
        @Schema(description = "Required provision for Pass bucket (ETB)")
        BigDecimal passProvision,

        @Schema(description = "Required provision for Special Mention bucket (ETB)")
        BigDecimal specialMentionProvision,

        @Schema(description = "Required provision for Substandard bucket (ETB)")
        BigDecimal substandardProvision,

        @Schema(description = "Required provision for Doubtful bucket (ETB)")
        BigDecimal doubtfulProvision,

        @Schema(description = "Required provision for Loss bucket (ETB)")
        BigDecimal lossProvision,

        @Schema(description = "Total required impairment provision (ETB)")
        BigDecimal totalProvisionRequired,

        // ── GL Posting details ─────────────────────────────────────────────────
        @Schema(description = "GL Debit account: Loan Impairment Loss Expense")
        String glDebitAccount,

        @Schema(description = "GL Credit account: Allowance for Credit Losses")
        String glCreditAccount,

        @Schema(description = "Unique journal entry GL posting reference")
        String glPostingRef,

        @Schema(description = "Timestamp when GL entry was posted")
        OffsetDateTime glPostedAt,

        @Schema(description = "Who triggered the run (SYSTEM_EOD or user identifier)")
        String triggeredBy,

        @Schema(description = "When the run completed")
        OffsetDateTime completedAt
) {
    public static Ifrs9ProvisionRunResponse fromDomain(LoanImpairmentProvisionRun domain) {
        if (domain == null) return null;
        return new Ifrs9ProvisionRunResponse(
                domain.getRunId(),
                domain.getBusinessDate(),
                domain.getRunType(),
                domain.getStatus(),
                domain.getTotalLoansEvaluated() != null ? domain.getTotalLoansEvaluated() : 0,
                domain.getTotalPortfolioBalance(),
                domain.getPassBalance(),
                domain.getSpecialMentionBalance(),
                domain.getSubstandardBalance(),
                domain.getDoubtfulBalance(),
                domain.getLossBalance(),
                domain.getPassProvision(),
                domain.getSpecialMentionProvision(),
                domain.getSubstandardProvision(),
                domain.getDoubtfulProvision(),
                domain.getLossProvision(),
                domain.getTotalProvisionRequired(),
                domain.getGlDebitAccount(),
                domain.getGlCreditAccount(),
                domain.getGlPostingRef(),
                domain.getGlPostedAt(),
                domain.getTriggeredBy(),
                domain.getCompletedAt()
        );
    }
}
