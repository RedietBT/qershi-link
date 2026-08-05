package com.kab.qershi.transaction.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain entity representing a General Ledger (GL) Journal Entry header.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class JournalEntry {

    private UUID entryId;
    private String transactionRef;
    private Instant postingDate;
    private String description;
    private Instant createdAt;
    private List<JournalLine> lines = new ArrayList<>();

    public JournalEntry() {}

    public JournalEntry(UUID entryId, String transactionRef, Instant postingDate, String description, Instant createdAt) {
        this.entryId = entryId;
        this.transactionRef = transactionRef;
        this.postingDate = postingDate != null ? postingDate : Instant.now();
        this.description = description;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public UUID getEntryId() { return entryId; }
    public void setEntryId(UUID entryId) { this.entryId = entryId; }

    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public Instant getPostingDate() { return postingDate; }
    public void setPostingDate(Instant postingDate) { this.postingDate = postingDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public List<JournalLine> getLines() { return lines; }
    public void setLines(List<JournalLine> lines) { this.lines = lines; }
}
