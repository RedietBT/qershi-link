package com.kab.qershi.profile.infrastructure.rest.dto;

import com.kab.qershi.profile.domain.model.Gender;
import com.kab.qershi.profile.domain.model.MaritalStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * REST API Request DTO for member onboarding profile creation.
 * Member IDs are 100% auto-generated using SACCO initials, year, and a 6-digit sequence.
 *
 * @author KAB Digital Solution PLC
 * @version 1.1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload container for member profile registration. Member No is automatically generated.")
public class CreateProfileRequest {

    @NotNull(message = "User ID is required")
    @Schema(description = "UUID of the user account created in Identity Auth Service", example = "7cad2f18-33d2-4761-b218-a70015bb4926")
    private UUID userId;

    @Schema(description = "Name of the SACCO for generating customized Member ID prefix (optional)", example = "Awash SACCO")
    private String saccoName;

    @NotBlank(message = "First name is required")
    @Pattern(regexp = "^[A-Za-z\\s\\-']{2,100}$", message = "First name must contain only alphabetic characters")
    @Schema(description = "Member first name", example = "Abebe")
    private String firstName;

    @NotBlank(message = "Middle name is required")
    @Pattern(regexp = "^[A-Za-z\\s\\-']{2,100}$", message = "Middle name must contain only alphabetic characters")
    @Schema(description = "Member father name", example = "Bikila")
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Pattern(regexp = "^[A-Za-z\\s\\-']{2,100}$", message = "Last name must contain only alphabetic characters")
    @Schema(description = "Member grandfather name", example = "Tadesse")
    private String lastName;

    @NotNull(message = "Gender is required")
    @Schema(description = "Gender designation", example = "MALE")
    private Gender gender;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Schema(description = "Member date of birth (YYYY-MM-DD)", example = "1990-05-15")
    private LocalDate dateOfBirth;

    @NotNull(message = "Marital status is required")
    @Schema(description = "Marital status", example = "SINGLE")
    private MaritalStatus maritalStatus;

    @NotNull(message = "Submitted by user ID is required")
    @Schema(description = "UUID of the maker/admin officer registering the member", example = "018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f")
    private UUID submittedByUserId;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getSaccoName() { return saccoName; }
    public void setSaccoName(String saccoName) { this.saccoName = saccoName; }

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
