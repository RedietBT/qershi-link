package com.kab.qershi.account.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Pure domain model representing a partial monetary lien hold (loan collateral / guarantee block).
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class AccountLien {

    private UUID lienId;
    private String accountNo;
    private BigDecimal lienAmount;
    private String reason;
    private String referenceNo;
    private UUID placedByUserId;
    private UUID releasedByUserId;
    private LienStatus status;
    private LocalDateTime placedAt;
    private LocalDateTime releasedAt;

    public AccountLien() {
        this.status = LienStatus.ACTIVE;
        this.placedAt = LocalDateTime.now();
    }

    public AccountLien(UUID lienId, String accountNo, BigDecimal lienAmount, String reason,
                       String referenceNo, UUID placedByUserId, UUID releasedByUserId,
                       LienStatus status, LocalDateTime placedAt, LocalDateTime releasedAt) {
        this.lienId = lienId;
        this.accountNo = accountNo;
        this.lienAmount = lienAmount;
        this.reason = reason;
        this.referenceNo = referenceNo;
        this.placedByUserId = placedByUserId;
        this.releasedByUserId = releasedByUserId;
        this.status = status != null ? status : LienStatus.ACTIVE;
        this.placedAt = placedAt != null ? placedAt : LocalDateTime.now();
        this.releasedAt = releasedAt;
    }

    public void release(UUID releasingUserId) {
        if (releasingUserId == null) {
            throw new IllegalArgumentException("Releasing User ID is required to release an active lien hold.");
        }
        this.status = LienStatus.RELEASED;
        this.releasedByUserId = releasingUserId;
        this.releasedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getLienId() { return lienId; }
    public void setLienId(UUID lienId) { this.lienId = lienId; }

    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }

    public BigDecimal getLienAmount() { return lienAmount; }
    public void setLienAmount(BigDecimal lienAmount) { this.lienAmount = lienAmount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }

    public UUID getPlacedByUserId() { return placedByUserId; }
    public void setPlacedByUserId(UUID placedByUserId) { this.placedByUserId = placedByUserId; }

    public UUID getReleasedByUserId() { return releasedByUserId; }
    public void setReleasedByUserId(UUID releasedByUserId) { this.releasedByUserId = releasedByUserId; }

    public LienStatus getStatus() { return status; }
    public void setStatus(LienStatus status) { this.status = status; }

    public LocalDateTime getPlacedAt() { return placedAt; }
    public void setPlacedAt(LocalDateTime placedAt) { this.placedAt = placedAt; }

    public LocalDateTime getReleasedAt() { return releasedAt; }
    public void setReleasedAt(LocalDateTime releasedAt) { this.releasedAt = releasedAt; }
}
