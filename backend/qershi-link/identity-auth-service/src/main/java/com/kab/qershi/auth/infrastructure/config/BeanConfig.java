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
import com.kab.qershi.auth.infrastructure.adapters.AfroMessageAdapter;
import com.kab.qershi.auth.infrastructure.security.JwtTokenProvider; // Import this
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

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
            MessagingPort messagingPort) { // Ensure this is available in your config
        return new AuthenticationService(userRepositoryPort, saccoRepositoryPort, jwtTokenProvider, passwordEncoder, messagingPort);
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

    // 🛠️ ADDED: Injection for your Super Admin registration
    @Bean
    public SuperAdminService superAdminService(
            UserRepositoryPort userRepositoryPort,
            PasswordEncoder passwordEncoder,
            MessagingPort messagingPort) {
        return new SuperAdminService(userRepositoryPort, passwordEncoder, messagingPort);
    }

    // Note: Ensure your JwtTokenProvider is also managed as a bean
    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        return new JwtTokenProvider();
    }

    @Bean
    public MessagingPort messagingPort() {
        return new AfroMessageAdapter();
    }

    @Bean
    public RbacManagementUseCase rbacManagementUseCase(RoleRepositoryPort roleRepositoryPort) {
        return new RbacManagementService(roleRepositoryPort);
    }
}