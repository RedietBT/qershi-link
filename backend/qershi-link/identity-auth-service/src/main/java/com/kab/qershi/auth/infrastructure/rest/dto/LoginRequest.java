package com.kab.qershi.auth.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * REST API payload container enforcing input validation rules for identity authentication requests.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Schema(description = "Request schema for multi-tenant user authentication credentials")
public record LoginRequest(

        @Schema(description = "User primary mobile telephone identifier handles", example = "+251912345678", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Mobile number (MSISDN) cannot be blank.")
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid mobile number format. Must adhere to E.164 compliance standard.")
        String msisdn,

        @Schema(description = "Secure security access verification credential pin", example = "1234", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Security validation PIN cannot be blank.")
        @Size(min = 4, max = 6, message = "Security PIN must contain between 4 and 6 numeric characters.")
        @Pattern(regexp = "^\\d+$", message = "Security PIN must consist strictly of digits.")
        String pin
) {}