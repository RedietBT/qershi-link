package com.kab.qershi.loan.origination.infrastructure.rest.dto;

import com.kab.qershi.loan.origination.domain.model.GroupMember;
import com.kab.qershi.loan.origination.domain.model.LoanGroup;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * REST response DTO for borrowing groups.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record GroupResponse(
        UUID groupId,
        String groupName,
        boolean isFormal,
        String licenseNo,
        List<GroupMember> members,
        Instant createdAt,
        Instant updatedAt
) {
    public static GroupResponse fromDomain(LoanGroup domain) {
        return new GroupResponse(
                domain.getGroupId(),
                domain.getGroupName(),
                domain.isFormal(),
                domain.getLicenseNo(),
                domain.getMembers(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
