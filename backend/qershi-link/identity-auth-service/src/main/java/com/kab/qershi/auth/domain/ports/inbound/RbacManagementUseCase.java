package com.kab.qershi.auth.domain.ports.inbound;

import java.util.List;
import java.util.UUID;

public interface RbacManagementUseCase {

    record CreateRoleCommand(
            String roleName,
            List<UUID> permissionIds // Refactored: Pass explicit UUIDs of rows in the permissions table
    ) {}

    record RoleResult(
            UUID roleId,
            String roleName,
            int assignedPermissionsCount,
            boolean isSystemDefined
    ) {}

    RoleResult createLocalRole(CreateRoleCommand command);
}