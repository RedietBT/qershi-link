package com.kab.qershi.auth.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Pure domain model representing an administrative and security audit event.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class AuditLog {
    private final UUID logId;
    private final UUID userId;
    private final UUID saccoId;
    private final String action;
    private final String resourceAffected;
    private final String status;
    private final String ipAddress;
    private final String details;
    private final OffsetDateTime timestamp;
    private String userMsisdn; // Optional demographic context

    public AuditLog(UUID logId, UUID userId, UUID saccoId, String action, String resourceAffected,
                    String status, String ipAddress, String details, OffsetDateTime timestamp) {
        this.logId = logId;
        this.userId = userId;
        this.saccoId = saccoId;
        this.action = action;
        this.resourceAffected = resourceAffected;
        this.status = status != null ? status : "SUCCESS";
        this.ipAddress = ipAddress;
        this.details = details;
        this.timestamp = timestamp != null ? timestamp : OffsetDateTime.now();
    }

    public UUID getLogId() {
        return logId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getSaccoId() {
        return saccoId;
    }

    public String getAction() {
        return action;
    }

    public String getResourceAffected() {
        return resourceAffected;
    }

    public String getStatus() {
        return status;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getDetails() {
        return details;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public String getUserMsisdn() {
        return userMsisdn;
    }

    public void setUserMsisdn(String userMsisdn) {
        this.userMsisdn = userMsisdn;
    }
}
