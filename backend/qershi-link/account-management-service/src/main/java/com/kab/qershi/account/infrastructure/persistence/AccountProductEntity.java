package com.kab.qershi.account.infrastructure.persistence;

import com.kab.qershi.account.domain.model.InterestPostingFrequency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity mapping account_products table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "account_products")
public class AccountProductEntity {

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_code", nullable = false, unique = true, length = 10)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "interest_rate_pa", nullable = false, precision = 7, scale = 4)
    private BigDecimal interestRatePa;

    @Enumerated(EnumType.STRING)
    @Column(name = "posting_frequency", nullable = false)
    private InterestPostingFrequency postingFrequency;

    @Column(name = "min_operating_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal minOperatingBalance;

    @Column(name = "min_monthly_contribution", nullable = false, precision = 19, scale = 4)
    private BigDecimal minMonthlyContribution;

    @Column(name = "term_period_months")
    private Integer termPeriodMonths;

    @Column(name = "early_withdrawal_penalty_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal earlyWithdrawalPenaltyPct;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public AccountProductEntity() {}

    public AccountProductEntity(UUID productId, String productCode, String productName, String category,
                                String currency, BigDecimal interestRatePa, InterestPostingFrequency postingFrequency,
                                BigDecimal minOperatingBalance, BigDecimal minMonthlyContribution,
                                Integer termPeriodMonths, BigDecimal earlyWithdrawalPenaltyPct, boolean active,
                                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.category = category;
        this.currency = currency;
        this.interestRatePa = interestRatePa;
        this.postingFrequency = postingFrequency;
        this.minOperatingBalance = minOperatingBalance;
        this.minMonthlyContribution = minMonthlyContribution;
        this.termPeriodMonths = termPeriodMonths;
        this.earlyWithdrawalPenaltyPct = earlyWithdrawalPenaltyPct;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
