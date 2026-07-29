package com.kab.qershi.auth.infrastructure.rest;

import com.kab.qershi.auth.domain.model.UserStatus;
import com.kab.qershi.auth.domain.ports.outbound.MessagingPort;
import com.kab.qershi.auth.infrastructure.grpc.ProfileServiceClient;
import com.kab.qershi.auth.infrastructure.persistence.SpringDataUserRepository;
import com.kab.qershi.auth.infrastructure.persistence.UserEntity;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

/**
 * REST controller managing systemic administrative operations on user security profiles.
 * Manages purely security metrics (MSISDN, Status) without demographic pollution.
 *
 * @author KAB Digital Solution PLC
 * @version 1.5.0
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Account Management", description = "Endpoints for administrative panels to track and perform CRUD options on identity records")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final SpringDataUserRepository userRepository;
    private final ProfileServiceClient profileServiceClient;
    private final PasswordEncoder passwordEncoder;
    private final MessagingPort messagingPort;

    public UserController(SpringDataUserRepository userRepository,
                          ProfileServiceClient profileServiceClient,
                          PasswordEncoder passwordEncoder,
                          MessagingPort messagingPort) {
        this.userRepository = userRepository;
        this.profileServiceClient = profileServiceClient;
        this.passwordEncoder = passwordEncoder;
        this.messagingPort = messagingPort;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN')")
    @Operation(summary = "Fetch user accounts", description = "Retrieves user account security records. Filterable by saccoId for tenant-scoped SACCO_ADMIN access.")
    public ResponseEntity<List<UserEntity>> getAllUsers(@RequestParam(required = false) UUID saccoId) {
        if (saccoId != null) {
            log.info("Retrieving user accounts for SACCO: {}", saccoId);
            return ResponseEntity.ok(userRepository.findBySaccoId(saccoId));
        }
        log.info("Retrieving all user accounts across platform");
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN')")
    @Operation(summary = "Register a new user", description = "Creates a new user account within a specific SACCO and dispatches an initial PIN via SMS.")
    public ResponseEntity<String> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Registering new user for SACCO: {} with phone: {}", request.saccoId(), request.msisdn());

        if (userRepository.findByMsisdn(request.msisdn()).isPresent()) {
            throw new IllegalArgumentException("User with phone number " + request.msisdn() + " is already registered.");
        }

        // System automatically generates a secure 6-digit initial PIN for SMS delivery
        String rawPin = String.format("%06d", new SecureRandom().nextInt(900000) + 100000);

        UserEntity userEntity = new UserEntity();
        userEntity.setUserId(UUID.randomUUID());
        userEntity.setMsisdn(request.msisdn());
        userEntity.setSaccoId(request.saccoId());
        userEntity.setGlobalRole(request.globalRole());
        userEntity.setStatus(UserStatus.PASSWORD_CHANGE_REQUIRED);
        userEntity.setCredentialHash(passwordEncoder.encode(rawPin));
        userEntity.setFailedLoginAttempts(0);

        userRepository.save(userEntity);

        // Dispatch SMS notification with initial PIN
        String smsMessage = "Welcome to Qershi Link! Your user account has been created. Your initial PIN is: " + rawPin;
        try {
            messagingPort.sendSms(request.msisdn(), smsMessage);
            log.info("Initial PIN SMS notification dispatched to {}", request.msisdn());
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", request.msisdn(), e.getMessage());
        }

        return ResponseEntity.ok("User registered successfully. Initial PIN (" + rawPin + ") sent via SMS to " + request.msisdn() + ".");
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
            userEntity.setMsisdn(request.msisdn());
            userEntity.setStatus(request.status());

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
        userRepository.deleteById(id);
        profileServiceClient.triggerProfileCascadeDeletion(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/roles/{roleId}")
    @Transactional
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