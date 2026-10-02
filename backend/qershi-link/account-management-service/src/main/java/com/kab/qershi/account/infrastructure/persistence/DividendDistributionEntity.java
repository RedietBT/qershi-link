package com.kab.qershi.account.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA Entity mapping the dividend_distributions table.
 * Infrastructure layer only — no domain logic.
 *
 * @author KAB Digital Solution PLC
 */
@Entity
@Table(name = "dividend_distributions")
public class DividendDistributionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "fiscal_year", nullable = false, unique = true)
    private Integer fiscalYear;

    @Column(name = "net_profit_pool", nullable = false, precision = 19, scale = 4)
    private BigDecimal netProfitPool = BigDecimal.ZERO;

    @Column(name = "declared_rate_percent", nullable = false, precision = 7, scale = 4)
    private BigDecimal declaredRatePercent = BigDecimal.ZERO;

    @Column(name = "total_dividend_distributed", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalDividendDistributed = BigDecimal.ZERO;

    @Column(name = "total_tax_withheld", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalTaxWithheld = BigDecimal.ZERO;

    @Column(name = "qualified_members_count", nullable = false)
    private Integer qualifiedMembersCount = 0;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "DRAFT";

    @Column(name = "simulated_at")
    private OffsetDateTime simulatedAt;

    @Column(name = "posted_at")
    private OffsetDateTime postedAt;

    @Column(name = "posted_by")
    private UUID postedBy;

    @Column(name = "gl_journal_ref", length = 100)
    private String glJournalRef;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public DividendDistributionEntity() {}

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Integer getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(Integer fiscalYear) { this.fiscalYear = fiscalYear; }
    public BigDecimal getNetProfitPool() { return netProfitPool; }
    public void setNetProfitPool(BigDecimal netProfitPool) { this.netProfitPool = netProfitPool; }
    public BigDecimal getDeclaredRatePercent() { return declaredRatePercent; }
    public void setDeclaredRatePercent(BigDecimal declaredRatePercent) { this.declaredRatePercent = declaredRatePercent; }
    public BigDecimal getTotalDividendDistributed() { return totalDividendDistributed; }
    public void setTotalDividendDistributed(BigDecimal v) { this.totalDividendDistributed = v; }
    public BigDecimal getTotalTaxWithheld() { return totalTaxWithheld; }
    public void setTotalTaxWithheld(BigDecimal v) { this.totalTaxWithheld = v; }
    public Integer getQualifiedMembersCount() { return qualifiedMembersCount; }
    public void setQualifiedMembersCount(Integer v) { this.qualifiedMembersCount = v; }
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
