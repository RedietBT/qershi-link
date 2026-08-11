package com.kab.qershi.loan.management.infrastructure.rest.dto;

import com.kab.qershi.loan.management.infrastructure.persistence.entity.LoanAuditLogEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * REST Response DTO for SACCO Loan Audit Logs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record LoanAuditLogResponse(
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
    public static LoanAuditLogResponse fromEntity(LoanAuditLogEntity entity) {
        if (entity == null) {
            return null;
        }
        return new LoanAuditLogResponse(
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
