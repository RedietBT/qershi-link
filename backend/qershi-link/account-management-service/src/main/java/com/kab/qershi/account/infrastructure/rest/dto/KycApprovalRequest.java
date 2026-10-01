package com.kab.qershi.account.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for Supervisor Four-Eye Checker sign-off on dormant account KYC reactivation.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Schema(description = "Payload for Supervisor Checker approval or rejection of KYC dormancy reactivation")
public record KycApprovalRequest(
        @Schema(description = "Supervisor audit notes or rejection rationale", example = "KYC documents audited and verified against physical archives. Approved for activation.")
        @NotBlank(message = "Supervisor notes are required.")
        String notes
) {}
