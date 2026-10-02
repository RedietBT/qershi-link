package com.kab.qershi.transaction.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Pure domain model representing a historical till closure audit log entry.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class TillClosingLog {

    private UUID logId;
    private UUID reconciliationId;
    private UUID tillId;
    private UUID tellerUserId;
    private String closingMode;
    private BigDecimal electronicBalance;
    private BigDecimal physicalTotal;
    private BigDecimal variance;
    private String status;
    private UUID journalEntryId;
    private Instant createdAt;

    public TillClosingLog() {
        this.closingMode = "BLIND";
        this.variance = BigDecimal.ZERO;
        this.status = "BALANCED";
        this.createdAt = Instant.now();
    }

    public TillClosingLog(UUID logId, UUID reconciliationId, UUID tillId, UUID tellerUserId,
                          String closingMode, BigDecimal electronicBalance, BigDecimal physicalTotal,
                          BigDecimal variance, String status, UUID journalEntryId, Instant createdAt) {
        this.logId = logId;
        this.reconciliationId = reconciliationId;
        this.tillId = tillId;
        this.tellerUserId = tellerUserId;
        this.closingMode = closingMode != null ? closingMode : "BLIND";
        this.electronicBalance = electronicBalance;
        this.physicalTotal = physicalTotal;
        this.variance = variance != null ? variance : BigDecimal.ZERO;
        this.status = status != null ? status : "BALANCED";
        this.journalEntryId = journalEntryId;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public UUID getLogId() {
        return logId;
    }

    public void setLogId(UUID logId) {
        this.logId = logId;
    }

    public UUID getReconciliationId() {
        return reconciliationId;
    }

    public void setReconciliationId(UUID reconciliationId) {
        this.reconciliationId = reconciliationId;
    }

    public UUID getTillId() {
        return tillId;
    }

    public void setTillId(UUID tillId) {
        this.tillId = tillId;
    }

    public UUID getTellerUserId() {
        return tellerUserId;
    }

    public void setTellerUserId(UUID tellerUserId) {
        this.tellerUserId = tellerUserId;
    }

    public String getClosingMode() {
        return closingMode;
    }

    public void setClosingMode(String closingMode) {
        this.closingMode = closingMode;
    }

    public BigDecimal getElectronicBalance() {
        return electronicBalance;
    }

    public void setElectronicBalance(BigDecimal electronicBalance) {
        this.electronicBalance = electronicBalance;
    }

    public BigDecimal getPhysicalTotal() {
        return physicalTotal;
    }

    public void setPhysicalTotal(BigDecimal physicalTotal) {
        this.physicalTotal = physicalTotal;
    }

    public BigDecimal getVariance() {
        return variance;
    }

    public void setVariance(BigDecimal variance) {
        this.variance = variance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getJournalEntryId() {
        return journalEntryId;
    }

    public void setJournalEntryId(UUID journalEntryId) {
        this.journalEntryId = journalEntryId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
