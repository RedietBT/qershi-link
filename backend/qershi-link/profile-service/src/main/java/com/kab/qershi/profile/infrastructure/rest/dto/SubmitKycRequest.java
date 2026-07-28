package com.kab.qershi.profile.infrastructure.rest.dto;

import com.kab.qershi.profile.domain.model.IdType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * REST API Request DTO for government ID document submission.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmitKycRequest {

    @NotNull(message = "ID type is required")
    private IdType idType;

    @NotBlank(message = "ID number is required")
    @Size(max = 100, message = "ID number cannot exceed 100 characters")
    private String idNumber;

    private LocalDate issueDate;

    private LocalDate expiryDate;

    @Size(max = 150, message = "Issuing authority cannot exceed 150 characters")
    private String issuingAuthority;

    public IdType getIdType() { return idType; }
    public void setIdType(IdType idType) { this.idType = idType; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getIssuingAuthority() { return issuingAuthority; }
    public void setIssuingAuthority(String issuingAuthority) { this.issuingAuthority = issuingAuthority; }
}
