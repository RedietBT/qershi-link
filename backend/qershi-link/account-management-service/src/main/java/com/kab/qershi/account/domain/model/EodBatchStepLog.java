package com.kab.qershi.account.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Pure domain model representing an execution step log in the EOD pipeline.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class EodBatchStepLog {

    private UUID stepId;
    private UUID batchId;
    private String stepName;
    private String status; // 'SUCCESS', 'FAILED', 'SKIPPED'
    private Long durationMs = 0L;
    private Integer recordsAffected = 0;
    private String errorMessage;
    private LocalDateTime createdAt = LocalDateTime.now();

    public EodBatchStepLog() {
        this.stepId = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
    }

    public EodBatchStepLog(UUID batchId, String stepName, String status, Long durationMs,
                           Integer recordsAffected, String errorMessage) {
        this.stepId = UUID.randomUUID();
        this.batchId = batchId;
        this.stepName = stepName;
        this.status = status;
        this.durationMs = durationMs != null ? durationMs : 0L;
        this.recordsAffected = recordsAffected != null ? recordsAffected : 0;
        this.errorMessage = errorMessage;
        this.createdAt = LocalDateTime.now();
    }

    public EodBatchStepLog(UUID stepId, UUID batchId, String stepName, String status, Long durationMs,
                           Integer recordsAffected, String errorMessage, LocalDateTime createdAt) {
        this.stepId = stepId != null ? stepId : UUID.randomUUID();
        this.batchId = batchId;
        this.stepName = stepName;
        this.status = status;
        this.durationMs = durationMs != null ? durationMs : 0L;
        this.recordsAffected = recordsAffected != null ? recordsAffected : 0;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
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
