package com.kab.qershi.transaction.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request payload for configuring or assigning a teller till to a branch teller.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Schema(description = "Payload for provisioning or assigning a teller cash drawer")
public record AssignTillRequest(
        @Schema(description = "Branch UUID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Branch ID is required.")
        UUID branchId,

        @Schema(description = "Branch Code", example = "001", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Branch Code is required.")
        String branchCode,

        @Schema(description = "Assigned Teller User UUID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Teller User ID is required.")
        UUID tellerUserId,

        @Schema(description = "Drawer/Till friendly designation", example = "Drawer 1 - Main Counter", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Till name is required.")
        String tillName,

        @Schema(description = "GL Account Code for this till", example = "1020-001-01")
        String tillGlCode,

        @Schema(description = "Maximum cash ceiling before mandatory vault transfer", example = "250000.00")
        BigDecimal maxCashLimit
) {}
