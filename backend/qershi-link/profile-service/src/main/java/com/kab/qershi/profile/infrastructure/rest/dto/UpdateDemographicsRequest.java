package com.kab.qershi.profile.infrastructure.rest.dto;

import com.kab.qershi.profile.domain.model.Gender;
import com.kab.qershi.profile.domain.model.MaritalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * REST API Request DTO for demographic updates.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDemographicsRequest {

    @NotBlank(message = "First name is required")
    @Pattern(regexp = "^[A-Za-z\\s\\-']{2,100}$", message = "First name must contain only alphabetic characters")
    private String firstName;

    @NotBlank(message = "Middle name is required")
    @Pattern(regexp = "^[A-Za-z\\s\\-']{2,100}$", message = "Middle name must contain only alphabetic characters")
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Pattern(regexp = "^[A-Za-z\\s\\-']{2,100}$", message = "Last name must contain only alphabetic characters")
    private String lastName;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Marital status is required")
    private MaritalStatus maritalStatus;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public MaritalStatus getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(MaritalStatus maritalStatus) { this.maritalStatus = maritalStatus; }
}
