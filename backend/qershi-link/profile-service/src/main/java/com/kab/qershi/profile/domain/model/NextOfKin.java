package com.kab.qershi.profile.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing a nominated beneficiary (Next of Kin) for member payout allocations.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Getter
@Setter
public class NextOfKin {

    private final UUID kinId;
    private final UUID userId;
    private String fullName;
    private String relationship;
    private String primaryPhone;
    private String idNumber;
    private String physicalAddress;
    private BigDecimal allocationPercentage;
    private final Instant createdAt;

    public NextOfKin(UUID kinId, UUID userId, String fullName, String relationship, String primaryPhone,
                     String idNumber, String physicalAddress, BigDecimal allocationPercentage) {
        this.kinId = kinId != null ? kinId : UUID.randomUUID();
        this.userId = userId;
        this.fullName = fullName;
        this.relationship = relationship;
        this.primaryPhone = primaryPhone;
        this.idNumber = idNumber;
        this.physicalAddress = physicalAddress;
        setAllocationPercentage(allocationPercentage);
        this.createdAt = Instant.now();
    }

    public UUID getKinId() { return kinId; }
    public UUID getUserId() { return userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public String getPrimaryPhone() { return primaryPhone; }
    public void setPrimaryPhone(String primaryPhone) { this.primaryPhone = primaryPhone; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public String getPhysicalAddress() { return physicalAddress; }
    public void setPhysicalAddress(String physicalAddress) { this.physicalAddress = physicalAddress; }

    public BigDecimal getAllocationPercentage() { return allocationPercentage; }
    public void setAllocationPercentage(BigDecimal allocationPercentage) {
        if (allocationPercentage == null || allocationPercentage.compareTo(BigDecimal.ZERO) < 0
                || allocationPercentage.compareTo(new BigDecimal("100.00")) > 0) {
            throw new IllegalArgumentException("Allocation percentage must be between 0.00% and 100.00%");
        }
        this.allocationPercentage = allocationPercentage;
    }

    public Instant getCreatedAt() { return createdAt; }
}
