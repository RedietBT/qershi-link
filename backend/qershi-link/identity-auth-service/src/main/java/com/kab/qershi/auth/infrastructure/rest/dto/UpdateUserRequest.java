package com.kab.qershi.auth.infrastructure.rest.dto;

import com.kab.qershi.auth.domain.model.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(
        @NotBlank(message = "MSISDN cannot be left blank.")
        String msisdn,

        @NotNull(message = "Account status identifier cannot be left null.")
        UserStatus status // Change this from String to the Enum type
) {}