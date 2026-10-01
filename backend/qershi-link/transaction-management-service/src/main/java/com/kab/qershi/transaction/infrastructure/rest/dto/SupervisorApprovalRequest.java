package com.kab.qershi.transaction.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload for supervisor sign-off on till cash variance")
public record SupervisorApprovalRequest(
        @Schema(description = "Supervisor review comments or approval rationale", example = "Physical cash variance verified by branch supervisor.")
        @NotBlank(message = "Supervisor approval notes are required.")
        String supervisorNotes
) {}
