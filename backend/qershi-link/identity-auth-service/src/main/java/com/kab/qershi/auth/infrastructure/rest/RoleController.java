package com.kab.qershi.auth.infrastructure.rest;

import com.kab.qershi.auth.domain.model.Permission;
import com.kab.qershi.auth.domain.model.Role;
import com.kab.qershi.auth.domain.ports.inbound.RbacManagementUseCase;
import com.kab.qershi.auth.infrastructure.rest.dto.UpdateRoleRequest;
import com.kab.qershi.auth.infrastructure.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller managing RBAC role definitions and permission mappings.
 * Injects inbound port RbacManagementUseCase.
 *
 * @author KAB Digital Solution PLC
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "RBAC Management", description = "Endpoints for managing custom tenant roles and permissions")
public class RoleController {

    private static final Logger log = LoggerFactory.getLogger(RoleController.class);
    private final RbacManagementUseCase rbacManagementUseCase;

    public RoleController(RbacManagementUseCase rbacManagementUseCase) {
        this.rbacManagementUseCase = rbacManagementUseCase;
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('ROLE_READ', 'ROLE_MANAGE')")
    @Operation(summary = "List all active permissions", description = "Retrieves all available permissions to be used for role creation and modification.")
    public ResponseEntity<List<Permission>> getAllPermissions() {
        return ResponseEntity.ok(rbacManagementUseCase.getAllPermissions());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('ROLE_READ', 'ROLE_MANAGE')")
    @Operation(summary = "Fetch all roles", description = "SUPER_ADMIN sees system-defined platform roles only. SACCO_ADMIN sees system roles (excluding SUPER_ADMIN) and custom tenant roles.")
    public ResponseEntity<List<Role>> getAllRoles(Authentication authentication) {
        boolean isSuperAdmin = SecurityUtils.isSuperAdmin(authentication);
        return ResponseEntity.ok(rbacManagementUseCase.getAllRoles(isSuperAdmin));
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('ROLE_READ', 'ROLE_MANAGE')")
    @Operation(summary = "Get role details by ID", description = "Retrieves a specific role definition including its assigned permission list. SACCO admins cannot view SUPER_ADMIN role details.")
    public ResponseEntity<Role> getRoleById(@PathVariable UUID roleId, Authentication authentication) {
        boolean isSuperAdmin = SecurityUtils.isSuperAdmin(authentication);
        return ResponseEntity.ok(rbacManagementUseCase.getRoleById(roleId, isSuperAdmin));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('ROLE_CREATE', 'ROLE_MANAGE')")
    @Operation(summary = "Create a custom local role", description = "Allows administrators to bundle specific permissions into a custom role. SACCO administrators are forbidden from creating SUPER_ADMIN roles.")
    public ResponseEntity<RbacManagementUseCase.RoleResult> createRole(
            @RequestBody RbacManagementUseCase.CreateRoleCommand command,
            Authentication authentication) {

        log.info("Creating custom role: {}", command.roleName());

        if (!SecurityUtils.isSuperAdmin(authentication)) {
            if (command.roleName() != null && command.roleName().toUpperCase().contains("SUPER_ADMIN")) {
                log.warn("Forbidden attempt by SACCO admin to create SUPER_ADMIN role: {}", command.roleName());
                throw new SecurityException("SACCO tenant administrators are not permitted to create SUPER_ADMIN roles.");
            }
        }

        return ResponseEntity.ok(rbacManagementUseCase.createLocalRole(command));
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('ROLE_UPDATE', 'ROLE_MANAGE')")
    @Operation(summary = "Update custom role permissions", description = "Updates the role name and adds or removes permissions for a custom role. System-defined roles cannot be modified.")
    public ResponseEntity<Role> updateRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody UpdateRoleRequest request,
            Authentication authentication) {

        log.info("Updating role definition for ID: {}", roleId);
        boolean isSuperAdmin = SecurityUtils.isSuperAdmin(authentication);
        Role updated = rbacManagementUseCase.updateRole(roleId, request.roleName(), request.permissionIds(), isSuperAdmin);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('ROLE_DELETE', 'ROLE_MANAGE')")
    @Operation(summary = "Delete custom role safely", description = "Deletes a custom role if it is not system-defined and is not currently assigned to any active users.")
    @ApiResponse(responseCode = "204", description = "Role successfully deleted.")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID roleId) {
        log.info("Request received to delete role ID: {}", roleId);
        rbacManagementUseCase.deleteRole(roleId);
        return ResponseEntity.noContent().build();
    }
}