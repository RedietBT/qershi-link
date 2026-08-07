package com.kab.qershi.loan.origination.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing pledged collateral assets for a loan application.
 * Dynamic type string permits SACCO-configurable collateral categories (LAND, CROP, GOLD, etc.).
 *
 * @author KAB Digital Solution PLC
 * @version 1.1.0
 */
public class Collateral {

    private final UUID collateralId;
    private final UUID applicationId;
    private final String type;
    private final BigDecimal estimatedValue;
    private final String documentUrl;
    private final Instant createdAt;

    public Collateral(UUID collateralId, UUID applicationId, String type,
                      BigDecimal estimatedValue, String documentUrl, Instant createdAt) {
        this.collateralId = collateralId != null ? collateralId : UUID.randomUUID();
        this.applicationId = applicationId;
        this.type = type != null ? type.toUpperCase().trim() : "LAND";
        this.estimatedValue = estimatedValue != null ? estimatedValue : BigDecimal.ZERO;
        this.documentUrl = documentUrl;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public UUID getCollateralId() {
        return collateralId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getEstimatedValue() {
        return estimatedValue;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
