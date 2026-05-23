package com.kab.qershi.auth.domain.service;

import com.kab.qershi.auth.domain.model.Role;
import java.util.Set;

public class RbacDomainService {

    // Section 1.1.1.6.6 Global Core Capability Permissions Matrix
    private static final Set<String> CORE_PERMISSIONS = Set.of(
            "MEMBER_CREATE",
            "MEMBER_VIEW_BASIC",
            "LOAN_REQUEST_CREATE",
            "LOAN_APPROVE",
            "CASH_DEPOSIT",
            "SAVINGS_WITHDRAW",
            "REPORT_VIEW_ALL",
            "SACCO_ATTACH"
    );

    /**
     * Section 1.1.1.5.1: Permission Seeding Logic
     * Automatically maps every single global permission to the newly spawned tenant's ADMIN role.
     */
    public void seedAdminRolePermissions(Role adminRole) {
        if (!adminRole.isSystemDefined() || !"ADMIN".equalsIgnoreCase(adminRole.getRoleName())) {
            throw new IllegalArgumentException("System permission seeding can only be executed on primary infrastructure ADMIN roles.");
        }

        for (String permission : CORE_PERMISSIONS) {
            adminRole.grantPermission(permission);
        }
    }
}