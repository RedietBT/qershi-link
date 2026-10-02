package com.kab.qershi.auth.domain.ports.outbound;

import com.kab.qershi.auth.domain.model.Permission;
import com.kab.qershi.auth.domain.model.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepositoryPort {
    Optional<Role> findById(UUID roleId);
    Optional<Permission> findPermissionById(UUID permissionId);
    List<Role> findAll();
    List<Permission> findAllPermissions();
    void save(Role role);
    void delete(UUID roleId);
    long countUsersAssignedToRole(UUID roleId);
}