package com.kab.qershi.auth.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * REST API payload container enforcing input validation criteria for registering a new tenant workspace.
 *
 * @author KAB Digital Solution PLC
 * @version 1.1.0
 */
@Schema(description = "Request schema for onboarding a new SACCO or Union platform workspace tenant")
public record OnboardSaccoRequest(

        @Schema(description = "Official legally registered name of the corporate organization entity", example = "Awach SACCO", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "SACCO legal name cannot be empty.")
        @Size(min = 3, max = 100, message = "SACCO name must be between 3 and 100 characters in length.")
        @Pattern(regexp = "^[A-Za-z0-9 ]+$", message = "SACCO name must only contain alphanumeric characters and spaces to block HTML/XSS scripts.")
        String saccoName,

        @Schema(description = "Flags if the target entity represents a complex multi-sacco Union layer instance", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Union multi-tenant status declaration flag is mandatory.")
        Boolean isUnion,

        @Schema(description = "Minimum monetary capital savings buy-in required to open standard member records", example = "500.0000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Minimum financial share baseline mapping entry cannot be null.")
        @DecimalMin(value = "0.0", inclusive = true, message = "Minimum share buy-in parameters cannot fall below zero boundaries.")
        BigDecimal minShareRequirement,

        @Schema(description = "Primary mobile contact handle assigned to the initial supervisor account", example = "+251987654321", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Primary administrator mobile handle cannot be blank.")
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid administrator mobile number format. Must adhere to E.164 standard.")
        String adminMsisdn,

        @Schema(description = "Official identity name of the primary corporate supervisor account", example = "Arsema Degu", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Administrator contact name is required.")
        @Size(min = 2, max = 60, message = "Administrator name must stay within a secure limit of 60 characters.")
        @Pattern(regexp = "^[A-Za-z ]+$", message = "Administrator name must contain only letters and single spaces to mitigate tag injections.")
        String adminName,

        @Schema(description = "Operational regional domain designation where the workspace resides", example = "Addis Ababa", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Operational regional domain allocation is required.")
        @Size(min = 2, max = 50, message = "Region name string length must be contained securely.")
        @Pattern(regexp = "^[A-Za-z ]+$", message = "Region name must strictly map to alphabetical character strings.")
        String region
) {}