package com.kab.qershi.account.infrastructure.rest.dto;

import com.kab.qershi.account.domain.model.GlAccountType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Hierarchical tree node representation of a General Ledger Account.
 * Contains direct properties, aggregated rollup balance from descendants, and child sub-accounts.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class ChartOfAccountNodeDto {

    private UUID accountId;
    private String glCode;
    private String accountName;
    private GlAccountType accountType;
    private String parentGlCode;
    private BigDecimal balance;
    private BigDecimal rollupBalance;
    private String status;
    private Boolean isReconciled;
    private Boolean allowManualJournal;
    private String description;
    private List<ChartOfAccountNodeDto> children = new ArrayList<>();

    public ChartOfAccountNodeDto() {}

    public ChartOfAccountNodeDto(UUID accountId, String glCode, String accountName,
                                 GlAccountType accountType, String parentGlCode,
                                 BigDecimal balance, String status, Boolean isReconciled,
                                 Boolean allowManualJournal, String description) {
        this.accountId = accountId;
        this.glCode = glCode;
        this.accountName = accountName;
        this.accountType = accountType;
        this.parentGlCode = parentGlCode;
        this.balance = balance != null ? balance : BigDecimal.ZERO;
        this.rollupBalance = this.balance;
        this.status = status;
        this.isReconciled = isReconciled;
        this.allowManualJournal = allowManualJournal;
        this.description = description;
        this.children = new ArrayList<>();
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getRollupBalance() {
        return rollupBalance;
    }

    public void setRollupBalance(BigDecimal rollupBalance) {
        this.rollupBalance = rollupBalance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsReconciled() {
        return isReconciled;
    }

    public void setIsReconciled(Boolean reconciled) {
        isReconciled = reconciled;
    }

    public Boolean getAllowManualJournal() {
        return allowManualJournal;
    }

    public void setAllowManualJournal(Boolean allowManualJournal) {
        this.allowManualJournal = allowManualJournal;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ChartOfAccountNodeDto> getChildren() {
        return children;
    }

    public void setChildren(List<ChartOfAccountNodeDto> children) {
        this.children = children;
    }
}
