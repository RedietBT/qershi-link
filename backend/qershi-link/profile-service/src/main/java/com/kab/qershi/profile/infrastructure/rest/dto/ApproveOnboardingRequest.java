package com.kab.qershi.profile.infrastructure.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * REST API Request DTO for Four-Eye Principle Maker-Checker onboarding approval.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApproveOnboardingRequest {

    @NotNull(message = "Supervisor ID is required")
    private UUID supervisorId;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    public UUID getSupervisorId() { return supervisorId; }
    public void setSupervisorId(UUID supervisorId) { this.supervisorId = supervisorId; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
