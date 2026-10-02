package com.kab.qershi.account.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Pure domain model representing the SACCO tenant configuration.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class SaccoConfig {

    private UUID id;
    private String saccoCode;
    private String saccoName;
    private String branchCode;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public SaccoConfig() {
        this.saccoCode = "0001";
        this.saccoName = "Default SACCO";
        this.branchCode = "0001";
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public SaccoConfig(UUID id, String saccoCode, String saccoName, String branchCode,
                       OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.saccoCode = saccoCode != null ? saccoCode : "0001";
        this.saccoName = saccoName != null ? saccoName : "Default SACCO";
        this.branchCode = branchCode != null ? branchCode : "0001";
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : OffsetDateTime.now();
    }

    public void update(String saccoCode, String saccoName, String branchCode) {
        if (saccoCode != null && !saccoCode.isBlank()) this.saccoCode = saccoCode.trim();
        if (saccoName != null && !saccoName.isBlank()) this.saccoName = saccoName.trim();
        if (branchCode != null && !branchCode.isBlank()) this.branchCode = branchCode.trim();
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

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
