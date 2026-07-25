package com.kab.qershi.auth.application.usecase;

import com.kab.qershi.auth.domain.ports.outbound.MessagingPort;
import com.kab.qershi.auth.infrastructure.rest.dto.SuperAdminRegistrationRequest;
import com.kab.qershi.auth.domain.ports.outbound.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SuperAdminService {
    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final MessagingPort messagingPort;

    // You need a constant or configuration for the Platform/System SACCO ID
    private static final String PLATFORM_SACCO_ID = "00000000-0000-0000-0000-000000000000";
    private static final String SUPER_ADMIN_ROLE_ID = "018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f";

    @Transactional
    public void registerSuperAdmin(SuperAdminRegistrationRequest request) {
        String userId = UUID.randomUUID().toString();
        // Generate a secure temporary PIN
        String rawPin = String.format("%06d", new SecureRandom().nextInt(1000000));
        String hashedPin = passwordEncoder.encode(rawPin);

        userRepositoryPort.saveSuperAdmin(
                userId,
                request.msisdn(),
                hashedPin,
                "SUPER_ADMIN",
                PLATFORM_SACCO_ID
        );

        userRepositoryPort.assignRole(userId, SUPER_ADMIN_ROLE_ID, PLATFORM_SACCO_ID);

        // Trigger the SMS here
        messagingPort.sendSms(request.msisdn(), "Your Super Admin PIN is: " + rawPin);
    }
}