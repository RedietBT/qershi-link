package com.kab.qershi.transaction.domain.ports.outbound;

import com.kab.qershi.transaction.domain.model.JournalEntry;

import java.util.Optional;

/**
 * Outbound persistence port for General Ledger (GL) double-entry journal postings.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface JournalRepositoryPort {

    JournalEntry save(JournalEntry journalEntry);

    Optional<JournalEntry> findByTransactionRef(String transactionRef);
}
