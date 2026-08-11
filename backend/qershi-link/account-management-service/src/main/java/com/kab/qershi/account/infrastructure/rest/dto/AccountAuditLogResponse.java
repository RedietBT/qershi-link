package com.kab.qershi.account.infrastructure.rest.dto;

import com.kab.qershi.account.infrastructure.persistence.AccountAuditLogEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * REST Response DTO for SACCO Member Account Audit Logs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record AccountAuditLogResponse(
        UUID logId,
        String accountNo,
        UUID userId,
        UUID performedByUserId,
        String action,
        String fieldName,
        String oldValue,
        String newValue,
        OffsetDateTime createdAt
) {
    public static AccountAuditLogResponse fromEntity(AccountAuditLogEntity entity) {
        if (entity == null) {
            return null;
        }
        return new AccountAuditLogResponse(
                entity.getLogId(),
                entity.getAccountNo(),
                entity.getUserId(),
                entity.getPerformedByUserId(),
                entity.getAction(),
                entity.getFieldName(),
                entity.getOldValue(),
                entity.getNewValue(),
                entity.getCreatedAt()
        );
    }
}
