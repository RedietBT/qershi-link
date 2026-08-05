package com.kab.qershi.transaction.infrastructure.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request payload DTO for over-the-counter Cash Withdrawal.
 */
public class WithdrawalRequest {

    @NotBlank(message = "Account number is required.")
    private String accountNo;

    @NotNull(message = "Withdrawal amount is required.")
    @DecimalMin(value = "0.01", message = "Withdrawal amount must be at least 0.01.")
    private BigDecimal amount;

    private String narration;

    public WithdrawalRequest() {}

    public WithdrawalRequest(String accountNo, BigDecimal amount, String narration) {
        this.accountNo = accountNo;
        this.amount = amount;
        this.narration = narration;
    }

    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getNarration() { return narration; }
    public void setNarration(String narration) { this.narration = narration; }
}
