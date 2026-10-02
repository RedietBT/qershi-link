package com.kab.qershi.transaction.infrastructure.rest.dto;

import com.kab.qershi.transaction.domain.model.TellerTill;
import com.kab.qershi.transaction.domain.model.TillStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * REST Response DTO for Teller Drawer (Till) status and balance.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record TellerTillResponse(
        UUID tillId,
        UUID branchId,
        String branchCode,
        UUID tellerUserId,
        String tillName,
        String tillGlCode,
        BigDecimal openingCash,
        BigDecimal currentCash,
        BigDecimal maxCashLimit,
        TillStatus status,
        Instant openedAt,
        Instant closedAt,
        Instant createdAt
) {
    public static TellerTillResponse fromDomain(TellerTill till) {
        if (till == null) return null;
        return new TellerTillResponse(
                till.getTillId(),
                till.getBranchId(),
                till.getBranchCode(),
                till.getTellerUserId(),
                till.getTillName(),
                till.getTillGlCode(),
                till.getOpeningCash(),
                till.getCurrentCash(),
                till.getMaxCashLimit(),
                till.getStatus(),
                till.getOpenedAt(),
                till.getClosedAt(),
                till.getCreatedAt()
        );
    }
}
