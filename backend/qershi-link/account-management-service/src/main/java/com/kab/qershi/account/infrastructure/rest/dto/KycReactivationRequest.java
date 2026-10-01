package com.kab.qershi.account.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for initiating Maker KYC Reactivation for a dormant account.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Schema(description = "Payload for submitting in-person KYC biometric re-verification for dormant account reactivation")
public record KycReactivationRequest(
        @Schema(description = "Justification or member reason for account reactivation", example = "Member returned from abroad, resumed salary deposit")
        @NotBlank(message = "Reactivation reason is required.")
        String reason,

        @Schema(description = "Details of in-person verification (e.g. Kebele ID #, biometric match, signature verification)", example = "Kebele ID verified (ID #AA-89212), signature matched account opening card")
        @NotBlank(message = "KYC verification details are required.")
        String kycVerificationNotes
) {}
