package com.kab.qershi.transaction;

import com.kab.qershi.transaction.application.usecase.TransferService;
import com.kab.qershi.transaction.domain.model.Transaction;
import com.kab.qershi.transaction.domain.model.TransactionStatus;
import com.kab.qershi.transaction.domain.ports.outbound.AccountClientPort;
import com.kab.qershi.transaction.domain.ports.outbound.JournalRepositoryPort;
import com.kab.qershi.transaction.domain.ports.outbound.TransactionRepositoryPort;
import com.kab.qershi.transaction.infrastructure.persistence.SpringDataTransactionAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("TransferService Business Logic Tests")
class TransferServiceTest {

    private TransactionRepositoryPort transactionRepositoryPort;
    private JournalRepositoryPort journalRepositoryPort;
    private AccountClientPort accountClientPort;
    private SpringDataTransactionAuditLogRepository auditLogRepository;
    private TransferService transferService;

    @BeforeEach
    void setUp() {
        transactionRepositoryPort = mock(TransactionRepositoryPort.class);
        journalRepositoryPort = mock(JournalRepositoryPort.class);
        accountClientPort = mock(AccountClientPort.class);
        auditLogRepository = mock(SpringDataTransactionAuditLogRepository.class);

        transferService = new TransferService(
                transactionRepositoryPort,
                journalRepositoryPort,
                accountClientPort,
                auditLogRepository
        );
    }

    @Test
    @DisplayName("Should successfully debit sender, credit receiver, and complete transfer")
    void shouldSuccessfullyProcessTransferWithBalanceUpdates() {
        String senderAcc = "ACC-001";
        String receiverAcc = "ACC-002";
        BigDecimal amount = new BigDecimal("500.00");
        UUID operatorId = UUID.randomUUID();

        when(transactionRepositoryPort.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(accountClientPort.validateDebit(eq(senderAcc), eq(amount)))
                .thenReturn(new AccountClientPort.ValidationResult(true, "OK", new BigDecimal("1000.00")));
        when(accountClientPort.validateCredit(eq(receiverAcc), eq(amount)))
                .thenReturn(new AccountClientPort.ValidationResult(true, "OK", new BigDecimal("200.00")));
        when(accountClientPort.getAccountInfo(eq(senderAcc)))
                .thenReturn(new AccountClientPort.AccountInfo(
                        UUID.randomUUID().toString(), senderAcc, UUID.randomUUID().toString(),
                        "SACCO_1", "BR_1", "PROD_SAVINGS", new BigDecimal("1000.00"),
                        BigDecimal.ZERO, new BigDecimal("1000.00"), "ACTIVE", "NONE", "+251911223344", "John Doe"
                ));
        when(transactionRepositoryPort.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountClientPort.postTransaction(eq(senderAcc), eq(amount), eq("DEBIT"))).thenReturn(true);
        when(accountClientPort.postTransaction(eq(receiverAcc), eq(amount), eq("CREDIT"))).thenReturn(true);

        Transaction result = transferService.processTransfer(
                senderAcc, receiverAcc, amount, "Internal Transfer", "IDEMP-123", operatorId
        );

        assertNotNull(result);
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        assertEquals(amount, result.getAmount());

        // Verify balance updates were performed
        verify(accountClientPort, times(1)).postTransaction(eq(senderAcc), eq(amount), eq("DEBIT"));
        verify(accountClientPort, times(1)).postTransaction(eq(receiverAcc), eq(amount), eq("CREDIT"));
        verify(journalRepositoryPort, times(1)).save(any());
    }

    @Test
    @DisplayName("Should compensate sender if receiver credit fails")
    void shouldCompensateSenderWhenReceiverCreditFails() {
        String senderAcc = "ACC-001";
        String receiverAcc = "ACC-002";
        BigDecimal amount = new BigDecimal("500.00");
        UUID operatorId = UUID.randomUUID();

        when(accountClientPort.validateDebit(eq(senderAcc), eq(amount)))
                .thenReturn(new AccountClientPort.ValidationResult(true, "OK", new BigDecimal("1000.00")));
        when(accountClientPort.validateCredit(eq(receiverAcc), eq(amount)))
                .thenReturn(new AccountClientPort.ValidationResult(true, "OK", new BigDecimal("200.00")));
        when(accountClientPort.getAccountInfo(eq(senderAcc)))
                .thenReturn(new AccountClientPort.AccountInfo(
                        UUID.randomUUID().toString(), senderAcc, UUID.randomUUID().toString(),
                        "SACCO_1", "BR_1", "PROD_SAVINGS", new BigDecimal("1000.00"),
                        BigDecimal.ZERO, new BigDecimal("1000.00"), "ACTIVE", "NONE", "+251911223344", "John Doe"
                ));
        when(transactionRepositoryPort.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Debit succeeds, but credit fails
        when(accountClientPort.postTransaction(eq(senderAcc), eq(amount), eq("DEBIT"))).thenReturn(true);
        when(accountClientPort.postTransaction(eq(receiverAcc), eq(amount), eq("CREDIT"))).thenReturn(false);
        when(accountClientPort.postTransaction(eq(senderAcc), eq(amount), eq("CREDIT"))).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                transferService.processTransfer(senderAcc, receiverAcc, amount, "Test", null, operatorId)
        );

        assertTrue(ex.getMessage().contains("Failed to credit receiver account"));

        // Verify compensation credit was triggered for sender
        verify(accountClientPort, times(1)).postTransaction(eq(senderAcc), eq(amount), eq("DEBIT"));
        verify(accountClientPort, times(1)).postTransaction(eq(receiverAcc), eq(amount), eq("CREDIT"));
        verify(accountClientPort, times(1)).postTransaction(eq(senderAcc), eq(amount), eq("CREDIT"));
    }
}
