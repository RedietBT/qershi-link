package com.kab.qershi.profile.infrastructure.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * REST API Request DTO for contact address saves.
 * Enforces E.164 phone formatting and email validations.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaveAddressRequest {

    @NotBlank(message = "Primary phone is required")
    @Pattern(regexp = "^\\+251\\d{9}$", message = "Phone must comply with E.164 format (+251XXXXXXXXX)")
    private String primaryPhone;

    @Pattern(regexp = "^\\+251\\d{9}$", message = "Secondary phone must comply with E.164 format (+251XXXXXXXXX)")
    private String secondaryPhone;

    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Region is required")
    @Size(max = 100, message = "Region cannot exceed 100 characters")
    private String region;

    @NotBlank(message = "Zone / Subcity is required")
    @Size(max = 100, message = "Zone / Subcity cannot exceed 100 characters")
    private String zoneSubcity;

    @NotBlank(message = "Woreda is required")
    @Size(max = 100, message = "Woreda cannot exceed 100 characters")
    private String woreda;

    @Size(max = 50, message = "House number cannot exceed 50 characters")
    private String houseNumber;

    public String getPrimaryPhone() { return primaryPhone; }
    public void setPrimaryPhone(String primaryPhone) { this.primaryPhone = primaryPhone; }

    public String getSecondaryPhone() { return secondaryPhone; }
    public void setSecondaryPhone(String secondaryPhone) { this.secondaryPhone = secondaryPhone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getZoneSubcity() { return zoneSubcity; }
    public void setZoneSubcity(String zoneSubcity) { this.zoneSubcity = zoneSubcity; }

    public String getWoreda() { return woreda; }
    public void setWoreda(String woreda) { this.woreda = woreda; }

    public String getHouseNumber() { return houseNumber; }
    public void setHouseNumber(String houseNumber) { this.houseNumber = houseNumber; }
}
