package com.kab.qershi.loan.management.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA Entity for per-loan IFRS 9 / NBE risk classification detail lines.
 * One record per active loan per provision run.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "loan_impairment_provision_lines")
public class LoanImpairmentProvisionLineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "line_id", nullable = false, updatable = false)
    private UUID lineId;

    @Column(name = "run_id", nullable = false)
    private UUID runId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "account_no", length = 50)
    private String accountNo;

    @Column(name = "days_past_due", nullable = false)
    private Integer daysPastDue = 0;

    @Column(name = "ifrs9_stage", nullable = false, length = 20)
    private String ifrs9Stage;

    @Column(name = "ifrs9_bucket_label", nullable = false, length = 50)
    private String ifrs9BucketLabel;

    @Column(name = "dpd_from", nullable = false)
    private Integer dpdFrom;

    @Column(name = "dpd_to")
    private Integer dpdTo;

    @Column(name = "outstanding_principal", nullable = false, precision = 18, scale = 2)
    private BigDecimal outstandingPrincipal = BigDecimal.ZERO;

    @Column(name = "provision_rate_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal provisionRatePct = BigDecimal.ZERO;

    @Column(name = "provision_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal provisionAmount = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public LoanImpairmentProvisionLineEntity() {}

    // ── Getters & Setters ───────────────────────────────────────────────────────

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
