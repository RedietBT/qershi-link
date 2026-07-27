package com.kab.qershi.profile.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing Four-Eye Principle (Maker-Checker) onboarding governance and approval audit records.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Getter
@Setter
public class MemberGovernance {

    private final UUID governanceId;
    private final UUID userId;
    private final UUID submittedByUserId;
    private UUID approvedByUserId;
    private Instant approvalDate;
    private String remarks;

    public MemberGovernance(UUID governanceId, UUID userId, UUID submittedByUserId,
                            UUID approvedByUserId, Instant approvalDate, String remarks) {
        this.governanceId = governanceId != null ? governanceId : UUID.randomUUID();
        this.userId = userId;
        this.submittedByUserId = submittedByUserId;
        this.approvedByUserId = approvedByUserId;
        this.approvalDate = approvalDate;
        this.remarks = remarks;
    }

    public UUID getGovernanceId() { return governanceId; }
    public UUID getUserId() { return userId; }
    public UUID getSubmittedByUserId() { return submittedByUserId; }

    public UUID getApprovedByUserId() { return approvedByUserId; }
    public void setApprovedByUserId(UUID approvedByUserId) { this.approvedByUserId = approvedByUserId; }

    public Instant getApprovalDate() { return approvalDate; }
    public void setApprovalDate(Instant approvalDate) { this.approvalDate = approvalDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public void approve(UUID supervisorId, String remarks) {
        this.approvedByUserId = supervisorId;
        this.approvalDate = Instant.now();
        this.remarks = remarks;
    }
}
