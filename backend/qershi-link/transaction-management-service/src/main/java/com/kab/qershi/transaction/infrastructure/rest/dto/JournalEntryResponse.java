package com.kab.qershi.transaction.infrastructure.rest.dto;

import com.kab.qershi.transaction.domain.model.JournalEntry;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Response DTO for General Ledger (GL) Journal Entry header and all Debit/Credit lines.
 */
public class JournalEntryResponse {

    private UUID entryId;
    private String transactionRef;
    private Instant postingDate;
    private String description;
    private Instant createdAt;
    private List<JournalLineResponse> lines = new ArrayList<>();

    public JournalEntryResponse() {}

    public static JournalEntryResponse fromDomain(JournalEntry domain) {
        if (domain == null) return null;
        JournalEntryResponse dto = new JournalEntryResponse();
        dto.setEntryId(domain.getEntryId());
        dto.setTransactionRef(domain.getTransactionRef());
        dto.setPostingDate(domain.getPostingDate());
        dto.setDescription(domain.getDescription());
        dto.setCreatedAt(domain.getCreatedAt());

        if (domain.getLines() != null) {
            dto.setLines(domain.getLines().stream()
                    .map(JournalLineResponse::fromDomain)
                    .collect(Collectors.toList()));
        }
        return dto;
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

    public List<JournalLineResponse> getLines() { return lines; }
    public void setLines(List<JournalLineResponse> lines) { this.lines = lines; }
}
