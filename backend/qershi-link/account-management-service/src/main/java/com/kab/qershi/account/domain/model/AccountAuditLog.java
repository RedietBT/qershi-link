package com.kab.qershi.account.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Pure domain model representing an audit log entry for account lifecycle events.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class AccountAuditLog {

    private UUID logId;
    private String accountNo;
    private UUID userId;
    private UUID performedByUserId;
    private String action;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private LocalDateTime createdAt;

    public AccountAuditLog() {
        this.createdAt = LocalDateTime.now();
    }

    public AccountAuditLog(UUID logId, String accountNo, UUID userId, UUID performedByUserId,
                           String action, String fieldName, String oldValue, String newValue,
                           LocalDateTime createdAt) {
        this.logId = logId;
        this.accountNo = accountNo;
        this.userId = userId;
        this.performedByUserId = performedByUserId;
        this.action = action;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getLogId() { return logId; }
    public void setLogId(UUID logId) { this.logId = logId; }

    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getPerformedByUserId() { return performedByUserId; }
    public void setPerformedByUserId(UUID performedByUserId) { this.performedByUserId = performedByUserId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
