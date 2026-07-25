package com.kab.qershi.auth.infrastructure.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SuperAdminRegistrationRequest(
        @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Invalid MSISDN format") String msisdn
) {}
