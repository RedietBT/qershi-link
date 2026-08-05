package com.kab.qershi.transaction.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing a SACCO financial transaction.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class Transaction {

    private UUID transactionId;
    private String transactionRef;
    private String accountNo;
    private String saccoCode;
    private UUID userId;
    private UUID processedByUserId;
    private TransactionType transactionType;
    private BigDecimal amount;
    private String currency;
    private TransactionStatus status;
    private String narration;
    private String idempotencyKey;
    private Instant createdAt;

    public Transaction() {}

    public Transaction(UUID transactionId, String transactionRef, String accountNo, String saccoCode,
                       UUID userId, UUID processedByUserId, TransactionType transactionType,
                       BigDecimal amount, String currency, TransactionStatus status,
                       String narration, String idempotencyKey, Instant createdAt) {
        this.transactionId = transactionId;
        this.transactionRef = transactionRef;
        this.accountNo = accountNo;
        this.saccoCode = saccoCode;
        this.userId = userId;
        this.processedByUserId = processedByUserId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.currency = currency != null ? currency : "ETB";
        this.status = status != null ? status : TransactionStatus.PENDING;
        this.narration = narration;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public UUID getTransactionId() { return transactionId; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }

    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }

    public String getSaccoCode() { return saccoCode; }
    public void setSaccoCode(String saccoCode) { this.saccoCode = saccoCode; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getProcessedByUserId() { return processedByUserId; }
    public void setProcessedByUserId(UUID processedByUserId) { this.processedByUserId = processedByUserId; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public String getNarration() { return narration; }
    public void setNarration(String narration) { this.narration = narration; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
