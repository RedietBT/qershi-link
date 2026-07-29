package com.kab.qershi.auth.infrastructure.rest.dto;

import com.kab.qershi.auth.domain.model.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * REST API payload container enforcing input validation and schema documentation for user updates.
 *
 * @author KAB Digital Solution PLC
 * @version 1.1.0
 */
@Schema(description = "Request schema for updating user security parameters")
public record UpdateUserRequest(
        @Schema(
                description = "Primary mobile telephone identifier handle formatted in Ethiopian E.164 (+251XXXXXXXXX)",
                example = "+251995220266",
                pattern = "^\\+251\\d{9}$",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "MSISDN cannot be left blank.")
        @Pattern(regexp = "^\\+251\\d{9}$", message = "Phone number must comply with Ethiopian E.164 format (+251XXXXXXXXX)")
        String msisdn,

        @Schema(
                description = "Updated user lifecycle status",
                example = "ACTIVE",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Account status identifier cannot be left null.")
        UserStatus status
) {}