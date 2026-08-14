package com.kab.qershi.profile.infrastructure.rest.dto;

import com.kab.qershi.profile.infrastructure.persistence.ProfileAuditLogEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * REST Response DTO for SACCO Member Profile Audit Logs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record ProfileAuditLogResponse(
        UUID logId,
        UUID userId,
        String primaryPhone,
        UUID performedByUserId,
        String action,
        String fieldName,
        String oldValue,
        String newValue,
        OffsetDateTime createdAt
) {
    public static ProfileAuditLogResponse fromEntity(ProfileAuditLogEntity entity) {
        if (entity == null) {
            return null;
        }
        String phone = null;
        if (entity.getAddress() != null) {
            phone = entity.getAddress().getPrimaryPhone();
        }

        return new ProfileAuditLogResponse(
                entity.getLogId(),
                entity.getUserId(),
                phone,
                entity.getPerformedByUserId(),
                entity.getAction(),
                entity.getFieldName(),
                entity.getOldValue(),
                entity.getNewValue(),
                entity.getCreatedAt()
        );
    }
}
