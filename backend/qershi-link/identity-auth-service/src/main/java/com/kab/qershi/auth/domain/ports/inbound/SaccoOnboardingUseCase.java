package com.kab.qershi.auth.domain.ports.inbound;

import com.kab.qershi.auth.domain.model.Sacco;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public interface SaccoOnboardingUseCase {

    // Input DTO
    record OnboardCommand(
            String saccoName,
            boolean isUnion,
            BigDecimal minShareRequirement,
            String adminMsisdn,
            String adminName,
            String region,
            Map<String, Object> metadata
    ) {}

    // Output Data Transfer object returning the created state
    record OnboardResult(
            UUID saccoId,
            String schemaName,
            UUID adminUserId,
            String status,
            java.time.Instant createdAt
    ) {}

    OnboardResult onboardSacco(OnboardCommand command);
}