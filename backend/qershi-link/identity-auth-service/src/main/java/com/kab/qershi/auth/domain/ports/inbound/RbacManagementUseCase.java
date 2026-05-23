package com.kab.qershi.auth.domain.ports.inbound;

import java.util.List;
import java.util.UUID;

public interface RbacManagementUseCase {

    record CreateRoleCommand(
            String roleName,
            List<String> permissions
    ) {}

    record RoleResult(
            UUID roleId,
            String roleName,
            int assignedPermissionsCount,
            boolean isSystemDefined
    ) {}

    RoleResult createLocalRole(CreateRoleCommand command);
}