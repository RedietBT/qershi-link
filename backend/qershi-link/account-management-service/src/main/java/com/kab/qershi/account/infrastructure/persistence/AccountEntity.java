package com.kab.qershi.account.infrastructure.persistence;

import com.kab.qershi.account.domain.model.AccountStatus;
import com.kab.qershi.account.domain.model.FreezeStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity mapping accounts table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "account_no", nullable = false, unique = true, length = 50)
    private String accountNo;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "sacco_code", nullable = false, length = 20)
    private String saccoCode;

    @Column(name = "branch_code", nullable = false, length = 20)
    private String branchCode;

    @Column(name = "product_code", nullable = false, length = 10)
    private String productCode;

    @Column(name = "book_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal bookBalance;

    @Column(name = "lien_hold_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal lienHoldAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "freeze_status", nullable = false)
    private FreezeStatus freezeStatus;

    @Column(name = "opened_date", nullable = false)
    private LocalDateTime openedDate;

    @Column(name = "approved_by_user_id")
    private UUID approvedByUserId;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "closed_date")
    private LocalDateTime closedDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public AccountEntity() {}

    public AccountEntity(UUID accountId, String accountNo, UUID userId, String saccoCode, String branchCode,
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
        this.bookBalance = bookBalance;
        this.lienHoldAmount = lienHoldAmount;
        this.status = status;
        this.freezeStatus = freezeStatus;
        this.openedDate = openedDate;
        this.approvedByUserId = approvedByUserId;
        this.approvalDate = approvalDate;
        this.closedDate = closedDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
