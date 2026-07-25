package com.kab.qershi.auth.domain.service;

import com.kab.qershi.auth.domain.model.Permission;
import com.kab.qershi.auth.domain.model.Role;
import java.util.Collection;

public class RbacDomainService {

    /**
     * Section 1.1.1.5.1: Custom Role Domain Validation
     * Enforces core business constraints before custom local roles are persisted.
     */
    public void validateCustomRoleAssignment(Role role, Collection<Permission> permissionsToGrant) {
        // Enforce immutability of core infrastructure profiles
        if (role.isSystemDefined()) {
            throw new IllegalStateException("System defined core roles cannot have their scopes altered.");
        }

        if (permissionsToGrant == null || permissionsToGrant.isEmpty()) {
            throw new IllegalArgumentException("A role must be granted at least one active capability.");
        }

        // Business Rule: Guard against assigning deactivated capabilities
        for (Permission permission : permissionsToGrant) {
            if (!permission.isActive()) {
                throw new IllegalArgumentException(
                        "Security violation: Cannot grant inactive system capability: " + permission.toAuthority()
                );
            }
        }
    }
}