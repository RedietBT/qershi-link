package com.kab.qershi.auth.domain.ports.outbound;

import com.kab.qershi.auth.domain.model.Permission;
import com.kab.qershi.auth.domain.model.Role;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepositoryPort {
    Optional<Role> findById(UUID roleId);
    Optional<Permission> findPermissionById(UUID permissionId);
    void save(Role role);
}