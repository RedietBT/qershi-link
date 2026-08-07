package com.kab.qershi.auth.infrastructure.rest;

import com.kab.qershi.auth.domain.model.UserStatus;
import com.kab.qershi.auth.domain.ports.outbound.MessagingPort;
import com.kab.qershi.auth.infrastructure.grpc.ProfileServiceClient;
import com.kab.qershi.auth.infrastructure.persistence.SaccoEntity;
import com.kab.qershi.auth.infrastructure.persistence.SpringDataSaccoRepository;
import com.kab.qershi.auth.infrastructure.persistence.SpringDataUserRepository;
import com.kab.qershi.auth.infrastructure.persistence.UserEntity;
import com.kab.qershi.auth.infrastructure.rest.dto.CreateUserRequest;
import com.kab.qershi.auth.infrastructure.rest.dto.UpdateUserRequest;
import com.kab.qershi.auth.infrastructure.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller managing systemic administrative operations on user security profiles.
 * Manages purely security metrics (MSISDN, Status) without demographic pollution.
 * Enforces JWT tenant-scoped access for SACCO_ADMIN users while preserving global SUPER_ADMIN visibility.
 *
 * @author KAB Digital Solution PLC
 * @version 1.9.0
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Account Management", description = "Endpoints for administrative panels to track and perform CRUD options on identity records")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final SpringDataUserRepository userRepository;
    private final SpringDataSaccoRepository saccoRepository;
    private final ProfileServiceClient profileServiceClient;
    private final PasswordEncoder passwordEncoder;
    private final MessagingPort messagingPort;

    public UserController(SpringDataUserRepository userRepository,
                          SpringDataSaccoRepository saccoRepository,
                          ProfileServiceClient profileServiceClient,
                          PasswordEncoder passwordEncoder,
                          @Qualifier("notificationGrpcClientAdapter") MessagingPort messagingPort) {
        this.userRepository = userRepository;
        this.saccoRepository = saccoRepository;
        this.profileServiceClient = profileServiceClient;
        this.passwordEncoder = passwordEncoder;
        this.messagingPort = messagingPort;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN')")
    @Operation(summary = "Fetch user accounts", description = "Retrieves user accounts. SUPER_ADMIN can view all platform users or filter by saccoId. SACCO_ADMIN is automatically restricted to users within their SACCO via JWT claims.")
    public ResponseEntity<List<UserEntity>> getAllUsers(
            @RequestParam(required = false) UUID saccoId,
            Authentication authentication) {

        if (SecurityUtils.isSuperAdmin(authentication)) {
            if (saccoId != null) {
                log.info("SUPER_ADMIN retrieving user accounts for SACCO: {}", saccoId);
                return ResponseEntity.ok(userRepository.findBySaccoId(saccoId));
            }
            log.info("SUPER_ADMIN retrieving all user accounts across platform");
            return ResponseEntity.ok(userRepository.findAll());
        }

        // SACCO_ADMIN or tenant user: MUST use saccoId extracted from their JWT token
        UUID tenantSaccoId = SecurityUtils.extractSaccoId(authentication);
        if (tenantSaccoId == null) {
            log.warn("Forbidden attempt to fetch users: SACCO_ADMIN token missing saccoId claim");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        log.info("Tenant admin retrieving user accounts scoped to SACCO: {}", tenantSaccoId);
        return ResponseEntity.ok(userRepository.findBySaccoId(tenantSaccoId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN')")
    @Operation(summary = "Register a new user", description = "Creates a new user account within a specific SACCO and dispatches an initial PIN via SMS.")
    public ResponseEntity<String> createUser(@Valid @RequestBody CreateUserRequest request, Authentication authentication) {
        
        // Enforce tenant boundary for SACCO_ADMIN creates
        if (!SecurityUtils.isSuperAdmin(authentication)) {
            UUID tenantSaccoId = SecurityUtils.extractSaccoId(authentication);
            if (tenantSaccoId != null && !tenantSaccoId.equals(request.saccoId())) {
                log.warn("SACCO admin attempted to create user for different SACCO: {}", request.saccoId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("SACCO administrators can only create users within their own SACCO.");
            }
        }

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

        // Fetch SACCO name to welcome user under their SACCO identity
        String saccoName = saccoRepository.findById(request.saccoId())
                .map(SaccoEntity::getSaccoName)
                .orElse("your SACCO");

        // Dispatch SMS notification with initial PIN (welcome with SACCO name)
        String smsMessage = "Welcome to " + saccoName + "! Your user account has been created. Your initial PIN is: " + rawPin;
        try {
            messagingPort.sendSms(request.msisdn(), smsMessage);
            log.info("Initial PIN SMS notification dispatched to {}", request.msisdn());
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", request.msisdn(), e.getMessage());
        }

        return ResponseEntity.ok("User registered successfully. Initial PIN sent via SMS to " + request.msisdn() + ".");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN')")
    @Operation(summary = "Get user account details by ID", description = "SUPER_ADMIN can access any user. SACCO_ADMIN is restricted to users within their SACCO.")
    public ResponseEntity<UserEntity> getUserById(@PathVariable UUID id, Authentication authentication) {
        Optional<UserEntity> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        UserEntity user = userOpt.get();

        if (!SecurityUtils.isSuperAdmin(authentication)) {
            UUID tenantSaccoId = SecurityUtils.extractSaccoId(authentication);
            if (tenantSaccoId == null || !tenantSaccoId.equals(user.getSaccoId())) {
                log.warn("Forbidden attempt to access user {} outside tenant SACCO context {}", id, tenantSaccoId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN')")
    @Operation(summary = "Update user security parameters", description = "Updates mobile phone registration handles and status switches dynamically.")
    public ResponseEntity<UserEntity> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {

        Optional<UserEntity> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        UserEntity userEntity = userOpt.get();

        if (!SecurityUtils.isSuperAdmin(authentication)) {
            UUID tenantSaccoId = SecurityUtils.extractSaccoId(authentication);
            if (tenantSaccoId == null || !tenantSaccoId.equals(userEntity.getSaccoId())) {
                log.warn("Forbidden attempt to update user {} outside tenant SACCO context", id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        userEntity.setMsisdn(request.msisdn());
        userEntity.setStatus(request.status());

        UserEntity savedEntity = userRepository.save(userEntity);
        return ResponseEntity.ok(savedEntity);
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('ROLE_UPDATE', 'ROLE_MANAGE')")
    @Operation(
            summary = "Assign role to user",
            description = "Maps a specific role to a user within a specific tenant (SACCO) context."
    )
    @ApiResponse(responseCode = "204", description = "Role successfully assigned to user.")
    public ResponseEntity<Void> assignRole(
            @PathVariable UUID userId,
            @PathVariable UUID roleId,
            @RequestParam UUID saccoId,
            Authentication authentication) {

        if (!SecurityUtils.isSuperAdmin(authentication)) {
            UUID tenantSaccoId = SecurityUtils.extractSaccoId(authentication);
            if (tenantSaccoId != null && !tenantSaccoId.equals(saccoId)) {
                log.warn("SACCO admin attempted to assign role in different SACCO: {}", saccoId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        log.info("Assigning role {} to user {} in SACCO {}", roleId, userId, saccoId);
        userRepository.insertUserRole(userId, roleId, saccoId);
        return ResponseEntity.noContent().build();
    }
}