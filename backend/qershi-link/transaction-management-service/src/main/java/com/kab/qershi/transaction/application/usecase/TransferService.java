package com.kab.qershi.transaction.application.usecase;

import com.kab.qershi.transaction.domain.model.EntryType;
import com.kab.qershi.transaction.domain.model.JournalEntry;
import com.kab.qershi.transaction.domain.model.JournalLine;
import com.kab.qershi.transaction.domain.model.Transaction;
import com.kab.qershi.transaction.domain.model.TransactionStatus;
import com.kab.qershi.transaction.domain.model.TransactionType;
import com.kab.qershi.transaction.domain.ports.inbound.TransferUseCase;
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
 * Use case service implementing Member-to-Member internal transfers.
 * Performs atomic debit/credit validations and posts General Ledger entries.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
import com.kab.qershi.transaction.infrastructure.persistence.SpringDataTransactionAuditLogRepository;
import com.kab.qershi.transaction.infrastructure.persistence.TransactionAuditLogEntity;
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
    private final SpringDataTransactionAuditLogRepository auditLogRepository;

    public TransferService(TransactionRepositoryPort transactionRepositoryPort,
                           JournalRepositoryPort journalRepositoryPort,
                           AccountClientPort accountClientPort,
                           SpringDataTransactionAuditLogRepository auditLogRepository) {
        this.transactionRepositoryPort = transactionRepositoryPort;
        this.journalRepositoryPort = journalRepositoryPort;
        this.accountClientPort = accountClientPort;
        this.auditLogRepository = auditLogRepository;
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
                log.info("Idempotent request detected. Returning existing transaction {}", existing.get().getTransactionRef());
                return existing.get();
            }
        }

        // 2. Assess applicable internal transfer fee via gRPC
        AccountClientPort.TariffResult tariff = accountClientPort.calculateTariff("TRANSFER_INTERNAL", amount);
        BigDecimal fee = (tariff != null && tariff.feeApplicable()) ? tariff.feeAmount() : BigDecimal.ZERO;
        BigDecimal totalDebit = amount.add(fee);

        // 3. Validate Sender Debit capability via gRPC for total amount (transfer + fee)
        AccountClientPort.ValidationResult senderValidation = accountClientPort.validateDebit(senderAccountNo, totalDebit);
        if (!senderValidation.isValid()) {
            throw new IllegalArgumentException("Transfer rejected: Sender account " + senderAccountNo + " - " + senderValidation.message());
        }

        // 4. Validate Receiver Credit capability via gRPC
        AccountClientPort.ValidationResult receiverValidation = accountClientPort.validateCredit(receiverAccountNo, amount);
        if (!receiverValidation.isValid()) {
            throw new IllegalArgumentException("Transfer rejected: Receiver account " + receiverAccountNo + " - " + receiverValidation.message());
        }

        // 5. Fetch Sender Account Info
        AccountClientPort.AccountInfo senderInfo = accountClientPort.getAccountInfo(senderAccountNo);

        // 6. Build & Save Master Transaction Record for Transfer
        String txRef = generateTransactionRef("TRF");
        Transaction tx = new Transaction(
                UUID.randomUUID(),
                txRef,
                senderAccountNo,
                senderInfo.saccoCode(),
                UUID.fromString(senderInfo.userId()),
                processedByUserId,
                TransactionType.MEMBER_TRANSFER,
                amount,
                "ETB",
                TransactionStatus.COMPLETED,
                narration != null ? narration : "Member Transfer to " + receiverAccountNo,
                idempotencyKey,
                Instant.now()
        );
        Transaction savedTx = transactionRepositoryPort.save(tx);

        try {
            String auditDesc = "Transfer Amount: ETB " + amount +
                    (fee.compareTo(BigDecimal.ZERO) > 0 ? " | Transfer Fee (" + tariff.tariffCode() + "): ETB " + fee : "") +
                    " | To Account: " + receiverAccountNo + " | Narration: " + narration;
            auditLogRepository.save(new TransactionAuditLogEntity(
                    null,
                    txRef,
                    senderAccountNo,
                    processedByUserId,
                    "MEMBER_TRANSFER",
                    auditDesc,
                    OffsetDateTime.now()
            ));
        } catch (Exception ex) {
            log.warn("Failed writing transfer transaction audit log: {}", ex.getMessage());
        }

        // 5.5. Debit Sender Account (transfer + fee) & Credit Receiver Account (transfer amount) via gRPC
        boolean debitSenderOk = accountClientPort.postTransaction(senderAccountNo, totalDebit, "DEBIT");
        if (!debitSenderOk) {
            throw new RuntimeException("Failed to debit sender account " + senderAccountNo + " in account-management-service.");
        }

        boolean creditReceiverOk = accountClientPort.postTransaction(receiverAccountNo, amount, "CREDIT");
        if (!creditReceiverOk) {
            // Compensate sender account if receiver credit fails
            log.error("Credit to receiver account {} failed! Initiating compensation credit for sender account {}", receiverAccountNo, senderAccountNo);
            boolean compensated = accountClientPort.postTransaction(senderAccountNo, totalDebit, "CREDIT");
            if (!compensated) {
                log.error("CRITICAL: Failed to compensate sender account {} for amount {} after failed transfer to {}", senderAccountNo, totalDebit, receiverAccountNo);
            }
            throw new RuntimeException("Failed to credit receiver account " + receiverAccountNo + " in account-management-service.");
        }

        // 6. Create & Post Balanced General Ledger Journal Entry
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
                totalDebit,
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

        java.util.List<JournalLine> lines = new java.util.ArrayList<>();
        lines.add(debitSenderLine);
        lines.add(creditReceiverLine);

        if (fee.compareTo(BigDecimal.ZERO) > 0) {
            String feeGl = (tariff != null && tariff.feeGlCode() != null) ? tariff.feeGlCode() : "4020";
            JournalLine creditFeeLine = new JournalLine(
                    UUID.randomUUID(),
                    journalEntry.getEntryId(),
                    feeGl + "-FEE-COMMISSION-INCOME",
                    EntryType.CREDIT,
                    fee,
                    Instant.now()
            );
            lines.add(creditFeeLine);
        }

        journalEntry.setLines(lines);
        journalRepositoryPort.save(journalEntry);

        log.info("Member Transfer COMPLETED successfully: txRef={}, sender={}, receiver={}, amount={}, fee={}",
                txRef, senderAccountNo, receiverAccountNo, amount, fee);
        return savedTx;
    }

    private String generateTransactionRef(String prefix) {
        String dateStr = DATE_FORMATTER.format(Instant.now());
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TX-" + prefix + "-" + dateStr + "-" + uniqueSuffix;
    }
}
