package com.kab.qershi.account.infrastructure.rest.dto;

import com.kab.qershi.account.domain.model.GlAccountType;
import java.math.BigDecimal;

/**
 * Line item in a Trial Balance statement.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class TrialBalanceLineDto {

    private String glCode;
    private String accountName;
    private GlAccountType accountType;
    private String parentGlCode;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;

    public TrialBalanceLineDto() {}

    public TrialBalanceLineDto(String glCode, String accountName, GlAccountType accountType,
                               String parentGlCode, BigDecimal debitAmount, BigDecimal creditAmount) {
        this.glCode = glCode;
        this.accountName = accountName;
        this.accountType = accountType;
        this.parentGlCode = parentGlCode;
        this.debitAmount = debitAmount != null ? debitAmount : BigDecimal.ZERO;
        this.creditAmount = creditAmount != null ? creditAmount : BigDecimal.ZERO;
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

    public GlAccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(GlAccountType accountType) {
        this.accountType = accountType;
    }

    public String getParentGlCode() {
        return parentGlCode;
    }

    public void setParentGlCode(String parentGlCode) {
        this.parentGlCode = parentGlCode;
    }

    public BigDecimal getDebitAmount() {
        return debitAmount;
    }

    public void setDebitAmount(BigDecimal debitAmount) {
        this.debitAmount = debitAmount;
    }

    public BigDecimal getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(BigDecimal creditAmount) {
        this.creditAmount = creditAmount;
    }
}
