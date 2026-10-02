package com.kab.qershi.auth.domain.ports.inbound;

import com.kab.qershi.auth.domain.model.Permission;
import com.kab.qershi.auth.domain.model.Role;

import java.util.List;
import java.util.UUID;

public interface RbacManagementUseCase {

    record CreateRoleCommand(
            String roleName,
            List<UUID> permissionIds
    ) {}

    record RoleResult(
            UUID roleId,
            String roleName,
            int assignedPermissionsCount,
            boolean isSystemDefined
    ) {}

    List<Permission> getAllPermissions();

    List<Role> getAllRoles(boolean isSuperAdmin);

    Role getRoleById(UUID roleId, boolean isSuperAdmin);

    RoleResult createLocalRole(CreateRoleCommand command);

    Role updateRole(UUID roleId, String roleName, List<UUID> permissionIds, boolean isSuperAdmin);

    void deleteRole(UUID roleId);
}