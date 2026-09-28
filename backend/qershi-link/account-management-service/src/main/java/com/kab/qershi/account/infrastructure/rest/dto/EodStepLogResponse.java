package com.kab.qershi.account.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Audit response for an individual EOD pipeline execution step.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Schema(description = "EOD pipeline step execution log")
public record EodStepLogResponse(
        @Schema(description = "Step log identifier")
        UUID stepId,

        @Schema(description = "Step name (e.g. CUTOFF_LOCK, SAVINGS_INTEREST_ACCRUAL, LOAN_PAR_AGING)")
        String stepName,

        @Schema(description = "Execution status (SUCCESS, FAILED, SKIPPED)")
        String status,

        @Schema(description = "Duration in milliseconds")
        Long durationMs,

        @Schema(description = "Number of records processed or affected")
        Integer recordsAffected,

        @Schema(description = "Error details if failed")
        String errorMessage,

        @Schema(description = "Timestamp when step was executed")
        LocalDateTime createdAt
) {}
