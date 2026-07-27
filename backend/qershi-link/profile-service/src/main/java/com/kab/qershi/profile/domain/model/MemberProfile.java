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
public class MemberProfile {

    private final UUID userId;
    @Setter private String memberNo;
    @Setter private String firstName;
    @Setter private String middleName;
    @Setter private String lastName;
    @Setter private Gender gender;
    @Setter private LocalDate dateOfBirth;
    @Setter private MaritalStatus maritalStatus;
    @Setter private MemberStatus status;
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

    public void updateTimestamp() {
        this.updatedAt = Instant.now();
    }

    public String getFullName() {
        return (firstName + " " + middleName + " " + lastName).trim();
    }
}
