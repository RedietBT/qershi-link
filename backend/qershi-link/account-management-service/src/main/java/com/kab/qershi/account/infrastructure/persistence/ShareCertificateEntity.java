package com.kab.qershi.account.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA Entity mapping the share_certificates table.
 * Tracks official serialized share certificates held by members.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "share_certificates")
public class ShareCertificateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "share_account_id", nullable = false)
    private UUID shareAccountId;

    @Column(name = "certificate_number", nullable = false, unique = true, length = 50)
    private String certificateNumber;

    @Column(name = "start_serial", nullable = false)
    private Long startSerial;

    @Column(name = "end_serial", nullable = false)
    private Long endSerial;

    @Column(name = "share_count", nullable = false)
    private Integer shareCount;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate = LocalDate.now();

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE"; // ACTIVE, TRANSFERRED, SURRENDERED

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public ShareCertificateEntity() {}

    public ShareCertificateEntity(UUID shareAccountId, String certificateNumber, Long startSerial, Long endSerial, Integer shareCount, LocalDate issueDate) {
        this.shareAccountId = shareAccountId;
        this.certificateNumber = certificateNumber;
        this.startSerial = startSerial;
        this.endSerial = endSerial;
        this.shareCount = shareCount;
        this.issueDate = issueDate != null ? issueDate : LocalDate.now();
        this.status = "ACTIVE";
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getShareAccountId() { return shareAccountId; }
    public void setShareAccountId(UUID shareAccountId) { this.shareAccountId = shareAccountId; }

    public String getCertificateNumber() { return certificateNumber; }
    public void setCertificateNumber(String certificateNumber) { this.certificateNumber = certificateNumber; }

    public Long getStartSerial() { return startSerial; }
    public void setStartSerial(Long startSerial) { this.startSerial = startSerial; }

    public Long getEndSerial() { return endSerial; }
    public void setEndSerial(Long endSerial) { this.endSerial = endSerial; }

    public Integer getShareCount() { return shareCount; }
    public void setShareCount(Integer shareCount) { this.shareCount = shareCount; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
