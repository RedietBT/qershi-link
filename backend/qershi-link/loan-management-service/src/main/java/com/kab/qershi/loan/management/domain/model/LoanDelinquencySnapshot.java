package com.kab.qershi.loan.management.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Pure Domain Aggregate representing a daily Delinquency Snapshot.
 * Captures Days Past Due (DPD), overdue balances, and regulatory PAR bucket allocations.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class LoanDelinquencySnapshot {

    private UUID snapshotId;
    private UUID accountId;
    private LocalDate businessDate;
    private Integer daysPastDue;
    private BigDecimal overduePrincipal;
    private BigDecimal overdueInterest;
    private BigDecimal totalOverdue;
    private String parBucket;
    private BigDecimal provisionRatePct;
    private BigDecimal provisionAmount;
    private OffsetDateTime createdAt;

    public LoanDelinquencySnapshot() {}

    public LoanDelinquencySnapshot(UUID snapshotId, UUID accountId, LocalDate businessDate,
                                  Integer daysPastDue, BigDecimal overduePrincipal, BigDecimal overdueInterest,
                                  BigDecimal totalOverdue, String parBucket, BigDecimal provisionRatePct,
                                  BigDecimal provisionAmount, OffsetDateTime createdAt) {
        this.snapshotId = snapshotId;
        this.accountId = accountId;
        this.businessDate = businessDate;
        this.daysPastDue = daysPastDue;
        this.overduePrincipal = overduePrincipal;
        this.overdueInterest = overdueInterest;
        this.totalOverdue = totalOverdue;
        this.parBucket = parBucket;
        this.provisionRatePct = provisionRatePct;
        this.provisionAmount = provisionAmount;
        this.createdAt = createdAt;
    }

    public UUID getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(UUID snapshotId) {
        this.snapshotId = snapshotId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public LocalDate getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(LocalDate businessDate) {
        this.businessDate = businessDate;
    }

    public Integer getDaysPastDue() {
        return daysPastDue;
    }

    public void setDaysPastDue(Integer daysPastDue) {
        this.daysPastDue = daysPastDue;
    }

    public BigDecimal getOverduePrincipal() {
        return overduePrincipal;
    }

    public void setOverduePrincipal(BigDecimal overduePrincipal) {
        this.overduePrincipal = overduePrincipal;
    }

    public BigDecimal getOverdueInterest() {
        return overdueInterest;
    }

    public void setOverdueInterest(BigDecimal overdueInterest) {
        this.overdueInterest = overdueInterest;
    }

    public BigDecimal getTotalOverdue() {
        return totalOverdue;
    }

    public void setTotalOverdue(BigDecimal totalOverdue) {
        this.totalOverdue = totalOverdue;
    }

    public String getParBucket() {
        return parBucket;
    }

    public void setParBucket(String parBucket) {
        this.parBucket = parBucket;
    }

    public BigDecimal getProvisionRatePct() {
        return provisionRatePct;
    }

    public void setProvisionRatePct(BigDecimal provisionRatePct) {
        this.provisionRatePct = provisionRatePct;
    }

    public BigDecimal getProvisionAmount() {
        return provisionAmount;
    }

    public void setProvisionAmount(BigDecimal provisionAmount) {
        this.provisionAmount = provisionAmount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
