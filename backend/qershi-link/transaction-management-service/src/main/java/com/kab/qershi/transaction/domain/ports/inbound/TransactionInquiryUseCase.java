package com.kab.qershi.transaction.domain.ports.inbound;

import com.kab.qershi.transaction.domain.model.JournalEntry;
import com.kab.qershi.transaction.domain.model.Transaction;

import java.util.List;

/**
 * Inbound port interface declaring transaction history lookup & GL journal audit capabilities.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface TransactionInquiryUseCase {

    List<Transaction> getAccountTransactions(String accountNo);

    Transaction getTransactionByRef(String transactionRef);

    JournalEntry getJournalEntryByTransactionRef(String transactionRef);
}
