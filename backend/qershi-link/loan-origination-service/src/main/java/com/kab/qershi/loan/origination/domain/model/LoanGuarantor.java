package com.kab.qershi.loan.origination.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing a member peer guarantor pledging savings liens for a loan application.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class LoanGuarantor {

    private final UUID guarantorId;
    private final UUID applicationId;
    private final UUID guarantorUserId;
    private final String guarantorName;
    private final String guarantorPhone;
    private final String savingsAccountNo;
    private final BigDecimal guaranteedAmount;
    private UUID lienId;
    private String status;
    private final Instant createdAt;
    private Instant updatedAt;

    public LoanGuarantor(UUID guarantorId, UUID applicationId, UUID guarantorUserId,
                         String guarantorName, String guarantorPhone, String savingsAccountNo,
                         BigDecimal guaranteedAmount, UUID lienId, String status,
                         Instant createdAt, Instant updatedAt) {
        this.guarantorId = guarantorId != null ? guarantorId : UUID.randomUUID();
        this.applicationId = applicationId;
        this.guarantorUserId = guarantorUserId;
        this.guarantorName = guarantorName;
        this.guarantorPhone = guarantorPhone;
        this.savingsAccountNo = savingsAccountNo;
        this.guaranteedAmount = guaranteedAmount != null ? guaranteedAmount : BigDecimal.ZERO;
        this.lienId = lienId;
        this.status = status != null ? status : "PENDING";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public UUID getGuarantorId() { return guarantorId; }
    public UUID getApplicationId() { return applicationId; }
    public UUID getGuarantorUserId() { return guarantorUserId; }
    public String getGuarantorName() { return guarantorName; }
    public String getGuarantorPhone() { return guarantorPhone; }
    public String getSavingsAccountNo() { return savingsAccountNo; }
    public BigDecimal getGuaranteedAmount() { return guaranteedAmount; }
    public UUID getLienId() { return lienId; }
    public void setLienId(UUID lienId) { this.lienId = lienId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
