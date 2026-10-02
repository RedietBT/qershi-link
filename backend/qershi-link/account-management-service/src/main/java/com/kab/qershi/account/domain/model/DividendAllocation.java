package com.kab.qershi.account.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Pure Domain Model — Per-member Dividend Allocation Line.
 * Stores the calculated dividend entitlement for one member within a distribution run.
 * No JPA / infrastructure coupling.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class DividendAllocation {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_POSTED  = "POSTED";
    public static final String STATUS_FAILED  = "FAILED";

    private UUID id;
    private UUID distributionId;
    private UUID memberId;
    private UUID shareAccountId;
    private UUID destinationAccountId;
    private String destinationAccountNumber;
    private BigDecimal weightedAverageShares;
    private BigDecimal grossDividend;
    private BigDecimal taxWithheld;
    private BigDecimal netDividendPayout;
    private String status;
    private String failureReason;
    private String glJournalRef;
    private OffsetDateTime createdAt;

    // ── Constructors ──────────────────────────────────────────────────────────

    public DividendAllocation() {}

    public DividendAllocation(
            UUID id, UUID distributionId, UUID memberId, UUID shareAccountId,
            UUID destinationAccountId, String destinationAccountNumber,
            BigDecimal weightedAverageShares, BigDecimal grossDividend,
            BigDecimal taxWithheld, BigDecimal netDividendPayout,
            String status, String failureReason, String glJournalRef,
            OffsetDateTime createdAt) {
        this.id = id;
        this.distributionId = distributionId;
        this.memberId = memberId;
        this.shareAccountId = shareAccountId;
        this.destinationAccountId = destinationAccountId;
        this.destinationAccountNumber = destinationAccountNumber;
        this.weightedAverageShares = weightedAverageShares;
        this.grossDividend = grossDividend;
        this.taxWithheld = taxWithheld;
        this.netDividendPayout = netDividendPayout;
        this.status = status;
        this.failureReason = failureReason;
        this.glJournalRef = glJournalRef;
        this.createdAt = createdAt;
    }

    // ── Domain Behaviour ─────────────────────────────────────────────────────

    public void markPosted(String glRef) {
        this.status = STATUS_POSTED;
        this.glJournalRef = glRef;
        this.failureReason = null;
    }

    public void markFailed(String reason) {
        this.status = STATUS_FAILED;
        this.failureReason = reason;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getDistributionId() { return distributionId; }
    public void setDistributionId(UUID distributionId) { this.distributionId = distributionId; }

    public UUID getMemberId() { return memberId; }
    public void setMemberId(UUID memberId) { this.memberId = memberId; }

    public UUID getShareAccountId() { return shareAccountId; }
    public void setShareAccountId(UUID shareAccountId) { this.shareAccountId = shareAccountId; }

    public UUID getDestinationAccountId() { return destinationAccountId; }
    public void setDestinationAccountId(UUID destinationAccountId) { this.destinationAccountId = destinationAccountId; }

    public String getDestinationAccountNumber() { return destinationAccountNumber; }
    public void setDestinationAccountNumber(String destinationAccountNumber) { this.destinationAccountNumber = destinationAccountNumber; }

    public BigDecimal getWeightedAverageShares() { return weightedAverageShares; }
    public void setWeightedAverageShares(BigDecimal weightedAverageShares) { this.weightedAverageShares = weightedAverageShares; }

    public BigDecimal getGrossDividend() { return grossDividend; }
    public void setGrossDividend(BigDecimal grossDividend) { this.grossDividend = grossDividend; }

    public BigDecimal getTaxWithheld() { return taxWithheld; }
    public void setTaxWithheld(BigDecimal taxWithheld) { this.taxWithheld = taxWithheld; }

    public BigDecimal getNetDividendPayout() { return netDividendPayout; }
    public void setNetDividendPayout(BigDecimal netDividendPayout) { this.netDividendPayout = netDividendPayout; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public String getGlJournalRef() { return glJournalRef; }
    public void setGlJournalRef(String glJournalRef) { this.glJournalRef = glJournalRef; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
