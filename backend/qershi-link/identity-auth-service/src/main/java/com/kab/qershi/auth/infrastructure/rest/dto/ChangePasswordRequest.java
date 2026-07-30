package com.kab.qershi.auth.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for secure PIN rotation.
 * Supports explicit MSISDN input for first-time password rotation or JWT-extracted MSISDN.
 *
 * @author KAB Digital Solution PLC
 * @version 1.2.0
 */
public record ChangePasswordRequest(
        @Schema(description = "Phone number formatted in Ethiopian E.164 (+251XXXXXXXXX) (Optional if Bearer JWT is provided)", example = "+251995220266")
        @Pattern(regexp = "^(\\+251\\d{9})?$", message = "Phone number must comply with Ethiopian E.164 format (+251XXXXXXXXX)")
        String msisdn,

        @Schema(description = "The current temporary or existing PIN", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
        @Size(min = 6, max = 6, message = "PIN must be exactly 6 digits")
        @Pattern(regexp = "^\\d{6}$", message = "PIN must be numeric")
        String oldPin,

        @Schema(description = "The new 6-digit PIN", example = "654321", requiredMode = Schema.RequiredMode.REQUIRED)
        @Size(min = 6, max = 6, message = "PIN must be exactly 6 digits")
        @Pattern(regexp = "^\\d{6}$", message = "PIN must be numeric")
        String newPin
) {}