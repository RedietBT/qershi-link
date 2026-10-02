package com.kab.qershi.transaction.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Pure domain model representing an itemized physical banknote denomination quantity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class TillDenomination {

    private UUID denominationId;
    private UUID reconciliationId;
    private BigDecimal denominationValue;
    private int quantity;
    private BigDecimal totalAmount;
    private Instant createdAt;

    public TillDenomination() {
        this.quantity = 0;
        this.totalAmount = BigDecimal.ZERO;
        this.createdAt = Instant.now();
    }

    public TillDenomination(UUID denominationId, UUID reconciliationId,
                            BigDecimal denominationValue, int quantity,
                            BigDecimal totalAmount, Instant createdAt) {
        this.denominationId = denominationId;
        this.reconciliationId = reconciliationId;
        this.denominationValue = denominationValue;
        this.quantity = quantity;
        this.totalAmount = totalAmount != null ? totalAmount :
                (denominationValue != null ? denominationValue.multiply(BigDecimal.valueOf(quantity)) : BigDecimal.ZERO);
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public UUID getDenominationId() {
        return denominationId;
    }

    public void setDenominationId(UUID denominationId) {
        this.denominationId = denominationId;
    }

    public UUID getReconciliationId() {
        return reconciliationId;
    }

    public void setReconciliationId(UUID reconciliationId) {
        this.reconciliationId = reconciliationId;
    }

    public BigDecimal getDenominationValue() {
        return denominationValue;
    }

    public void setDenominationValue(BigDecimal denominationValue) {
        this.denominationValue = denominationValue;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
