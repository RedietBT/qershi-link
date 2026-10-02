package com.kab.qershi.auth.application.usecase;

import com.kab.qershi.auth.domain.model.Permission;
import com.kab.qershi.auth.domain.model.Role;
import com.kab.qershi.auth.domain.ports.inbound.RbacManagementUseCase;
import com.kab.qershi.auth.domain.ports.outbound.RoleRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service for managing RBAC role definitions and permission mappings.
 * Pure application service with zero infrastructure imports.
 *
 * @author KAB Digital Solution PLC
 * @version 2.0.0
 */
@Service
public class RbacManagementService implements RbacManagementUseCase {

    private final RoleRepositoryPort roleRepositoryPort;

    public RbacManagementService(RoleRepositoryPort roleRepositoryPort) {
        this.roleRepositoryPort = roleRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        return roleRepositoryPort.findAllPermissions();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllRoles(boolean isSuperAdmin) {
        List<Role> allRoles = roleRepositoryPort.findAll();
        if (isSuperAdmin) {
            return allRoles.stream()
                    .filter(Role::isSystemDefined)
                    .collect(Collectors.toList());
        } else {
            return allRoles.stream()
                    .filter(role -> !role.getRoleName().equalsIgnoreCase("SUPER_ADMIN"))
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Role getRoleById(UUID roleId, boolean isSuperAdmin) {
        Role role = roleRepositoryPort.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));

        if (!isSuperAdmin && role.getRoleName().equalsIgnoreCase("SUPER_ADMIN")) {
            throw new SecurityException("Forbidden attempt by SACCO admin to view SUPER_ADMIN role details.");
        }

        return role;
    }

    @Override
    @Transactional
    public RoleResult createLocalRole(CreateRoleCommand command) {
        Role newRole = new Role(null, command.roleName(), false);

        command.permissionIds().forEach(id -> {
            Permission permission = roleRepositoryPort.findPermissionById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + id));
            newRole.grantPermission(permission);
        });

        roleRepositoryPort.save(newRole);

        return new RoleResult(newRole.getRoleId(), newRole.getRoleName(),
                newRole.getPermissions().size(), newRole.isSystemDefined());
    }

    @Override
    @Transactional
    public Role updateRole(UUID roleId, String roleName, List<UUID> permissionIds, boolean isSuperAdmin) {
        Role role = roleRepositoryPort.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));

        if (role.isSystemDefined()) {
            throw new IllegalStateException("System-defined roles cannot be modified.");
        }

        if (!isSuperAdmin) {
            if (roleName != null && roleName.toUpperCase().contains("SUPER_ADMIN")) {
                throw new SecurityException("SACCO tenant administrators are not permitted to rename roles to SUPER_ADMIN.");
            }
        }

        Role updatedRole = new Role(roleId, roleName, false);
        for (UUID pId : permissionIds) {
            Permission p = roleRepositoryPort.findPermissionById(pId)
                    .orElseThrow(() -> new IllegalArgumentException("Permission not found with ID: " + pId));
            updatedRole.grantPermission(p);
        }

        roleRepositoryPort.save(updatedRole);
        return updatedRole;
    }

    @Override
    @Transactional
    public void deleteRole(UUID roleId) {
        Role role = roleRepositoryPort.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));

        if (role.isSystemDefined()) {
            throw new IllegalStateException("System-defined roles cannot be deleted.");
        }

        long userCount = roleRepositoryPort.countUsersAssignedToRole(roleId);
        if (userCount > 0) {
            throw new IllegalStateException("Cannot delete role '" + role.getRoleName() + "' because it is currently assigned to " + userCount + " user(s). Unassign the role from all users before deleting.");
        }

        roleRepositoryPort.delete(roleId);
    }
}