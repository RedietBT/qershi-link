package com.kab.qershi.auth.infrastructure.rest;

import com.kab.qershi.auth.domain.ports.inbound.RbacManagementUseCase;
import com.kab.qershi.auth.infrastructure.persistence.PermissionEntity;
import com.kab.qershi.auth.infrastructure.persistence.SpringDataPermissionRepository; // 1. Import
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "RBAC Management", description = "Endpoints for managing custom tenant roles and permissions")
@RequiredArgsConstructor
public class RoleController {

    private static final Logger log = LoggerFactory.getLogger(RoleController.class);
    private final RbacManagementUseCase rbacManagementUseCase;
    private final SpringDataPermissionRepository permissionRepository; // 2. Inject

    @GetMapping("/permissions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN')")
    @Operation(summary = "List all active permissions", description = "Retrieves all available permissions to be used for role creation.")
    public ResponseEntity<List<PermissionEntity>> getAllPermissions() {
        return ResponseEntity.ok(permissionRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('SACCO_ADMIN')")
    @Operation(summary = "Create a custom local role", description = "Allows administrators to bundle specific permissions into a custom role.")
    public ResponseEntity<RbacManagementUseCase.RoleResult> createRole(
            @RequestBody RbacManagementUseCase.CreateRoleCommand command) {
        log.info("SACCO_ADMIN creating custom role: {}", command.roleName());
        return ResponseEntity.ok(rbacManagementUseCase.createLocalRole(command));
    }
}