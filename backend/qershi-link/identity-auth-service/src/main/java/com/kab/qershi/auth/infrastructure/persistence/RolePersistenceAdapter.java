package com.kab.qershi.auth.infrastructure.persistence;

import com.kab.qershi.auth.domain.model.Role;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Hexagonal Outbound Adapter translating isolated role configuration controls between domain structures and JPA contexts.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class RolePersistenceAdapter {

    private final SpringDataRoleRepository repository;

    public RolePersistenceAdapter(SpringDataRoleRepository repository) {
        this.repository = repository;
    }

    /**
     * Persists a role model directly inside the active schema context.
     */
    public void save(Role role) {
        RoleEntity entity = new RoleEntity();
        entity.setRoleId(role.getRoleId());
        entity.setRoleName(role.getRoleName());
        entity.setSystemDefined(role.isSystemDefined());
        entity.getPermissions().addAll(role.getPermissions());

        repository.save(entity);
    }

    /**
     * Resolves a role from the active schema context by its unique identifier.
     */
    public Optional<Role> findById(UUID roleId) {
        return repository.findById(roleId).map(entity -> {
            Role role = new Role(entity.getRoleId(), entity.getRoleName(), entity.isSystemDefined());
            entity.getPermissions().forEach(role::grantPermission);
            return role;
        });
    }
}