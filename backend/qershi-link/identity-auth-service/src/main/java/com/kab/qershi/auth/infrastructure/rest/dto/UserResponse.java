package com.kab.qershi.auth.infrastructure.rest.dto;

import com.kab.qershi.auth.domain.model.GlobalRole;
import com.kab.qershi.auth.domain.model.UserStatus;
import com.kab.qershi.auth.infrastructure.persistence.UserEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * REST Response DTO exposing safe user account fields.
 * Explicitly excludes sensitive fields such as credential hashes and failed login counters.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record UserResponse(
        UUID userId,
        String msisdn,
        UUID saccoId,
        GlobalRole globalRole,
        UserStatus status,
        Instant lastLoginAt
) {
    public static UserResponse fromEntity(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return new UserResponse(
                entity.getUserId(),
                entity.getMsisdn(),
                entity.getSaccoId(),
                entity.getGlobalRole(),
                entity.getStatus(),
                entity.getLastLoginAt()
        );
    }
}
