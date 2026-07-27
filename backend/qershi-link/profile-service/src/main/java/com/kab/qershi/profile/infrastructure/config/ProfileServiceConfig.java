package com.kab.qershi.profile.infrastructure.config;

import com.kab.qershi.profile.application.usecase.KycVerificationService;
import com.kab.qershi.profile.application.usecase.NextOfKinService;
import com.kab.qershi.profile.application.usecase.ProfileManagementService;
import com.kab.qershi.profile.domain.ports.inbound.KycVerificationUseCase;
import com.kab.qershi.profile.domain.ports.inbound.NextOfKinUseCase;
import com.kab.qershi.profile.domain.ports.inbound.ProfileManagementUseCase;
import com.kab.qershi.profile.domain.ports.outbound.KycRepositoryPort;
import com.kab.qershi.profile.domain.ports.outbound.NextOfKinRepositoryPort;
import com.kab.qershi.profile.domain.ports.outbound.ProfileRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Configuration wiring Application Use Case Services with their Outbound Persistence Adapters.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Configuration
public class ProfileServiceConfig {

    @Bean
    public ProfileManagementUseCase profileManagementUseCase(ProfileRepositoryPort profileRepository,
                                                             KycRepositoryPort kycRepository,
                                                             NextOfKinRepositoryPort nextOfKinRepository) {
        return new ProfileManagementService(profileRepository, kycRepository, nextOfKinRepository);
    }

    @Bean
    public KycVerificationUseCase kycVerificationUseCase(KycRepositoryPort kycRepository,
                                                         ProfileRepositoryPort profileRepository) {
        return new KycVerificationService(kycRepository, profileRepository);
    }

    @Bean
    public NextOfKinUseCase nextOfKinUseCase(NextOfKinRepositoryPort nextOfKinRepository,
                                             ProfileRepositoryPort profileRepository) {
        return new NextOfKinService(nextOfKinRepository, profileRepository);
    }
}
