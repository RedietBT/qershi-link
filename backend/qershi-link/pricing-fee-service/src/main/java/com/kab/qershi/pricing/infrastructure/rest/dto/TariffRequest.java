package com.kab.qershi.pricing.infrastructure.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * Request payload for creating and updating transaction tariffs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class TariffRequest {

    @NotBlank(message = "Tariff code is required")
    private String tariffCode;

    @NotBlank(message = "Tariff name is required")
    private String tariffName;

    @NotBlank(message = "Transaction type is required")
    private String transactionType;

    @NotBlank(message = "Fee type is required (FLAT or PERCENTAGE)")
    private String feeType;

    @NotNull(message = "Fee value is required")
    @PositiveOrZero(message = "Fee value must be greater than or equal to 0")
    private BigDecimal feeValue;

    private BigDecimal minFee;
    private BigDecimal maxFee;

    private String feeGlCode = "4020";
    private String currency = "ETB";
    private Boolean isActive = true;
    private String description;

    public TariffRequest() {}

    public String getTariffCode() { return tariffCode; }
    public void setTariffCode(String tariffCode) { this.tariffCode = tariffCode; }

    public String getTariffName() { return tariffName; }
    public void setTariffName(String tariffName) { this.tariffName = tariffName; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getFeeType() { return feeType; }
    public void setFeeType(String feeType) { this.feeType = feeType; }

    public BigDecimal getFeeValue() { return feeValue; }
    public void setFeeValue(BigDecimal feeValue) { this.feeValue = feeValue; }

    public BigDecimal getMinFee() { return minFee; }
    public void setMinFee(BigDecimal minFee) { this.minFee = minFee; }

    public BigDecimal getMaxFee() { return maxFee; }
    public void setMaxFee(BigDecimal maxFee) { this.maxFee = maxFee; }

    public String getFeeGlCode() { return feeGlCode; }
    public void setFeeGlCode(String feeGlCode) { this.feeGlCode = feeGlCode; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
