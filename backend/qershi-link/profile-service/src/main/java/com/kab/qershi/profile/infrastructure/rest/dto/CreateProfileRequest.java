package com.kab.qershi.profile.infrastructure.rest.dto;

import com.kab.qershi.profile.domain.model.Gender;
import com.kab.qershi.profile.domain.model.MaritalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * REST API Request DTO for member onboarding profile creation.
 * Enforces strict alphabetic name validation to disallow numeric input.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProfileRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @Size(max = 50, message = "Member number cannot exceed 50 characters")
    private String memberNo;

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

    @NotNull(message = "Submitted by user ID is required")
    private UUID submittedByUserId;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getMemberNo() { return memberNo; }
    public void setMemberNo(String memberNo) { this.memberNo = memberNo; }

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

    public UUID getSubmittedByUserId() { return submittedByUserId; }
    public void setSubmittedByUserId(UUID submittedByUserId) { this.submittedByUserId = submittedByUserId; }
}
