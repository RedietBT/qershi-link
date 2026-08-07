package com.kab.qershi.loan.origination.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing a member belonging to a borrowing group.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class GroupMember {

    private final UUID groupId;
    private final UUID userId;
    private final boolean isLeader;
    private final Instant joinedAt;

    public GroupMember(UUID groupId, UUID userId, boolean isLeader, Instant joinedAt) {
        this.groupId = groupId;
        this.userId = userId;
        this.isLeader = isLeader;
        this.joinedAt = joinedAt != null ? joinedAt : Instant.now();
    }

    public UUID getGroupId() {
        return groupId;
    }

    public UUID getUserId() {
        return userId;
    }

    public boolean isLeader() {
        return isLeader;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}
