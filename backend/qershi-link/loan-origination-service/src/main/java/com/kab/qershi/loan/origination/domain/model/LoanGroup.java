package com.kab.qershi.loan.origination.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Domain entity representing a SACCO borrowing group.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class LoanGroup {

    private final UUID groupId;
    private final String groupName;
    private final boolean isFormal;
    private final String licenseNo;
    private final List<GroupMember> members;
    private final Instant createdAt;
    private final Instant updatedAt;

    public LoanGroup(UUID groupId, String groupName, boolean isFormal, String licenseNo,
                     List<GroupMember> members, Instant createdAt, Instant updatedAt) {
        this.groupId = groupId != null ? groupId : UUID.randomUUID();
        this.groupName = groupName;
        this.isFormal = isFormal;
        this.licenseNo = licenseNo;
        this.members = members != null ? new ArrayList<>(members) : new ArrayList<>();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public UUID getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public boolean isFormal() {
        return isFormal;
    }

    public String getLicenseNo() {
        return licenseNo;
    }

    public List<GroupMember> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
