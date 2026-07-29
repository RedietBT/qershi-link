package com.kab.qershi.auth.infrastructure.rest.dto;

import com.kab.qershi.auth.domain.model.GlobalRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

/**
 * REST API payload container enforcing input validation and schema documentation for user registration.
 *
 * @author KAB Digital Solution PLC
 * @version 1.1.0
 */
@Schema(description = "Request schema for registering new user identity accounts")
public record CreateUserRequest(
        @Schema(
                description = "Primary mobile telephone identifier handle formatted in Ethiopian E.164 (+251XXXXXXXXX)",
                example = "+251995220266",
                pattern = "^\\+251\\d{9}$",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Phone number (msisdn) cannot be blank.")
        @Pattern(regexp = "^\\+251\\d{9}$", message = "Phone number must comply with Ethiopian E.164 format (+251XXXXXXXXX)")
        String msisdn,

        @Schema(
                description = "Optional custom 4 to 6 digit security PIN. If omitted or null, a 6-digit initial PIN is automatically generated and sent via SMS.",
                example = "123456"
        )
        String pin,

        @Schema(
                description = "Global platform role assigned to the user",
                example = "SACCO_USER",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Global role cannot be null.")
        GlobalRole globalRole,

        @Schema(
                description = "UUID identifier of the target SACCO workspace entity",
                example = "5fe71c52-7a7b-4a66-8087-48de9cd798b5",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Target saccoId cannot be null.")
        UUID saccoId
) {}
