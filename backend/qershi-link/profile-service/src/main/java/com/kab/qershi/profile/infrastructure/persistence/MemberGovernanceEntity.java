package com.kab.qershi.profile.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity mapping for member_governance database table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "member_governance")
@Getter
@Setter
public class MemberGovernanceEntity {

    @Id
    @Column(name = "governance_id", nullable = false, updatable = false)
    private UUID governanceId;

    @Column(name = "user_id", nullable = false, unique = true, updatable = false)
    private UUID userId;

    @Column(name = "submitted_by_user_id", nullable = false, updatable = false)
    private UUID submittedByUserId;

    @Column(name = "approved_by_user_id")
    private UUID approvedByUserId;

    @Column(name = "approval_date")
    private Instant approvalDate;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    public MemberGovernanceEntity() {}

    public MemberGovernanceEntity(UUID governanceId, UUID userId, UUID submittedByUserId,
                                  UUID approvedByUserId, Instant approvalDate, String remarks) {
        this.governanceId = governanceId;
        this.userId = userId;
        this.submittedByUserId = submittedByUserId;
        this.approvedByUserId = approvedByUserId;
        this.approvalDate = approvalDate;
        this.remarks = remarks;
    }

    public UUID getGovernanceId() { return governanceId; }
    public void setGovernanceId(UUID governanceId) { this.governanceId = governanceId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getSubmittedByUserId() { return submittedByUserId; }
    public void setSubmittedByUserId(UUID submittedByUserId) { this.submittedByUserId = submittedByUserId; }

    public UUID getApprovedByUserId() { return approvedByUserId; }
    public void setApprovedByUserId(UUID approvedByUserId) { this.approvedByUserId = approvedByUserId; }

    public Instant getApprovalDate() { return approvalDate; }
    public void setApprovalDate(Instant approvalDate) { this.approvalDate = approvalDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
