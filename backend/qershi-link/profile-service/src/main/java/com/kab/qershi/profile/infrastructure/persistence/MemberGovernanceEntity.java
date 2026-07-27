package com.kab.qershi.profile.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@AllArgsConstructor
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
}
