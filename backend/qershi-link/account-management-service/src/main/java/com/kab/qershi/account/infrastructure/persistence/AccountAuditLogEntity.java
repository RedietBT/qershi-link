package com.kab.qershi.account.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA Entity mapping sacco_xxx.account_audit_logs table.
 * Records financial and state changes for member savings accounts for SACCO audit compliance.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "account_audit_logs")
public class AccountAuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "log_id", nullable = false, updatable = false)
    private UUID logId;

    @Column(name = "account_no", length = 50)
    private String accountNo;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "performed_by_user_id", nullable = false)
    private UUID performedByUserId;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "field_name", length = 100)
    private String fieldName;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public AccountAuditLogEntity() {}

    public AccountAuditLogEntity(UUID logId, String accountNo, UUID userId, UUID performedByUserId,
                                String action, String fieldName, String oldValue, String newValue,
                                OffsetDateTime createdAt) {
        this.logId = logId;
        this.accountNo = accountNo;
        this.userId = userId;
        this.performedByUserId = performedByUserId;
        this.action = action;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.createdAt = createdAt;
    }

    public UUID getLogId() {
        return logId;
    }

    public void setLogId(UUID logId) {
        this.logId = logId;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getPerformedByUserId() {
        return performedByUserId;
    }

    public void setPerformedByUserId(UUID performedByUserId) {
        this.performedByUserId = performedByUserId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
