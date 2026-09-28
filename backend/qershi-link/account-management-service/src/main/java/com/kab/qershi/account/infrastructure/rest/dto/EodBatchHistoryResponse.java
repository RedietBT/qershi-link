package com.kab.qershi.account.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Audit response for a historical EOD batch execution.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Schema(description = "EOD batch execution history record")
public record EodBatchHistoryResponse(
        @Schema(description = "Unique batch run identifier")
        UUID batchId,

        @Schema(description = "Business date processed")
        LocalDate businessDate,

        @Schema(description = "Batch start timestamp")
        LocalDateTime startedAt,

        @Schema(description = "Batch completion timestamp")
        LocalDateTime completedAt,

        @Schema(description = "Batch status (IN_PROGRESS, COMPLETED, FAILED)")
        String status,

        @Schema(description = "Trigger mechanism (SYSTEM_CRON, MANUAL_OVERRIDE)")
        String triggeredBy,

        @Schema(description = "Total savings accounts accrued")
        Integer totalAccountsAccrued,

        @Schema(description = "Total interest accrued (ETB)")
        BigDecimal totalInterestAccrued,

        @Schema(description = "Total active loans evaluated for delinquency")
        Integer totalLoansEvaluated,

        @Schema(description = "Total accounts transitioned to DORMANT")
        Integer totalAccountsDormant,

        @Schema(description = "Execution summary notes")
        String summaryNotes,

        @Schema(description = "Granular pipeline step execution logs")
        List<EodStepLogResponse> steps
) {}
