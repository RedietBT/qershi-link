package com.kab.qershi.profile.infrastructure.rest.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * REST API Request DTO for updating a Next of Kin beneficiary.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNextOfKinRequest {

    @NotBlank(message = "Full name is required")
    @Pattern(regexp = "^[A-Za-z\\s\\-']{2,255}$", message = "Full name must contain only alphabetic characters")
    private String fullName;

    @NotBlank(message = "Relationship is required")
    @Size(max = 50, message = "Relationship cannot exceed 50 characters")
    private String relationship;

    @NotBlank(message = "Primary phone is required")
    @Pattern(regexp = "^\\+251\\d{9}$", message = "Phone must comply with E.164 format (+251XXXXXXXXX)")
    private String primaryPhone;

    @Size(max = 100, message = "ID number cannot exceed 100 characters")
    private String idNumber;

    @Size(max = 255, message = "Physical address cannot exceed 255 characters")
    private String physicalAddress;

    @NotNull(message = "Allocation percentage is required")
    @DecimalMin(value = "0.00", message = "Allocation percentage cannot be negative")
    @DecimalMax(value = "100.00", message = "Allocation percentage cannot exceed 100.00%")
    private BigDecimal allocationPercentage;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public String getPrimaryPhone() { return primaryPhone; }
    public void setPrimaryPhone(String primaryPhone) { this.primaryPhone = primaryPhone; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public String getPhysicalAddress() { return physicalAddress; }
    public void setPhysicalAddress(String physicalAddress) { this.physicalAddress = physicalAddress; }

    public BigDecimal getAllocationPercentage() { return allocationPercentage; }
    public void setAllocationPercentage(BigDecimal allocationPercentage) { this.allocationPercentage = allocationPercentage; }
}
