package com.kab.qershi.auth.infrastructure.rest;

import com.kab.qershi.auth.domain.ports.inbound.RbacManagementUseCase;
import com.kab.qershi.auth.infrastructure.persistence.PermissionEntity;
import com.kab.qershi.auth.infrastructure.persistence.RoleEntity;
import com.kab.qershi.auth.infrastructure.persistence.SpringDataPermissionRepository;
import com.kab.qershi.auth.infrastructure.persistence.SpringDataRoleRepository;
import com.kab.qershi.auth.infrastructure.rest.dto.UpdateRoleRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * REST controller managing RBAC role definitions and permission mappings.
 *
 * @author KAB Digital Solution PLC
 * @version 1.7.0
 */
@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "RBAC Management", description = "Endpoints for managing custom tenant roles and permissions")
public class RoleController {

    private static final Logger log = LoggerFactory.getLogger(RoleController.class);
    private final RbacManagementUseCase rbacManagementUseCase;
    private final SpringDataRoleRepository roleRepository;
    private final SpringDataPermissionRepository permissionRepository;

    public RoleController(RbacManagementUseCase rbacManagementUseCase,
                          SpringDataRoleRepository roleRepository,
                          SpringDataPermissionRepository permissionRepository) {
        this.rbacManagementUseCase = rbacManagementUseCase;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('ROLE_READ', 'ROLE_MANAGE')")
    @Operation(summary = "List all active permissions", description = "Retrieves all available permissions to be used for role creation and modification.")
    public ResponseEntity<List<PermissionEntity>> getAllPermissions() {
        return ResponseEntity.ok(permissionRepository.findAll());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('ROLE_READ', 'ROLE_MANAGE')")
    @Operation(summary = "Fetch all roles", description = "Retrieves all custom and system-defined roles with their assigned permission sets.")
    public ResponseEntity<List<RoleEntity>> getAllRoles() {
        return ResponseEntity.ok(roleRepository.findAll());
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('ROLE_READ', 'ROLE_MANAGE')")
    @Operation(summary = "Get role details by ID", description = "Retrieves a specific role definition including its assigned permission list.")
    public ResponseEntity<RoleEntity> getRoleById(@PathVariable UUID roleId) {
        return roleRepository.findById(roleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('ROLE_CREATE', 'ROLE_MANAGE')")
    @Operation(summary = "Create a custom local role", description = "Allows administrators to bundle specific permissions into a custom role.")
    public ResponseEntity<RbacManagementUseCase.RoleResult> createRole(
            @RequestBody RbacManagementUseCase.CreateRoleCommand command) {
        log.info("Creating custom role: {}", command.roleName());
        return ResponseEntity.ok(rbacManagementUseCase.createLocalRole(command));
    }

    @PutMapping("/{roleId}")
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('ROLE_UPDATE', 'ROLE_MANAGE')")
    @Operation(summary = "Update custom role permissions", description = "Updates the role name and adds or removes permissions for a custom role. System-defined roles cannot be modified.")
    public ResponseEntity<RoleEntity> updateRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody UpdateRoleRequest request) {

        log.info("Updating role definition for ID: {}", roleId);

        RoleEntity roleEntity = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));

        if (roleEntity.isSystemDefined()) {
            throw new IllegalStateException("System-defined roles cannot be modified.");
        }

        roleEntity.setRoleName(request.roleName());

        // Fetch and re-assign updated list of permissions
        List<PermissionEntity> updatedPermissions = permissionRepository.findAllById(request.permissionIds());
        roleEntity.setPermissions(new HashSet<>(updatedPermissions));

        RoleEntity savedRole = roleRepository.save(roleEntity);
        return ResponseEntity.ok(savedRole);
    }

    @DeleteMapping("/{roleId}")
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('ROLE_DELETE', 'ROLE_MANAGE')")
    @Operation(summary = "Delete custom role safely", description = "Deletes a custom role if it is not system-defined and is not currently assigned to any active users.")
    @ApiResponse(responseCode = "204", description = "Role successfully deleted.")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID roleId) {
        log.info("Request received to delete role ID: {}", roleId);

        RoleEntity roleEntity = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));

        if (roleEntity.isSystemDefined()) {
            throw new IllegalStateException("System-defined roles cannot be deleted.");
        }

        long userCount = roleRepository.countUsersAssignedToRole(roleId);
        if (userCount > 0) {
            throw new IllegalStateException("Cannot delete role '" + roleEntity.getRoleName() + "' because it is currently assigned to " + userCount + " user(s). Unassign the role from all users before deleting.");
        }

        roleRepository.delete(roleEntity);
        return ResponseEntity.noContent().build();
    }
}