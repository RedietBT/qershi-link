package com.kab.qershi.auth.domain.model;

import java.util.UUID;

public class Permission {
    private final UUID permissionId;
    private final String resource;
    private final String action;
    private final String description;
    private final boolean isActive;

    public Permission(UUID permissionId, String resource, String action, String description, boolean isActive) {
        this.permissionId = permissionId;
        this.resource = resource;
        this.action = action;
        this.description = description;
        this.isActive = isActive;
    }

    /**
     * Combines resource and action into a standard Spring Security authority string.
     * e.g., "MEMBER:CREATE"
     */
    public String toAuthority() {
        return this.resource + ":" + this.action;
    }

    public UUID getPermissionId() { return permissionId; }
    public String getResource() { return resource; }
    public String getAction() { return action; }
    public String getDescription() { return description; }
    public boolean isActive() { return isActive; }
}