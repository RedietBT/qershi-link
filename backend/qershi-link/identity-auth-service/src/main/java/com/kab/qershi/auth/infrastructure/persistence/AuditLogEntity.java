package com.kab.qershi.auth.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA Entity mapping master_schema.audit_logs table.
 * Stores global platform security and administrative event records for SUPER_ADMIN compliance monitoring.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "audit_logs", schema = "master_schema")
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "log_id", nullable = false, updatable = false)
    private UUID logId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "sacco_id")
    private UUID saccoId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "resource_affected", length = 100)
    private String resourceAffected;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private OffsetDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = OffsetDateTime.now();
        }
        if (status == null) {
            status = "SUCCESS";
        }
    }

    public AuditLogEntity() {}

    public AuditLogEntity(UUID logId, UUID userId, UUID saccoId, String action, String resourceAffected,
                          String status, String ipAddress, String details, OffsetDateTime timestamp) {
        this.logId = logId;
        this.userId = userId;
        this.saccoId = saccoId;
        this.action = action;
        this.resourceAffected = resourceAffected;
        this.status = status;
        this.ipAddress = ipAddress;
        this.details = details;
        this.timestamp = timestamp;
    }

    public UUID getLogId() {
        return logId;
    }

    public void setLogId(UUID logId) {
        this.logId = logId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getSaccoId() {
        return saccoId;
    }

    public void setSaccoId(UUID saccoId) {
        this.saccoId = saccoId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResourceAffected() {
        return resourceAffected;
    }

    public void setResourceAffected(String resourceAffected) {
        this.resourceAffected = resourceAffected;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
