package com.kab.qershi.common.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Domain Event published when loan funds are disbursed to a member's savings account.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class LoanDisbursedEvent implements Serializable {

    private String saccoCode;
    private String loanId;
    private String accountNo;
    private String recipientPhone;
    private String memberName;
    private BigDecimal amount;
    private Instant timestamp;

    public LoanDisbursedEvent() {}

    public LoanDisbursedEvent(String saccoCode,
                              String loanId,
                              String accountNo,
                              String recipientPhone,
                              String memberName,
                              BigDecimal amount,
                              Instant timestamp) {
        this.saccoCode = saccoCode;
        this.loanId = loanId;
        this.accountNo = accountNo;
        this.recipientPhone = recipientPhone;
        this.memberName = memberName;
        this.amount = amount;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public String getSaccoCode() { return saccoCode; }
    public void setSaccoCode(String saccoCode) { this.saccoCode = saccoCode; }

    public String getLoanId() { return loanId; }
    public void setLoanId(String loanId) { this.loanId = loanId; }

    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
