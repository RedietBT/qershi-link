package com.kab.qershi.account.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity mapping eod_batch_executions table.
 * Records each End-of-Day batch processing run, summary counters, and completion status.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "eod_batch_executions")
public class EodBatchExecutionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "batch_id")
    private UUID batchId;

    @Column(name = "business_date", nullable = false)
    private LocalDate businessDate;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "status", nullable = false, length = 30)
    private String status; // 'IN_PROGRESS', 'COMPLETED', 'FAILED'

    @Column(name = "triggered_by", nullable = false, length = 50)
    private String triggeredBy; // 'SYSTEM_CRON', 'MANUAL_OVERRIDE'

    @Column(name = "triggered_by_user_id")
    private UUID triggeredByUserId;

    @Column(name = "total_accounts_accrued", nullable = false)
    private Integer totalAccountsAccrued = 0;

    @Column(name = "total_interest_accrued", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalInterestAccrued = BigDecimal.ZERO;

    @Column(name = "total_loans_evaluated", nullable = false)
    private Integer totalLoansEvaluated = 0;

    @Column(name = "total_accounts_dormant", nullable = false)
    private Integer totalAccountsDormant = 0;

    @Column(name = "summary_notes")
    private String summaryNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public EodBatchExecutionEntity() {}

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

    public String getSummaryNotes() { return summaryNotes; }
    public void setSummaryNotes(String summaryNotes) { this.summaryNotes = summaryNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
