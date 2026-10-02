package com.kab.qershi.transaction.application.usecase;

import com.kab.qershi.common.event.TransactionCompletedEvent;
import com.kab.qershi.transaction.domain.model.EntryType;
import com.kab.qershi.transaction.domain.model.JournalEntry;
import com.kab.qershi.transaction.domain.model.JournalLine;
import com.kab.qershi.transaction.domain.model.Transaction;
import com.kab.qershi.transaction.domain.model.TransactionAuditLog;
import com.kab.qershi.transaction.domain.model.TransactionStatus;
import com.kab.qershi.transaction.domain.model.TransactionType;
import com.kab.qershi.transaction.domain.ports.inbound.TransferUseCase;
import com.kab.qershi.transaction.domain.ports.outbound.AccountClientPort;
import com.kab.qershi.transaction.domain.ports.outbound.JournalRepositoryPort;
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
 * Use case service implementing Member-to-Member internal transfers.
 * Performs atomic debit/credit validations and posts General Ledger entries.
 * Pure Hexagonal Architecture implementation with zero infrastructure coupling.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class TransferService implements TransferUseCase {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.systemDefault());

    private final TransactionRepositoryPort transactionRepositoryPort;
    private final JournalRepositoryPort journalRepositoryPort;
    private final AccountClientPort accountClientPort;
    private final TransactionAuditLogRepositoryPort auditLogRepositoryPort;
    private final TransactionEventPublisherPort eventPublisher;
    private final TenantContextPort tenantContextPort;

    public TransferService(TransactionRepositoryPort transactionRepositoryPort,
                           JournalRepositoryPort journalRepositoryPort,
                           AccountClientPort accountClientPort,
                           TransactionAuditLogRepositoryPort auditLogRepositoryPort,
                           TransactionEventPublisherPort eventPublisher,
                           TenantContextPort tenantContextPort) {
        this.transactionRepositoryPort = transactionRepositoryPort;
        this.journalRepositoryPort = journalRepositoryPort;
        this.accountClientPort = accountClientPort;
        this.auditLogRepositoryPort = auditLogRepositoryPort;
        this.eventPublisher = eventPublisher;
        this.tenantContextPort = tenantContextPort;
    }

    @Override
    @Transactional
    public Transaction processTransfer(String senderAccountNo, String receiverAccountNo, BigDecimal amount,
                                        String narration, String idempotencyKey, UUID processedByUserId) {
        log.info("Processing Member Transfer: sender={}, receiver={}, amount={}, idempotencyKey={}",
                senderAccountNo, receiverAccountNo, amount, idempotencyKey);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be strictly greater than zero.");
        }

        if (senderAccountNo == null || senderAccountNo.trim().equalsIgnoreCase(receiverAccountNo)) {
            throw new IllegalArgumentException("Sender and receiver account numbers must be distinct.");
        }

        // 1. Idempotency Check
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Transaction> existing = transactionRepositoryPort.findByIdempotencyKey(idempotencyKey.trim());
            if (existing.isPresent()) {
                log.info("Idempotent transfer request detected: returning existing transaction {}", existing.get().getTransactionRef());
                return existing.get();
            }
        }

        // 2. Validate Sender Account
        AccountClientPort.AccountInfo senderInfo = accountClientPort.getAccountInfo(senderAccountNo);
        if (senderInfo == null) {
            throw new IllegalArgumentException("Sender account not found: " + senderAccountNo);
        }
        if (!"ACTIVE".equalsIgnoreCase(senderInfo.status())) {
            throw new IllegalStateException("Sender account is not ACTIVE. Current status: " + senderInfo.status());
        }
        if (senderInfo.availableBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds in sender account: Available ETB " +
                    senderInfo.availableBalance() + ", transfer requested: ETB " + amount);
        }

        // 3. Validate Receiver Account
        AccountClientPort.AccountInfo receiverCheck = accountClientPort.getAccountInfo(receiverAccountNo);
        if (receiverCheck == null) {
            throw new IllegalArgumentException("Receiver account not found: " + receiverAccountNo);
        }
        if (!"ACTIVE".equalsIgnoreCase(receiverCheck.status())) {
            throw new IllegalStateException("Receiver account is not ACTIVE. Current status: " + receiverCheck.status());
        }

        // 4. Generate Unique CBS Transaction Reference
        String txRef = generateTransactionRef("TRF");

        // 5. Create & Save Domain Transaction Record
        Transaction tx = new Transaction(
                UUID.randomUUID(),
                txRef,
                senderAccountNo,
                processedByUserId,
                TransactionType.MEMBER_TRANSFER,
                amount,
                TransactionStatus.COMPLETED,
                narration != null ? narration.trim() : "Member to member fund transfer",
                idempotencyKey
        );
        Transaction savedTx = transactionRepositoryPort.save(tx);

        try {
            auditLogRepositoryPort.save(new TransactionAuditLog(
                    null,
                    txRef,
                    senderAccountNo,
                    processedByUserId,
                    "MEMBER_TRANSFER",
                    "Transfer Amount: ETB " + amount + " | To Account: " + receiverAccountNo + " | Narration: " + narration,
                    OffsetDateTime.now()
            ));
        } catch (Exception ex) {
            log.warn("Failed writing transfer transaction audit log: {}", ex.getMessage());
        }

        // 5.5. Debit Sender Account & Credit Receiver Account via gRPC
        boolean debitSenderOk = accountClientPort.postTransaction(senderAccountNo, amount, "DEBIT");
        if (!debitSenderOk) {
            throw new RuntimeException("Failed to debit sender account in account-management-service.");
        }

        boolean creditReceiverOk = accountClientPort.postTransaction(receiverAccountNo, amount, "CREDIT");
        if (!creditReceiverOk) {
            // Reversal logic could occur here in full CBS saga; for now trigger compensating reversal
            accountClientPort.postTransaction(senderAccountNo, amount, "CREDIT");
            throw new RuntimeException("Failed to credit receiver account; sender debit has been reversed.");
        }

        // 6. Post Balanced General Ledger Double-Entry
        JournalEntry journalEntry = new JournalEntry(
                UUID.randomUUID(),
                txRef,
                Instant.now(),
                "GL Posting for Transfer " + txRef + " from " + senderAccountNo + " to " + receiverAccountNo,
                Instant.now()
        );

        JournalLine debitSenderLine = new JournalLine(
                UUID.randomUUID(),
                journalEntry.getEntryId(),
                "2010-MEMBER-SAVINGS-" + senderAccountNo,
                EntryType.DEBIT,
                amount,
                Instant.now()
        );

        JournalLine creditReceiverLine = new JournalLine(
                UUID.randomUUID(),
                journalEntry.getEntryId(),
                "2010-MEMBER-SAVINGS-" + receiverAccountNo,
                EntryType.CREDIT,
                amount,
                Instant.now()
        );

        journalEntry.setLines(List.of(debitSenderLine, creditReceiverLine));
        journalRepositoryPort.save(journalEntry);

        try {
            String tenantSchema = tenantContextPort != null ? tenantContextPort.getCurrentTenantSchema() : "default";
            AccountClientPort.AccountInfo receiverInfo = accountClientPort.getAccountInfo(receiverAccountNo);

            eventPublisher.publishTransactionCompleted(new TransactionCompletedEvent(
                    tenantSchema,
                    txRef,
                    senderAccountNo,
                    senderInfo != null ? senderInfo.phoneNumber() : null,
                    senderInfo != null ? senderInfo.fullName() : "Member",
                    "TRANSFER",
                    amount,
                    senderInfo != null && senderInfo.availableBalance() != null ? senderInfo.availableBalance() : BigDecimal.ZERO,
                    receiverInfo != null ? receiverInfo.fullName() : receiverAccountNo,
                    receiverAccountNo,
                    Instant.now()
            ));
        } catch (Exception ex) {
            log.warn("Failed dispatching transfer completed Kafka event: {}", ex.getMessage());
        }

        log.info("Member Transfer COMPLETED successfully: txRef={}, sender={}, receiver={}, amount={}",
                txRef, senderAccountNo, receiverAccountNo, amount);
        return savedTx;
    }

    private String generateTransactionRef(String prefix) {
        String dateStr = DATE_FORMATTER.format(Instant.now());
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TX-" + prefix + "-" + dateStr + "-" + uniqueSuffix;
    }
}
