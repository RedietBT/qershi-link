package com.kab.qershi.loan.management.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Pure Domain Aggregate representing a SACCO Member Loan Account (Dynamic Tier-1 Banking Standards).
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class LoanAccount {

    private UUID accountId;
    private String accountNo;
    private UUID applicationId;
    private UUID userId;
    private UUID productId;
    private BigDecimal principalAmount;
    private BigDecimal interestRatePct;
    private Integer termMonths;
    private String repaymentFrequency;
    private String interestType;
    private OffsetDateTime disbursementDate;
    private LoanStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public LoanAccount() {}

    public LoanAccount(UUID accountId, String accountNo, UUID applicationId, UUID userId, UUID productId,
                       BigDecimal principalAmount, BigDecimal interestRatePct, Integer termMonths,
                       String repaymentFrequency, String interestType,
                       OffsetDateTime disbursementDate, LoanStatus status,
                       OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.accountId = accountId;
        this.accountNo = accountNo;
        this.applicationId = applicationId;
        this.userId = userId;
        this.productId = productId;
        this.principalAmount = principalAmount;
        this.interestRatePct = interestRatePct;
        this.termMonths = termMonths;
        this.repaymentFrequency = repaymentFrequency;
        this.interestType = interestType;
        this.disbursementDate = disbursementDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(BigDecimal principalAmount) {
        this.principalAmount = principalAmount;
    }

    public BigDecimal getInterestRatePct() {
        return interestRatePct;
    }

    public void setInterestRatePct(BigDecimal interestRatePct) {
        this.interestRatePct = interestRatePct;
    }

    public Integer getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(Integer termMonths) {
        this.termMonths = termMonths;
    }

    public String getRepaymentFrequency() {
        return repaymentFrequency;
    }

    public void setRepaymentFrequency(String repaymentFrequency) {
        this.repaymentFrequency = repaymentFrequency;
    }

    public String getInterestType() {
        return interestType;
    }

    public void setInterestType(String interestType) {
        this.interestType = interestType;
    }

    public OffsetDateTime getDisbursementDate() {
        return disbursementDate;
    }

    public void setDisbursementDate(OffsetDateTime disbursementDate) {
        this.disbursementDate = disbursementDate;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
