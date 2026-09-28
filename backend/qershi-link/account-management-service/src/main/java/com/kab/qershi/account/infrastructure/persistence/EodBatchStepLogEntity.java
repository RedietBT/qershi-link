package com.kab.qershi.account.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity mapping eod_batch_step_logs table.
 * Granular audit trail for each pipeline phase during EOD execution.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "eod_batch_step_logs")
public class EodBatchStepLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "step_id")
    private UUID stepId;

    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @Column(name = "step_name", nullable = false, length = 100)
    private String stepName;

    @Column(name = "status", nullable = false, length = 30)
    private String status; // 'SUCCESS', 'FAILED', 'SKIPPED'

    @Column(name = "duration_ms", nullable = false)
    private Long durationMs = 0L;

    @Column(name = "records_affected", nullable = false)
    private Integer recordsAffected = 0;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public EodBatchStepLogEntity() {}

    public EodBatchStepLogEntity(UUID batchId, String stepName, String status, Long durationMs,
                                 Integer recordsAffected, String errorMessage) {
        this.batchId = batchId;
        this.stepName = stepName;
        this.status = status;
        this.durationMs = durationMs;
        this.recordsAffected = recordsAffected;
        this.errorMessage = errorMessage;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getStepId() { return stepId; }
    public void setStepId(UUID stepId) { this.stepId = stepId; }

    public UUID getBatchId() { return batchId; }
    public void setBatchId(UUID batchId) { this.batchId = batchId; }

    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public Integer getRecordsAffected() { return recordsAffected; }
    public void setRecordsAffected(Integer recordsAffected) { this.recordsAffected = recordsAffected; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
