package com.kab.qershi.account.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA Entity mapping sacco_xxx.sacco_configs database table.
 * Stores SACCO identification code and branch configuration.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "sacco_configs")
public class SaccoConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "config_id", nullable = false, updatable = false)
    private UUID configId;

    @Column(name = "sacco_code", nullable = false, unique = true, length = 20)
    private String saccoCode;

    @Column(name = "sacco_name", length = 200)
    private String saccoName;

    @Column(name = "branch_code", nullable = false, length = 20)
    private String branchCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public SaccoConfigEntity() {}

    public SaccoConfigEntity(UUID configId, String saccoCode, String saccoName, String branchCode) {
        this.configId = configId;
        this.saccoCode = saccoCode;
        this.saccoName = saccoName;
        this.branchCode = branchCode != null ? branchCode : "0001";
    }

    public UUID getConfigId() { return configId; }
    public void setConfigId(UUID configId) { this.configId = configId; }

    public String getSaccoCode() { return saccoCode; }
    public void setSaccoCode(String saccoCode) { this.saccoCode = saccoCode; }

    public String getSaccoName() { return saccoName; }
    public void setSaccoName(String saccoName) { this.saccoName = saccoName; }

    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
