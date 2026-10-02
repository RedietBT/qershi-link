package com.kab.qershi.account.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Pure Domain Model — Annual AGM Dividend Distribution Header.
 * Represents the master record for a single fiscal-year dividend distribution run.
 * No JPA / infrastructure coupling.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class DividendDistribution {

    public static final String STATUS_DRAFT      = "DRAFT";
    public static final String STATUS_SIMULATED  = "SIMULATED";
    public static final String STATUS_POSTED     = "POSTED";

    private UUID id;
    private int fiscalYear;
    private BigDecimal netProfitPool;
    private BigDecimal declaredRatePercent;
    private BigDecimal totalDividendDistributed;
    private BigDecimal totalTaxWithheld;
    private int qualifiedMembersCount;
    private String status;
    private OffsetDateTime simulatedAt;
    private OffsetDateTime postedAt;
    private UUID postedBy;
    private String glJournalRef;
    private OffsetDateTime createdAt;

    // ── Constructors ──────────────────────────────────────────────────────────

    public DividendDistribution() {}

    public DividendDistribution(
            UUID id, int fiscalYear, BigDecimal netProfitPool,
            BigDecimal declaredRatePercent, BigDecimal totalDividendDistributed,
            BigDecimal totalTaxWithheld, int qualifiedMembersCount,
            String status, OffsetDateTime simulatedAt, OffsetDateTime postedAt,
            UUID postedBy, String glJournalRef, OffsetDateTime createdAt) {
        this.id = id;
        this.fiscalYear = fiscalYear;
        this.netProfitPool = netProfitPool;
        this.declaredRatePercent = declaredRatePercent;
        this.totalDividendDistributed = totalDividendDistributed;
        this.totalTaxWithheld = totalTaxWithheld;
        this.qualifiedMembersCount = qualifiedMembersCount;
        this.status = status;
        this.simulatedAt = simulatedAt;
        this.postedAt = postedAt;
        this.postedBy = postedBy;
        this.glJournalRef = glJournalRef;
        this.createdAt = createdAt;
    }

    /** Factory method — creates a new DRAFT distribution */
    public static DividendDistribution createDraft(int fiscalYear, BigDecimal netProfitPool, BigDecimal declaredRatePercent) {
        return new DividendDistribution(
                UUID.randomUUID(), fiscalYear, netProfitPool, declaredRatePercent,
                BigDecimal.ZERO, BigDecimal.ZERO, 0, STATUS_DRAFT,
                null, null, null, null, OffsetDateTime.now()
        );
    }

    // ── Domain Behaviour ─────────────────────────────────────────────────────

    public boolean isDraft()     { return STATUS_DRAFT.equalsIgnoreCase(status); }
    public boolean isSimulated() { return STATUS_SIMULATED.equalsIgnoreCase(status); }
    public boolean isPosted()    { return STATUS_POSTED.equalsIgnoreCase(status); }

    public void markSimulated(BigDecimal totalDividend, BigDecimal totalTax, int membersCount) {
        if (!isDraft() && !isSimulated()) {
            throw new IllegalStateException("Distribution can only be simulated from DRAFT or SIMULATED state. Current: " + status);
        }
        this.totalDividendDistributed = totalDividend;
        this.totalTaxWithheld = totalTax;
        this.qualifiedMembersCount = membersCount;
        this.status = STATUS_SIMULATED;
        this.simulatedAt = OffsetDateTime.now();
    }

    public void markPosted(UUID postedByUserId, String glRef) {
        if (!isSimulated()) {
            throw new IllegalStateException("Distribution must be SIMULATED before posting. Current: " + status);
        }
        this.status = STATUS_POSTED;
        this.postedAt = OffsetDateTime.now();
        this.postedBy = postedByUserId;
        this.glJournalRef = glRef;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public int getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(int fiscalYear) { this.fiscalYear = fiscalYear; }

    public BigDecimal getNetProfitPool() { return netProfitPool; }
    public void setNetProfitPool(BigDecimal netProfitPool) { this.netProfitPool = netProfitPool; }

    public BigDecimal getDeclaredRatePercent() { return declaredRatePercent; }
    public void setDeclaredRatePercent(BigDecimal declaredRatePercent) { this.declaredRatePercent = declaredRatePercent; }

    public BigDecimal getTotalDividendDistributed() { return totalDividendDistributed; }
    public void setTotalDividendDistributed(BigDecimal totalDividendDistributed) { this.totalDividendDistributed = totalDividendDistributed; }

    public BigDecimal getTotalTaxWithheld() { return totalTaxWithheld; }
    public void setTotalTaxWithheld(BigDecimal totalTaxWithheld) { this.totalTaxWithheld = totalTaxWithheld; }

    public int getQualifiedMembersCount() { return qualifiedMembersCount; }
    public void setQualifiedMembersCount(int qualifiedMembersCount) { this.qualifiedMembersCount = qualifiedMembersCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OffsetDateTime getSimulatedAt() { return simulatedAt; }
    public void setSimulatedAt(OffsetDateTime simulatedAt) { this.simulatedAt = simulatedAt; }

    public OffsetDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(OffsetDateTime postedAt) { this.postedAt = postedAt; }

    public UUID getPostedBy() { return postedBy; }
    public void setPostedBy(UUID postedBy) { this.postedBy = postedBy; }

    public String getGlJournalRef() { return glJournalRef; }
    public void setGlJournalRef(String glJournalRef) { this.glJournalRef = glJournalRef; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
