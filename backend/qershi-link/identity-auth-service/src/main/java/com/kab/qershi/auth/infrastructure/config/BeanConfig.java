package com.kab.qershi.auth.infrastructure.config;

import com.kab.qershi.auth.domain.ports.outbound.SaccoRepositoryPort;
import com.kab.qershi.auth.domain.ports.outbound.UserRepositoryPort;
import com.kab.qershi.auth.domain.service.IdentityDomainService;
import com.kab.qershi.auth.domain.service.RbacDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration providing pure domain service beans to the IoC container.
 *
 * @author KAB Digital Solution PLC
 * @version 2.0.0
 */
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
}