package com.kab.qershi.loan.origination.domain.ports.inbound;

import com.kab.qershi.loan.origination.domain.model.LoanGroup;

import java.util.List;
import java.util.UUID;

/**
 * Inbound port for managing SACCO borrowing groups and roster assignments.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface GroupManagementUseCase {

    record CreateGroupCommand(
            String groupName,
            boolean isFormal,
            String licenseNo,
            List<UUID> memberUserIds,
            UUID leaderUserId
    ) {}

    LoanGroup createGroup(CreateGroupCommand command);

    LoanGroup getGroupById(UUID groupId);

    List<LoanGroup> listGroups();
}
