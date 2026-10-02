package com.kab.qershi.transaction.infrastructure.rest.dto;

import com.kab.qershi.transaction.domain.model.TillCashReconciliation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * REST Response DTO for end-of-day till cash reconciliation.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record TillCashReconciliationResponse(
        UUID reconciliationId,
        UUID tillId,
        UUID tellerUserId,
        BigDecimal electronicCashBalance,
        BigDecimal physicalCashCounted,
        BigDecimal cashVariance,
        int notes200Count,
        int notes100Count,
        int notes50Count,
        int notes10Count,
        int notes5Count,
        BigDecimal coinsAmount,
        String varianceType,
        BigDecimal varianceAmount,
        String varianceGlCode,
        UUID journalEntryId,
        String status,
        UUID supervisorApprovedBy,
        Instant supervisorApprovedAt,
        String supervisorNotes,
        String reconciliationNotes,
        Instant createdAt
) {
    public static TillCashReconciliationResponse fromDomain(TillCashReconciliation rec) {
        if (rec == null) return null;
        return new TillCashReconciliationResponse(
                rec.getReconciliationId(),
                rec.getTillId(),
                rec.getTellerUserId(),
                rec.getElectronicCashBalance(),
                rec.getPhysicalCashCounted(),
                rec.getCashVariance(),
                rec.getNotes200Count(),
                rec.getNotes100Count(),
                rec.getNotes50Count(),
                rec.getNotes10Count(),
                rec.getNotes5Count(),
                rec.getCoinsAmount(),
                rec.getVarianceType(),
                rec.getVarianceAmount(),
                rec.getVarianceGlCode(),
                rec.getJournalEntryId(),
                rec.getStatus(),
                rec.getSupervisorApprovedBy(),
                rec.getSupervisorApprovedAt(),
                rec.getSupervisorNotes(),
                rec.getReconciliationNotes(),
                rec.getCreatedAt()
        );
    }
}
