package com.kab.qershi.loan.origination.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapping loan_collateral table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "loan_collateral")
public class LoanCollateralEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "collateral_id")
    private UUID collateralId;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "estimated_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal estimatedValue = BigDecimal.ZERO;

    @Column(name = "document_url")
    private String documentUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public LoanCollateralEntity() {}

    public LoanCollateralEntity(UUID collateralId, UUID applicationId, String type,
                                BigDecimal estimatedValue, String documentUrl, Instant createdAt) {
        this.collateralId = collateralId;
        this.applicationId = applicationId;
        this.type = type;
        this.estimatedValue = estimatedValue != null ? estimatedValue : BigDecimal.ZERO;
        this.documentUrl = documentUrl;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public UUID getCollateralId() {
        return collateralId;
    }

    public void setCollateralId(UUID collateralId) {
        this.collateralId = collateralId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getEstimatedValue() {
        return estimatedValue;
    }

    public void setEstimatedValue(BigDecimal estimatedValue) {
        this.estimatedValue = estimatedValue;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
