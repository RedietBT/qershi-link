package com.kab.qershi.auth.application.usecase;

import com.kab.qershi.auth.domain.ports.outbound.MessagingPort;
import com.kab.qershi.auth.domain.ports.outbound.UserRepositoryPort;
import com.kab.qershi.auth.infrastructure.rest.dto.SuperAdminRegistrationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Service for registering platform-level Super Admin accounts into master schema.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class SuperAdminService {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminService.class);
    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final MessagingPort messagingPort;

    public SuperAdminService(UserRepositoryPort userRepositoryPort, PasswordEncoder passwordEncoder,
                             @Qualifier("notificationGrpcClientAdapter") MessagingPort messagingPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
        this.messagingPort = messagingPort;
    }

    private static final String PLATFORM_SACCO_ID = "00000000-0000-0000-0000-000000000000";
    private static final String SUPER_ADMIN_ROLE_ID = "018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f";

    @Transactional
    public void registerSuperAdmin(SuperAdminRegistrationRequest request) {
        String userId = UUID.randomUUID().toString();
        // Generate a secure temporary PIN
        String rawPin = String.format("%06d", new SecureRandom().nextInt(900000) + 100000);
        String hashedPin = passwordEncoder.encode(rawPin);

        userRepositoryPort.saveSuperAdmin(
                userId,
                request.msisdn(),
                hashedPin,
                "SUPER_ADMIN",
                PLATFORM_SACCO_ID
        );

        userRepositoryPort.assignRole(userId, SUPER_ADMIN_ROLE_ID, PLATFORM_SACCO_ID);

        log.info("Registered Super Admin for MSISDN {}", request.msisdn());
        messagingPort.sendSms(request.msisdn(), "Welcome to System Platform! Your Super Admin PIN is: " + rawPin);
    }
}