package com.kab.qershi.account.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Pure domain aggregate root representing a member core account in SACCO ledger.
 * Modeled after enterprise Core Banking Systems (Oracle FLEXCUBE / Temenos Transact / Finacle).
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class Account {

    private UUID accountId;
    private String accountNo;
    private UUID userId;
    private String saccoCode;
    private String branchCode;
    private String productCode;
    private BigDecimal bookBalance;
    private BigDecimal lienHoldAmount;
    private AccountStatus status;
    private FreezeStatus freezeStatus;
    private LocalDateTime openedDate;
    private UUID approvedByUserId;
    private LocalDateTime approvalDate;
    private LocalDateTime closedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal dailyWithdrawnAmount = BigDecimal.ZERO;
    private java.time.LocalDate dailyWithdrawnDate = java.time.LocalDate.now();
    private java.time.LocalDate lastActivityDate = java.time.LocalDate.now();
    private java.time.LocalDate dormancyDate;
    private String reactivationStatus = "NONE";
    private UUID reactivationMakerUserId;
    private String reactivationMakerNotes;
    private UUID reactivationCheckerUserId;
    private String reactivationCheckerNotes;
    private LocalDateTime reactivatedAt;
    private BigDecimal accruedInterestPayable = BigDecimal.ZERO;
    private java.time.LocalDate lastInterestAccrualDate;
    private java.time.LocalDate lastCapitalizationDate;

    public Account() {
        this.bookBalance = BigDecimal.ZERO;
        this.lienHoldAmount = BigDecimal.ZERO;
        this.status = AccountStatus.PENDING_APPROVAL;
        this.freezeStatus = FreezeStatus.NONE;
        this.openedDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.dailyWithdrawnAmount = BigDecimal.ZERO;
        this.dailyWithdrawnDate = java.time.LocalDate.now();
        this.lastActivityDate = java.time.LocalDate.now();
        this.reactivationStatus = "NONE";
    }

    public Account(UUID accountId, String accountNo, UUID userId, String saccoCode, String branchCode,
                   String productCode, BigDecimal bookBalance, BigDecimal lienHoldAmount,
                   AccountStatus status, FreezeStatus freezeStatus, LocalDateTime openedDate,
                   UUID approvedByUserId, LocalDateTime approvalDate, LocalDateTime closedDate,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.accountId = accountId;
        this.accountNo = accountNo;
        this.userId = userId;
        this.saccoCode = saccoCode;
        this.branchCode = branchCode;
        this.productCode = productCode;
        this.bookBalance = bookBalance != null ? bookBalance : BigDecimal.ZERO;
        this.lienHoldAmount = lienHoldAmount != null ? lienHoldAmount : BigDecimal.ZERO;
        this.status = status != null ? status : AccountStatus.PENDING_APPROVAL;
        this.freezeStatus = freezeStatus != null ? freezeStatus : FreezeStatus.NONE;
        this.openedDate = openedDate != null ? openedDate : LocalDateTime.now();
        this.approvedByUserId = approvedByUserId;
        this.approvalDate = approvalDate;
        this.closedDate = closedDate;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
        this.dailyWithdrawnAmount = BigDecimal.ZERO;
        this.dailyWithdrawnDate = java.time.LocalDate.now();
    }

    /**
     * Core Banking Balance Invariant Equation:
     * Available Balance = Book Balance - Active Liens - Minimum Operating Balance
     *
     * @param minOperatingBalance Minimum unwithdrawable balance required by product rules
     * @return BigDecimal Net available funds (guaranteed non-negative)
     */
    public BigDecimal getAvailableBalance(BigDecimal minOperatingBalance) {
        BigDecimal minBalance = minOperatingBalance != null ? minOperatingBalance : BigDecimal.ZERO;
        BigDecimal encumbered = lienHoldAmount.add(minBalance);
        BigDecimal available = bookBalance.subtract(encumbered);
        return available.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : available;
    }

    /**
     * Validates if a debit transaction of the specified amount can be executed.
     */
    public boolean canPerformDebit(BigDecimal amount, BigDecimal minOperatingBalance) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (status != AccountStatus.ACTIVE) {
            return false;
        }
        if (freezeStatus.blocksDebit()) {
            return false;
        }
        BigDecimal available = getAvailableBalance(minOperatingBalance);
        return amount.compareTo(available) <= 0;
    }

    /**
     * Validates if a credit transaction of the specified amount can be executed.
     */
    public boolean canPerformCredit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (status != AccountStatus.ACTIVE) {
            return false;
        }
        return !freezeStatus.blocksCredit();
    }

    /**
     * Executes a credit transaction by updating the book balance and resetting activity timestamp.
     */
    public void credit(BigDecimal amount) {
        if (!canPerformCredit(amount)) {
            throw new IllegalStateException("Account cannot be credited. Status: " + status + " | Freeze: " + freezeStatus);
        }
        this.bookBalance = this.bookBalance.add(amount);
        this.lastActivityDate = java.time.LocalDate.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Calculates cumulative debits executed today. Returns 0 if new calendar day.
     */
    public BigDecimal getDailyWithdrawnAmountToday() {
        if (dailyWithdrawnDate == null || !dailyWithdrawnDate.equals(java.time.LocalDate.now())) {
            return BigDecimal.ZERO;
        }
        return dailyWithdrawnAmount != null ? dailyWithdrawnAmount : BigDecimal.ZERO;
    }

    /**
     * Executes a debit transaction by updating the book balance, activity timestamp, and daily withdrawal accumulator.
     */
    public void debit(BigDecimal amount, BigDecimal minOperatingBalance) {
        if (!canPerformDebit(amount, minOperatingBalance)) {
            throw new IllegalStateException("Account cannot be debited. Insufficient funds or freeze in place.");
        }
        this.bookBalance = this.bookBalance.subtract(amount);

        java.time.LocalDate today = java.time.LocalDate.now();
        if (this.dailyWithdrawnDate == null || !this.dailyWithdrawnDate.equals(today)) {
            this.dailyWithdrawnAmount = amount;
            this.dailyWithdrawnDate = today;
        } else {
            BigDecimal currentDaily = this.dailyWithdrawnAmount != null ? this.dailyWithdrawnAmount : BigDecimal.ZERO;
            this.dailyWithdrawnAmount = currentDaily.add(amount);
        }

        this.lastActivityDate = today;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Places a monetary lien hold on the account.
     */
    public void placeLien(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Lien hold amount must be strictly greater than zero.");
        }
        this.lienHoldAmount = this.lienHoldAmount.add(amount);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Releases an active monetary lien hold from the account.
     */
    public void releaseLien(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Lien release amount must be strictly greater than zero.");
        }
        if (amount.compareTo(this.lienHoldAmount) > 0) {
            this.lienHoldAmount = BigDecimal.ZERO;
        } else {
            this.lienHoldAmount = this.lienHoldAmount.subtract(amount);
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Four-Eye Maker-Checker Approval workflow method for initial account opening.
     */
    public void approveAccount(UUID checkerUserId) {
        if (checkerUserId == null) {
            throw new IllegalArgumentException("Checker User ID is required for Four-Eye account approval.");
        }
        this.status = AccountStatus.ACTIVE;
        this.approvedByUserId = checkerUserId;
        this.approvalDate = LocalDateTime.now();
        this.lastActivityDate = java.time.LocalDate.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Maker Step: Customer Service / Teller initiates in-person KYC reactivation for a DORMANT account.
     */
    public void initiateReactivation(UUID makerUserId, String notes) {
        if (this.status != AccountStatus.DORMANT) {
            throw new IllegalStateException("Only DORMANT accounts can be submitted for KYC reactivation. Current status: " + this.status);
        }
        if (makerUserId == null) {
            throw new IllegalArgumentException("Maker user ID is required to initiate KYC reactivation.");
        }
        this.reactivationStatus = "PENDING_CHECKER_APPROVAL";
        this.reactivationMakerUserId = makerUserId;
        this.reactivationMakerNotes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checker Step: Branch Manager / Supervisor validates KYC documents and approves reactivation.
     * Enforces Anti-Self-Approval rule (Maker != Checker).
     */
    public void approveReactivation(UUID checkerUserId, String notes) {
        if (this.status != AccountStatus.DORMANT) {
            throw new IllegalStateException("Account is not DORMANT. Current status: " + this.status);
        }
        if (!"PENDING_CHECKER_APPROVAL".equalsIgnoreCase(this.reactivationStatus)) {
            throw new IllegalStateException("Account does not have a pending reactivation request. Current reactivation status: " + this.reactivationStatus);
        }
        if (checkerUserId == null) {
            throw new IllegalArgumentException("Checker user ID is required to approve KYC reactivation.");
        }
        if (checkerUserId.equals(this.reactivationMakerUserId)) {
            throw new IllegalStateException("Four-Eye Anti-Self-Approval Violation: The maker user (" + this.reactivationMakerUserId + ") cannot approve their own reactivation request.");
        }
        this.status = AccountStatus.ACTIVE;
        this.dormancyDate = null;
        this.lastActivityDate = java.time.LocalDate.now();
        this.reactivationStatus = "APPROVED";
        this.reactivationCheckerUserId = checkerUserId;
        this.reactivationCheckerNotes = notes;
        this.reactivatedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checker Step: Supervisor rejects KYC reactivation.
     */
    public void rejectReactivation(UUID checkerUserId, String reason) {
        if (!"PENDING_CHECKER_APPROVAL".equalsIgnoreCase(this.reactivationStatus)) {
            throw new IllegalStateException("Account does not have a pending reactivation request.");
        }
        if (checkerUserId != null && checkerUserId.equals(this.reactivationMakerUserId)) {
            throw new IllegalStateException("Four-Eye Anti-Self-Approval Violation: The maker user cannot reject/decide their own request.");
        }
        this.reactivationStatus = "REJECTED";
        this.reactivationCheckerUserId = checkerUserId;
        this.reactivationCheckerNotes = reason;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Flags account as DORMANT following central bank dormancy threshold elapsed.
     */
    public void markDormant(java.time.LocalDate businessDate) {
        this.status = AccountStatus.DORMANT;
        this.dormancyDate = businessDate;
        this.reactivationStatus = "NONE";
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Accrues daily interest calculation into cumulative accrued payable.
     */
    public void accrueDailyInterest(BigDecimal dailyAccrual, java.time.LocalDate businessDate) {
        if (dailyAccrual != null && dailyAccrual.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal current = this.accruedInterestPayable != null ? this.accruedInterestPayable : BigDecimal.ZERO;
            this.accruedInterestPayable = current.add(dailyAccrual);
            this.lastInterestAccrualDate = businessDate;
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Capitalizes accumulated interest payable into principal ledger book balance.
     */
    public void capitalizeAccruedInterest(BigDecimal netPayout, java.time.LocalDate businessDate) {
        if (netPayout != null && netPayout.compareTo(BigDecimal.ZERO) > 0) {
            this.bookBalance = this.bookBalance.add(netPayout);
        }
        this.accruedInterestPayable = BigDecimal.ZERO;
        this.lastCapitalizationDate = businessDate;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }

    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getSaccoCode() { return saccoCode; }
    public void setSaccoCode(String saccoCode) { this.saccoCode = saccoCode; }

    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public BigDecimal getBookBalance() { return bookBalance; }
    public void setBookBalance(BigDecimal bookBalance) { this.bookBalance = bookBalance; }

    public BigDecimal getLienHoldAmount() { return lienHoldAmount; }
    public void setLienHoldAmount(BigDecimal lienHoldAmount) { this.lienHoldAmount = lienHoldAmount; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public FreezeStatus getFreezeStatus() { return freezeStatus; }
    public void setFreezeStatus(FreezeStatus freezeStatus) { this.freezeStatus = freezeStatus; }

    public LocalDateTime getOpenedDate() { return openedDate; }
    public void setOpenedDate(LocalDateTime openedDate) { this.openedDate = openedDate; }

    public UUID getApprovedByUserId() { return approvedByUserId; }
    public void setApprovedByUserId(UUID approvedByUserId) { this.approvedByUserId = approvedByUserId; }

    public LocalDateTime getApprovalDate() { return approvalDate; }
    public void setApprovalDate(LocalDateTime approvalDate) { this.approvalDate = approvalDate; }

    public LocalDateTime getClosedDate() { return closedDate; }
    public void setClosedDate(LocalDateTime closedDate) { this.closedDate = closedDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public BigDecimal getDailyWithdrawnAmount() { return dailyWithdrawnAmount; }
    public void setDailyWithdrawnAmount(BigDecimal dailyWithdrawnAmount) { this.dailyWithdrawnAmount = dailyWithdrawnAmount; }

    public java.time.LocalDate getDailyWithdrawnDate() { return dailyWithdrawnDate; }
    public void setDailyWithdrawnDate(java.time.LocalDate dailyWithdrawnDate) { this.dailyWithdrawnDate = dailyWithdrawnDate; }

    public java.time.LocalDate getLastActivityDate() { return lastActivityDate; }
    public void setLastActivityDate(java.time.LocalDate lastActivityDate) { this.lastActivityDate = lastActivityDate; }

    public java.time.LocalDate getDormancyDate() { return dormancyDate; }
    public void setDormancyDate(java.time.LocalDate dormancyDate) { this.dormancyDate = dormancyDate; }

    public String getReactivationStatus() { return reactivationStatus; }
    public void setReactivationStatus(String reactivationStatus) { this.reactivationStatus = reactivationStatus; }

    public UUID getReactivationMakerUserId() { return reactivationMakerUserId; }
    public void setReactivationMakerUserId(UUID reactivationMakerUserId) { this.reactivationMakerUserId = reactivationMakerUserId; }

    public String getReactivationMakerNotes() { return reactivationMakerNotes; }
    public void setReactivationMakerNotes(String reactivationMakerNotes) { this.reactivationMakerNotes = reactivationMakerNotes; }

    public UUID getReactivationCheckerUserId() { return reactivationCheckerUserId; }
    public void setReactivationCheckerUserId(UUID reactivationCheckerUserId) { this.reactivationCheckerUserId = reactivationCheckerUserId; }

    public String getReactivationCheckerNotes() { return reactivationCheckerNotes; }
    public void setReactivationCheckerNotes(String reactivationCheckerNotes) { this.reactivationCheckerNotes = reactivationCheckerNotes; }

    public LocalDateTime getReactivatedAt() { return reactivatedAt; }
    public void setReactivatedAt(LocalDateTime reactivatedAt) { this.reactivatedAt = reactivatedAt; }

    public BigDecimal getAccruedInterestPayable() { return accruedInterestPayable; }
    public void setAccruedInterestPayable(BigDecimal accruedInterestPayable) { this.accruedInterestPayable = accruedInterestPayable; }

    public java.time.LocalDate getLastInterestAccrualDate() { return lastInterestAccrualDate; }
    public void setLastInterestAccrualDate(java.time.LocalDate lastInterestAccrualDate) { this.lastInterestAccrualDate = lastInterestAccrualDate; }

    public java.time.LocalDate getLastCapitalizationDate() { return lastCapitalizationDate; }
    public void setLastCapitalizationDate(java.time.LocalDate lastCapitalizationDate) { this.lastCapitalizationDate = lastCapitalizationDate; }
}
