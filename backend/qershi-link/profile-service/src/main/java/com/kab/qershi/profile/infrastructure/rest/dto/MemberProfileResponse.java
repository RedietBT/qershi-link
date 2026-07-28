package com.kab.qershi.profile.infrastructure.rest.dto;

import com.kab.qershi.profile.domain.model.Gender;
import com.kab.qershi.profile.domain.model.MaritalStatus;
import com.kab.qershi.profile.domain.model.MemberAddress;
import com.kab.qershi.profile.domain.model.MemberEmployment;
import com.kab.qershi.profile.domain.model.MemberGovernance;
import com.kab.qershi.profile.domain.model.MemberProfile;
import com.kab.qershi.profile.domain.model.MemberStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * REST API Response DTO summarizing member profile, contact address, employment, and governance.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Getter
@Setter
public class MemberProfileResponse {

    private UUID userId;
    private String memberNo;
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private MaritalStatus maritalStatus;
    private MemberStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    private MemberAddress address;
    private MemberEmployment employment;
    private MemberGovernance governance;

    public MemberProfileResponse() {}

    public MemberProfileResponse(UUID userId, String memberNo, String firstName, String middleName, String lastName,
                                 String fullName, Gender gender, LocalDate dateOfBirth, MaritalStatus maritalStatus,
                                 MemberStatus status, Instant createdAt, Instant updatedAt,
                                 MemberAddress address, MemberEmployment employment, MemberGovernance governance) {
        this.userId = userId;
        this.memberNo = memberNo;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.maritalStatus = maritalStatus;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.address = address;
        this.employment = employment;
        this.governance = governance;
    }

    public static MemberProfileResponse fromDomain(MemberProfile profile, MemberAddress address,
                                                    MemberEmployment employment, MemberGovernance governance) {
        if (profile == null) return null;
        return new MemberProfileResponse(
                profile.getUserId(),
                profile.getMemberNo(),
                profile.getFirstName(),
                profile.getMiddleName(),
                profile.getLastName(),
                profile.getFullName(),
                profile.getGender(),
                profile.getDateOfBirth(),
                profile.getMaritalStatus(),
                profile.getStatus(),
                profile.getCreatedAt(),
                profile.getUpdatedAt(),
                address,
                employment,
                governance
        );
    }

    public UUID getUserId() { return userId; }
    public String getMemberNo() { return memberNo; }
    public String getFirstName() { return firstName; }
    public String getMiddleName() { return middleName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return fullName; }
    public Gender getGender() { return gender; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public MaritalStatus getMaritalStatus() { return maritalStatus; }
    public MemberStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public MemberAddress getAddress() { return address; }
    public MemberEmployment getEmployment() { return employment; }
    public MemberGovernance getGovernance() { return governance; }
}
