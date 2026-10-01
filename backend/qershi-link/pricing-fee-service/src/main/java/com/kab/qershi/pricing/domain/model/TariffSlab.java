package com.kab.qershi.pricing.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Domain entity representing an amount bracket / slab in a tiered tariff schedule.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class TariffSlab {
    private UUID slabId;
    private UUID tariffId;
    private int slabOrder;
    private BigDecimal fromAmount;
    private BigDecimal toAmount; // null indicates unbounded / infinity
    private String feeType; // 'FLAT', 'PERCENTAGE'
    private BigDecimal feeValue;
    private BigDecimal minFee;
    private BigDecimal maxFee;

    public TariffSlab() {}

    public TariffSlab(UUID slabId, UUID tariffId, int slabOrder, BigDecimal fromAmount,
                      BigDecimal toAmount, String feeType, BigDecimal feeValue,
                      BigDecimal minFee, BigDecimal maxFee) {
        this.slabId = slabId;
        this.tariffId = tariffId;
        this.slabOrder = slabOrder;
        this.fromAmount = fromAmount != null ? fromAmount : BigDecimal.ZERO;
        this.toAmount = toAmount;
        this.feeType = feeType != null ? feeType : "FLAT";
        this.feeValue = feeValue != null ? feeValue : BigDecimal.ZERO;
        this.minFee = minFee;
        this.maxFee = maxFee;
    }

    public UUID getSlabId() { return slabId; }
    public void setSlabId(UUID slabId) { this.slabId = slabId; }

    public UUID getTariffId() { return tariffId; }
    public void setTariffId(UUID tariffId) { this.tariffId = tariffId; }

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
}
