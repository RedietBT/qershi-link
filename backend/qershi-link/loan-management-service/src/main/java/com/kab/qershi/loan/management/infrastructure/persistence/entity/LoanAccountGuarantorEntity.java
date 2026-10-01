package com.kab.qershi.loan.management.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity mapping loan_account_guarantors table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "loan_account_guarantors")
public class LoanAccountGuarantorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "guarantor_id", nullable = false, updatable = false)
    private UUID guarantorId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "application_id")
    private UUID applicationId;

    @Column(name = "guarantor_user_id", nullable = false)
    private UUID guarantorUserId;

    @Column(name = "guarantor_name", length = 150)
    private String guarantorName;

    @Column(name = "guarantor_phone", length = 20)
    private String guarantorPhone;

    @Column(name = "savings_account_no", nullable = false, length = 50)
    private String savingsAccountNo;

    @Column(name = "guaranteed_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal guaranteedAmount;

    @Column(name = "lien_id")
    private UUID lienId;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "HELD";

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public LoanAccountGuarantorEntity() {}

    public LoanAccountGuarantorEntity(UUID guarantorId, UUID accountId, UUID applicationId,
                                      UUID guarantorUserId, String guarantorName, String guarantorPhone,
                                      String savingsAccountNo, BigDecimal guaranteedAmount,
                                      UUID lienId, String status, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.guarantorId = guarantorId;
        this.accountId = accountId;
        this.applicationId = applicationId;
        this.guarantorUserId = guarantorUserId;
        this.guarantorName = guarantorName;
        this.guarantorPhone = guarantorPhone;
        this.savingsAccountNo = savingsAccountNo;
        this.guaranteedAmount = guaranteedAmount;
        this.lienId = lienId;
        this.status = status != null ? status : "HELD";
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : OffsetDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (updatedAt == null) updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getGuarantorId() { return guarantorId; }
    public void setGuarantorId(UUID guarantorId) { this.guarantorId = guarantorId; }
    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public UUID getGuarantorUserId() { return guarantorUserId; }
    public void setGuarantorUserId(UUID guarantorUserId) { this.guarantorUserId = guarantorUserId; }
    public String getGuarantorName() { return guarantorName; }
    public void setGuarantorName(String guarantorName) { this.guarantorName = guarantorName; }
    public String getGuarantorPhone() { return guarantorPhone; }
    public void setGuarantorPhone(String guarantorPhone) { this.guarantorPhone = guarantorPhone; }
    public String getSavingsAccountNo() { return savingsAccountNo; }
    public void setSavingsAccountNo(String savingsAccountNo) { this.savingsAccountNo = savingsAccountNo; }
    public BigDecimal getGuaranteedAmount() { return guaranteedAmount; }
    public void setGuaranteedAmount(BigDecimal guaranteedAmount) { this.guaranteedAmount = guaranteedAmount; }
    public UUID getLienId() { return lienId; }
    public void setLienId(UUID lienId) { this.lienId = lienId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
