package com.kab.qershi.account.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA Entity mapping the dividend_allocations table.
 * Infrastructure layer only — no domain logic.
 *
 * @author KAB Digital Solution PLC
 */
@Entity
@Table(name = "dividend_allocations")
public class DividendAllocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "distribution_id", nullable = false)
    private UUID distributionId;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "share_account_id", nullable = false)
    private UUID shareAccountId;

    @Column(name = "destination_account_id")
    private UUID destinationAccountId;

    @Column(name = "destination_account_number", length = 50)
    private String destinationAccountNumber;

    @Column(name = "weighted_average_shares", nullable = false, precision = 19, scale = 4)
    private BigDecimal weightedAverageShares = BigDecimal.ZERO;

    @Column(name = "gross_dividend", nullable = false, precision = 19, scale = 4)
    private BigDecimal grossDividend = BigDecimal.ZERO;

    @Column(name = "tax_withheld", nullable = false, precision = 19, scale = 4)
    private BigDecimal taxWithheld = BigDecimal.ZERO;

    @Column(name = "net_dividend_payout", nullable = false, precision = 19, scale = 4)
    private BigDecimal netDividendPayout = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "gl_journal_ref", length = 100)
    private String glJournalRef;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public DividendAllocationEntity() {}

    // Getters & Setters
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
    public void setDestinationAccountNumber(String v) { this.destinationAccountNumber = v; }
    public BigDecimal getWeightedAverageShares() { return weightedAverageShares; }
    public void setWeightedAverageShares(BigDecimal v) { this.weightedAverageShares = v; }
    public BigDecimal getGrossDividend() { return grossDividend; }
    public void setGrossDividend(BigDecimal v) { this.grossDividend = v; }
    public BigDecimal getTaxWithheld() { return taxWithheld; }
    public void setTaxWithheld(BigDecimal v) { this.taxWithheld = v; }
    public BigDecimal getNetDividendPayout() { return netDividendPayout; }
    public void setNetDividendPayout(BigDecimal v) { this.netDividendPayout = v; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public String getGlJournalRef() { return glJournalRef; }
    public void setGlJournalRef(String glJournalRef) { this.glJournalRef = glJournalRef; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
