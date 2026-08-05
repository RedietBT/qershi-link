package com.kab.qershi.transaction.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing an individual General Ledger (GL) double-entry bookkeeping debit/credit line.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class JournalLine {

    private UUID lineId;
    private UUID entryId;
    private String glAccountCode;
    private EntryType entryType;
    private BigDecimal amount;
    private Instant createdAt;

    public JournalLine() {}

    public JournalLine(UUID lineId, UUID entryId, String glAccountCode, EntryType entryType, BigDecimal amount, Instant createdAt) {
        this.lineId = lineId;
        this.entryId = entryId;
        this.glAccountCode = glAccountCode;
        this.entryType = entryType;
        this.amount = amount;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public UUID getLineId() { return lineId; }
    public void setLineId(UUID lineId) { this.lineId = lineId; }

    public UUID getEntryId() { return entryId; }
    public void setEntryId(UUID entryId) { this.entryId = entryId; }

    public String getGlAccountCode() { return glAccountCode; }
    public void setGlAccountCode(String glAccountCode) { this.glAccountCode = glAccountCode; }

    public EntryType getEntryType() { return entryType; }
    public void setEntryType(EntryType entryType) { this.entryType = entryType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
