package com.kab.qershi.loan.origination.infrastructure.persistence.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite Primary Key class for LoanGroupMemberEntity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class LoanGroupMemberId implements Serializable {

    private UUID groupId;
    private UUID userId;

    public LoanGroupMemberId() {}

    public LoanGroupMemberId(UUID groupId, UUID userId) {
        this.groupId = groupId;
        this.userId = userId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoanGroupMemberId that = (LoanGroupMemberId) o;
        return Objects.equals(groupId, that.groupId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, userId);
    }
}
