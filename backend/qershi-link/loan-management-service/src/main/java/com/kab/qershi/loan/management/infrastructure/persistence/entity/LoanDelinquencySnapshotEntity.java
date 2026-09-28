package com.kab.qershi.loan.management.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity mapping loan_delinquency_snapshots table.
 * Records end-of-day snapshot of Days Past Due (DPD) and regulatory PAR aging provisioning.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "loan_delinquency_snapshots")
public class LoanDelinquencySnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "snapshot_id", nullable = false, updatable = false)
    private UUID snapshotId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "business_date", nullable = false)
    private LocalDate businessDate;

    @Column(name = "days_past_due", nullable = false)
    private Integer daysPastDue = 0;

    @Column(name = "overdue_principal", nullable = false, precision = 15, scale = 2)
    private BigDecimal overduePrincipal = BigDecimal.ZERO;

    @Column(name = "overdue_interest", nullable = false, precision = 15, scale = 2)
    private BigDecimal overdueInterest = BigDecimal.ZERO;

    @Column(name = "total_overdue", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalOverdue = BigDecimal.ZERO;

    @Column(name = "par_bucket", nullable = false, length = 30)
    private String parBucket = "CURRENT";

    @Column(name = "provision_rate_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal provisionRatePct = new BigDecimal("1.00");

    @Column(name = "provision_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal provisionAmount = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public LoanDelinquencySnapshotEntity() {}

    public UUID getSnapshotId() { return snapshotId; }
    public void setSnapshotId(UUID snapshotId) { this.snapshotId = snapshotId; }

    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }

    public LocalDate getBusinessDate() { return businessDate; }
    public void setBusinessDate(LocalDate businessDate) { this.businessDate = businessDate; }

    public Integer getDaysPastDue() { return daysPastDue; }
    public void setDaysPastDue(Integer daysPastDue) { this.daysPastDue = daysPastDue; }

    public BigDecimal getOverduePrincipal() { return overduePrincipal; }
    public void setOverduePrincipal(BigDecimal overduePrincipal) { this.overduePrincipal = overduePrincipal; }

    public BigDecimal getOverdueInterest() { return overdueInterest; }
    public void setOverdueInterest(BigDecimal overdueInterest) { this.overdueInterest = overdueInterest; }

    public BigDecimal getTotalOverdue() { return totalOverdue; }
    public void setTotalOverdue(BigDecimal totalOverdue) { this.totalOverdue = totalOverdue; }

    public String getParBucket() { return parBucket; }
    public void setParBucket(String parBucket) { this.parBucket = parBucket; }

    public BigDecimal getProvisionRatePct() { return provisionRatePct; }
    public void setProvisionRatePct(BigDecimal provisionRatePct) { this.provisionRatePct = provisionRatePct; }

    public BigDecimal getProvisionAmount() { return provisionAmount; }
    public void setProvisionAmount(BigDecimal provisionAmount) { this.provisionAmount = provisionAmount; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
