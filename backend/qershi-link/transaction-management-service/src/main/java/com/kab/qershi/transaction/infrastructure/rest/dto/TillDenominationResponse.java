package com.kab.qershi.transaction.infrastructure.rest.dto;

import com.kab.qershi.transaction.domain.model.TillDenomination;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * REST Response DTO for banknote denomination breakdown.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public record TillDenominationResponse(
        UUID denominationId,
        UUID reconciliationId,
        BigDecimal denominationValue,
        int quantity,
        BigDecimal totalAmount,
        Instant createdAt
) {
    public static TillDenominationResponse fromDomain(TillDenomination denom) {
        if (denom == null) return null;
        return new TillDenominationResponse(
                denom.getDenominationId(),
                denom.getReconciliationId(),
                denom.getDenominationValue(),
                denom.getQuantity(),
                denom.getTotalAmount(),
                denom.getCreatedAt()
        );
    }
}
