package com.kab.qershi.profile.infrastructure.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * REST API Request DTO for supervisor KYC verification or rejection.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerifyKycRequest {

    @Size(max = 500, message = "Verification notes cannot exceed 500 characters")
    private String notes;

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
