package com.kab.qershi.loan.origination.application.usecase;

import com.kab.qershi.loan.origination.domain.model.GroupMember;
import com.kab.qershi.loan.origination.domain.model.LoanGroup;
import com.kab.qershi.loan.origination.domain.ports.inbound.GroupManagementUseCase;
import com.kab.qershi.loan.origination.domain.ports.outbound.LoanGroupRepositoryPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Application service implementing GroupManagementUseCase.
 * Handles SACCO borrowing group onboarding, minimum member validation, and leader designation.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class GroupManagementService implements GroupManagementUseCase {

    private static final Logger log = LoggerFactory.getLogger(GroupManagementService.class);
    private final LoanGroupRepositoryPort groupRepositoryPort;

    public GroupManagementService(LoanGroupRepositoryPort groupRepositoryPort) {
        this.groupRepositoryPort = groupRepositoryPort;
    }

    @Override
    @Transactional
    public LoanGroup createGroup(CreateGroupCommand command) {
        if (command.groupName() == null || command.groupName().isBlank()) {
            throw new IllegalArgumentException("Borrowing group name cannot be empty.");
        }

        List<UUID> memberIds = command.memberUserIds() != null ? command.memberUserIds() : List.of();
        if (memberIds.size() < 2) {
            throw new IllegalArgumentException("A borrowing group must contain at least 2 SACCO members.");
        }

        if (command.isFormal()) {
            if (command.licenseNo() == null || command.licenseNo().isBlank()) {
                throw new IllegalArgumentException("Formal borrowing groups require a valid cooperative registration license number.");
            }
            if (groupRepositoryPort.existsByLicenseNo(command.licenseNo().trim())) {
                throw new IllegalStateException("A borrowing group with this license number is already registered.");
            }
        }

        UUID groupId = UUID.randomUUID();
        UUID leaderId = command.leaderUserId() != null ? command.leaderUserId() : memberIds.get(0);

        List<GroupMember> members = new ArrayList<>();
        for (UUID userId : memberIds) {
            boolean isLeader = userId.equals(leaderId);
            members.add(new GroupMember(groupId, userId, isLeader, Instant.now()));
        }

        LoanGroup group = new LoanGroup(
                groupId,
                command.groupName().trim(),
                command.isFormal(),
                command.isFormal() ? command.licenseNo().trim() : null,
                members,
                Instant.now(),
                Instant.now()
        );

        LoanGroup savedGroup = groupRepositoryPort.save(group);
        log.info("Successfully created SACCO borrowing group: '{}' (ID: {}) with {} members.",
                savedGroup.getGroupName(), savedGroup.getGroupId(), savedGroup.getMembers().size());

        return savedGroup;
    }

    @Override
    @Transactional(readOnly = true)
    public LoanGroup getGroupById(UUID groupId) {
        return groupRepositoryPort.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Loan group not found with ID: " + groupId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanGroup> listGroups() {
        return groupRepositoryPort.findAll();
    }
}
