package com.kab.qershi.auth.infrastructure.rest.dto;

import com.kab.qershi.auth.infrastructure.persistence.AuditLogEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * REST Response DTO exposing system audit log records for SUPER_ADMIN.
 * Excludes sensitive internal fields while providing full operational visibility.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record AuditLogResponse(
        UUID logId,
        UUID userId,
        UUID saccoId,
        String action,
        String resourceAffected,
        String status,
        String ipAddress,
        String details,
        OffsetDateTime timestamp
) {
    public static AuditLogResponse fromEntity(AuditLogEntity entity) {
        if (entity == null) {
            return null;
        }
        return new AuditLogResponse(
                entity.getLogId(),
                entity.getUserId(),
                entity.getSaccoId(),
                entity.getAction(),
                entity.getResourceAffected(),
                entity.getStatus(),
                entity.getIpAddress(),
                entity.getDetails(),
                entity.getTimestamp()
        );
    }
}
