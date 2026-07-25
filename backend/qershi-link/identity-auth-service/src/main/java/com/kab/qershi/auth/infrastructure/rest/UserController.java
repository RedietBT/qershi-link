package com.kab.qershi.auth.infrastructure.rest;

import com.kab.qershi.auth.infrastructure.persistence.SpringDataUserRepository;
import com.kab.qershi.auth.infrastructure.persistence.UserEntity;
import com.kab.qershi.auth.infrastructure.grpc.ProfileServiceClient;
import com.kab.qershi.auth.infrastructure.rest.dto.CreateUserRequest;
import com.kab.qershi.auth.infrastructure.rest.dto.UpdateUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller managing systemic administrative operations on user security profiles.
 * Manages purely security metrics (MSISDN, Status) without demographic pollution.
 *
 * @author KAB Digital Solution PLC
 * @version 1.3.0
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Account Management", description = "Endpoints for administrative panels to track and perform CRUD options on identity records")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final SpringDataUserRepository userRepository;
    private final ProfileServiceClient profileServiceClient;

    public UserController(SpringDataUserRepository userRepository, ProfileServiceClient profileServiceClient) {
        this.userRepository = userRepository;
        this.profileServiceClient = profileServiceClient;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN')")
    @Operation(summary = "Fetch all user accounts", description = "Retrieves a list of all user account security records. Restricted to Platform and Tenant Administrators.")
    public ResponseEntity<List<UserEntity>> getAllUsers() {
        log.info("Retrieving all user accounts");
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN')")
    @Operation(summary = "Register a new user", description = "Creates a new user account within a specific SACCO.")
    public ResponseEntity<String> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Registering new user for SACCO: {}", request.saccoId());
        return ResponseEntity.ok("User registered successfully. Initial PIN sent via SMS.");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN')")
    @Operation(summary = "Get user account details by ID", description = "Allowed for Platform Admins and Local Tenant Admins.")
    public ResponseEntity<UserEntity> getUserById(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN')")
    @Operation(summary = "Update user security parameters", description = "Updates mobile phone registration handles and status switches dynamically.")
    public ResponseEntity<UserEntity> updateUser(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        return userRepository.findById(id).map(userEntity -> {
            // Use setters available on the Entity
            userEntity.setMsisdn(request.msisdn());
            userEntity.setStatus(request.status());

            // Save the updated entity
            UserEntity savedEntity = userRepository.save(userEntity);
            return ResponseEntity.ok(savedEntity);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Purge user identity and issue cascading deletions", description = "Strictly gated to global platform SUPER_ADMIN actors.")
    @ApiResponse(responseCode = "204", description = "User records and corresponding profiles successfully evicted across services.")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        log.warn("Purging user identity: {}", id);
        // 1. Evict core credential from the authentication master_schema database index
        userRepository.deleteById(id);

        // 2. Clear out dependencies inside profile_schema via inter-service gRPC communication
        profileServiceClient.triggerProfileCascadeDeletion(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN')")
    @Operation(
            summary = "Assign role to user",
            description = "Maps a specific role to a user within a specific tenant (SACCO) context."
    )
    @ApiResponse(responseCode = "204", description = "Role successfully assigned to user.")
    public ResponseEntity<Void> assignRole(
            @PathVariable UUID userId,
            @PathVariable UUID roleId,
            @RequestParam UUID saccoId) {

        log.info("Assigning role {} to user {} in SACCO {}", roleId, userId, saccoId);
        userRepository.insertUserRole(userId, roleId, saccoId);
        return ResponseEntity.noContent().build();
    }
}