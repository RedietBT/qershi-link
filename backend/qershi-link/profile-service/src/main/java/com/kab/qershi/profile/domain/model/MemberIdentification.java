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
@Setter
public class MemberIdentification {

    private final UUID identificationId;
    private final UUID userId;
    private IdType idType;
    private String idNumber;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String issuingAuthority;
    private KycStatus kycStatus;
    private UUID verifiedByUserId;
    private String verificationNotes;
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

    public UUID getIdentificationId() { return identificationId; }
    public UUID getUserId() { return userId; }

    public IdType getIdType() { return idType; }
    public void setIdType(IdType idType) { this.idType = idType; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getIssuingAuthority() { return issuingAuthority; }
    public void setIssuingAuthority(String issuingAuthority) { this.issuingAuthority = issuingAuthority; }

    public KycStatus getKycStatus() { return kycStatus; }
    public void setKycStatus(KycStatus kycStatus) { this.kycStatus = kycStatus; }

    public UUID getVerifiedByUserId() { return verifiedByUserId; }
    public void setVerifiedByUserId(UUID verifiedByUserId) { this.verifiedByUserId = verifiedByUserId; }

    public String getVerificationNotes() { return verificationNotes; }
    public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }

    public Instant getCreatedAt() { return createdAt; }

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
