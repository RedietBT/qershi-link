package com.kab.qershi.auth.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * REST API payload container for requesting initial PIN resend via SMS.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Schema(description = "Request schema for requesting an initial PIN resend via SMS")
public record ResendPinRequest(
        @Schema(
                description = "Primary mobile telephone identifier handle formatted in Ethiopian E.164 (+251XXXXXXXXX)",
                example = "+251995220266",
                pattern = "^\\+251\\d{9}$",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Phone number (msisdn) cannot be blank.")
        @Pattern(regexp = "^\\+251\\d{9}$", message = "Phone number must comply with Ethiopian E.164 format (+251XXXXXXXXX)")
        String msisdn
) {}
