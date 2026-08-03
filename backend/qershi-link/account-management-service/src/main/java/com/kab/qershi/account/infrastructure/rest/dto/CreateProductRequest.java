package com.kab.qershi.account.infrastructure.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new SACCO Deposit Product.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class CreateProductRequest {

    @NotBlank(message = "Product name is required.")
    @Size(max = 150, message = "Product name must not exceed 150 characters.")
    private String productName;

    @NotBlank(message = "Category is required.")
    @Size(max = 50, message = "Category must not exceed 50 characters.")
    private String category;

    @Size(max = 3, message = "Currency code must be 3 characters.")
    private String currency = "ETB";

    @DecimalMin(value = "0.0000", message = "Interest rate per annum cannot be negative.")
    private BigDecimal interestRatePa = BigDecimal.ZERO;

    private String postingFrequency = "MONTHLY";

    @DecimalMin(value = "0.0000", message = "Minimum operating balance cannot be negative.")
    private BigDecimal minOperatingBalance = BigDecimal.ZERO;

    @DecimalMin(value = "0.0000", message = "Minimum monthly contribution cannot be negative.")
    private BigDecimal minMonthlyContribution = BigDecimal.ZERO;

    private Integer termPeriodMonths;

    @DecimalMin(value = "0.00", message = "Early withdrawal penalty percentage cannot be negative.")
    private BigDecimal earlyWithdrawalPenaltyPct = BigDecimal.ZERO;

    public CreateProductRequest() {}

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getInterestRatePa() { return interestRatePa; }
    public void setInterestRatePa(BigDecimal interestRatePa) { this.interestRatePa = interestRatePa; }

    public String getPostingFrequency() { return postingFrequency; }
    public void setPostingFrequency(String postingFrequency) { this.postingFrequency = postingFrequency; }

    public BigDecimal getMinOperatingBalance() { return minOperatingBalance; }
    public void setMinOperatingBalance(BigDecimal minOperatingBalance) { this.minOperatingBalance = minOperatingBalance; }

    public BigDecimal getMinMonthlyContribution() { return minMonthlyContribution; }
    public void setMinMonthlyContribution(BigDecimal minMonthlyContribution) { this.minMonthlyContribution = minMonthlyContribution; }

    public Integer getTermPeriodMonths() { return termPeriodMonths; }
    public void setTermPeriodMonths(Integer termPeriodMonths) { this.termPeriodMonths = termPeriodMonths; }

    public BigDecimal getEarlyWithdrawalPenaltyPct() { return earlyWithdrawalPenaltyPct; }
    public void setEarlyWithdrawalPenaltyPct(BigDecimal earlyWithdrawalPenaltyPct) { this.earlyWithdrawalPenaltyPct = earlyWithdrawalPenaltyPct; }
}
