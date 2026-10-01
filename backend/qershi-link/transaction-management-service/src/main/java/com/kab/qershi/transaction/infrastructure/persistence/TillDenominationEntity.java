package com.kab.qershi.transaction.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity mapping till_denominations table.
 * Records the physical banknote denomination quantities during blind balancing.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "till_denominations", indexes = {
        @Index(name = "idx_till_denom_rec", columnList = "reconciliation_id")
})
public class TillDenominationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "denomination_id", nullable = false, updatable = false)
    private UUID denominationId;

    @Column(name = "reconciliation_id", nullable = false)
    private UUID reconciliationId;

    @Column(name = "denomination_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal denominationValue;

    @Column(name = "quantity", nullable = false)
    private int quantity = 0;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public TillDenominationEntity() {}

    public TillDenominationEntity(UUID reconciliationId, BigDecimal denominationValue, int quantity, BigDecimal totalAmount) {
        this.reconciliationId = reconciliationId;
        this.denominationValue = denominationValue;
        this.quantity = quantity;
        this.totalAmount = totalAmount != null ? totalAmount : denominationValue.multiply(BigDecimal.valueOf(quantity));
        this.createdAt = Instant.now();
    }

    public UUID getDenominationId() { return denominationId; }
    public void setDenominationId(UUID denominationId) { this.denominationId = denominationId; }

    public UUID getReconciliationId() { return reconciliationId; }
    public void setReconciliationId(UUID reconciliationId) { this.reconciliationId = reconciliationId; }

    public BigDecimal getDenominationValue() { return denominationValue; }
    public void setDenominationValue(BigDecimal denominationValue) { this.denominationValue = denominationValue; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Instant getCreatedAt() { return createdAt; }
}
