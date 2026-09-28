package com.kab.qershi.transaction.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Request payload for opening a teller cash drawer for the day.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Schema(description = "Payload to open a teller cash drawer for business operations")
public record OpenTillRequest(
        @Schema(description = "Initial physical opening cash transferred from vault", example = "10000.00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Opening cash amount is required.")
        @DecimalMin(value = "0.00", message = "Opening cash cannot be negative.")
        BigDecimal openingCash
) {}
