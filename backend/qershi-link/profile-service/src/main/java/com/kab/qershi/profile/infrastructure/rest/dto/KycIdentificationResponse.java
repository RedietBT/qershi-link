package com.kab.qershi.profile.infrastructure.rest.dto;

import com.kab.qershi.profile.domain.model.IdType;
import com.kab.qershi.profile.domain.model.KycStatus;
import com.kab.qershi.profile.domain.model.MemberIdentification;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * REST API Response DTO for government ID document verification records.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Getter
@Setter
public class KycIdentificationResponse {

    private UUID identificationId;
    private UUID userId;
    private IdType idType;
    private String idNumber;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String issuingAuthority;
    private KycStatus kycStatus;
    private UUID verifiedByUserId;
    private String verificationNotes;
    private Instant createdAt;

    public KycIdentificationResponse() {}

    public KycIdentificationResponse(UUID identificationId, UUID userId, IdType idType, String idNumber,
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

    public static KycIdentificationResponse fromDomain(MemberIdentification domain) {
        if (domain == null) return null;
        return new KycIdentificationResponse(
                domain.getIdentificationId(),
                domain.getUserId(),
                domain.getIdType(),
                domain.getIdNumber(),
                domain.getIssueDate(),
                domain.getExpiryDate(),
                domain.getIssuingAuthority(),
                domain.getKycStatus(),
                domain.getVerifiedByUserId(),
                domain.getVerificationNotes(),
                domain.getCreatedAt()
        );
    }

    public UUID getIdentificationId() { return identificationId; }
    public UUID getUserId() { return userId; }
    public IdType getIdType() { return idType; }
    public String getIdNumber() { return idNumber; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public String getIssuingAuthority() { return issuingAuthority; }
    public KycStatus getKycStatus() { return kycStatus; }
    public UUID getVerifiedByUserId() { return verifiedByUserId; }
    public String getVerificationNotes() { return verificationNotes; }
    public Instant getCreatedAt() { return createdAt; }
}
