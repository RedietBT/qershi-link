package com.kab.qershi.transaction.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Pure domain model representing an immutable financial audit log entry.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class TransactionAuditLog {

    private UUID logId;
    private String transactionRef;
    private String accountNo;
    private UUID performedByUserId;
    private String action;
    private String details;
    private OffsetDateTime createdAt;

    public TransactionAuditLog() {}

    public TransactionAuditLog(UUID logId, String transactionRef, String accountNo,
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
