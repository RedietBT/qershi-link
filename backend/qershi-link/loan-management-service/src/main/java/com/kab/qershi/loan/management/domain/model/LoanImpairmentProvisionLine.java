package com.kab.qershi.loan.management.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Pure Domain Aggregate representing an IFRS 9 / NBE Per-Loan Impairment Classification Line.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class LoanImpairmentProvisionLine {

    private UUID lineId;
    private UUID runId;
    private UUID accountId;
    private String accountNo;
    private Integer daysPastDue;
    private String ifrs9Stage;
    private String ifrs9BucketLabel;
    private Integer dpdFrom;
    private Integer dpdTo;
    private BigDecimal outstandingPrincipal;
    private BigDecimal provisionRatePct;
    private BigDecimal provisionAmount;
    private OffsetDateTime createdAt;

    public LoanImpairmentProvisionLine() {}

    public LoanImpairmentProvisionLine(UUID lineId, UUID runId, UUID accountId, String accountNo,
                                      Integer daysPastDue, String ifrs9Stage, String ifrs9BucketLabel,
                                      Integer dpdFrom, Integer dpdTo, BigDecimal outstandingPrincipal,
                                      BigDecimal provisionRatePct, BigDecimal provisionAmount,
                                      OffsetDateTime createdAt) {
        this.lineId = lineId;
        this.runId = runId;
        this.accountId = accountId;
        this.accountNo = accountNo;
        this.daysPastDue = daysPastDue;
        this.ifrs9Stage = ifrs9Stage;
        this.ifrs9BucketLabel = ifrs9BucketLabel;
        this.dpdFrom = dpdFrom;
        this.dpdTo = dpdTo;
        this.outstandingPrincipal = outstandingPrincipal;
        this.provisionRatePct = provisionRatePct;
        this.provisionAmount = provisionAmount;
        this.createdAt = createdAt;
    }

    public UUID getLineId() { return lineId; }
    public void setLineId(UUID lineId) { this.lineId = lineId; }

    public UUID getRunId() { return runId; }
    public void setRunId(UUID runId) { this.runId = runId; }

    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }

    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }

    public Integer getDaysPastDue() { return daysPastDue; }
    public void setDaysPastDue(Integer daysPastDue) { this.daysPastDue = daysPastDue; }

    public String getIfrs9Stage() { return ifrs9Stage; }
    public void setIfrs9Stage(String ifrs9Stage) { this.ifrs9Stage = ifrs9Stage; }

    public String getIfrs9BucketLabel() { return ifrs9BucketLabel; }
    public void setIfrs9BucketLabel(String ifrs9BucketLabel) { this.ifrs9BucketLabel = ifrs9BucketLabel; }

    public Integer getDpdFrom() { return dpdFrom; }
    public void setDpdFrom(Integer dpdFrom) { this.dpdFrom = dpdFrom; }

    public Integer getDpdTo() { return dpdTo; }
    public void setDpdTo(Integer dpdTo) { this.dpdTo = dpdTo; }

    public BigDecimal getOutstandingPrincipal() { return outstandingPrincipal; }
    public void setOutstandingPrincipal(BigDecimal outstandingPrincipal) { this.outstandingPrincipal = outstandingPrincipal; }

    public BigDecimal getProvisionRatePct() { return provisionRatePct; }
    public void setProvisionRatePct(BigDecimal provisionRatePct) { this.provisionRatePct = provisionRatePct; }

    public BigDecimal getProvisionAmount() { return provisionAmount; }
    public void setProvisionAmount(BigDecimal provisionAmount) { this.provisionAmount = provisionAmount; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
