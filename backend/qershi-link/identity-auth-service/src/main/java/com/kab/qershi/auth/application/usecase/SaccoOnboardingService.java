package com.kab.qershi.auth.application.usecase;

import com.kab.qershi.auth.domain.model.*;
import com.kab.qershi.auth.domain.ports.inbound.SaccoOnboardingUseCase;
import com.kab.qershi.auth.domain.ports.outbound.*;
import com.kab.qershi.auth.domain.service.IdentityDomainService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.UUID;

public class SaccoOnboardingService implements SaccoOnboardingUseCase {

    private final SaccoRepositoryPort saccoRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final TenantProvisioningPort tenantProvisioningPort;
    private final IdentityDomainService identityDomainService;
    private final MessagingPort messagingPort;
    private final PasswordEncoder passwordEncoder;

    private static final UUID SYSTEM_ADMIN_ROLE_ID = UUID.fromString("018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f");

    public SaccoOnboardingService(
            SaccoRepositoryPort saccoRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            TenantProvisioningPort tenantProvisioningPort,
            IdentityDomainService identityDomainService,
            MessagingPort messagingPort,
            PasswordEncoder passwordEncoder) {
        this.saccoRepositoryPort = saccoRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.tenantProvisioningPort = tenantProvisioningPort;
        this.identityDomainService = identityDomainService;
        this.messagingPort = messagingPort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public OnboardResult onboardSacco(OnboardCommand command) {
        if (saccoRepositoryPort.existsBySaccoName(command.saccoName())) {
            throw new IllegalStateException("A SACCO with this legal name is already registered.");
        }
        identityDomainService.validateUniqueUserIdentifiers(command.adminMsisdn());

        String sanitizedSchemaName = identityDomainService.generateAndSanitizeSchemaName(command.saccoName(), command.isUnion());

        // Generate 6-digit initial PIN
        String rawPin = String.format("%06d", new SecureRandom().nextInt(900000) + 100000);
        String hashedPin = passwordEncoder.encode(rawPin);

        UUID saccoId = UUID.randomUUID();
        Sacco newSacco = new Sacco(saccoId, command.saccoName(), sanitizedSchemaName, command.isUnion(), command.minShareRequirement());
        GlobalRole assignedGlobalRole = command.isUnion() ? GlobalRole.UNION_ADMIN : GlobalRole.SACCO_ADMIN;

        User adminUser = new User(UUID.randomUUID(), command.adminMsisdn(), saccoId, hashedPin, assignedGlobalRole);

        try {
            tenantProvisioningPort.provisionTenantSchema(sanitizedSchemaName);
            newSacco.activate();
            saccoRepositoryPort.save(newSacco);

            Role defaultAdminRole = new Role(SYSTEM_ADMIN_ROLE_ID, "ADMIN", true);
            adminUser.assignLocalRole(defaultAdminRole);
            userRepositoryPort.save(adminUser);

            // Trigger SMS notification welcoming SACCO onboard to Qershi Link platform
            String smsBody = String.format(
                    "Welcome to Qershi Link! Your admin account for %s is ready. Use PIN: %s to log in. Please change your PIN immediately after your first login.",
                    command.saccoName(),
                    rawPin
            );
            messagingPort.sendSms(command.adminMsisdn(), smsBody);

        } catch (Exception ex) {
            tenantProvisioningPort.dropTenantSchema(sanitizedSchemaName);
            throw new RuntimeException("Onboarding failed. Rollback triggered.", ex);
        }

        return new OnboardResult(newSacco.getSaccoId(), newSacco.getSchemaName(), adminUser.getUserId(), newSacco.getStatus().name(), newSacco.getCreatedAt());
    }
}