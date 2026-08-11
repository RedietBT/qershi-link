package com.kab.qershi.auth.infrastructure.config;

import com.kab.qershi.auth.application.usecase.AuthenticationService;
import com.kab.qershi.auth.application.usecase.RbacManagementService;
import com.kab.qershi.auth.application.usecase.SaccoOnboardingService;
import com.kab.qershi.auth.application.usecase.SuperAdminService; // Assuming this name
import com.kab.qershi.auth.domain.ports.inbound.AuthenticationUseCase;
import com.kab.qershi.auth.domain.ports.inbound.RbacManagementUseCase;
import com.kab.qershi.auth.domain.ports.inbound.SaccoOnboardingUseCase;
import com.kab.qershi.auth.domain.ports.outbound.*;
import com.kab.qershi.auth.domain.service.IdentityDomainService;
import com.kab.qershi.auth.domain.service.RbacDomainService;
import com.kab.qershi.auth.infrastructure.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kab.qershi.auth.application.usecase.SystemAuditService;

@Configuration
public class BeanConfig {

    @Bean
    public IdentityDomainService identityDomainService(
            SaccoRepositoryPort saccoRepositoryPort,
            UserRepositoryPort userRepositoryPort) {
        return new IdentityDomainService(saccoRepositoryPort, userRepositoryPort);
    }

    @Bean
    public RbacDomainService rbacDomainService() {
        return new RbacDomainService();
    }

    @Bean
    public AuthenticationUseCase authenticationUseCase(
            UserRepositoryPort userRepositoryPort,
            SaccoRepositoryPort saccoRepositoryPort,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder,
            MessagingPort messagingPort,
            SystemAuditService systemAuditService) { // Ensure this is available in your config
        return new AuthenticationService(userRepositoryPort, saccoRepositoryPort, jwtTokenProvider, passwordEncoder, messagingPort, systemAuditService);
    }

    @Bean
    public SaccoOnboardingUseCase saccoOnboardingUseCase(
            SaccoRepositoryPort saccoRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            TenantProvisioningPort tenantProvisioningPort,
            IdentityDomainService identityDomainService,
            MessagingPort messagingPort,       // Added
            PasswordEncoder passwordEncoder) { // Added

        return new SaccoOnboardingService(
                saccoRepositoryPort,
                userRepositoryPort,
                tenantProvisioningPort,
                identityDomainService,
                messagingPort,
                passwordEncoder
        );
    }

    @Bean
    public SuperAdminService superAdminService(
            UserRepositoryPort userRepositoryPort,
            PasswordEncoder passwordEncoder,
            MessagingPort messagingPort) {
        return new SuperAdminService(userRepositoryPort, passwordEncoder, messagingPort);
    }

    @Bean
    public RbacManagementUseCase rbacManagementUseCase(RoleRepositoryPort roleRepositoryPort) {
        return new RbacManagementService(roleRepositoryPort);
    }
}