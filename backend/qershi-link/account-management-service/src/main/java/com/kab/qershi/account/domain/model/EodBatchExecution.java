package com.kab.qershi.account.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Pure domain model representing an End-of-Day (EOD) Batch Pipeline Execution run.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class EodBatchExecution {

    private UUID batchId;
    private LocalDate businessDate;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String status; // 'IN_PROGRESS', 'COMPLETED', 'FAILED'
    private String triggeredBy; // 'SYSTEM_CRON', 'MANUAL_OVERRIDE'
    private UUID triggeredByUserId;
    private Integer totalAccountsAccrued = 0;
    private BigDecimal totalInterestAccrued = BigDecimal.ZERO;
    private Integer totalLoansEvaluated = 0;
    private Integer totalAccountsDormant = 0;
    private Integer totalLoansProvisioned = 0;
    private String summaryNotes;
    private LocalDateTime createdAt = LocalDateTime.now();

    public EodBatchExecution() {
        this.batchId = UUID.randomUUID();
        this.startedAt = LocalDateTime.now();
        this.status = "IN_PROGRESS";
        this.totalAccountsAccrued = 0;
        this.totalInterestAccrued = BigDecimal.ZERO;
        this.totalLoansEvaluated = 0;
        this.totalAccountsDormant = 0;
        this.totalLoansProvisioned = 0;
        this.createdAt = LocalDateTime.now();
    }

    public EodBatchExecution(UUID batchId, LocalDate businessDate, LocalDateTime startedAt,
                             LocalDateTime completedAt, String status, String triggeredBy,
                             UUID triggeredByUserId, Integer totalAccountsAccrued,
                             BigDecimal totalInterestAccrued, Integer totalLoansEvaluated,
                             Integer totalAccountsDormant, Integer totalLoansProvisioned,
                             String summaryNotes, LocalDateTime createdAt) {
        this.batchId = batchId != null ? batchId : UUID.randomUUID();
        this.businessDate = businessDate;
        this.startedAt = startedAt != null ? startedAt : LocalDateTime.now();
        this.completedAt = completedAt;
        this.status = status != null ? status : "IN_PROGRESS";
        this.triggeredBy = triggeredBy;
        this.triggeredByUserId = triggeredByUserId;
        this.totalAccountsAccrued = totalAccountsAccrued != null ? totalAccountsAccrued : 0;
        this.totalInterestAccrued = totalInterestAccrued != null ? totalInterestAccrued : BigDecimal.ZERO;
        this.totalLoansEvaluated = totalLoansEvaluated != null ? totalLoansEvaluated : 0;
        this.totalAccountsDormant = totalAccountsDormant != null ? totalAccountsDormant : 0;
        this.totalLoansProvisioned = totalLoansProvisioned != null ? totalLoansProvisioned : 0;
        this.summaryNotes = summaryNotes;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public void markCompleted(String summaryNotes) {
        this.status = "COMPLETED";
        this.completedAt = LocalDateTime.now();
        this.summaryNotes = summaryNotes;
    }

    public void markFailed(String errorMessage) {
        this.status = "FAILED";
        this.completedAt = LocalDateTime.now();
        this.summaryNotes = "Batch failed: " + errorMessage;
    }

    // Getters and Setters
    public UUID getBatchId() { return batchId; }
    public void setBatchId(UUID batchId) { this.batchId = batchId; }

    public LocalDate getBusinessDate() { return businessDate; }
    public void setBusinessDate(LocalDate businessDate) { this.businessDate = businessDate; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }

    public UUID getTriggeredByUserId() { return triggeredByUserId; }
    public void setTriggeredByUserId(UUID triggeredByUserId) { this.triggeredByUserId = triggeredByUserId; }

    public Integer getTotalAccountsAccrued() { return totalAccountsAccrued; }
    public void setTotalAccountsAccrued(Integer totalAccountsAccrued) { this.totalAccountsAccrued = totalAccountsAccrued; }

    public BigDecimal getTotalInterestAccrued() { return totalInterestAccrued; }
    public void setTotalInterestAccrued(BigDecimal totalInterestAccrued) { this.totalInterestAccrued = totalInterestAccrued; }

    public Integer getTotalLoansEvaluated() { return totalLoansEvaluated; }
    public void setTotalLoansEvaluated(Integer totalLoansEvaluated) { this.totalLoansEvaluated = totalLoansEvaluated; }

    public Integer getTotalAccountsDormant() { return totalAccountsDormant; }
    public void setTotalAccountsDormant(Integer totalAccountsDormant) { this.totalAccountsDormant = totalAccountsDormant; }

    public Integer getTotalLoansProvisioned() { return totalLoansProvisioned; }
    public void setTotalLoansProvisioned(Integer totalLoansProvisioned) { this.totalLoansProvisioned = totalLoansProvisioned; }

    public String getSummaryNotes() { return summaryNotes; }
    public void setSummaryNotes(String summaryNotes) { this.summaryNotes = summaryNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
