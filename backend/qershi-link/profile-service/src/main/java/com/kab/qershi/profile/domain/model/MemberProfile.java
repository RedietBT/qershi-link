package com.kab.qershi.profile.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Domain Aggregate Root representing a SACCO member's core identity and lifecycle state.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Getter
@Setter
public class MemberProfile {

    private final UUID userId;
    private String memberNo;
    private String firstName;
    private String middleName;
    private String lastName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private MaritalStatus maritalStatus;
    private MemberStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public MemberProfile(UUID userId, String memberNo, String firstName, String middleName, String lastName,
                         Gender gender, LocalDate dateOfBirth, MaritalStatus maritalStatus, MemberStatus status) {
        this.userId = userId != null ? userId : UUID.randomUUID();
        this.memberNo = memberNo;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.maritalStatus = maritalStatus != null ? maritalStatus : MaritalStatus.SINGLE;
        this.status = status != null ? status : MemberStatus.PENDING_APPROVAL;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getUserId() { return userId; }
    public String getMemberNo() { return memberNo; }
    public void setMemberNo(String memberNo) { this.memberNo = memberNo; this.updatedAt = Instant.now(); }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; this.updatedAt = Instant.now(); }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; this.updatedAt = Instant.now(); }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; this.updatedAt = Instant.now(); }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; this.updatedAt = Instant.now(); }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; this.updatedAt = Instant.now(); }

    public MaritalStatus getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(MaritalStatus maritalStatus) { this.maritalStatus = maritalStatus; this.updatedAt = Instant.now(); }

    public MemberStatus getStatus() { return status; }
    public void setStatus(MemberStatus status) { this.status = status; this.updatedAt = Instant.now(); }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void updateTimestamp() {
        this.updatedAt = Instant.now();
    }

    public String getFullName() {
        return (firstName + " " + middleName + " " + lastName).trim();
    }
}
