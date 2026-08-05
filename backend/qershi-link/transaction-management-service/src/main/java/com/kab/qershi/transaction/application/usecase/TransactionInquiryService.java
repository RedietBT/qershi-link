package com.kab.qershi.transaction.application.usecase;

import com.kab.qershi.transaction.domain.model.JournalEntry;
import com.kab.qershi.transaction.domain.model.Transaction;
import com.kab.qershi.transaction.domain.ports.inbound.TransactionInquiryUseCase;
import com.kab.qershi.transaction.domain.ports.outbound.JournalRepositoryPort;
import com.kab.qershi.transaction.domain.ports.outbound.TransactionRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use case service implementing transaction statement inquiry and GL journal line lookups.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class TransactionInquiryService implements TransactionInquiryUseCase {

    private final TransactionRepositoryPort transactionRepositoryPort;
    private final JournalRepositoryPort journalRepositoryPort;

    public TransactionInquiryService(TransactionRepositoryPort transactionRepositoryPort,
                                     JournalRepositoryPort journalRepositoryPort) {
        this.transactionRepositoryPort = transactionRepositoryPort;
        this.journalRepositoryPort = journalRepositoryPort;
    }

    @Override
    public List<Transaction> getAccountTransactions(String accountNo) {
        if (accountNo == null || accountNo.isBlank()) {
            throw new IllegalArgumentException("Account number must not be blank.");
        }
        return transactionRepositoryPort.findByAccountNo(accountNo.trim());
    }

    @Override
    public Transaction getTransactionByRef(String transactionRef) {
        if (transactionRef == null || transactionRef.isBlank()) {
            throw new IllegalArgumentException("Transaction reference must not be blank.");
        }
        return transactionRepositoryPort.findByTransactionRef(transactionRef.trim())
                .orElseThrow(() -> new IllegalArgumentException("Transaction reference not found: " + transactionRef));
    }

    @Override
    public JournalEntry getJournalEntryByTransactionRef(String transactionRef) {
        if (transactionRef == null || transactionRef.isBlank()) {
            throw new IllegalArgumentException("Transaction reference must not be blank.");
        }
        return journalRepositoryPort.findByTransactionRef(transactionRef.trim())
                .orElseThrow(() -> new IllegalArgumentException("GL Journal entry not found for reference: " + transactionRef));
    }
}
