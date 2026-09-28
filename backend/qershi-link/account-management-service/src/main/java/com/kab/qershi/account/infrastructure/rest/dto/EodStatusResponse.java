package com.kab.qershi.account.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Live status of the Core Banking System Business Date and batch runner.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Schema(description = "Live Core Banking Business Date and EOD status")
public record EodStatusResponse(
        @Schema(description = "Active financial business date (e.g. 2026-09-28)")
        LocalDate currentBusinessDate,

        @Schema(description = "System operational status (OPEN, CUTOFF_LOCKED, PROCESSING_EOD, CLOSED)")
        String status,

        @Schema(description = "Flag indicating whether current business date is calendar month-end")
        Boolean isMonthEnd,

        @Schema(description = "Timestamp when the last EOD batch successfully completed")
        LocalDateTime lastEodCompletedAt,

        @Schema(description = "Latest batch run ID, if any")
        UUID lastBatchId,

        @Schema(description = "Latest batch run status, if any")
        String lastBatchStatus
) {}
