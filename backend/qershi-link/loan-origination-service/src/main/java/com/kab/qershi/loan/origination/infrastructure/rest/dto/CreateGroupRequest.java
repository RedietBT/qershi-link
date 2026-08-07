package com.kab.qershi.loan.origination.infrastructure.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

/**
 * REST request DTO for creating a new borrowing group.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record CreateGroupRequest(
        @NotBlank(message = "Group name is required")
        String groupName,

        boolean isFormal,

        String licenseNo,

        @NotEmpty(message = "Borrowing group must contain at least 2 member user IDs")
        List<UUID> memberUserIds,

        UUID leaderUserId
) {}
