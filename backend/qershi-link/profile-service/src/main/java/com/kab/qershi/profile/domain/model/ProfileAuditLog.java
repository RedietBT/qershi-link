package com.kab.qershi.profile.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing an immutable audit log entry for regulatory PII tracking.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Getter
public class ProfileAuditLog {

    private final UUID logId;
    private final UUID userId;
    private final UUID modifiedByUserId;
    private final String action;
    private final String fieldChanged;
    private final String oldValue;
    private final String newValue;
    private final Instant timestamp;

    public ProfileAuditLog(UUID logId, UUID userId, UUID modifiedByUserId, String action,
                           String fieldChanged, String oldValue, String newValue) {
        this.logId = logId != null ? logId : UUID.randomUUID();
        this.userId = userId;
        this.modifiedByUserId = modifiedByUserId;
        this.action = action;
        this.fieldChanged = fieldChanged;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.timestamp = Instant.now();
    }
}
