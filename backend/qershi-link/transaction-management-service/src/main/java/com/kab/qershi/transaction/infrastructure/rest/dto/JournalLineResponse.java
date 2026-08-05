package com.kab.qershi.transaction.infrastructure.rest.dto;

import com.kab.qershi.transaction.domain.model.EntryType;
import com.kab.qershi.transaction.domain.model.JournalLine;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for individual General Ledger double-entry lines.
 */
public class JournalLineResponse {

    private UUID lineId;
    private String glAccountCode;
    private EntryType entryType;
    private BigDecimal amount;
    private Instant createdAt;

    public JournalLineResponse() {}

    public static JournalLineResponse fromDomain(JournalLine domain) {
        if (domain == null) return null;
        JournalLineResponse dto = new JournalLineResponse();
        dto.setLineId(domain.getLineId());
        dto.setGlAccountCode(domain.getGlAccountCode());
        dto.setEntryType(domain.getEntryType());
        dto.setAmount(domain.getAmount());
        dto.setCreatedAt(domain.getCreatedAt());
        return dto;
    }

    public UUID getLineId() { return lineId; }
    public void setLineId(UUID lineId) { this.lineId = lineId; }

    public String getGlAccountCode() { return glAccountCode; }
    public void setGlAccountCode(String glAccountCode) { this.glAccountCode = glAccountCode; }

    public EntryType getEntryType() { return entryType; }
    public void setEntryType(EntryType entryType) { this.entryType = entryType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
