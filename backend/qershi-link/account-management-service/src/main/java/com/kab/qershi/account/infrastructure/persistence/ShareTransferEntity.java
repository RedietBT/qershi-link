package com.kab.qershi.account.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA Entity mapping the share_transfers table.
 * Audits peer-to-peer share transfers between members with Maker-Checker approval.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "share_transfers")
public class ShareTransferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "from_member_id", nullable = false)
    private UUID fromMemberId;

    @Column(name = "to_member_id", nullable = false)
    private UUID toMemberId;

    @Column(name = "certificate_id", nullable = false)
    private UUID certificateId;

    @Column(name = "share_count", nullable = false)
    private Integer shareCount;

    @Column(name = "transfer_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal transferPrice;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING_APPROVAL"; // PENDING_APPROVAL, APPROVED, REJECTED

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public ShareTransferEntity() {}

    public ShareTransferEntity(UUID fromMemberId, UUID toMemberId, UUID certificateId, Integer shareCount, BigDecimal transferPrice) {
        this.fromMemberId = fromMemberId;
        this.toMemberId = toMemberId;
        this.certificateId = certificateId;
        this.shareCount = shareCount;
        this.transferPrice = transferPrice;
        this.status = "PENDING_APPROVAL";
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getFromMemberId() { return fromMemberId; }
    public void setFromMemberId(UUID fromMemberId) { this.fromMemberId = fromMemberId; }

    public UUID getToMemberId() { return toMemberId; }
    public void setToMemberId(UUID toMemberId) { this.toMemberId = toMemberId; }

    public UUID getCertificateId() { return certificateId; }
    public void setCertificateId(UUID certificateId) { this.certificateId = certificateId; }

    public Integer getShareCount() { return shareCount; }
    public void setShareCount(Integer shareCount) { this.shareCount = shareCount; }

    public BigDecimal getTransferPrice() { return transferPrice; }
    public void setTransferPrice(BigDecimal transferPrice) { this.transferPrice = transferPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public UUID getApprovedBy() { return approvedBy; }
    public void setApprovedBy(UUID approvedBy) { this.approvedBy = approvedBy; }

    public OffsetDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(OffsetDateTime approvedAt) { this.approvedAt = approvedAt; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
