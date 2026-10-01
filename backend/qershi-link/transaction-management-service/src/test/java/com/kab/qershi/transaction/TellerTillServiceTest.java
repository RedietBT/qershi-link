package com.kab.qershi.transaction;

import com.kab.qershi.transaction.application.usecase.TellerTillService;
import com.kab.qershi.transaction.domain.model.JournalEntry;
import com.kab.qershi.transaction.domain.model.TillStatus;
import com.kab.qershi.transaction.domain.ports.outbound.JournalRepositoryPort;
import com.kab.qershi.transaction.infrastructure.persistence.*;
import com.kab.qershi.transaction.infrastructure.rest.dto.CloseTillRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("TellerTillService Blind Balancing Tests")
class TellerTillServiceTest {

    private SpringDataTellerTillRepository tillRepository;
    private SpringDataTillCashReconciliationRepository reconciliationRepository;
    private SpringDataTillDenominationRepository denominationRepository;
    private SpringDataTillClosingLogRepository closingLogRepository;
    private JournalRepositoryPort journalRepositoryPort;
    private TellerTillService tellerTillService;

    @BeforeEach
    void setUp() {
        tillRepository = mock(SpringDataTellerTillRepository.class);
        reconciliationRepository = mock(SpringDataTillCashReconciliationRepository.class);
        denominationRepository = mock(SpringDataTillDenominationRepository.class);
        closingLogRepository = mock(SpringDataTillClosingLogRepository.class);
        journalRepositoryPort = mock(JournalRepositoryPort.class);

        tellerTillService = new TellerTillService(
                tillRepository,
                reconciliationRepository,
                denominationRepository,
                closingLogRepository,
                journalRepositoryPort
        );
    }

    @Test
    @DisplayName("Should balance till perfectly when physical count equals electronic ledger")
    void shouldBalanceTillWithoutVariance() {
        UUID tillId = UUID.randomUUID();
        UUID tellerId = UUID.randomUUID();

        TellerTillEntity till = new TellerTillEntity();
        till.setTillId(tillId);
        till.setBranchId(UUID.randomUUID());
        till.setBranchCode("B001");
        till.setTillName("Main Drawer 1");
        till.setTellerUserId(tellerId);
        till.setStatus(TillStatus.OPEN);
        till.setTillGlCode("1020-001");
        // Electronic ledger has 32,500 ETB
        till.setCurrentCash(new BigDecimal("32500.00"));

        when(tillRepository.findByTellerUserId(tellerId)).thenReturn(Optional.of(till));
        when(tillRepository.save(any(TellerTillEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reconciliationRepository.save(any(TillCashReconciliationEntity.class))).thenAnswer(invocation -> {
            TillCashReconciliationEntity e = invocation.getArgument(0);
            e.setReconciliationId(UUID.randomUUID());
            return e;
        });

        // Physical count:
        // 200 * 150 = 30,000
        // 100 * 20  = 2,000
        // 50 * 10   = 500
        // Total = 32,500
        CloseTillRequest request = new CloseTillRequest(
                null,
                150, 20, 10, 0, 0,
                BigDecimal.ZERO,
                "End of morning shift"
        );

        TillCashReconciliationEntity result = tellerTillService.closeAndReconcileTill(tellerId, request);

        assertNotNull(result);
        assertEquals(new BigDecimal("32500.00"), result.getPhysicalCashCounted());
        assertEquals(new BigDecimal("32500.00"), result.getElectronicCashBalance());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getCashVariance()));
        assertEquals("NONE", result.getVarianceType());
        assertEquals("BALANCED", result.getStatus());
        assertEquals(TillStatus.CLOSED, till.getStatus());

        // Zero variance should not post shortage/overage GL entry
        verify(journalRepositoryPort, never()).save(any(JournalEntry.class));
        verify(denominationRepository, atLeastOnce()).saveAll(anyList());
        verify(closingLogRepository, times(1)).save(any(TillClosingLogEntity.class));
    }

