package com.kab.qershi.account.domain.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Pure Domain Model representing a legal serialized Share Certificate.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class ShareCertificate {

    private UUID id;
    private UUID shareAccountId;
    private String certificateNumber;
    private long startSerial;
    private long endSerial;
    private int shareCount;
    private LocalDate issueDate;
    private String status; // ACTIVE, TRANSFERRED, SURRENDERED
    private OffsetDateTime createdAt;

    public ShareCertificate(UUID id, UUID shareAccountId, String certificateNumber,
                            long startSerial, long endSerial, int shareCount,
                            LocalDate issueDate, String status, OffsetDateTime createdAt) {
        if (endSerial < startSerial) {
            throw new IllegalArgumentException("endSerial cannot be less than startSerial");
        }
        if (shareCount <= 0) {
            throw new IllegalArgumentException("shareCount must be greater than zero");
        }
        this.id = id != null ? id : UUID.randomUUID();
        this.shareAccountId = shareAccountId;
        this.certificateNumber = certificateNumber;
        this.startSerial = startSerial;
        this.endSerial = endSerial;
        this.shareCount = shareCount;
        this.issueDate = issueDate != null ? issueDate : LocalDate.now();
        this.status = status != null ? status : "ACTIVE";
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    public void markTransferred() {
        this.status = "TRANSFERRED";
    }

    public void reduceShares(int countToDeduct) {
        if (countToDeduct >= this.shareCount) {
            throw new IllegalArgumentException("Deduction would consume all shares. Use markTransferred() instead.");
        }
        this.shareCount -= countToDeduct;
    }

    public UUID getId() { return id; }
    public UUID getShareAccountId() { return shareAccountId; }
    public String getCertificateNumber() { return certificateNumber; }
    public long getStartSerial() { return startSerial; }
    public long getEndSerial() { return endSerial; }
    public int getShareCount() { return shareCount; }
    public LocalDate getIssueDate() { return issueDate; }
    public String getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
