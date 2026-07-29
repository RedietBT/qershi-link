package com.kab.qershi.auth.domain.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Role {
    private final UUID roleId;
    private final String roleName;
    private final boolean isSystemDefined;
    private final Set<Permission> permissions;

    public Role(UUID roleId, String roleName, boolean isSystemDefined) {
        this.roleId = roleId != null ? roleId : UUID.randomUUID();
        this.roleName = roleName;
        this.isSystemDefined = isSystemDefined;
        this.permissions = new HashSet<>();
    }

    public UUID getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public boolean isSystemDefined() {
        return isSystemDefined;
    }

    public void grantPermission(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Cannot grant a null permission.");
        }
        if (!permission.isActive()) {
            throw new IllegalArgumentException("Cannot grant an inactive system capability.");
        }
        if (isSystemDefined && !permissions.isEmpty()) {
            throw new IllegalStateException("System defined core roles cannot have their seeded scopes altered.");
        }
        this.permissions.add(permission);
    }

    // Defensive read-only wrapper for security
    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }
}