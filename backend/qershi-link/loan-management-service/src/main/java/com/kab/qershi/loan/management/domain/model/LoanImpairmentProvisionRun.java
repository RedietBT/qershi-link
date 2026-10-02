package com.kab.qershi.loan.management.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Pure Domain Aggregate representing an IFRS 9 / NBE Monthly Impairment Provision Run Header.
 * Tracks GL debit/credit posting references and portfolio-wide impairment balances.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class LoanImpairmentProvisionRun {

    private UUID runId;
    private LocalDate businessDate;
    private String runType;
    private String status;
    private Integer totalLoansEvaluated;
    private BigDecimal totalPortfolioBalance;
    private BigDecimal passBalance;
    private BigDecimal specialMentionBalance;
    private BigDecimal substandardBalance;
    private BigDecimal doubtfulBalance;
    private BigDecimal lossBalance;
    private BigDecimal passProvision;
    private BigDecimal specialMentionProvision;
    private BigDecimal substandardProvision;
    private BigDecimal doubtfulProvision;
    private BigDecimal lossProvision;
    private BigDecimal totalProvisionRequired;
    private String glDebitAccount;
    private String glCreditAccount;
    private String glPostingRef;
    private OffsetDateTime glPostedAt;
    private String triggeredBy;
    private UUID triggeredByUserId;
    private String errorMessage;
    private OffsetDateTime createdAt;
    private OffsetDateTime completedAt;

    public LoanImpairmentProvisionRun() {}

    public LoanImpairmentProvisionRun(UUID runId, LocalDate businessDate, String runType, String status,
                                     Integer totalLoansEvaluated, BigDecimal totalPortfolioBalance,
                                     BigDecimal passBalance, BigDecimal specialMentionBalance,
                                     BigDecimal substandardBalance, BigDecimal doubtfulBalance,
                                     BigDecimal lossBalance, BigDecimal passProvision,
                                     BigDecimal specialMentionProvision, BigDecimal substandardProvision,
                                     BigDecimal doubtfulProvision, BigDecimal lossProvision,
                                     BigDecimal totalProvisionRequired, String glDebitAccount,
                                     String glCreditAccount, String glPostingRef, OffsetDateTime glPostedAt,
                                     String triggeredBy, UUID triggeredByUserId, String errorMessage,
                                     OffsetDateTime createdAt, OffsetDateTime completedAt) {
        this.runId = runId;
        this.businessDate = businessDate;
        this.runType = runType;
        this.status = status;
        this.totalLoansEvaluated = totalLoansEvaluated;
        this.totalPortfolioBalance = totalPortfolioBalance;
        this.passBalance = passBalance;
        this.specialMentionBalance = specialMentionBalance;
        this.substandardBalance = substandardBalance;
        this.doubtfulBalance = doubtfulBalance;
        this.lossBalance = lossBalance;
        this.passProvision = passProvision;
        this.specialMentionProvision = specialMentionProvision;
        this.substandardProvision = substandardProvision;
        this.doubtfulProvision = doubtfulProvision;
        this.lossProvision = lossProvision;
        this.totalProvisionRequired = totalProvisionRequired;
        this.glDebitAccount = glDebitAccount;
        this.glCreditAccount = glCreditAccount;
        this.glPostingRef = glPostingRef;
        this.glPostedAt = glPostedAt;
        this.triggeredBy = triggeredBy;
        this.triggeredByUserId = triggeredByUserId;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public UUID getRunId() { return runId; }
    public void setRunId(UUID runId) { this.runId = runId; }

    public LocalDate getBusinessDate() { return businessDate; }
    public void setBusinessDate(LocalDate businessDate) { this.businessDate = businessDate; }

    public String getRunType() { return runType; }
    public void setRunType(String runType) { this.runType = runType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getTotalLoansEvaluated() { return totalLoansEvaluated; }
    public void setTotalLoansEvaluated(Integer totalLoansEvaluated) { this.totalLoansEvaluated = totalLoansEvaluated; }

    public BigDecimal getTotalPortfolioBalance() { return totalPortfolioBalance; }
    public void setTotalPortfolioBalance(BigDecimal totalPortfolioBalance) { this.totalPortfolioBalance = totalPortfolioBalance; }

    public BigDecimal getPassBalance() { return passBalance; }
    public void setPassBalance(BigDecimal passBalance) { this.passBalance = passBalance; }

    public BigDecimal getSpecialMentionBalance() { return specialMentionBalance; }
    public void setSpecialMentionBalance(BigDecimal specialMentionBalance) { this.specialMentionBalance = specialMentionBalance; }

    public BigDecimal getSubstandardBalance() { return substandardBalance; }
    public void setSubstandardBalance(BigDecimal substandardBalance) { this.substandardBalance = substandardBalance; }

    public BigDecimal getDoubtfulBalance() { return doubtfulBalance; }
    public void setDoubtfulBalance(BigDecimal doubtfulBalance) { this.doubtfulBalance = doubtfulBalance; }

    public BigDecimal getLossBalance() { return lossBalance; }
    public void setLossBalance(BigDecimal lossBalance) { this.lossBalance = lossBalance; }

    public BigDecimal getPassProvision() { return passProvision; }
    public void setPassProvision(BigDecimal passProvision) { this.passProvision = passProvision; }

    public BigDecimal getSpecialMentionProvision() { return specialMentionProvision; }
    public void setSpecialMentionProvision(BigDecimal specialMentionProvision) { this.specialMentionProvision = specialMentionProvision; }

    public BigDecimal getSubstandardProvision() { return substandardProvision; }
    public void setSubstandardProvision(BigDecimal substandardProvision) { this.substandardProvision = substandardProvision; }

    public BigDecimal getDoubtfulProvision() { return doubtfulProvision; }
    public void setDoubtfulProvision(BigDecimal doubtfulProvision) { this.doubtfulProvision = doubtfulProvision; }

    public BigDecimal getLossProvision() { return lossProvision; }
    public void setLossProvision(BigDecimal lossProvision) { this.lossProvision = lossProvision; }

    public BigDecimal getTotalProvisionRequired() { return totalProvisionRequired; }
    public void setTotalProvisionRequired(BigDecimal totalProvisionRequired) { this.totalProvisionRequired = totalProvisionRequired; }

    public String getGlDebitAccount() { return glDebitAccount; }
    public void setGlDebitAccount(String glDebitAccount) { this.glDebitAccount = glDebitAccount; }

    public String getGlCreditAccount() { return glCreditAccount; }
    public void setGlCreditAccount(String glCreditAccount) { this.glCreditAccount = glCreditAccount; }

    public String getGlPostingRef() { return glPostingRef; }
    public void setGlPostingRef(String glPostingRef) { this.glPostingRef = glPostingRef; }

    public OffsetDateTime getGlPostedAt() { return glPostedAt; }
    public void setGlPostedAt(OffsetDateTime glPostedAt) { this.glPostedAt = glPostedAt; }

    public String getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }

    public UUID getTriggeredByUserId() { return triggeredByUserId; }
    public void setTriggeredByUserId(UUID triggeredByUserId) { this.triggeredByUserId = triggeredByUserId; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
}
