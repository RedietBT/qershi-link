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

    public Account() {
        this.bookBalance = BigDecimal.ZERO;
        this.lienHoldAmount = BigDecimal.ZERO;
        this.status = AccountStatus.PENDING_APPROVAL;
        this.freezeStatus = FreezeStatus.NONE;
        this.openedDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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
     * Four-Eye Maker-Checker Approval workflow method.
     */
    public void approveAccount(UUID checkerUserId) {
        if (checkerUserId == null) {
            throw new IllegalArgumentException("Checker User ID is required for Four-Eye account approval.");
        }
        this.status = AccountStatus.ACTIVE;
        this.approvedByUserId = checkerUserId;
        this.approvalDate = LocalDateTime.now();
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
}
