package com.kab.qershi.account.infrastructure.rest.dto;

import java.math.BigDecimal;

/**
 * Common line item DTO for Financial Statement sections (Assets, Liabilities, Equity, Revenue, Expense).
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class ReportSectionLineDto {

    private String glCode;
    private String accountName;
    private BigDecimal amount;

    public ReportSectionLineDto() {}

    public ReportSectionLineDto(String glCode, String accountName, BigDecimal amount) {
        this.glCode = glCode;
        this.accountName = accountName;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
    }

    public String getGlCode() {
        return glCode;
    }

    public void setGlCode(String glCode) {
        this.glCode = glCode;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
