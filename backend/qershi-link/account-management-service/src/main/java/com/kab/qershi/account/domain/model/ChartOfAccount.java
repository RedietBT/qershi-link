package com.kab.qershi.account.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Pure Domain Model representing a General Ledger Account in the Chart of Accounts.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class ChartOfAccount {

    private UUID accountId;
    private String glCode;
    private String accountName;
    private GlAccountType accountType;
    private String parentGlCode;
    private String currency;
    private BigDecimal balance;
    private Boolean isReconciled;
    private Boolean allowManualJournal;
    private String status;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public ChartOfAccount(UUID accountId, String glCode, String accountName, GlAccountType accountType,
                          String parentGlCode, String currency, BigDecimal balance,
                          Boolean isReconciled, Boolean allowManualJournal, String status,
                          String description, Instant createdAt, Instant updatedAt) {
        this.accountId = accountId != null ? accountId : UUID.randomUUID();
        this.glCode = glCode;
        this.accountName = accountName;
        this.accountType = accountType;
        this.parentGlCode = parentGlCode;
        this.currency = currency != null ? currency : "ETB";
        this.balance = balance != null ? balance : BigDecimal.ZERO;
        this.isReconciled = isReconciled != null ? isReconciled : Boolean.TRUE;
        this.allowManualJournal = allowManualJournal != null ? allowManualJournal : Boolean.TRUE;
        this.status = status != null ? status : "ACTIVE";
        this.description = description;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public void updateBalance(BigDecimal newBalance) {
        this.balance = newBalance;
        this.updatedAt = Instant.now();
    }

    public void updateDetails(String accountName, GlAccountType accountType, String parentGlCode, String description, Boolean allowManualJournal) {
        if (accountName != null && !accountName.isBlank()) this.accountName = accountName;
        if (accountType != null) this.accountType = accountType;
        this.parentGlCode = parentGlCode;
        this.description = description;
        if (allowManualJournal != null) this.allowManualJournal = allowManualJournal;
        this.updatedAt = Instant.now();
    }

    public UUID getAccountId() { return accountId; }
    public String getGlCode() { return glCode; }
    public String getAccountName() { return accountName; }
    public GlAccountType getAccountType() { return accountType; }
    public String getParentGlCode() { return parentGlCode; }
    public String getCurrency() { return currency; }
    public BigDecimal getBalance() { return balance; }
    public Boolean getIsReconciled() { return isReconciled; }
    public Boolean getAllowManualJournal() { return allowManualJournal; }
    public String getStatus() { return status; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
