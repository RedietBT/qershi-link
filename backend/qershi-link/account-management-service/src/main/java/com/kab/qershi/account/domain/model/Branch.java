package com.kab.qershi.account.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Pure domain model representing a SACCO physical or digital branch.
 * Controls discretionary lending limits, branch vault GL assignment, and operational status.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class Branch {

    private UUID branchId;
    private String branchCode;
    private String branchName;
    private String region;
    private String address;
    private String contactPhone;
    private UUID managerUserId;
    private String vaultGlCode;
    private BigDecimal discretionaryLendingLimit;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Branch() {
        this.status = "ACTIVE";
        this.vaultGlCode = "1010-001";
        this.discretionaryLendingLimit = new BigDecimal("100000.0000");
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public Branch(UUID branchId, String branchCode, String branchName, String region,
                  String address, String contactPhone, UUID managerUserId,
                  String vaultGlCode, BigDecimal discretionaryLendingLimit, String status,
                  OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.branchId = branchId != null ? branchId : UUID.randomUUID();
        this.branchCode = branchCode;
        this.branchName = branchName;
        this.region = region;
        this.address = address;
        this.contactPhone = contactPhone;
        this.managerUserId = managerUserId;
        this.vaultGlCode = vaultGlCode != null ? vaultGlCode : "1010-001";
        this.discretionaryLendingLimit = discretionaryLendingLimit != null ? discretionaryLendingLimit : new BigDecimal("100000.0000");
        this.status = status != null ? status : "ACTIVE";
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : OffsetDateTime.now();
    }

    public void updateDetails(String branchName, String region, String address, String contactPhone,
                              UUID managerUserId, String vaultGlCode, BigDecimal discretionaryLendingLimit,
                              String status) {
        if (branchName != null && !branchName.isBlank()) this.branchName = branchName.trim();
        if (region != null) this.region = region.trim();
        if (address != null) this.address = address.trim();
        if (contactPhone != null) this.contactPhone = contactPhone.trim();
        if (managerUserId != null) this.managerUserId = managerUserId;
        if (vaultGlCode != null && !vaultGlCode.isBlank()) this.vaultGlCode = vaultGlCode.trim();
        if (discretionaryLendingLimit != null) this.discretionaryLendingLimit = discretionaryLendingLimit;
        if (status != null && !status.isBlank()) this.status = status.toUpperCase().trim();
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateStatus(String status) {
        String cleanStatus = status != null ? status.toUpperCase().trim() : "";
        if (!"ACTIVE".equals(cleanStatus) && !"INACTIVE".equals(cleanStatus)) {
            throw new IllegalArgumentException("Status must be either ACTIVE or INACTIVE");
        }
        this.status = cleanStatus;
        this.updatedAt = OffsetDateTime.now();
    }

    // Getters and Setters
    public UUID getBranchId() { return branchId; }
    public void setBranchId(UUID branchId) { this.branchId = branchId; }

    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public UUID getManagerUserId() { return managerUserId; }
    public void setManagerUserId(UUID managerUserId) { this.managerUserId = managerUserId; }

    public String getVaultGlCode() { return vaultGlCode; }
    public void setVaultGlCode(String vaultGlCode) { this.vaultGlCode = vaultGlCode; }

    public BigDecimal getDiscretionaryLendingLimit() { return discretionaryLendingLimit; }
    public void setDiscretionaryLendingLimit(BigDecimal discretionaryLendingLimit) { this.discretionaryLendingLimit = discretionaryLendingLimit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
