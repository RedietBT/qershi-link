package com.kab.qershi.auth.infrastructure.adapters;

import com.kab.qershi.auth.domain.model.Permission;
import com.kab.qershi.auth.domain.model.Role;
import com.kab.qershi.auth.domain.ports.outbound.RoleRepositoryPort;
import com.kab.qershi.auth.infrastructure.persistence.*;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RoleRepositoryAdapter implements RoleRepositoryPort {

    private final SpringDataRoleRepository roleRepository;
    private final SpringDataPermissionRepository permissionRepository;

    public RoleRepositoryAdapter(SpringDataRoleRepository roleRepository,
                                 SpringDataPermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public Optional<Role> findById(UUID roleId) {
        return roleRepository.findById(roleId).map(entity -> {
            Role role = new Role(entity.getRoleId(), entity.getRoleName(), entity.isSystemDefined());
            // Map JPA Permissions (RoleEntity) to Domain Permissions
            if (entity.getPermissions() != null) {
                entity.getPermissions().forEach(p -> role.grantPermission(new Permission(
                        p.getPermissionId(), p.getResource(), p.getAction(), p.getDescription(), p.isActive()
                )));
            }
            return role;
        });
    }

    @Override
    public Optional<Permission> findPermissionById(UUID permissionId) {
        return permissionRepository.findById(permissionId).map(p ->
                new Permission(p.getPermissionId(), p.getResource(), p.getAction(), p.getDescription(), p.isActive())
        );
    }

    @Override
    public void save(Role role) {
        // 1. Fetch existing entity or create a new one
        RoleEntity entity = roleRepository.findById(role.getRoleId())
                .orElse(new RoleEntity());

        // 2. Map basic fields
        entity.setRoleId(role.getRoleId());
        entity.setRoleName(role.getRoleName());
        entity.setSystemDefined(role.isSystemDefined());

        // 3. Map Domain Permissions back to Persistence Entities
        // We fetch the PermissionEntity objects based on the IDs present in the Domain Role
        Set<PermissionEntity> permissionEntities = role.getPermissions().stream()
                .map(p -> permissionRepository.findById(p.getPermissionId())
                        .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + p.getPermissionId())))
                .collect(Collectors.toSet());

        entity.setPermissions(permissionEntities);

        // 4. Save to database
        roleRepository.save(entity);
    }
}