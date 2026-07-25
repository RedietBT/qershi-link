package com.kab.qershi.auth.application.usecase;

import com.kab.qershi.auth.domain.model.Role;
import com.kab.qershi.auth.domain.ports.inbound.RbacManagementUseCase;
import com.kab.qershi.auth.domain.ports.outbound.RoleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RbacManagementService implements RbacManagementUseCase {

    private final RoleRepositoryPort roleRepositoryPort;

    @Override
    @Transactional
    public RoleResult createLocalRole(CreateRoleCommand command) {
        // 1. Create the new role
        Role newRole = new Role(null, command.roleName(), false);

        // 2. Fetch permissions by ID and grant them
        command.permissionIds().forEach(id -> {
            var permission = roleRepositoryPort.findPermissionById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + id));
            newRole.grantPermission(permission);
        });

        // 3. Persist
        roleRepositoryPort.save(newRole);

        return new RoleResult(newRole.getRoleId(), newRole.getRoleName(),
                newRole.getPermissions().size(), newRole.isSystemDefined());
    }
}