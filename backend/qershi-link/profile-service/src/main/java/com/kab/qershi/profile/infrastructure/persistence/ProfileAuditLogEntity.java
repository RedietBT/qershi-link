package com.kab.qershi.profile.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity mapping for profile_audit_logs database table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "profile_audit_logs")
@Getter
@Setter
public class ProfileAuditLogEntity {

    @Id
    @Column(name = "log_id", nullable = false, updatable = false)
    private UUID logId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "modified_by_user_id", nullable = false, updatable = false)
    private UUID modifiedByUserId;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "field_changed", length = 100)
    private String fieldChanged;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;

    public ProfileAuditLogEntity() {}

    public ProfileAuditLogEntity(UUID logId, UUID userId, UUID modifiedByUserId, String action,
                                String fieldChanged, String oldValue, String newValue, Instant timestamp) {
        this.logId = logId;
        this.userId = userId;
        this.modifiedByUserId = modifiedByUserId;
        this.action = action;
        this.fieldChanged = fieldChanged;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.timestamp = timestamp;
    }

    public UUID getLogId() { return logId; }
    public void setLogId(UUID logId) { this.logId = logId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getModifiedByUserId() { return modifiedByUserId; }
    public void setModifiedByUserId(UUID modifiedByUserId) { this.modifiedByUserId = modifiedByUserId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getFieldChanged() { return fieldChanged; }
    public void setFieldChanged(String fieldChanged) { this.fieldChanged = fieldChanged; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
