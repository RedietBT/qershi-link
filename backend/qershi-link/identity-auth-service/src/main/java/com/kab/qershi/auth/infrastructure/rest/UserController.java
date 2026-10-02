package com.kab.qershi.auth.infrastructure.rest;

import com.kab.qershi.auth.domain.model.User;
import com.kab.qershi.auth.domain.ports.inbound.UserManagementUseCase;
import com.kab.qershi.auth.infrastructure.rest.dto.CreateUserRequest;
import com.kab.qershi.auth.infrastructure.rest.dto.UpdateUserRequest;
import com.kab.qershi.auth.infrastructure.rest.dto.UserResponse;
import com.kab.qershi.auth.infrastructure.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller managing systemic administrative operations on user security profiles.
 * Injects inbound port UserManagementUseCase.
 *
 * @author KAB Digital Solution PLC
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Account Management", description = "Endpoints for administrative panels to track and perform CRUD options on identity records")
public class UserController {

    private final UserManagementUseCase userManagementUseCase;

    public UserController(UserManagementUseCase userManagementUseCase) {
        this.userManagementUseCase = userManagementUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN')")
    @Operation(summary = "Fetch user accounts", description = "Retrieves user accounts. SUPER_ADMIN can view all platform users or filter by saccoId. SACCO_ADMIN is automatically restricted to users within their SACCO via JWT claims.")
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @RequestParam(required = false) UUID saccoId,
            Authentication authentication) {

        boolean isSuperAdmin = SecurityUtils.isSuperAdmin(authentication);
        UUID tenantSaccoId = SecurityUtils.extractSaccoId(authentication);

        List<User> users = userManagementUseCase.getAllUsers(saccoId, isSuperAdmin, tenantSaccoId);
        List<UserResponse> response = users.stream()
                .map(UserResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN')")
    @Operation(summary = "Register a new user", description = "Creates a new user account within a specific SACCO and dispatches an initial PIN via SMS.")
    public ResponseEntity<String> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @RequestParam(required = false) UUID saccoId,
            Authentication authentication) {

        UUID targetSaccoId;
        if (!SecurityUtils.isSuperAdmin(authentication)) {
            targetSaccoId = SecurityUtils.extractSaccoId(authentication);
            if (targetSaccoId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("SACCO tenant context missing from JWT token.");
            }
        } else {
            targetSaccoId = saccoId != null ? saccoId : SecurityUtils.extractSaccoId(authentication);
            if (targetSaccoId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("saccoId is required for Super Admin user registration.");
            }
        }

        String result = userManagementUseCase.createUser(request.msisdn(), request.globalRole(), targetSaccoId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN')")
    @Operation(summary = "Get user account details by ID", description = "SUPER_ADMIN can access any user. SACCO_ADMIN is restricted to users within their SACCO.")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id, Authentication authentication) {
        boolean isSuperAdmin = SecurityUtils.isSuperAdmin(authentication);
        UUID tenantSaccoId = SecurityUtils.extractSaccoId(authentication);

        User user = userManagementUseCase.getUserById(id, isSuperAdmin, tenantSaccoId);
        return ResponseEntity.ok(UserResponse.fromDomain(user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN')")
    @Operation(summary = "Update user security parameters", description = "Updates mobile phone registration handles and status switches dynamically.")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {

        boolean isSuperAdmin = SecurityUtils.isSuperAdmin(authentication);
        UUID tenantSaccoId = SecurityUtils.extractSaccoId(authentication);

        User updated = userManagementUseCase.updateUser(id, request.msisdn(), request.status(), isSuperAdmin, tenantSaccoId);
        return ResponseEntity.ok(UserResponse.fromDomain(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Purge user identity and issue cascading deletions", description = "Strictly gated to global platform SUPER_ADMIN actors.")
    @ApiResponse(responseCode = "204", description = "User records and corresponding profiles successfully evicted across services.")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userManagementUseCase.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('ROLE_UPDATE', 'ROLE_MANAGE')")
    @Operation(
            summary = "Assign role to user",
            description = "Maps a specific role to a user within a specific tenant (SACCO) context."
    )
    @ApiResponse(responseCode = "204", description = "Role successfully assigned to user.")
    public ResponseEntity<Void> assignRole(
            @PathVariable UUID userId,
            @PathVariable UUID roleId,
            @RequestParam(required = false) UUID saccoId,
            Authentication authentication) {

        UUID targetSaccoId = saccoId;
        if (targetSaccoId == null) {
            targetSaccoId = SecurityUtils.extractSaccoId(authentication);
        }

        if (!SecurityUtils.isSuperAdmin(authentication)) {
            UUID tenantSaccoId = SecurityUtils.extractSaccoId(authentication);
            if (tenantSaccoId != null && targetSaccoId != null && !tenantSaccoId.equals(targetSaccoId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        userManagementUseCase.assignRole(userId, roleId, targetSaccoId);
        return ResponseEntity.noContent().build();
    }
}