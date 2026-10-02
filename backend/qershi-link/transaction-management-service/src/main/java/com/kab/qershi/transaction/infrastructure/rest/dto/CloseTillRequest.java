package com.kab.qershi.transaction.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

/**
 * Request payload for closing a teller drawer and performing banknote denomination reconciliation.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Schema(description = "Payload for end-of-day teller drawer closure and blind physical cash counting")
public record CloseTillRequest(
        @Schema(description = "Total physical cash counted by the teller (optional, computed if not supplied)", example = "45850.00")
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

        @Schema(description = "Total amount of coins or small change counted in ETB", example = "50.00")
        @DecimalMin(value = "0.00", message = "Coins amount cannot be negative.")
        BigDecimal coinsAmount,

        @Schema(description = "Explanatory notes regarding cash variance or drawer handover", example = "Balanced without variance. Cash transferred to Head Office vault.")
        String reconciliationNotes
) {
    public CloseTillRequest {
        if (coinsAmount == null) coinsAmount = BigDecimal.ZERO;
    }

    public com.kab.qershi.transaction.domain.ports.inbound.TellerTillUseCase.CloseTillCommand toCommand() {
        return new com.kab.qershi.transaction.domain.ports.inbound.TellerTillUseCase.CloseTillCommand(
                physicalCashCounted,
                notes200Count,
                notes100Count,
                notes50Count,
                notes10Count,
                notes5Count,
                coinsAmount,
                reconciliationNotes
        );
    }
}
