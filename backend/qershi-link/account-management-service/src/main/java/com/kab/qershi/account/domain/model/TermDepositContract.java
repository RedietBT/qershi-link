package com.kab.qershi.account.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Pure Domain Model representing a Fixed Term Deposit (FD) Contract.
 * Encapsulates calculation formulas and state transitions.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class TermDepositContract {

    private static final BigDecimal DAYS_IN_YEAR = new BigDecimal("365");

    private UUID contractId;
    private String contractNo;
    private String accountNo;
    private UUID userId;
    private String saccoCode;
    private String branchCode;
    private BigDecimal principalAmount;
    private Integer tenorMonths;
    private BigDecimal agreedInterestRatePa;
    private BigDecimal earlyBreakPenaltyPct;
    private LocalDate startDate;
    private LocalDate maturityDate;
    private LocalDate closedDate;
    private BigDecimal accruedInterest;
    private BigDecimal capitalizedInterest;
    private TermDepositStatus status;
    private Boolean autoRollover;
    private Integer rolloverTenorMonths;
    private BigDecimal penaltyAmount;
    private BigDecimal netPayoutAmount;
    private String glDebitAccount;
    private String glCreditAccount;
    private String openingGlRef;
    private String closingGlRef;
    private UUID makerUserId;
    private String makerNotes;
    private UUID checkerUserId;
    private String checkerNotes;
    private OffsetDateTime approvedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public TermDepositContract(
            UUID contractId, String contractNo, String accountNo, UUID userId,
            String saccoCode, String branchCode, BigDecimal principalAmount,
            Integer tenorMonths, BigDecimal agreedInterestRatePa, BigDecimal earlyBreakPenaltyPct,
            LocalDate startDate, LocalDate maturityDate, LocalDate closedDate,
            BigDecimal accruedInterest, BigDecimal capitalizedInterest,
            TermDepositStatus status, Boolean autoRollover, Integer rolloverTenorMonths,
            BigDecimal penaltyAmount, BigDecimal netPayoutAmount,
            String glDebitAccount, String glCreditAccount,
            String openingGlRef, String closingGlRef,
            UUID makerUserId, String makerNotes,
            UUID checkerUserId, String checkerNotes,
            OffsetDateTime approvedAt, OffsetDateTime createdAt, OffsetDateTime updatedAt) {

        this.contractId = contractId != null ? contractId : UUID.randomUUID();
        this.contractNo = contractNo;
        this.accountNo = accountNo;
        this.userId = userId;
        this.saccoCode = saccoCode;
        this.branchCode = branchCode;
        this.principalAmount = principalAmount;
        this.tenorMonths = tenorMonths;
        this.agreedInterestRatePa = agreedInterestRatePa;
        this.earlyBreakPenaltyPct = earlyBreakPenaltyPct != null ? earlyBreakPenaltyPct : new BigDecimal("2.00");
        this.startDate = startDate;
        this.maturityDate = maturityDate;
        this.closedDate = closedDate;
        this.accruedInterest = accruedInterest != null ? accruedInterest : BigDecimal.ZERO;
        this.capitalizedInterest = capitalizedInterest != null ? capitalizedInterest : BigDecimal.ZERO;
        this.status = status != null ? status : TermDepositStatus.PENDING_APPROVAL;
        this.autoRollover = autoRollover != null ? autoRollover : false;
        this.rolloverTenorMonths = rolloverTenorMonths;
        this.penaltyAmount = penaltyAmount;
        this.netPayoutAmount = netPayoutAmount;
        this.glDebitAccount = glDebitAccount != null ? glDebitAccount : "2060";
        this.glCreditAccount = glCreditAccount != null ? glCreditAccount : "1010";
        this.openingGlRef = openingGlRef;
        this.closingGlRef = closingGlRef;
        this.makerUserId = makerUserId;
        this.makerNotes = makerNotes;
        this.checkerUserId = checkerUserId;
        this.checkerNotes = checkerNotes;
        this.approvedAt = approvedAt;
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : OffsetDateTime.now();
    }

    // ── Domain Business Methods ──────────────────────────────────────────────

    public boolean isMatured(LocalDate businessDate) {
        return this.status == TermDepositStatus.ACTIVE && !businessDate.isBefore(this.maturityDate);
    }

    public BigDecimal computeDailyInterest() {
        if (this.status != TermDepositStatus.ACTIVE) {
            return BigDecimal.ZERO;
        }
        return this.principalAmount
                .multiply(this.agreedInterestRatePa)
                .divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP)
                .divide(DAYS_IN_YEAR, 4, RoundingMode.HALF_UP);
    }

    public void accrueDailyInterest(BigDecimal dailyAmount) {
        this.accruedInterest = this.accruedInterest.add(dailyAmount);
        this.updatedAt = OffsetDateTime.now();
    }

    public void approve(UUID checkerId, String notes, String glRef) {
        this.status = TermDepositStatus.ACTIVE;
        this.checkerUserId = checkerId;
        this.checkerNotes = notes;
        this.approvedAt = OffsetDateTime.now();
        this.openingGlRef = glRef;
        this.updatedAt = OffsetDateTime.now();
    }

    public void closeNormal(String glRef, LocalDate closureDate) {
        this.status = TermDepositStatus.CLOSED_NORMAL;
        this.capitalizedInterest = this.accruedInterest;
        this.penaltyAmount = BigDecimal.ZERO;
        this.netPayoutAmount = this.principalAmount.add(this.accruedInterest);
        this.closedDate = closureDate;
        this.closingGlRef = glRef;
        this.updatedAt = OffsetDateTime.now();
    }

    public void closeEarly(BigDecimal penalty, BigDecimal netPayout, String glRef, LocalDate closureDate) {
        this.status = TermDepositStatus.CLOSED_EARLY;
        this.penaltyAmount = penalty;
        this.netPayoutAmount = netPayout;
        this.closedDate = closureDate;
        this.closingGlRef = glRef;
        this.updatedAt = OffsetDateTime.now();
    }

    public void markRolledOver(String glRef, LocalDate rolloverDate) {
        this.status = TermDepositStatus.ROLLED_OVER;
        this.closedDate = rolloverDate;
        this.closingGlRef = glRef;
        this.updatedAt = OffsetDateTime.now();
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public UUID getContractId() { return contractId; }
    public String getContractNo() { return contractNo; }
    public String getAccountNo() { return accountNo; }
    public UUID getUserId() { return userId; }
    public String getSaccoCode() { return saccoCode; }
    public String getBranchCode() { return branchCode; }
    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public Integer getTenorMonths() { return tenorMonths; }
    public BigDecimal getAgreedInterestRatePa() { return agreedInterestRatePa; }
    public BigDecimal getEarlyBreakPenaltyPct() { return earlyBreakPenaltyPct; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getMaturityDate() { return maturityDate; }
    public LocalDate getClosedDate() { return closedDate; }
    public BigDecimal getAccruedInterest() { return accruedInterest; }
    public BigDecimal getCapitalizedInterest() { return capitalizedInterest; }
    public TermDepositStatus getStatus() { return status; }
    public Boolean getAutoRollover() { return autoRollover; }
    public Integer getRolloverTenorMonths() { return rolloverTenorMonths; }
    public BigDecimal getPenaltyAmount() { return penaltyAmount; }
    public BigDecimal getNetPayoutAmount() { return netPayoutAmount; }
    public String getGlDebitAccount() { return glDebitAccount; }
    public String getGlCreditAccount() { return glCreditAccount; }
    public String getOpeningGlRef() { return openingGlRef; }
    public String getClosingGlRef() { return closingGlRef; }
    public UUID getMakerUserId() { return makerUserId; }
    public String getMakerNotes() { return makerNotes; }
    public UUID getCheckerUserId() { return checkerUserId; }
    public String getCheckerNotes() { return checkerNotes; }
    public OffsetDateTime getApprovedAt() { return approvedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
