package com.kab.qershi.account.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Pure Domain Model representing an audited peer-to-peer share transfer.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class ShareTransfer {

    private UUID id;
    private UUID fromMemberId;
    private UUID toMemberId;
    private UUID certificateId;
    private int shareCount;
    private BigDecimal transferPrice;
    private String status; // PENDING_APPROVAL, APPROVED, REJECTED
    private UUID approvedBy;
    private OffsetDateTime approvedAt;
    private String rejectionReason;
    private OffsetDateTime createdAt;

    public ShareTransfer(UUID id, UUID fromMemberId, UUID toMemberId, UUID certificateId,
                         int shareCount, BigDecimal transferPrice, String status,
                         UUID approvedBy, OffsetDateTime approvedAt, String rejectionReason,
                         OffsetDateTime createdAt) {
        if (fromMemberId.equals(toMemberId)) {
            throw new IllegalArgumentException("Cannot transfer shares to self.");
        }
        if (shareCount <= 0) {
            throw new IllegalArgumentException("shareCount must be greater than zero.");
        }
        this.id = id != null ? id : UUID.randomUUID();
        this.fromMemberId = fromMemberId;
        this.toMemberId = toMemberId;
        this.certificateId = certificateId;
        this.shareCount = shareCount;
        this.transferPrice = transferPrice;
        this.status = status != null ? status : "PENDING_APPROVAL";
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
    }

    public void approve(UUID approverId) {
        this.status = "APPROVED";
        this.approvedBy = approverId;
        this.approvedAt = OffsetDateTime.now();
    }

    public void reject(UUID approverId, String reason) {
        this.status = "REJECTED";
        this.approvedBy = approverId;
        this.approvedAt = OffsetDateTime.now();
        this.rejectionReason = reason;
    }

    public UUID getId() { return id; }
    public UUID getFromMemberId() { return fromMemberId; }
    public UUID getToMemberId() { return toMemberId; }
    public UUID getCertificateId() { return certificateId; }
    public int getShareCount() { return shareCount; }
    public BigDecimal getTransferPrice() { return transferPrice; }
    public String getStatus() { return status; }
    public UUID getApprovedBy() { return approvedBy; }
    public OffsetDateTime getApprovedAt() { return approvedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
