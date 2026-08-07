package com.kab.qershi.transaction.application.usecase;

import com.kab.qershi.transaction.domain.model.EntryType;
import com.kab.qershi.transaction.domain.model.JournalEntry;
import com.kab.qershi.transaction.domain.model.JournalLine;
import com.kab.qershi.transaction.domain.model.Transaction;
import com.kab.qershi.transaction.domain.model.TransactionStatus;
import com.kab.qershi.transaction.domain.model.TransactionType;
import com.kab.qershi.transaction.domain.ports.inbound.CashTransactionUseCase;
import com.kab.qershi.transaction.domain.ports.outbound.AccountClientPort;
import com.kab.qershi.transaction.domain.ports.outbound.JournalRepositoryPort;
import com.kab.qershi.transaction.domain.ports.outbound.TransactionRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Use case service implementing over-the-counter Cash Deposits and Cash Withdrawals.
 * Enforces idempotency, balance safeguards, and General Ledger double-entry postings.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class CashTransactionService implements CashTransactionUseCase {

    private static final Logger log = LoggerFactory.getLogger(CashTransactionService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.systemDefault());

    private final TransactionRepositoryPort transactionRepositoryPort;
    private final JournalRepositoryPort journalRepositoryPort;
    private final AccountClientPort accountClientPort;
    private final com.kab.qershi.transaction.infrastructure.adapters.NotificationGrpcClientAdapter notificationAdapter;

    public CashTransactionService(TransactionRepositoryPort transactionRepositoryPort,
                                  JournalRepositoryPort journalRepositoryPort,
                                  AccountClientPort accountClientPort,
                                  com.kab.qershi.transaction.infrastructure.adapters.NotificationGrpcClientAdapter notificationAdapter) {
        this.transactionRepositoryPort = transactionRepositoryPort;
        this.journalRepositoryPort = journalRepositoryPort;
        this.accountClientPort = accountClientPort;
        this.notificationAdapter = notificationAdapter;
    }

    @Override
    @Transactional
    public Transaction processDeposit(String accountNo, BigDecimal amount, String narration,
                                       String idempotencyKey, UUID processedByUserId) {
        log.info("Processing Cash Deposit: accountNo={}, amount={}, idempotencyKey={}", accountNo, amount, idempotencyKey);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be strictly greater than zero.");
        }

        // 1. Idempotency Check
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Transaction> existing = transactionRepositoryPort.findByIdempotencyKey(idempotencyKey.trim());
            if (existing.isPresent()) {
                log.info("Idempotent request detected. Returning existing transaction {}", existing.get().getTransactionRef());
                return existing.get();
            }
        }

        // 2. Validate Credit capability via gRPC
        AccountClientPort.ValidationResult validation = accountClientPort.validateCredit(accountNo, amount);
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Deposit rejected for account " + accountNo + ": " + validation.message());
        }

        // 3. Fetch Account Info for saccoCode and userId
        AccountClientPort.AccountInfo accountInfo = accountClientPort.getAccountInfo(accountNo);

        // 4. Build & Save Transaction Record
        String txRef = generateTransactionRef("DEP");
        Transaction tx = new Transaction(
                UUID.randomUUID(),
                txRef,
                accountNo,
                accountInfo.saccoCode(),
                UUID.fromString(accountInfo.userId()),
                processedByUserId,
                TransactionType.CASH_DEPOSIT,
                amount,
                "ETB",
                TransactionStatus.COMPLETED,
                narration != null ? narration : "Teller Cash Deposit",
                idempotencyKey,
                Instant.now()
        );
        Transaction savedTx = transactionRepositoryPort.save(tx);

        // 5. Create & Post Balanced General Ledger Journal Entry
        JournalEntry journalEntry = new JournalEntry(
                UUID.randomUUID(),
                txRef,
                Instant.now(),
                "GL Posting for Cash Deposit " + txRef + " on Account " + accountNo,
                Instant.now()
        );

        JournalLine debitLine = new JournalLine(
                UUID.randomUUID(),
                journalEntry.getEntryId(),
                "1010-TELLER-VAULT-CASH",
                EntryType.DEBIT,
                amount,
                Instant.now()
        );

        JournalLine creditLine = new JournalLine(
                UUID.randomUUID(),
                journalEntry.getEntryId(),
                "2010-MEMBER-SAVINGS-" + accountNo,
                EntryType.CREDIT,
                amount,
                Instant.now()
        );

        journalEntry.setLines(List.of(debitLine, creditLine));
        journalRepositoryPort.save(journalEntry);

        try {
            BigDecimal newBal = accountInfo.availableBalance() != null ? accountInfo.availableBalance().add(amount) : amount;
            notificationAdapter.sendCashDepositNotification(accountInfo.phoneNumber(), accountInfo.fullName(), accountNo, amount, newBal);
        } catch (Exception ex) {
            log.warn("Failed dispatching cash deposit SMS: {}", ex.getMessage());
        }

        log.info("Cash Deposit COMPLETED successfully: txRef={}, accountNo={}, amount={}", txRef, accountNo, amount);
        return savedTx;
    }

    @Override
    @Transactional
    public Transaction processWithdrawal(String accountNo, BigDecimal amount, String narration,
                                          String idempotencyKey, UUID processedByUserId) {
        log.info("Processing Cash Withdrawal: accountNo={}, amount={}, idempotencyKey={}", accountNo, amount, idempotencyKey);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be strictly greater than zero.");
        }

        // 1. Idempotency Check
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Transaction> existing = transactionRepositoryPort.findByIdempotencyKey(idempotencyKey.trim());
            if (existing.isPresent()) {
                log.info("Idempotent request detected. Returning existing transaction {}", existing.get().getTransactionRef());
                return existing.get();
            }
        }

        // 2. Validate Debit capability via gRPC
        AccountClientPort.ValidationResult validation = accountClientPort.validateDebit(accountNo, amount);
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Withdrawal rejected for account " + accountNo + ": " + validation.message());
        }

        // 3. Fetch Account Info for saccoCode and userId
        AccountClientPort.AccountInfo accountInfo = accountClientPort.getAccountInfo(accountNo);

        // 4. Build & Save Transaction Record
        String txRef = generateTransactionRef("WTH");
        Transaction tx = new Transaction(
                UUID.randomUUID(),
                txRef,
                accountNo,
                accountInfo.saccoCode(),
                UUID.fromString(accountInfo.userId()),
                processedByUserId,
                TransactionType.CASH_WITHDRAWAL,
                amount,
                "ETB",
                TransactionStatus.COMPLETED,
                narration != null ? narration : "Teller Cash Withdrawal",
                idempotencyKey,
                Instant.now()
        );
        Transaction savedTx = transactionRepositoryPort.save(tx);

        // 5. Create & Post Balanced General Ledger Journal Entry
        JournalEntry journalEntry = new JournalEntry(
                UUID.randomUUID(),
                txRef,
                Instant.now(),
                "GL Posting for Cash Withdrawal " + txRef + " from Account " + accountNo,
                Instant.now()
        );

        JournalLine debitLine = new JournalLine(
                UUID.randomUUID(),
                journalEntry.getEntryId(),
                "2010-MEMBER-SAVINGS-" + accountNo,
                EntryType.DEBIT,
                amount,
                Instant.now()
        );

        JournalLine creditLine = new JournalLine(
                UUID.randomUUID(),
                journalEntry.getEntryId(),
                "1010-TELLER-VAULT-CASH",
                EntryType.CREDIT,
                amount,
                Instant.now()
        );

        journalEntry.setLines(List.of(debitLine, creditLine));
        journalRepositoryPort.save(journalEntry);

        try {
            BigDecimal newBal = accountInfo.availableBalance() != null ? accountInfo.availableBalance().subtract(amount) : BigDecimal.ZERO;
            notificationAdapter.sendCashWithdrawalNotification(accountInfo.phoneNumber(), accountInfo.fullName(), accountNo, amount, newBal);
        } catch (Exception ex) {
            log.warn("Failed dispatching cash withdrawal SMS: {}", ex.getMessage());
        }

        log.info("Cash Withdrawal COMPLETED successfully: txRef={}, accountNo={}, amount={}", txRef, accountNo, amount);
        return savedTx;
    }

    private String generateTransactionRef(String prefix) {
        String dateStr = DATE_FORMATTER.format(Instant.now());
        int randomNum = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "TX-" + prefix + "-" + dateStr + "-" + randomNum;
    }
}
