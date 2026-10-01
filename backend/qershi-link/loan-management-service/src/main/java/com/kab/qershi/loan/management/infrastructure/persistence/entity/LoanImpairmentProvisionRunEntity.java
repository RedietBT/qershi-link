package com.kab.qershi.loan.management.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA Entity for IFRS 9 / NBE monthly loan impairment provision run header.
 * One record per month-end provisioning run. Tracks GL debit/credit posting references.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "loan_impairment_provision_runs")
public class LoanImpairmentProvisionRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "run_id", nullable = false, updatable = false)
    private UUID runId;

    @Column(name = "business_date", nullable = false)
    private LocalDate businessDate;

    @Column(name = "run_type", nullable = false, length = 20)
    private String runType = "MONTH_END";

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "total_loans_evaluated", nullable = false)
    private Integer totalLoansEvaluated = 0;

    @Column(name = "total_portfolio_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalPortfolioBalance = BigDecimal.ZERO;

    // ----- Bucket balances -----
    @Column(name = "pass_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal passBalance = BigDecimal.ZERO;

    @Column(name = "special_mention_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal specialMentionBalance = BigDecimal.ZERO;

    @Column(name = "substandard_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal substandardBalance = BigDecimal.ZERO;

    @Column(name = "doubtful_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal doubtfulBalance = BigDecimal.ZERO;

    @Column(name = "loss_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal lossBalance = BigDecimal.ZERO;

    // ----- Required provision amounts -----
    @Column(name = "pass_provision", nullable = false, precision = 18, scale = 2)
    private BigDecimal passProvision = BigDecimal.ZERO;

    @Column(name = "special_mention_provision", nullable = false, precision = 18, scale = 2)
    private BigDecimal specialMentionProvision = BigDecimal.ZERO;

    @Column(name = "substandard_provision", nullable = false, precision = 18, scale = 2)
    private BigDecimal substandardProvision = BigDecimal.ZERO;

    @Column(name = "doubtful_provision", nullable = false, precision = 18, scale = 2)
    private BigDecimal doubtfulProvision = BigDecimal.ZERO;

    @Column(name = "loss_provision", nullable = false, precision = 18, scale = 2)
    private BigDecimal lossProvision = BigDecimal.ZERO;

    @Column(name = "total_provision_required", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalProvisionRequired = BigDecimal.ZERO;

    // ----- GL Posting -----
    @Column(name = "gl_debit_account", nullable = false, length = 20)
    private String glDebitAccount = "5030";

    @Column(name = "gl_credit_account", nullable = false, length = 20)
    private String glCreditAccount = "1039";

    @Column(name = "gl_posting_ref", length = 100)
    private String glPostingRef;

    @Column(name = "gl_posted_at")
    private OffsetDateTime glPostedAt;

    // ----- Audit -----
    @Column(name = "triggered_by", nullable = false, length = 100)
    private String triggeredBy = "SYSTEM_EOD";

    @Column(name = "triggered_by_user_id")
    private UUID triggeredByUserId;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public LoanImpairmentProvisionRunEntity() {}

    // ── Getters & Setters ───────────────────────────────────────────────────────

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
