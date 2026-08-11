package com.kab.qershi.transaction.infrastructure.rest.dto;

import com.kab.qershi.transaction.infrastructure.persistence.TransactionAuditLogEntity;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * REST Response DTO for SACCO Financial Transaction Audit Logs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record TransactionAuditLogResponse(
        UUID logId,
        String transactionRef,
        String accountNo,
        UUID performedByUserId,
        String action,
        String details,
        OffsetDateTime createdAt
) {
    public static TransactionAuditLogResponse fromEntity(TransactionAuditLogEntity entity) {
        if (entity == null) {
            return null;
        }
        return new TransactionAuditLogResponse(
                entity.getLogId(),
                entity.getTransactionRef(),
                entity.getAccountNo(),
                entity.getPerformedByUserId(),
                entity.getAction(),
                entity.getDetails(),
                entity.getCreatedAt()
        );
    }
}
