package com.kab.qershi.account.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Pure domain model representing a dynamic SACCO deposit product configuration.
 * Modeled after enterprise CBS (Oracle FLEXCUBE / Temenos Product Factory).
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class AccountProduct {

    private UUID productId;
    private String productCode;
    private String productName;
    private String category;
    private String currency;
    private BigDecimal interestRatePa;
    private InterestPostingFrequency postingFrequency;
    private BigDecimal minOperatingBalance;
    private BigDecimal minMonthlyContribution;
    private Integer termPeriodMonths;
    private BigDecimal earlyWithdrawalPenaltyPct;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AccountProduct() {
        this.currency = "ETB";
        this.interestRatePa = BigDecimal.ZERO;
        this.postingFrequency = InterestPostingFrequency.MONTHLY;
        this.minOperatingBalance = BigDecimal.ZERO;
        this.minMonthlyContribution = BigDecimal.ZERO;
        this.earlyWithdrawalPenaltyPct = BigDecimal.ZERO;
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public AccountProduct(UUID productId, String productCode, String productName, String category,
                          String currency, BigDecimal interestRatePa, InterestPostingFrequency postingFrequency,
                          BigDecimal minOperatingBalance, BigDecimal minMonthlyContribution,
                          Integer termPeriodMonths, BigDecimal earlyWithdrawalPenaltyPct, boolean active,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.category = category;
        this.currency = currency != null ? currency : "ETB";
        this.interestRatePa = interestRatePa != null ? interestRatePa : BigDecimal.ZERO;
        this.postingFrequency = postingFrequency != null ? postingFrequency : InterestPostingFrequency.MONTHLY;
        this.minOperatingBalance = minOperatingBalance != null ? minOperatingBalance : BigDecimal.ZERO;
        this.minMonthlyContribution = minMonthlyContribution != null ? minMonthlyContribution : BigDecimal.ZERO;
        this.termPeriodMonths = termPeriodMonths;
        this.earlyWithdrawalPenaltyPct = earlyWithdrawalPenaltyPct != null ? earlyWithdrawalPenaltyPct : BigDecimal.ZERO;
        this.active = active;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    public boolean isFixedTerm() {
        return termPeriodMonths != null && termPeriodMonths > 0;
    }

    public void validateDepositAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be strictly greater than zero.");
        }
        if (minMonthlyContribution != null && minMonthlyContribution.compareTo(BigDecimal.ZERO) > 0) {
            if (amount.compareTo(minMonthlyContribution) < 0) {
                throw new IllegalArgumentException("Deposit amount (" + amount + ") is less than the minimum required contribution (" + minMonthlyContribution + ") for product " + productCode);
            }
        }
    }

    // Getters and Setters
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getInterestRatePa() { return interestRatePa; }
    public void setInterestRatePa(BigDecimal interestRatePa) { this.interestRatePa = interestRatePa; }

    public InterestPostingFrequency getPostingFrequency() { return postingFrequency; }
    public void setPostingFrequency(InterestPostingFrequency postingFrequency) { this.postingFrequency = postingFrequency; }

    public BigDecimal getMinOperatingBalance() { return minOperatingBalance; }
    public void setMinOperatingBalance(BigDecimal minOperatingBalance) { this.minOperatingBalance = minOperatingBalance; }

    public BigDecimal getMinMonthlyContribution() { return minMonthlyContribution; }
    public void setMinMonthlyContribution(BigDecimal minMonthlyContribution) { this.minMonthlyContribution = minMonthlyContribution; }

    public Integer getTermPeriodMonths() { return termPeriodMonths; }
    public void setTermPeriodMonths(Integer termPeriodMonths) { this.termPeriodMonths = termPeriodMonths; }

    public BigDecimal getEarlyWithdrawalPenaltyPct() { return earlyWithdrawalPenaltyPct; }
    public void setEarlyWithdrawalPenaltyPct(BigDecimal earlyWithdrawalPenaltyPct) { this.earlyWithdrawalPenaltyPct = earlyWithdrawalPenaltyPct; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
