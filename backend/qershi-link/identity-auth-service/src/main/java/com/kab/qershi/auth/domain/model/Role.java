package com.kab.qershi.auth.domain.model;

import lombok.Getter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class Role {
    private final UUID roleId;
    private final String roleName;
    private final boolean isSystemDefined;
    private final Set<String> permissions;

    public Role(UUID roleId, String roleName, boolean isSystemDefined) {
        this.roleId = roleId != null ? roleId : UUID.randomUUID();
        this.roleName = roleName;
        this.isSystemDefined = isSystemDefined;
        this.permissions = new HashSet<>();
    }

    public void grantPermission(String permissionCode) {
        if (isSystemDefined && !permissions.isEmpty()) {
            throw new IllegalStateException("System defined core roles cannot have their seeded scopes altered.");
        }
        this.permissions.add(permissionCode);
    }

    // Defensive read-only wrapper for security
    public Set<String> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }
}