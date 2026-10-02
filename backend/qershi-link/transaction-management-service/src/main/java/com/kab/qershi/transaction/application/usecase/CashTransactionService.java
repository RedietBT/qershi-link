package com.kab.qershi.transaction.application.usecase;

import com.kab.qershi.common.event.TransactionCompletedEvent;
import com.kab.qershi.transaction.domain.model.EntryType;
import com.kab.qershi.transaction.domain.model.JournalEntry;
import com.kab.qershi.transaction.domain.model.JournalLine;
import com.kab.qershi.transaction.domain.model.Transaction;
import com.kab.qershi.transaction.domain.model.TransactionAuditLog;
import com.kab.qershi.transaction.domain.model.TransactionStatus;
import com.kab.qershi.transaction.domain.model.TransactionType;
import com.kab.qershi.transaction.domain.ports.inbound.CashTransactionUseCase;
import com.kab.qershi.transaction.domain.ports.inbound.TellerTillUseCase;
import com.kab.qershi.transaction.domain.ports.outbound.AccountClientPort;
import com.kab.qershi.transaction.domain.ports.outbound.JournalRepositoryPort;
import com.kab.qershi.transaction.domain.ports.outbound.NotificationClientPort;
import com.kab.qershi.transaction.domain.ports.outbound.TenantContextPort;
import com.kab.qershi.transaction.domain.ports.outbound.TransactionAuditLogRepositoryPort;
import com.kab.qershi.transaction.domain.ports.outbound.TransactionEventPublisherPort;
import com.kab.qershi.transaction.domain.ports.outbound.TransactionRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Use case service implementing over-the-counter Cash Deposits and Cash Withdrawals.
 * Enforces idempotency, balance safeguards, and General Ledger double-entry postings.
 * Strictly decoupled from infrastructure layer via Hexagonal Architecture ports.
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
    private final NotificationClientPort notificationClientPort;
    private final TransactionAuditLogRepositoryPort auditLogRepositoryPort;
    private final TellerTillUseCase tellerTillUseCase;
    private final TransactionEventPublisherPort eventPublisher;
    private final TenantContextPort tenantContextPort;

    public CashTransactionService(TransactionRepositoryPort transactionRepositoryPort,
                                  JournalRepositoryPort journalRepositoryPort,
                                  AccountClientPort accountClientPort,
                                  NotificationClientPort notificationClientPort,
                                  TransactionAuditLogRepositoryPort auditLogRepositoryPort,
                                  TellerTillUseCase tellerTillUseCase,
                                  TransactionEventPublisherPort eventPublisher,
                                  TenantContextPort tenantContextPort) {
        this.transactionRepositoryPort = transactionRepositoryPort;
        this.journalRepositoryPort = journalRepositoryPort;
        this.accountClientPort = accountClientPort;
        this.notificationClientPort = notificationClientPort;
        this.auditLogRepositoryPort = auditLogRepositoryPort;
        this.tellerTillUseCase = tellerTillUseCase;
        this.eventPublisher = eventPublisher;
        this.tenantContextPort = tenantContextPort;
    }

    @Override
    @Transactional
    public Transaction processDeposit(String accountNo, BigDecimal amount, String narration,
                                       String idempotencyKey, UUID processedByUserId) {
        log.info("Processing Cash Deposit: accountNo={}, amount={}, idempotencyKey={}", accountNo, amount, idempotencyKey);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be strictly greater than zero.");
        }
        if (accountNo == null || accountNo.isBlank()) {
            throw new IllegalArgumentException("Account number must not be blank.");
        }

        // 1. Idempotency Check
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Transaction> existing = transactionRepositoryPort.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Idempotent deposit request detected: returning existing transaction {}", existing.get().getTransactionRef());
                return existing.get();
            }
        }

        // 2. Account Invariant Validation via gRPC Port
        AccountClientPort.AccountInfo accountInfo = accountClientPort.getAccountInfo(accountNo);
        if (accountInfo == null) {
            throw new IllegalArgumentException("Target savings account not found: " + accountNo);
        }
        if (!"ACTIVE".equalsIgnoreCase(accountInfo.status())) {
            throw new IllegalStateException("Cannot deposit to account: current account status is " + accountInfo.status());
        }

        // 3. Generate Unique CBS Transaction Reference
        String txRef = generateTransactionRef("DEP");

        // 4. Create and Persist Domain Transaction Entity
        Transaction tx = new Transaction(
                UUID.randomUUID(),
                txRef,
                accountNo,
                processedByUserId,
                TransactionType.CASH_DEPOSIT,
                amount,
                TransactionStatus.COMPLETED,
                narration != null ? narration.trim() : "Over-the-counter cash deposit",
                idempotencyKey
        );
        Transaction savedTx = transactionRepositoryPort.save(tx);

        try {
            auditLogRepositoryPort.save(new TransactionAuditLog(
                    null,
                    txRef,
                    accountNo,
                    processedByUserId,
                    "CASH_DEPOSIT",
                    "Deposit Amount: ETB " + amount + " | Narration: " + narration,
                    OffsetDateTime.now()
            ));
        } catch (Exception ex) {
            log.warn("Failed writing deposit transaction audit log: {}", ex.getMessage());
        }

        // 4.5. Update the actual account book balance via gRPC
        boolean balanceUpdated = accountClientPort.postTransaction(accountNo, amount, "CREDIT");
        if (!balanceUpdated) {
            throw new RuntimeException("Failed to update account balance in account-management-service.");
        }

        // 4.6. Mutate physical cash drawer balance for operating teller
        if (processedByUserId != null) {
            tellerTillUseCase.recordCashMovement(processedByUserId, amount, true);
        }

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
            String tenantSchema = tenantContextPort != null ? tenantContextPort.getCurrentTenantSchema() : "default";

            // Publish async Kafka domain event (partitioned by saccoCode)
            eventPublisher.publishTransactionCompleted(new TransactionCompletedEvent(
                    tenantSchema,
                    txRef,
                    accountNo,
                    accountInfo.phoneNumber(),
                    accountInfo.fullName(),
                    "DEPOSIT",
                    amount,
                    newBal,
                    null,
                    null,
                    Instant.now()
            ));

            notificationClientPort.sendCashDepositNotification(accountInfo.phoneNumber(), accountInfo.fullName(), accountNo, amount, newBal);
        } catch (Exception ex) {
            log.warn("Failed dispatching cash deposit notification: {}", ex.getMessage());
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
        if (accountNo == null || accountNo.isBlank()) {
            throw new IllegalArgumentException("Account number must not be blank.");
        }

        // 1. Idempotency Check
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Transaction> existing = transactionRepositoryPort.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Idempotent withdrawal request detected: returning existing transaction {}", existing.get().getTransactionRef());
                return existing.get();
            }
        }

        // 2. Account Balance Safeguard via gRPC Port
        AccountClientPort.AccountInfo accountInfo = accountClientPort.getAccountInfo(accountNo);
        if (accountInfo == null) {
            throw new IllegalArgumentException("Target savings account not found: " + accountNo);
        }
        if (!"ACTIVE".equalsIgnoreCase(accountInfo.status())) {
            throw new IllegalStateException("Cannot withdraw from account: current account status is " + accountInfo.status());
        }
        if (accountInfo.availableBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds: Available balance ETB " +
                    accountInfo.availableBalance() + ", withdrawal requested: ETB " + amount);
        }

        // 3. Generate Unique CBS Transaction Reference
        String txRef = generateTransactionRef("WTH");

        // 4. Create and Persist Domain Transaction Entity
        Transaction tx = new Transaction(
                UUID.randomUUID(),
                txRef,
                accountNo,
                processedByUserId,
                TransactionType.CASH_WITHDRAWAL,
                amount,
                TransactionStatus.COMPLETED,
                narration != null ? narration.trim() : "Over-the-counter cash withdrawal",
                idempotencyKey
        );
        Transaction savedTx = transactionRepositoryPort.save(tx);

        try {
            auditLogRepositoryPort.save(new TransactionAuditLog(
                    null,
                    txRef,
                    accountNo,
                    processedByUserId,
                    "CASH_WITHDRAWAL",
                    "Withdrawal Amount: ETB " + amount + " | Narration: " + narration,
                    OffsetDateTime.now()
            ));
        } catch (Exception ex) {
            log.warn("Failed writing withdrawal transaction audit log: {}", ex.getMessage());
        }

        // 4.4. Mutate physical cash drawer balance for operating teller (safeguards drawer balance)
        if (processedByUserId != null) {
            tellerTillUseCase.recordCashMovement(processedByUserId, amount, false);
        }

        // 4.5. Update the actual account book balance via gRPC
        boolean balanceUpdated = accountClientPort.postTransaction(accountNo, amount, "DEBIT");
        if (!balanceUpdated) {
            throw new RuntimeException("Failed to update account balance in account-management-service.");
        }

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
            String tenantSchema = tenantContextPort != null ? tenantContextPort.getCurrentTenantSchema() : "default";

            // Publish async Kafka domain event (partitioned by saccoCode)
            eventPublisher.publishTransactionCompleted(new TransactionCompletedEvent(
                    tenantSchema,
                    txRef,
                    accountNo,
                    accountInfo.phoneNumber(),
                    accountInfo.fullName(),
                    "WITHDRAWAL",
                    amount,
                    newBal,
                    null,
                    null,
                    Instant.now()
            ));

            notificationClientPort.sendCashWithdrawalNotification(accountInfo.phoneNumber(), accountInfo.fullName(), accountNo, amount, newBal);
        } catch (Exception ex) {
            log.warn("Failed dispatching cash withdrawal notification: {}", ex.getMessage());
        }

        log.info("Cash Withdrawal COMPLETED successfully: txRef={}, accountNo={}, amount={}", txRef, accountNo, amount);
        return savedTx;
    }

    private String generateTransactionRef(String prefix) {
        String dateStr = DATE_FORMATTER.format(Instant.now());
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TX-" + prefix + "-" + dateStr + "-" + uniqueSuffix;
    }
}
