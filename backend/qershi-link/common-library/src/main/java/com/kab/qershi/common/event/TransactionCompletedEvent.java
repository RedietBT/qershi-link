package com.kab.qershi.common.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Domain Event published upon successful ledger posting of a deposit, withdrawal, or transfer.
 * Serialized as simple JSON POJO.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class TransactionCompletedEvent implements Serializable {

    private String saccoCode;
    private String transactionId;
    private String accountNo;
    private String recipientPhone;
    private String memberName;
    private String transactionType; // DEPOSIT, WITHDRAWAL, TRANSFER
    private BigDecimal amount;
    private BigDecimal balance;
    private String receiverName;
    private String receiverAccountNo;
    private Instant timestamp;

    public TransactionCompletedEvent() {}

    public TransactionCompletedEvent(String saccoCode,
                                     String transactionId,
                                     String accountNo,
                                     String recipientPhone,
                                     String memberName,
                                     String transactionType,
                                     BigDecimal amount,
                                     BigDecimal balance,
                                     String receiverName,
                                     String receiverAccountNo,
                                     Instant timestamp) {
        this.saccoCode = saccoCode;
        this.transactionId = transactionId;
        this.accountNo = accountNo;
        this.recipientPhone = recipientPhone;
        this.memberName = memberName;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balance = balance;
        this.receiverName = receiverName;
        this.receiverAccountNo = receiverAccountNo;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public String getSaccoCode() { return saccoCode; }
    public void setSaccoCode(String saccoCode) { this.saccoCode = saccoCode; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getReceiverAccountNo() { return receiverAccountNo; }
    public void setReceiverAccountNo(String receiverAccountNo) { this.receiverAccountNo = receiverAccountNo; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
