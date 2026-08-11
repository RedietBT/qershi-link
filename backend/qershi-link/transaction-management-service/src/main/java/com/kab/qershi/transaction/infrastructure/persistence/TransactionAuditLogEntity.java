package com.kab.qershi.transaction.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA Entity mapping sacco_xxx.transaction_audit_logs table.
 * Records financial cash transactions and member transfer audit events.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "transaction_audit_logs")
public class TransactionAuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "log_id", nullable = false, updatable = false)
    private UUID logId;

    @Column(name = "transaction_ref", length = 50)
    private String transactionRef;

    @Column(name = "account_no", length = 50)
    private String accountNo;

    @Column(name = "performed_by_user_id", nullable = false)
    private UUID performedByUserId;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public TransactionAuditLogEntity() {}

    public TransactionAuditLogEntity(UUID logId, String transactionRef, String accountNo,
                                      UUID performedByUserId, String action, String details,
                                      OffsetDateTime createdAt) {
        this.logId = logId;
        this.transactionRef = transactionRef;
        this.accountNo = accountNo;
        this.performedByUserId = performedByUserId;
        this.action = action;
        this.details = details;
        this.createdAt = createdAt;
    }

    public UUID getLogId() {
        return logId;
    }

    public void setLogId(UUID logId) {
        this.logId = logId;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
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

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
