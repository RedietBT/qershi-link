package com.kab.qershi.account.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA Entity mapping tenant-schema branches database table.
 * Manages physical SACCO branch offices, discretionary lending limits, and vault GL accounts.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "branches")
public class BranchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "branch_id", nullable = false, updatable = false)
    private UUID branchId;

    @Column(name = "branch_code", nullable = false, unique = true, length = 10)
    private String branchCode;

    @Column(name = "branch_name", nullable = false, length = 150)
    private String branchName;

    @Column(name = "region", nullable = false, length = 100)
    private String region;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    @Column(name = "manager_user_id")
    private UUID managerUserId;

    @Column(name = "vault_gl_code", nullable = false, length = 50)
    private String vaultGlCode;

    @Column(name = "discretionary_lending_limit", nullable = false, precision = 19, scale = 4)
    private BigDecimal discretionaryLendingLimit;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

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
        if (status == null || status.isBlank()) {
            status = "ACTIVE";
        }
        if (vaultGlCode == null || vaultGlCode.isBlank()) {
            vaultGlCode = "1010-001";
        }
        if (discretionaryLendingLimit == null) {
            discretionaryLendingLimit = new BigDecimal("100000.0000");
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public BranchEntity() {}

    public BranchEntity(UUID branchId, String branchCode, String branchName, String region,
                        String address, String contactPhone, UUID managerUserId,
                        String vaultGlCode, BigDecimal discretionaryLendingLimit, String status) {
        this.branchId = branchId;
        this.branchCode = branchCode;
        this.branchName = branchName;
        this.region = region;
        this.address = address;
        this.contactPhone = contactPhone;
        this.managerUserId = managerUserId;
        this.vaultGlCode = vaultGlCode != null ? vaultGlCode : "1010-001";
        this.discretionaryLendingLimit = discretionaryLendingLimit != null ? discretionaryLendingLimit : new BigDecimal("100000.0000");
        this.status = status != null ? status : "ACTIVE";
    }

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
