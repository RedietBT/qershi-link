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
public class MemberGovernance {

    private final UUID governanceId;
    private final UUID userId;
    private final UUID submittedByUserId;
    @Setter private UUID approvedByUserId;
    @Setter private Instant approvalDate;
    @Setter private String remarks;

    public MemberGovernance(UUID governanceId, UUID userId, UUID submittedByUserId,
                            UUID approvedByUserId, Instant approvalDate, String remarks) {
        this.governanceId = governanceId != null ? governanceId : UUID.randomUUID();
        this.userId = userId;
        this.submittedByUserId = submittedByUserId;
        this.approvedByUserId = approvedByUserId;
        this.approvalDate = approvalDate;
        this.remarks = remarks;
    }

    public void approve(UUID supervisorId, String remarks) {
        this.approvedByUserId = supervisorId;
        this.approvalDate = Instant.now();
        this.remarks = remarks;
    }
}
