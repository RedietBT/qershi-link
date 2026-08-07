package com.kab.qershi.loan.origination.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapping approval_workflow_logs table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "approval_workflow_logs")
public class ApprovalWorkflowLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "log_id")
    private UUID logId;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "action_by", nullable = false)
    private UUID actionBy;

    @Column(name = "action_type", nullable = false, length = 30)
    private String actionType;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "action_at", nullable = false, updatable = false)
    private Instant actionAt = Instant.now();

    public ApprovalWorkflowLogEntity() {}

    public ApprovalWorkflowLogEntity(UUID logId, UUID applicationId, UUID actionBy,
                                    String actionType, String remarks, Instant actionAt) {
        this.logId = logId;
        this.applicationId = applicationId;
        this.actionBy = actionBy;
        this.actionType = actionType;
        this.remarks = remarks;
        this.actionAt = actionAt != null ? actionAt : Instant.now();
    }

    public UUID getLogId() {
        return logId;
    }

    public void setLogId(UUID logId) {
        this.logId = logId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public UUID getActionBy() {
        return actionBy;
    }

    public void setActionBy(UUID actionBy) {
        this.actionBy = actionBy;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Instant getActionAt() {
        return actionAt;
    }

    public void setActionAt(Instant actionAt) {
        this.actionAt = actionAt;
    }
}
