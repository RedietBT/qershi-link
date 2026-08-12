package com.kab.qershi.account.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for configuring SACCO identification code.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Schema(description = "Payload for establishing or updating SACCO identification code")
public record SaccoConfigRequest(
        @Schema(description = "Unique 4-digit or alphanumeric SACCO code", example = "1122", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "SACCO code is required.")
        @Size(max = 20, message = "SACCO code must not exceed 20 characters.")
        String saccoCode,

        @Schema(description = "Legal SACCO name (Optional)", example = "Awach SACCO")
        String saccoName
) {}
