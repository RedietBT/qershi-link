package com.kab.qershi.profile.infrastructure.rest.dto;

import com.kab.qershi.profile.domain.model.NextOfKin;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * REST API Response DTO for nominated beneficiaries.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Getter
@Setter
public class NextOfKinResponse {

    private UUID kinId;
    private UUID userId;
    private String fullName;
    private String relationship;
    private String primaryPhone;
    private String idNumber;
    private String physicalAddress;
    private BigDecimal allocationPercentage;
    private Instant createdAt;

    public NextOfKinResponse() {}

    public NextOfKinResponse(UUID kinId, UUID userId, String fullName, String relationship, String primaryPhone,
                             String idNumber, String physicalAddress, BigDecimal allocationPercentage, Instant createdAt) {
        this.kinId = kinId;
        this.userId = userId;
        this.fullName = fullName;
        this.relationship = relationship;
        this.primaryPhone = primaryPhone;
        this.idNumber = idNumber;
        this.physicalAddress = physicalAddress;
        this.allocationPercentage = allocationPercentage;
        this.createdAt = createdAt;
    }

    public static NextOfKinResponse fromDomain(NextOfKin domain) {
        if (domain == null) return null;
        return new NextOfKinResponse(
                domain.getKinId(),
                domain.getUserId(),
                domain.getFullName(),
                domain.getRelationship(),
                domain.getPrimaryPhone(),
                domain.getIdNumber(),
                domain.getPhysicalAddress(),
                domain.getAllocationPercentage(),
                domain.getCreatedAt()
        );
    }

    public UUID getKinId() { return kinId; }
    public UUID getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getRelationship() { return relationship; }
    public String getPrimaryPhone() { return primaryPhone; }
    public String getIdNumber() { return idNumber; }
    public String getPhysicalAddress() { return physicalAddress; }
    public BigDecimal getAllocationPercentage() { return allocationPercentage; }
    public Instant getCreatedAt() { return createdAt; }
}
