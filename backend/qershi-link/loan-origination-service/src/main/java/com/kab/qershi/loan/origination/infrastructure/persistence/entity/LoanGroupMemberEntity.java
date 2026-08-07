package com.kab.qershi.loan.origination.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapping loan_group_members table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "loan_group_members")
@IdClass(LoanGroupMemberId.class)
public class LoanGroupMemberEntity {

    @Id
    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "is_leader", nullable = false)
    private boolean leader = false;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt = Instant.now();

    public LoanGroupMemberEntity() {}

    public LoanGroupMemberEntity(UUID groupId, UUID userId, boolean leader, Instant joinedAt) {
        this.groupId = groupId;
        this.userId = userId;
        this.leader = leader;
        this.joinedAt = joinedAt != null ? joinedAt : Instant.now();
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public boolean isLeader() {
        return leader;
    }

    public void setLeader(boolean leader) {
        this.leader = leader;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }
}
