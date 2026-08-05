package com.kab.qershi.transaction.infrastructure.rest.dto;

import com.kab.qershi.transaction.domain.model.Transaction;
import com.kab.qershi.transaction.domain.model.TransactionStatus;
import com.kab.qershi.transaction.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for customer financial transaction details.
 */
public class TransactionResponse {

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

    public TransactionResponse() {}

    public static TransactionResponse fromDomain(Transaction domain) {
        if (domain == null) return null;
        TransactionResponse dto = new TransactionResponse();
        dto.setTransactionId(domain.getTransactionId());
        dto.setTransactionRef(domain.getTransactionRef());
        dto.setAccountNo(domain.getAccountNo());
        dto.setSaccoCode(domain.getSaccoCode());
        dto.setUserId(domain.getUserId());
        dto.setProcessedByUserId(domain.getProcessedByUserId());
        dto.setTransactionType(domain.getTransactionType());
        dto.setAmount(domain.getAmount());
        dto.setCurrency(domain.getCurrency());
        dto.setStatus(domain.getStatus());
        dto.setNarration(domain.getNarration());
        dto.setIdempotencyKey(domain.getIdempotencyKey());
        dto.setCreatedAt(domain.getCreatedAt());
        return dto;
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
