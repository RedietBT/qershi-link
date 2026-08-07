package com.kab.qershi.loan.management.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Pure Domain Entity representing a Loan Repayment Audit Transaction.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class LoanRepayment {

    private UUID repaymentId;
    private UUID accountId;
    private String transactionRef;
    private BigDecimal amountPaid;
    private BigDecimal principalPortion;
    private BigDecimal interestPortion;
    private BigDecimal penaltyPortion;
    private OffsetDateTime paymentDate;
    private PaymentChannel paymentChannel;
    private String remarks;
    private OffsetDateTime createdAt;

    public LoanRepayment() {}

    public LoanRepayment(UUID repaymentId, UUID accountId, String transactionRef, BigDecimal amountPaid,
                         BigDecimal principalPortion, BigDecimal interestPortion, BigDecimal penaltyPortion,
                         OffsetDateTime paymentDate, PaymentChannel paymentChannel, String remarks,
                         OffsetDateTime createdAt) {
        this.repaymentId = repaymentId;
        this.accountId = accountId;
        this.transactionRef = transactionRef;
        this.amountPaid = amountPaid;
        this.principalPortion = principalPortion;
        this.interestPortion = interestPortion;
        this.penaltyPortion = penaltyPortion;
        this.paymentDate = paymentDate;
        this.paymentChannel = paymentChannel;
        this.remarks = remarks;
        this.createdAt = createdAt;
    }

    public UUID getRepaymentId() {
        return repaymentId;
    }

    public void setRepaymentId(UUID repaymentId) {
        this.repaymentId = repaymentId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public BigDecimal getPrincipalPortion() {
        return principalPortion;
    }

    public void setPrincipalPortion(BigDecimal principalPortion) {
        this.principalPortion = principalPortion;
    }

    public BigDecimal getInterestPortion() {
        return interestPortion;
    }

    public void setInterestPortion(BigDecimal interestPortion) {
        this.interestPortion = interestPortion;
    }

    public BigDecimal getPenaltyPortion() {
        return penaltyPortion;
    }

    public void setPenaltyPortion(BigDecimal penaltyPortion) {
        this.penaltyPortion = penaltyPortion;
    }

    public OffsetDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(OffsetDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentChannel getPaymentChannel() {
        return paymentChannel;
    }

    public void setPaymentChannel(PaymentChannel paymentChannel) {
        this.paymentChannel = paymentChannel;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
