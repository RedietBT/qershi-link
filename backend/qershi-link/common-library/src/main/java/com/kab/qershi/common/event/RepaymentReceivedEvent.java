package com.kab.qershi.common.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Domain Event published when loan repayment is collected and applied against loan schedule.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class RepaymentReceivedEvent implements Serializable {

    private String saccoCode;
    private String loanId;
    private String accountNo;
    private String recipientPhone;
    private String memberName;
    private BigDecimal amount;
    private BigDecimal remainingBalance;
    private Instant timestamp;

    public RepaymentReceivedEvent() {}

    public RepaymentReceivedEvent(String saccoCode,
                                  String loanId,
                                  String accountNo,
                                  String recipientPhone,
                                  String memberName,
                                  BigDecimal amount,
                                  BigDecimal remainingBalance,
                                  Instant timestamp) {
        this.saccoCode = saccoCode;
        this.loanId = loanId;
        this.accountNo = accountNo;
        this.recipientPhone = recipientPhone;
        this.memberName = memberName;
        this.amount = amount;
        this.remainingBalance = remainingBalance;
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

    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
