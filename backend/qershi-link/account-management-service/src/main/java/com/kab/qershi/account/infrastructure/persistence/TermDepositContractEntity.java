package com.kab.qershi.account.infrastructure.persistence;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA Entity mapping the term_deposit_contracts table.
 * Represents a Fixed Term Deposit (FD) contract for a member.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "term_deposit_contracts")
public class TermDepositContractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "contract_id", nullable = false, updatable = false)
    private UUID contractId;

    @Column(name = "contract_no", nullable = false, unique = true, length = 50)
    private String contractNo;

    @Column(name = "account_no", nullable = false, length = 50)
    private String accountNo;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "sacco_code", nullable = false, length = 20)
    private String saccoCode;

    @Column(name = "branch_code", nullable = false, length = 20)
    private String branchCode;

    @Column(name = "principal_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal principalAmount;

    @Column(name = "tenor_months", nullable = false)
    private Integer tenorMonths;

    @Column(name = "agreed_interest_rate_pa", nullable = false, precision = 7, scale = 4)
    private BigDecimal agreedInterestRatePa;

    @Column(name = "early_break_penalty_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal earlyBreakPenaltyPct = new BigDecimal("2.00");

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;

    @Column(name = "closed_date")
    private LocalDate closedDate;

    @Column(name = "accrued_interest", nullable = false, precision = 19, scale = 4)
    private BigDecimal accruedInterest = BigDecimal.ZERO;

    @Column(name = "capitalized_interest", nullable = false, precision = 19, scale = 4)
    private BigDecimal capitalizedInterest = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 30,
            columnDefinition = "term_deposit_status")
    @Enumerated(EnumType.STRING)
    private TermDepositStatus status = TermDepositStatus.PENDING_APPROVAL;

    @Column(name = "auto_rollover", nullable = false)
    private Boolean autoRollover = false;

    @Column(name = "rollover_tenor_months")
    private Integer rolloverTenorMonths;

    @Column(name = "penalty_amount", precision = 19, scale = 4)
    private BigDecimal penaltyAmount;

    @Column(name = "net_payout_amount", precision = 19, scale = 4)
    private BigDecimal netPayoutAmount;

    @Column(name = "gl_debit_account", nullable = false, length = 20)
    private String glDebitAccount = "2060";

    @Column(name = "gl_credit_account", nullable = false, length = 20)
    private String glCreditAccount = "1010";

    @Column(name = "opening_gl_ref", length = 100)
    private String openingGlRef;

    @Column(name = "closing_gl_ref", length = 100)
    private String closingGlRef;

    // Maker-Checker
    @Column(name = "maker_user_id", nullable = false)
    private UUID makerUserId;

    @Column(name = "maker_notes", columnDefinition = "TEXT")
    private String makerNotes;

    @Column(name = "checker_user_id")
    private UUID checkerUserId;

    @Column(name = "checker_notes", columnDefinition = "TEXT")
    private String checkerNotes;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public TermDepositContractEntity() {}

    // ── Enum ─────────────────────────────────────────────────────────────────
    public enum TermDepositStatus {
        ACTIVE, MATURED, ROLLED_OVER, CLOSED_NORMAL, CLOSED_EARLY, PENDING_APPROVAL
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public UUID getContractId() { return contractId; }
    public void setContractId(UUID contractId) { this.contractId = contractId; }

    public String getContractNo() { return contractNo; }
    public void setContractNo(String contractNo) { this.contractNo = contractNo; }

    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getSaccoCode() { return saccoCode; }
    public void setSaccoCode(String saccoCode) { this.saccoCode = saccoCode; }

    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }

    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal principalAmount) { this.principalAmount = principalAmount; }

    public Integer getTenorMonths() { return tenorMonths; }
    public void setTenorMonths(Integer tenorMonths) { this.tenorMonths = tenorMonths; }

    public BigDecimal getAgreedInterestRatePa() { return agreedInterestRatePa; }
    public void setAgreedInterestRatePa(BigDecimal agreedInterestRatePa) { this.agreedInterestRatePa = agreedInterestRatePa; }

    public BigDecimal getEarlyBreakPenaltyPct() { return earlyBreakPenaltyPct; }
    public void setEarlyBreakPenaltyPct(BigDecimal earlyBreakPenaltyPct) { this.earlyBreakPenaltyPct = earlyBreakPenaltyPct; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getMaturityDate() { return maturityDate; }
    public void setMaturityDate(LocalDate maturityDate) { this.maturityDate = maturityDate; }

    public LocalDate getClosedDate() { return closedDate; }
    public void setClosedDate(LocalDate closedDate) { this.closedDate = closedDate; }

    public BigDecimal getAccruedInterest() { return accruedInterest; }
    public void setAccruedInterest(BigDecimal accruedInterest) { this.accruedInterest = accruedInterest; }

    public BigDecimal getCapitalizedInterest() { return capitalizedInterest; }
    public void setCapitalizedInterest(BigDecimal capitalizedInterest) { this.capitalizedInterest = capitalizedInterest; }

    public TermDepositStatus getStatus() { return status; }
    public void setStatus(TermDepositStatus status) { this.status = status; }

    public Boolean getAutoRollover() { return autoRollover; }
    public void setAutoRollover(Boolean autoRollover) { this.autoRollover = autoRollover; }

    public Integer getRolloverTenorMonths() { return rolloverTenorMonths; }
    public void setRolloverTenorMonths(Integer rolloverTenorMonths) { this.rolloverTenorMonths = rolloverTenorMonths; }

    public BigDecimal getPenaltyAmount() { return penaltyAmount; }
    public void setPenaltyAmount(BigDecimal penaltyAmount) { this.penaltyAmount = penaltyAmount; }

    public BigDecimal getNetPayoutAmount() { return netPayoutAmount; }
    public void setNetPayoutAmount(BigDecimal netPayoutAmount) { this.netPayoutAmount = netPayoutAmount; }

    public String getGlDebitAccount() { return glDebitAccount; }
    public void setGlDebitAccount(String glDebitAccount) { this.glDebitAccount = glDebitAccount; }

    public String getGlCreditAccount() { return glCreditAccount; }
    public void setGlCreditAccount(String glCreditAccount) { this.glCreditAccount = glCreditAccount; }

    public String getOpeningGlRef() { return openingGlRef; }
    public void setOpeningGlRef(String openingGlRef) { this.openingGlRef = openingGlRef; }

    public String getClosingGlRef() { return closingGlRef; }
    public void setClosingGlRef(String closingGlRef) { this.closingGlRef = closingGlRef; }

    public UUID getMakerUserId() { return makerUserId; }
    public void setMakerUserId(UUID makerUserId) { this.makerUserId = makerUserId; }

    public String getMakerNotes() { return makerNotes; }
    public void setMakerNotes(String makerNotes) { this.makerNotes = makerNotes; }

    public UUID getCheckerUserId() { return checkerUserId; }
    public void setCheckerUserId(UUID checkerUserId) { this.checkerUserId = checkerUserId; }

    public String getCheckerNotes() { return checkerNotes; }
    public void setCheckerNotes(String checkerNotes) { this.checkerNotes = checkerNotes; }

    public OffsetDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(OffsetDateTime approvedAt) { this.approvedAt = approvedAt; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
