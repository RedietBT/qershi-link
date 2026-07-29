package com.kab.qershi.auth.infrastructure.adapters;

import com.kab.qershi.auth.domain.model.Permission;
import com.kab.qershi.auth.domain.model.Role;
import com.kab.qershi.auth.domain.ports.outbound.RoleRepositoryPort;
import com.kab.qershi.auth.infrastructure.persistence.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
        if (roleId == null) return Optional.empty();
        return roleRepository.findById(roleId).map(entity -> {
            Role role = new Role(entity.getRoleId(), entity.getRoleName(), entity.isSystemDefined());
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
        if (permissionId == null) return Optional.empty();
        return permissionRepository.findById(permissionId).map(p ->
                new Permission(p.getPermissionId(), p.getResource(), p.getAction(), p.getDescription(), p.isActive())
        );
    }

    @Override
    @Transactional
    public void save(Role role) {
        RoleEntity entity = roleRepository.findById(role.getRoleId())
                .orElseGet(() -> {
                    RoleEntity newEntity = new RoleEntity();
                    newEntity.setRoleId(role.getRoleId());
                    newEntity.setCreatedAt(Instant.now());
                    return newEntity;
                });

        entity.setRoleName(role.getRoleName());
        entity.setSystemDefined(role.isSystemDefined());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
        }

        Set<PermissionEntity> permissionEntities = role.getPermissions().stream()
                .map(p -> permissionRepository.findById(p.getPermissionId())
                        .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + p.getPermissionId())))
                .collect(Collectors.toSet());

        entity.setPermissions(permissionEntities);

        roleRepository.save(entity);
    }
}