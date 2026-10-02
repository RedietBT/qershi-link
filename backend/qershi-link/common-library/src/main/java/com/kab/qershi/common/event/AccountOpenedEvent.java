package com.kab.qershi.common.event;

import java.io.Serializable;
import java.time.Instant;

/**
 * Domain Event published when a new savings/share account is opened and activated.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class AccountOpenedEvent implements Serializable {

    private String saccoCode;
    private String accountNo;
    private String recipientPhone;
    private String memberName;
    private String productName;
    private Instant timestamp;

    public AccountOpenedEvent() {}

    public AccountOpenedEvent(String saccoCode,
                              String accountNo,
                              String recipientPhone,
                              String memberName,
                              String productName,
                              Instant timestamp) {
        this.saccoCode = saccoCode;
        this.accountNo = accountNo;
        this.recipientPhone = recipientPhone;
        this.memberName = memberName;
        this.productName = productName;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public String getSaccoCode() { return saccoCode; }
    public void setSaccoCode(String saccoCode) { this.saccoCode = saccoCode; }

    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
