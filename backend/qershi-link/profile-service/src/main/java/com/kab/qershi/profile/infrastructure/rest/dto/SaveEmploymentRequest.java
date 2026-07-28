package com.kab.qershi.profile.infrastructure.rest.dto;

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
 * REST API Request DTO for employment profile saves.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaveEmploymentRequest {

    @NotBlank(message = "Occupation sector is required")
    @Size(max = 100, message = "Occupation sector cannot exceed 100 characters")
    private String occupationSector;

    @Size(max = 200, message = "Employer name cannot exceed 200 characters")
    private String employerName;

    @NotNull(message = "Monthly income is required")
    @DecimalMin(value = "0.0", message = "Monthly income cannot be negative")
    private BigDecimal monthlyIncome;

    @Pattern(regexp = "^[0-9]{10}$", message = "TIN number must be 10 numeric digits")
    private String tinNumber;

    public String getOccupationSector() { return occupationSector; }
    public void setOccupationSector(String occupationSector) { this.occupationSector = occupationSector; }

    public String getEmployerName() { return employerName; }
    public void setEmployerName(String employerName) { this.employerName = employerName; }

    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public String getTinNumber() { return tinNumber; }
    public void setTinNumber(String tinNumber) { this.tinNumber = tinNumber; }
}
