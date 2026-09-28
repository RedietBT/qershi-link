package com.kab.qershi.transaction.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Request payload for closing a teller drawer and performing banknote denomination reconciliation.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Schema(description = "Payload for end-of-day teller drawer closure and physical cash counting")
public record CloseTillRequest(
        @Schema(description = "Total physical cash counted by the teller", example = "45850.00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Physical cash counted is required.")
        @DecimalMin(value = "0.00", message = "Physical cash counted cannot be negative.")
        BigDecimal physicalCashCounted,

        @Schema(description = "Count of 200 ETB banknotes", example = "150")
        @Min(value = 0, message = "Banknote count cannot be negative.")
        int notes200Count,

        @Schema(description = "Count of 100 ETB banknotes", example = "100")
        @Min(value = 0, message = "Banknote count cannot be negative.")
        int notes100Count,

        @Schema(description = "Count of 50 ETB banknotes", example = "50")
        @Min(value = 0, message = "Banknote count cannot be negative.")
        int notes50Count,

        @Schema(description = "Count of 10 ETB banknotes", example = "30")
        @Min(value = 0, message = "Banknote count cannot be negative.")
        int notes10Count,

        @Schema(description = "Count of 5 ETB banknotes", example = "10")
        @Min(value = 0, message = "Banknote count cannot be negative.")
        int notes5Count,

        @Schema(description = "Explanatory notes regarding cash variance or drawer handover", example = "Balanced without variance. Cash transferred to Head Office vault.")
        String reconciliationNotes
) {}
