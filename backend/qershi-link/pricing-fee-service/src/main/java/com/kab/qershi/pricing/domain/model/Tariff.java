package com.kab.qershi.pricing.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing a transaction tariff schedule.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class Tariff {
    private UUID tariffId;
    private String tariffCode;
    private String tariffName;
    private String transactionType;
    private String feeType; // 'FLAT', 'PERCENTAGE'
    private BigDecimal feeValue;
    private BigDecimal minFee;
    private BigDecimal maxFee;
    private String feeGlCode;
    private String currency;
    private boolean active;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public Tariff() {}

    public Tariff(UUID tariffId, String tariffCode, String tariffName, String transactionType,
                  String feeType, BigDecimal feeValue, BigDecimal minFee, BigDecimal maxFee,
                  String feeGlCode, String currency, boolean active, String description) {
        this.tariffId = tariffId;
        this.tariffCode = tariffCode;
        this.tariffName = tariffName;
        this.transactionType = transactionType;
        this.feeType = feeType != null ? feeType : "FLAT";
        this.feeValue = feeValue != null ? feeValue : BigDecimal.ZERO;
        this.minFee = minFee;
        this.maxFee = maxFee;
        this.feeGlCode = feeGlCode != null ? feeGlCode : "4020";
        this.currency = currency != null ? currency : "ETB";
        this.active = active;
        this.description = description;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getTariffId() { return tariffId; }
    public void setTariffId(UUID tariffId) { this.tariffId = tariffId; }

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

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
