package com.kab.qershi.loan.origination.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapping collateral_types table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "collateral_types")
public class CollateralTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "type_id")
    private UUID typeId;

    @Column(name = "type_code", nullable = false, unique = true, length = 50)
    private String typeCode;

    @Column(name = "type_name", nullable = false, length = 100)
    private String typeName;

    @Column(name = "min_coverage_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal minCoveragePct = new BigDecimal("100.00");

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public CollateralTypeEntity() {}

    public CollateralTypeEntity(UUID typeId, String typeCode, String typeName, BigDecimal minCoveragePct, boolean active, Instant createdAt) {
        this.typeId = typeId;
        this.typeCode = typeCode;
        this.typeName = typeName;
        this.minCoveragePct = minCoveragePct;
        this.active = active;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public UUID getTypeId() {
        return typeId;
    }

    public void setTypeId(UUID typeId) {
        this.typeId = typeId;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public BigDecimal getMinCoveragePct() {
        return minCoveragePct;
    }

    public void setMinCoveragePct(BigDecimal minCoveragePct) {
        this.minCoveragePct = minCoveragePct;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
