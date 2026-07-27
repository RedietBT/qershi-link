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
public class NextOfKin {

    private final UUID kinId;
    private final UUID userId;
    @Setter private String fullName;
    @Setter private String relationship;
    @Setter private String primaryPhone;
    @Setter private String idNumber;
    @Setter private String physicalAddress;
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

    public void setAllocationPercentage(BigDecimal allocationPercentage) {
        if (allocationPercentage == null || allocationPercentage.compareTo(BigDecimal.ZERO) < 0
                || allocationPercentage.compareTo(new BigDecimal("100.00")) > 0) {
            throw new IllegalArgumentException("Allocation percentage must be between 0.00% and 100.00%");
        }
        this.allocationPercentage = allocationPercentage;
    }
}
