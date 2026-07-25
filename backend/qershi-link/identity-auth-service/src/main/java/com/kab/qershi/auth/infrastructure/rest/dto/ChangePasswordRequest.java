package com.kab.qershi.auth.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for secure PIN rotation.
 */
public record ChangePasswordRequest(
        @Schema(description = "The current temporary or existing PIN", example = "123456", required = true)
        @Size(min = 6, max = 6, message = "PIN must be exactly 6 digits")
        @Pattern(regexp = "^\\d{6}$", message = "PIN must be numeric")
        String oldPin,

        @Schema(description = "The new 6-digit PIN", example = "654321", required = true)
        @Size(min = 6, max = 6, message = "PIN must be exactly 6 digits")
        @Pattern(regexp = "^\\d{6}$", message = "PIN must be numeric")
        String newPin
) {}