package com.kab.qershi.pricing.infrastructure.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * Request DTO for configuring an amount bracket / slab within a tiered tariff.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class TariffSlabRequest {

    private int slabOrder = 1;

    @NotNull(message = "From amount is required")
    @PositiveOrZero(message = "From amount must be >= 0")
    private BigDecimal fromAmount = BigDecimal.ZERO;

    private BigDecimal toAmount; // null indicates unbounded / infinity

    @NotBlank(message = "Slab fee type is required (FLAT or PERCENTAGE)")
    private String feeType = "FLAT";

    @NotNull(message = "Slab fee value is required")
    @PositiveOrZero(message = "Slab fee value must be >= 0")
    private BigDecimal feeValue = BigDecimal.ZERO;

    private BigDecimal minFee;
    private BigDecimal maxFee;

    public TariffSlabRequest() {}

    public TariffSlabRequest(int slabOrder, BigDecimal fromAmount, BigDecimal toAmount,
                             String feeType, BigDecimal feeValue, BigDecimal minFee, BigDecimal maxFee) {
        this.slabOrder = slabOrder;
        this.fromAmount = fromAmount != null ? fromAmount : BigDecimal.ZERO;
        this.toAmount = toAmount;
        this.feeType = feeType != null ? feeType : "FLAT";
        this.feeValue = feeValue != null ? feeValue : BigDecimal.ZERO;
        this.minFee = minFee;
        this.maxFee = maxFee;
    }

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
