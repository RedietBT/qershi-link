package com.kab.qershi.account.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA Entity mapping product_maker_checker_rules table.
 * Stores per-product transaction limits, max balance storing limits, and Four-Eyes overrides.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "product_maker_checker_rules")
public class ProductMakerCheckerRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rule_id", nullable = false, updatable = false)
    private UUID ruleId;

    @Column(name = "product_code", nullable = false, unique = true, length = 20)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "min_operating_balance", nullable = false, precision = 18, scale = 4)
    private BigDecimal minOperatingBalance = new BigDecimal("100.0000");

    @Column(name = "max_balance_limit", nullable = false, precision = 18, scale = 4)
    private BigDecimal maxBalanceLimit = new BigDecimal("1000000.0000");

    @Column(name = "single_withdrawal_limit", nullable = false, precision = 18, scale = 4)
    private BigDecimal singleWithdrawalLimit = new BigDecimal("50000.0000");

    @Column(name = "daily_withdrawal_limit", nullable = false, precision = 18, scale = 4)
    private BigDecimal dailyWithdrawalLimit = new BigDecimal("150000.0000");

    @Column(name = "enable_maker_checker", nullable = false)
    private boolean enableMakerChecker = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public ProductMakerCheckerRuleEntity() {}

    public ProductMakerCheckerRuleEntity(String productCode, String productName, String category,
                                        BigDecimal minOperatingBalance, BigDecimal maxBalanceLimit,
                                        BigDecimal singleWithdrawalLimit, BigDecimal dailyWithdrawalLimit,
                                        boolean enableMakerChecker) {
        this.productCode = productCode;
        this.productName = productName;
        this.category = category;
        this.minOperatingBalance = minOperatingBalance;
        this.maxBalanceLimit = maxBalanceLimit;
        this.singleWithdrawalLimit = singleWithdrawalLimit;
        this.dailyWithdrawalLimit = dailyWithdrawalLimit;
        this.enableMakerChecker = enableMakerChecker;
    }

    public UUID getRuleId() { return ruleId; }
    public void setRuleId(UUID ruleId) { this.ruleId = ruleId; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getMinOperatingBalance() { return minOperatingBalance; }
    public void setMinOperatingBalance(BigDecimal minOperatingBalance) { this.minOperatingBalance = minOperatingBalance; }

    public BigDecimal getMaxBalanceLimit() { return maxBalanceLimit; }
    public void setMaxBalanceLimit(BigDecimal maxBalanceLimit) { this.maxBalanceLimit = maxBalanceLimit; }

    public BigDecimal getSingleWithdrawalLimit() { return singleWithdrawalLimit; }
    public void setSingleWithdrawalLimit(BigDecimal singleWithdrawalLimit) { this.singleWithdrawalLimit = singleWithdrawalLimit; }

    public BigDecimal getDailyWithdrawalLimit() { return dailyWithdrawalLimit; }
    public void setDailyWithdrawalLimit(BigDecimal dailyWithdrawalLimit) { this.dailyWithdrawalLimit = dailyWithdrawalLimit; }

    public boolean isEnableMakerChecker() { return enableMakerChecker; }
    public void setEnableMakerChecker(boolean enableMakerChecker) { this.enableMakerChecker = enableMakerChecker; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
