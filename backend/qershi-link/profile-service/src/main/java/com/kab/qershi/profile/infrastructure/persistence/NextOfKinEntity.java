package com.kab.qershi.profile.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity mapping for next_of_kin database table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "next_of_kin")
@Getter
@Setter
public class NextOfKinEntity {

    @Id
    @Column(name = "kin_id", nullable = false, updatable = false)
    private UUID kinId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "relationship", nullable = false, length = 50)
    private String relationship;

    @Column(name = "primary_phone", nullable = false, length = 15)
    private String primaryPhone;

    @Column(name = "id_number", length = 100)
    private String idNumber;

    @Column(name = "physical_address", length = 255)
    private String physicalAddress;

    @Column(name = "allocation_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal allocationPercentage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public NextOfKinEntity() {}

    public NextOfKinEntity(UUID kinId, UUID userId, String fullName, String relationship, String primaryPhone,
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

    public UUID getKinId() { return kinId; }
    public void setKinId(UUID kinId) { this.kinId = kinId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

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
    public void setAllocationPercentage(BigDecimal allocationPercentage) { this.allocationPercentage = allocationPercentage; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
