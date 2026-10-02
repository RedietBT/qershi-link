package com.kab.qershi.auth.application.usecase;

import com.kab.qershi.auth.domain.model.GlobalRole;
import com.kab.qershi.auth.domain.model.Sacco;
import com.kab.qershi.auth.domain.model.User;
import com.kab.qershi.auth.domain.model.UserStatus;
import com.kab.qershi.auth.domain.ports.inbound.UserManagementUseCase;
import com.kab.qershi.auth.domain.ports.outbound.MessagingPort;
import com.kab.qershi.auth.domain.ports.outbound.PasswordEncoderPort;
import com.kab.qershi.auth.domain.ports.outbound.ProfileClientPort;
import com.kab.qershi.auth.domain.ports.outbound.SaccoRepositoryPort;
import com.kab.qershi.auth.domain.ports.outbound.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

/**
 * Application service for managing user security profiles and administrative role mappings.
 * Pure application service with zero infrastructure imports.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class UserManagementService implements UserManagementUseCase {

    private static final Logger log = LoggerFactory.getLogger(UserManagementService.class);

    private final UserRepositoryPort userRepositoryPort;
    private final SaccoRepositoryPort saccoRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final MessagingPort messagingPort;
    private final ProfileClientPort profileClientPort;

    public UserManagementService(UserRepositoryPort userRepositoryPort,
                                 SaccoRepositoryPort saccoRepositoryPort,
                                 PasswordEncoderPort passwordEncoderPort,
                                 @Qualifier("notificationGrpcClientAdapter") MessagingPort messagingPort,
                                 ProfileClientPort profileClientPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.saccoRepositoryPort = saccoRepositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.messagingPort = messagingPort;
        this.profileClientPort = profileClientPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers(UUID saccoId, boolean isSuperAdmin, UUID tenantSaccoId) {
        if (isSuperAdmin) {
            if (saccoId != null) {
                log.info("SUPER_ADMIN retrieving user accounts for SACCO: {}", saccoId);
                return userRepositoryPort.findBySaccoId(saccoId);
            } else {
                log.info("SUPER_ADMIN retrieving all user accounts across platform");
                return userRepositoryPort.findAll();
            }
        } else {
            if (tenantSaccoId == null) {
                throw new SecurityException("Tenant user context missing saccoId claim.");
            }
            log.info("Tenant admin retrieving user accounts scoped to SACCO: {}", tenantSaccoId);
            return userRepositoryPort.findBySaccoId(tenantSaccoId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(UUID id, boolean isSuperAdmin, UUID tenantSaccoId) {
        User user = userRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        if (!isSuperAdmin) {
            if (tenantSaccoId == null || !tenantSaccoId.equals(user.getSaccoId())) {
                throw new SecurityException("Access denied to user outside tenant SACCO context.");
            }
        }

        return user;
    }

    @Override
    @Transactional
    public String createUser(String msisdn, GlobalRole globalRole, UUID targetSaccoId) {
        if (targetSaccoId == null) {
            throw new IllegalArgumentException("saccoId is required for user registration.");
        }

        log.info("Registering new user for SACCO: {} with phone: {}", targetSaccoId, msisdn);

        if (userRepositoryPort.findByMsisdn(msisdn).isPresent()) {
            throw new IllegalArgumentException("User with phone number " + msisdn + " is already registered.");
        }

        // Generate 6-digit initial PIN
        String rawPin = String.format("%06d", new SecureRandom().nextInt(900000) + 100000);
        String hashedPin = passwordEncoderPort.encode(rawPin);

        UUID userId = UUID.randomUUID();
        User user = new User(
                userId,
                msisdn,
                targetSaccoId,
                hashedPin,
                globalRole
        );
        user.setStatus(UserStatus.PASSWORD_CHANGE_REQUIRED);

        userRepositoryPort.save(user);

        // Assign default ADMIN role in master_schema.user_roles
        UUID defaultRoleId = UUID.fromString("018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f");
        userRepositoryPort.insertUserRole(userId, defaultRoleId, targetSaccoId);

        // Fetch SACCO name for personalized SMS greeting
        String saccoName = saccoRepositoryPort.findById(targetSaccoId)
                .map(Sacco::getSaccoName)
                .orElse("your SACCO");

        String smsMessage = "Welcome to " + saccoName + "! Your user account has been created. Your initial PIN is: " + rawPin;
        try {
            messagingPort.sendSms(msisdn, smsMessage);
            log.info("Initial PIN SMS notification dispatched to {}", msisdn);
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", msisdn, e.getMessage());
        }

        return "User registered successfully. Initial PIN sent via SMS to " + msisdn + ".";
    }

    @Override
    @Transactional
    public User updateUser(UUID id, String msisdn, UserStatus status, boolean isSuperAdmin, UUID tenantSaccoId) {
        User user = userRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        if (!isSuperAdmin) {
            if (tenantSaccoId == null || !tenantSaccoId.equals(user.getSaccoId())) {
                throw new SecurityException("Forbidden attempt to update user outside tenant SACCO context.");
            }
        }

        user.setMsisdn(msisdn);
        user.setStatus(status);

        return userRepositoryPort.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepositoryPort.existsById(id)) {
            throw new IllegalArgumentException("User not found with ID: " + id);
        }

        log.warn("Purging user identity: {}", id);
        userRepositoryPort.deleteById(id);
        profileClientPort.triggerProfileCascadeDeletion(id);
    }

    @Override
    @Transactional
    public void assignRole(UUID userId, UUID roleId, UUID targetSaccoId) {
        if (targetSaccoId == null) {
            targetSaccoId = userRepositoryPort.findById(userId)
                    .map(User::getSaccoId)
                    .orElseThrow(() -> new IllegalArgumentException("Cannot determine SACCO context for user role assignment."));
        }

        log.info("Assigning role {} to user {} in SACCO {}", roleId, userId, targetSaccoId);
        userRepositoryPort.insertUserRole(userId, roleId, targetSaccoId);
    }
}
