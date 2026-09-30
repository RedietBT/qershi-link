package com.kab.qershi.account.infrastructure.persistence;

import com.kab.qershi.account.domain.model.GlAccountType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity mapping the General Ledger Chart of Accounts table (chart_of_accounts).
 * Represents individual parent and sub-ledger accounts in the double-entry accounting system.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "chart_of_accounts", indexes = {
        @Index(name = "idx_coa_parent_gl_code", columnList = "parent_gl_code"),
        @Index(name = "idx_coa_account_type", columnList = "account_type"),
        @Index(name = "idx_coa_status", columnList = "status")
})
public class ChartOfAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "account_id", updatable = false, nullable = false)
    private UUID accountId;

    @Column(name = "gl_code", nullable = false, unique = true, length = 50)
    private String glCode;

    @Column(name = "account_name", nullable = false, length = 150)
    private String accountName;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private GlAccountType accountType;

    @Column(name = "parent_gl_code", length = 50)
    private String parentGlCode;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "ETB";

    @Column(name = "balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "is_reconciled", nullable = false)
    private Boolean isReconciled = Boolean.TRUE;

    @Column(name = "allow_manual_journal", nullable = false)
    private Boolean allowManualJournal = Boolean.TRUE;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public ChartOfAccountEntity() {}

    public ChartOfAccountEntity(String glCode, String accountName, GlAccountType accountType,
                                String parentGlCode, String description) {
        this.glCode = glCode;
        this.accountName = accountName;
        this.accountType = accountType;
        this.parentGlCode = parentGlCode;
        this.description = description;
        this.balance = BigDecimal.ZERO;
        this.currency = "ETB";
        this.status = "ACTIVE";
        this.isReconciled = Boolean.TRUE;
        this.allowManualJournal = Boolean.TRUE;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
