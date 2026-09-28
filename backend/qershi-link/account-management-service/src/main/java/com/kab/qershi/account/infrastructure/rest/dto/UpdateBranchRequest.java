package com.kab.qershi.account.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request payload for updating an existing SACCO branch.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Schema(description = "Payload for updating an existing SACCO branch")
public record UpdateBranchRequest(
        @Schema(description = "Branch commercial name", example = "Bole Medhanealem Branch", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Branch name is required.")
        @Size(max = 100, message = "Branch name must not exceed 100 characters.")
        String branchName,

        @Schema(description = "Region / State", example = "Addis Ababa")
        String region,

        @Schema(description = "Physical address or landmark", example = "Bole Subcity, Woreda 03, Cameroon St.")
        String address,

        @Schema(description = "Contact phone number", example = "+251911000000")
        String contactPhone,

        @Schema(description = "UUID of assigned branch manager")
        UUID managerUserId,

        @Schema(description = "Vault GL Account Code for branch physical cash", example = "1010-002")
        String vaultGlCode,

        @Schema(description = "Branch manager discretionary lending approval limit", example = "250000.00")
        BigDecimal discretionaryLendingLimit,

        @Schema(description = "Operational status: ACTIVE or INACTIVE", example = "ACTIVE")
        String status
) {}
