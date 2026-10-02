package com.kab.qershi.loan.management.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Pure Domain Aggregate representing a SACCO Loan Peer Guarantor.
 * Tracks guarantor identity, guarantee commitment, and lien hold lifecycle.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class LoanAccountGuarantor {

    private UUID guarantorId;
    private UUID accountId;
    private UUID applicationId;
    private UUID guarantorUserId;
    private String guarantorName;
    private String guarantorPhone;
    private String savingsAccountNo;
    private BigDecimal guaranteedAmount;
    private UUID lienId;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public LoanAccountGuarantor() {}

    public LoanAccountGuarantor(UUID guarantorId, UUID accountId, UUID applicationId,
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
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getGuarantorId() {
        return guarantorId;
    }

    public void setGuarantorId(UUID guarantorId) {
        this.guarantorId = guarantorId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public UUID getGuarantorUserId() {
        return guarantorUserId;
    }

    public void setGuarantorUserId(UUID guarantorUserId) {
        this.guarantorUserId = guarantorUserId;
    }

    public String getGuarantorName() {
        return guarantorName;
    }

    public void setGuarantorName(String guarantorName) {
        this.guarantorName = guarantorName;
    }

    public String getGuarantorPhone() {
        return guarantorPhone;
    }

    public void setGuarantorPhone(String guarantorPhone) {
        this.guarantorPhone = guarantorPhone;
    }

    public String getSavingsAccountNo() {
        return savingsAccountNo;
    }

    public void setSavingsAccountNo(String savingsAccountNo) {
        this.savingsAccountNo = savingsAccountNo;
    }

    public BigDecimal getGuaranteedAmount() {
        return guaranteedAmount;
    }

    public void setGuaranteedAmount(BigDecimal guaranteedAmount) {
        this.guaranteedAmount = guaranteedAmount;
    }

    public UUID getLienId() {
        return lienId;
    }

    public void setLienId(UUID lienId) {
        this.lienId = lienId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