    @Test
    @DisplayName("Should detect cash shortage and require supervisor approval when variance > 100 ETB")
    void shouldDetectShortageAndRequireSupervisorApproval() {
        UUID tillId = UUID.randomUUID();
        UUID tellerId = UUID.randomUUID();

        TellerTillEntity till = new TellerTillEntity();
        till.setTillId(tillId);
        till.setBranchId(UUID.randomUUID());
        till.setBranchCode("B001");
        till.setTillName("Main Drawer 1");
        till.setTellerUserId(tellerId);
        till.setStatus(TillStatus.OPEN);
        till.setTillGlCode("1020-001");
        // Electronic ledger: 10,000 ETB
        till.setCurrentCash(new BigDecimal("10000.00"));

        when(tillRepository.findByTellerUserId(tellerId)).thenReturn(Optional.of(till));
        when(tillRepository.save(any(TellerTillEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reconciliationRepository.save(any(TillCashReconciliationEntity.class))).thenAnswer(invocation -> {
            TillCashReconciliationEntity e = invocation.getArgument(0);
            if (e.getReconciliationId() == null) e.setReconciliationId(UUID.randomUUID());
            return e;
        });

        // Physical count:
        // 200 * 45 = 9,000
        // 100 * 5  = 500
        // Total = 9,500 ETB (Shortage of 500 ETB)
        CloseTillRequest request = new CloseTillRequest(
                null,
                45, 5, 0, 0, 0,
                BigDecimal.ZERO,
                "Physical count shortage detected"
        );

        TillCashReconciliationEntity result = tellerTillService.closeAndReconcileTill(tellerId, request);

        assertNotNull(result);
        assertEquals(new BigDecimal("9500.00"), result.getPhysicalCashCounted());
        assertEquals("SHORTAGE", result.getVarianceType());
        assertEquals(new BigDecimal("500.00"), result.getVarianceAmount());
        assertEquals("5090", result.getVarianceGlCode());
        // Since variance (500) > threshold (100), requires supervisor
        assertEquals("PENDING_SUPERVISOR_APPROVAL", result.getStatus());

        // GL Entry posted for Shortage: Debit 5090, Credit 1020-001
        ArgumentCaptor<JournalEntry> journalCaptor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalRepositoryPort, times(1)).save(journalCaptor.capture());
        JournalEntry entry = journalCaptor.getValue();
        assertEquals(2, entry.getLines().size());
        assertEquals(new BigDecimal("500.00"), entry.getLines().get(0).getAmount());
    }

    @Test
    @DisplayName("Should detect cash overage and post income GL entry")
    void shouldDetectOverageAndPostIncomeEntry() {
        UUID tillId = UUID.randomUUID();
        UUID tellerId = UUID.randomUUID();

        TellerTillEntity till = new TellerTillEntity();
        till.setTillId(tillId);
        till.setBranchId(UUID.randomUUID());
        till.setBranchCode("B001");
        till.setTillName("Main Drawer 1");
        till.setTellerUserId(tellerId);
        till.setStatus(TillStatus.OPEN);
        till.setTillGlCode("1020-001");
        // Electronic ledger: 5,000 ETB
        till.setCurrentCash(new BigDecimal("5000.00"));

        when(tillRepository.findByTellerUserId(tellerId)).thenReturn(Optional.of(till));
        when(tillRepository.save(any(TellerTillEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reconciliationRepository.save(any(TillCashReconciliationEntity.class))).thenAnswer(invocation -> {
            TillCashReconciliationEntity e = invocation.getArgument(0);
            if (e.getReconciliationId() == null) e.setReconciliationId(UUID.randomUUID());
            return e;
        });

        // Physical count:
        // 200 * 25 = 5,000
        // Coins = 50.00
        // Total = 5,050 ETB (Overage of 50 ETB <= 100 ETB threshold)
        CloseTillRequest request = new CloseTillRequest(
                null,
                25, 0, 0, 0, 0,
                new BigDecimal("50.00"),
                "Minor coin overage"
        );

        TillCashReconciliationEntity result = tellerTillService.closeAndReconcileTill(tellerId, request);

        assertNotNull(result);
        assertEquals(new BigDecimal("5050.00"), result.getPhysicalCashCounted());
        assertEquals("OVERAGE", result.getVarianceType());
        assertEquals(new BigDecimal("50.00"), result.getVarianceAmount());
        assertEquals("4090", result.getVarianceGlCode());
        // Since variance (50) <= threshold (100), status is AUTO_RESOLVED_OVERAGE
        assertEquals("AUTO_RESOLVED_OVERAGE", result.getStatus());

        // GL Entry posted for Overage: Debit 1020-001, Credit 4090
        ArgumentCaptor<JournalEntry> journalCaptor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalRepositoryPort, times(1)).save(journalCaptor.capture());
        JournalEntry entry = journalCaptor.getValue();
        assertEquals(2, entry.getLines().size());
        assertEquals(new BigDecimal("50.00"), entry.getLines().get(0).getAmount());
    }

    @Test
    @DisplayName("Should approve reconciliation by supervisor")
    void shouldApproveReconciliationBySupervisor() {
        UUID recId = UUID.randomUUID();
        UUID supervisorId = UUID.randomUUID();

        TillCashReconciliationEntity entity = new TillCashReconciliationEntity();
        entity.setReconciliationId(recId);
        entity.setStatus("PENDING_SUPERVISOR_APPROVAL");
        entity.setVarianceType("SHORTAGE");
        entity.setVarianceAmount(new BigDecimal("350.00"));

        when(reconciliationRepository.findById(recId)).thenReturn(Optional.of(entity));
        when(reconciliationRepository.save(any(TillCashReconciliationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TillCashReconciliationEntity updated = tellerTillService.supervisorApproveReconciliation(
                recId, supervisorId, "Verified count with teller, approved write-off"
        );

        assertEquals("SUPERVISOR_APPROVED", updated.getStatus());
        assertEquals(supervisorId, updated.getSupervisorApprovedBy());
        assertNotNull(updated.getSupervisorApprovedAt());
        assertEquals("Verified count with teller, approved write-off", updated.getSupervisorNotes());
    }
}
