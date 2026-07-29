package com.kab.qershi.auth.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * REST API payload container for updating role name and permission assignments.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Schema(description = "Request payload container for updating custom tenant role definition and permissions")
public record UpdateRoleRequest(
        @Schema(description = "Custom role display name", example = "CUSTOM_ROLE_NAME")
        @NotBlank(message = "Role name cannot be left blank.")
        String roleName,

        @Schema(description = "Updated list of permission UUIDs assigned to this role")
        @NotNull(message = "Permission IDs list cannot be null.")
        List<UUID> permissionIds
) {}
