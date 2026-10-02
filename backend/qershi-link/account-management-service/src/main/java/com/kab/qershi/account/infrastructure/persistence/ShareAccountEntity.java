package com.kab.qershi.account.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA Entity mapping the share_accounts table.
 * Represents a member's equity share capital account under GL 3100.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "share_accounts")
public class ShareAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "account_number", nullable = false, unique = true, length = 32)
    private String accountNumber;

    @Column(name = "total_shares", nullable = false)
    private Integer totalShares = 0;

    @Column(name = "share_nominal_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal shareNominalValue = new BigDecimal("1000.0000");

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "sacco_code", length = 20)
    private String saccoCode;

    @Column(name = "branch_code", length = 20)
    private String branchCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public ShareAccountEntity() {}

    public ShareAccountEntity(UUID memberId, String accountNumber, BigDecimal shareNominalValue, String saccoCode, String branchCode) {
        this.memberId = memberId;
        this.accountNumber = accountNumber;
        this.shareNominalValue = shareNominalValue != null ? shareNominalValue : new BigDecimal("1000.0000");
        this.totalShares = 0;
        this.totalAmount = BigDecimal.ZERO;
        this.status = "ACTIVE";
        this.saccoCode = saccoCode;
        this.branchCode = branchCode;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getMemberId() { return memberId; }
    public void setMemberId(UUID memberId) { this.memberId = memberId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public Integer getTotalShares() { return totalShares; }
    public void setTotalShares(Integer totalShares) { this.totalShares = totalShares; }

    public BigDecimal getShareNominalValue() { return shareNominalValue; }
    public void setShareNominalValue(BigDecimal shareNominalValue) { this.shareNominalValue = shareNominalValue; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSaccoCode() { return saccoCode; }
    public void setSaccoCode(String saccoCode) { this.saccoCode = saccoCode; }

    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
