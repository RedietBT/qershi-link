package com.kab.qershi.pricing.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity mapping tariff_slabs table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "tariff_slabs", indexes = {
        @Index(name = "idx_tariff_slabs_tariff_id", columnList = "tariff_id"),
        @Index(name = "idx_tariff_slabs_lookup", columnList = "tariff_id, from_amount, to_amount")
})
public class TariffSlabEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "slab_id", nullable = false, updatable = false)
    private UUID slabId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff_id", nullable = false)
    private TariffEntity tariff;

    @Column(name = "slab_order", nullable = false)
    private int slabOrder = 1;

    @Column(name = "from_amount", nullable = false, precision = 15, scale = 4)
    private BigDecimal fromAmount = BigDecimal.ZERO;

    @Column(name = "to_amount", precision = 15, scale = 4)
    private BigDecimal toAmount;

    @Column(name = "fee_type", nullable = false, length = 20)
    private String feeType = "FLAT";

    @Column(name = "fee_value", nullable = false, precision = 15, scale = 4)
    private BigDecimal feeValue = BigDecimal.ZERO;

    @Column(name = "min_fee", precision = 15, scale = 2)
    private BigDecimal minFee;

    @Column(name = "max_fee", precision = 15, scale = 2)
    private BigDecimal maxFee;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public TariffSlabEntity() {}

    public TariffSlabEntity(UUID slabId, TariffEntity tariff, int slabOrder, BigDecimal fromAmount,
                            BigDecimal toAmount, String feeType, BigDecimal feeValue,
                            BigDecimal minFee, BigDecimal maxFee) {
        this.slabId = slabId;
        this.tariff = tariff;
        this.slabOrder = slabOrder;
        this.fromAmount = fromAmount != null ? fromAmount : BigDecimal.ZERO;
        this.toAmount = toAmount;
        this.feeType = feeType != null ? feeType : "FLAT";
        this.feeValue = feeValue != null ? feeValue : BigDecimal.ZERO;
        this.minFee = minFee;
        this.maxFee = maxFee;
        this.createdAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public UUID getSlabId() { return slabId; }
    public void setSlabId(UUID slabId) { this.slabId = slabId; }

    public TariffEntity getTariff() { return tariff; }
    public void setTariff(TariffEntity tariff) { this.tariff = tariff; }

    public int getSlabOrder() { return slabOrder; }
    public void setSlabOrder(int slabOrder) { this.slabOrder = slabOrder; }

    public BigDecimal getFromAmount() { return fromAmount; }
    public void setFromAmount(BigDecimal fromAmount) { this.fromAmount = fromAmount; }

    public BigDecimal getToAmount() { return toAmount; }
    public void setToAmount(BigDecimal toAmount) { this.toAmount = toAmount; }

    public String getFeeType() { return feeType; }
    public void setFeeType(String feeType) { this.feeType = feeType; }

    public BigDecimal getFeeValue() { return feeValue; }
    public void setFeeValue(BigDecimal feeValue) { this.feeValue = feeValue; }

    public BigDecimal getMinFee() { return minFee; }
    public void setMinFee(BigDecimal minFee) { this.minFee = minFee; }

    public BigDecimal getMaxFee() { return maxFee; }
    public void setMaxFee(BigDecimal maxFee) { this.maxFee = maxFee; }

    public Instant getCreatedAt() { return createdAt; }
}
