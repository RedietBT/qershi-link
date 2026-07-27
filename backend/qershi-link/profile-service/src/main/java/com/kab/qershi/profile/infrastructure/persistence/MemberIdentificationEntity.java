package com.kab.qershi.profile.infrastructure.persistence;

import com.kab.qershi.profile.domain.model.IdType;
import com.kab.qershi.profile.domain.model.KycStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA Entity mapping for member_identifications database table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "member_identifications")
@Getter
@Setter
public class MemberIdentificationEntity {

    @Id
    @Column(name = "identification_id", nullable = false, updatable = false)
    private UUID identificationId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "id_type", nullable = false)
    private IdType idType;

    @Column(name = "id_number", nullable = false, length = 100)
    private String idNumber;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "issuing_authority", length = 150)
    private String issuingAuthority;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false)
    private KycStatus kycStatus;

    @Column(name = "verified_by_user_id")
    private UUID verifiedByUserId;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public MemberIdentificationEntity() {}

    public MemberIdentificationEntity(UUID identificationId, UUID userId, IdType idType, String idNumber,
                                      LocalDate issueDate, LocalDate expiryDate, String issuingAuthority,
                                      KycStatus kycStatus, UUID verifiedByUserId, String verificationNotes, Instant createdAt) {
        this.identificationId = identificationId;
        this.userId = userId;
        this.idType = idType;
        this.idNumber = idNumber;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.issuingAuthority = issuingAuthority;
        this.kycStatus = kycStatus;
        this.verifiedByUserId = verifiedByUserId;
        this.verificationNotes = verificationNotes;
        this.createdAt = createdAt;
    }

    public UUID getIdentificationId() { return identificationId; }
    public void setIdentificationId(UUID identificationId) { this.identificationId = identificationId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

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
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
