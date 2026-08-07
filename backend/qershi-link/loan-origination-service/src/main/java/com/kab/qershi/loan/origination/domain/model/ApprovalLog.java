package com.kab.qershi.loan.origination.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing Maker-Checker decision audit trail log.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class ApprovalLog {

    private final UUID logId;
    private final UUID applicationId;
    private final UUID actionBy;
    private final WorkflowAction actionType;
    private final String remarks;
    private final Instant actionAt;

    public ApprovalLog(UUID logId, UUID applicationId, UUID actionBy,
                       WorkflowAction actionType, String remarks, Instant actionAt) {
        this.logId = logId != null ? logId : UUID.randomUUID();
        this.applicationId = applicationId;
        this.actionBy = actionBy;
        this.actionType = actionType;
        this.remarks = remarks;
        this.actionAt = actionAt != null ? actionAt : Instant.now();
    }

    public UUID getLogId() {
        return logId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public UUID getActionBy() {
        return actionBy;
    }

    public WorkflowAction getActionType() {
        return actionType;
    }

    public String getRemarks() {
        return remarks;
    }

    public Instant getActionAt() {
        return actionAt;
    }
}
