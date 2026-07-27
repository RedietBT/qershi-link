package com.kab.qershi.profile.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Domain entity representing an official government identity document verification record for KYC.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Getter
public class MemberIdentification {

    private final UUID identificationId;
    private final UUID userId;
    @Setter private IdType idType;
    @Setter private String idNumber;
    @Setter private LocalDate issueDate;
    @Setter private LocalDate expiryDate;
    @Setter private String issuingAuthority;
    @Setter private KycStatus kycStatus;
    @Setter private UUID verifiedByUserId;
    @Setter private String verificationNotes;
    private final Instant createdAt;

    public MemberIdentification(UUID identificationId, UUID userId, IdType idType, String idNumber,
                                LocalDate issueDate, LocalDate expiryDate, String issuingAuthority,
                                KycStatus kycStatus, UUID verifiedByUserId, String verificationNotes) {
        this.identificationId = identificationId != null ? identificationId : UUID.randomUUID();
        this.userId = userId;
        this.idType = idType;
        this.idNumber = idNumber;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.issuingAuthority = issuingAuthority;
        this.kycStatus = kycStatus != null ? kycStatus : KycStatus.UNVERIFIED;
        this.verifiedByUserId = verifiedByUserId;
        this.verificationNotes = verificationNotes;
        this.createdAt = Instant.now();
    }

    public void verify(UUID supervisorId, String notes) {
        this.kycStatus = KycStatus.VERIFIED;
        this.verifiedByUserId = supervisorId;
        this.verificationNotes = notes;
    }

    public void reject(UUID supervisorId, String notes) {
        this.kycStatus = KycStatus.REJECTED;
        this.verifiedByUserId = supervisorId;
        this.verificationNotes = notes;
    }
}
