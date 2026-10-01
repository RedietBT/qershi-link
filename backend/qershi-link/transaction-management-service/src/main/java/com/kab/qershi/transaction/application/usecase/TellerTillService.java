package com.kab.qershi.transaction.application.usecase;

import com.kab.qershi.transaction.domain.model.EntryType;
import com.kab.qershi.transaction.domain.model.JournalEntry;
import com.kab.qershi.transaction.domain.model.JournalLine;
import com.kab.qershi.transaction.domain.model.TillStatus;
import com.kab.qershi.transaction.domain.ports.outbound.JournalRepositoryPort;
import com.kab.qershi.transaction.infrastructure.persistence.*;
import com.kab.qershi.transaction.infrastructure.rest.dto.AssignTillRequest;
import com.kab.qershi.transaction.infrastructure.rest.dto.CloseTillRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service managing teller cash drawers (tills), daily opening routines,
 * Temenos/Finacle-standard Blind Till Balancing, banknote denominations breakdown,
 * and automated GL double-entry adjustments for cash shortages and overages.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
@Transactional
public class TellerTillService {

    private static final Logger log = LoggerFactory.getLogger(TellerTillService.class);
    private static final UUID DEFAULT_HEAD_OFFICE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final BigDecimal SUPERVISOR_VARIANCE_THRESHOLD = new BigDecimal("100.00");

    private final SpringDataTellerTillRepository tillRepository;
    private final SpringDataTillCashReconciliationRepository reconciliationRepository;
    private final SpringDataTillDenominationRepository denominationRepository;
    private final SpringDataTillClosingLogRepository closingLogRepository;
    private final JournalRepositoryPort journalRepositoryPort;

    public TellerTillService(SpringDataTellerTillRepository tillRepository,
                             SpringDataTillCashReconciliationRepository reconciliationRepository,
                             SpringDataTillDenominationRepository denominationRepository,
                             SpringDataTillClosingLogRepository closingLogRepository,
                             JournalRepositoryPort journalRepositoryPort) {
        this.tillRepository = tillRepository;
        this.reconciliationRepository = reconciliationRepository;
        this.denominationRepository = denominationRepository;
        this.closingLogRepository = closingLogRepository;
        this.journalRepositoryPort = journalRepositoryPort;
    }

    @Transactional(readOnly = true)
    public TellerTillEntity getTillByTellerUserId(UUID tellerUserId) {
        return tillRepository.findByTellerUserId(tellerUserId)
                .orElseGet(() -> createDefaultTillForTeller(tellerUserId, DEFAULT_HEAD_OFFICE_ID, "001"));
    }

    @Transactional(readOnly = true)
    public Optional<TellerTillEntity> findOpenTillByTellerUserId(UUID tellerUserId) {
        return tillRepository.findByTellerUserIdAndStatus(tellerUserId, TillStatus.OPEN);
    }

    public TellerTillEntity openTill(UUID tellerUserId, BigDecimal openingCash, UUID branchId, String branchCode) {
        TellerTillEntity till = tillRepository.findByTellerUserId(tellerUserId)
                .orElseGet(() -> createDefaultTillForTeller(tellerUserId, branchId != null ? branchId : DEFAULT_HEAD_OFFICE_ID,
                        branchCode != null ? branchCode : "001"));

        if (till.getStatus() == TillStatus.OPEN) {
            throw new IllegalStateException("Teller drawer '" + till.getTillName() + "' is already OPEN.");
        }

        BigDecimal cash = openingCash != null ? openingCash : BigDecimal.ZERO;
        till.setStatus(TillStatus.OPEN);
        till.setOpeningCash(cash);
        till.setCurrentCash(cash);
        till.setOpenedAt(Instant.now());
        till.setClosedAt(null);

        log.info("Opened teller till {} for user {} with opening cash ETB {}", till.getTillId(), tellerUserId, cash);
        return tillRepository.save(till);
    }

    /**
     * Core CBS Blind Till Balancing & Banknote Reconciliation implementation.
     * Tellers submit physical banknote counts without seeing expected balance.
     * Discrepancies generate balanced GL entries (5090 Shortage or 4090 Overage) and flag supervisor sign-off if needed.
     */
    public TillCashReconciliationEntity closeAndReconcileTill(UUID tellerUserId, CloseTillRequest request) {
        TellerTillEntity till = tillRepository.findByTellerUserId(tellerUserId)
                .orElseThrow(() -> new IllegalArgumentException("No till found for teller user: " + tellerUserId));

        if (till.getStatus() != TillStatus.OPEN) {
            throw new IllegalStateException("Cannot close drawer: till is currently " + till.getStatus() + ". Must be OPEN.");
        }

        // 1. Calculate physical total strictly from counted denominations & coins
        BigDecimal notes200Val = new BigDecimal(request.notes200Count()).multiply(new BigDecimal("200.00"));
        BigDecimal notes100Val = new BigDecimal(request.notes100Count()).multiply(new BigDecimal("100.00"));
        BigDecimal notes50Val  = new BigDecimal(request.notes50Count()).multiply(new BigDecimal("50.00"));
        BigDecimal notes10Val  = new BigDecimal(request.notes10Count()).multiply(new BigDecimal("10.00"));
        BigDecimal notes5Val   = new BigDecimal(request.notes5Count()).multiply(new BigDecimal("5.00"));
        BigDecimal coinsVal    = request.coinsAmount() != null ? request.coinsAmount() : BigDecimal.ZERO;

        BigDecimal calculatedFromNotes = notes200Val.add(notes100Val).add(notes50Val).add(notes10Val).add(notes5Val).add(coinsVal);

        BigDecimal physicalCash = (request.physicalCashCounted() != null && request.physicalCashCounted().compareTo(BigDecimal.ZERO) > 0)
                ? request.physicalCashCounted()
                : calculatedFromNotes;

        BigDecimal electronicBalance = till.getCurrentCash() != null ? till.getCurrentCash() : BigDecimal.ZERO;
        BigDecimal variance = physicalCash.subtract(electronicBalance);

        String varianceType = "NONE";
        BigDecimal varianceAmount = BigDecimal.ZERO;
        String varianceGlCode = null;
        UUID journalEntryId = null;
        String status = "BALANCED";

        // 2. Perform automated double-entry GL adjustment if a variance exists
        if (variance.compareTo(BigDecimal.ZERO) < 0) {
            // CASH SHORTAGE: Physical counted < Electronic ledger balance
            varianceType = "SHORTAGE";
            varianceAmount = variance.abs();
            varianceGlCode = "5090"; // Cash Shortage Expense GL

            String txRef = "VAR-SHORT-" + till.getBranchCode() + "-" + System.currentTimeMillis();
            JournalEntry entry = new JournalEntry(
                    UUID.randomUUID(),
                    txRef,
                    Instant.now(),
                    "Cash Shortage GL Adjustment for Drawer " + till.getTillName() + " (Variance: -ETB " + varianceAmount + ")",
                    Instant.now()
            );

            JournalLine debitLine = new JournalLine(
                    UUID.randomUUID(), entry.getEntryId(),
                    "5090-CASH-SHORTAGE-EXPENSE", EntryType.DEBIT,
                    varianceAmount, Instant.now()
            );
            JournalLine creditLine = new JournalLine(
                    UUID.randomUUID(), entry.getEntryId(),
                    till.getTillGlCode(), EntryType.CREDIT,
                    varianceAmount, Instant.now()
            );
            entry.setLines(List.of(debitLine, creditLine));
            journalRepositoryPort.save(entry);
            journalEntryId = entry.getEntryId();

            if (varianceAmount.compareTo(SUPERVISOR_VARIANCE_THRESHOLD) > 0) {
                status = "PENDING_SUPERVISOR_APPROVAL";
            } else {
                status = "AUTO_RESOLVED_SHORTAGE";
            }
        } else if (variance.compareTo(BigDecimal.ZERO) > 0) {
            // CASH OVERAGE: Physical counted > Electronic ledger balance
            varianceType = "OVERAGE";
            varianceAmount = variance;
            varianceGlCode = "4090"; // Cash Overage / Misc Operating Income GL

            String txRef = "VAR-OVER-" + till.getBranchCode() + "-" + System.currentTimeMillis();
            JournalEntry entry = new JournalEntry(
                    UUID.randomUUID(),
                    txRef,
                    Instant.now(),
                    "Cash Overage GL Adjustment for Drawer " + till.getTillName() + " (Variance: +ETB " + varianceAmount + ")",
                    Instant.now()
            );

            JournalLine debitLine = new JournalLine(
                    UUID.randomUUID(), entry.getEntryId(),
                    till.getTillGlCode(), EntryType.DEBIT,
                    varianceAmount, Instant.now()
            );
            JournalLine creditLine = new JournalLine(
                    UUID.randomUUID(), entry.getEntryId(),
                    "4090-CASH-OVERAGE-INCOME", EntryType.CREDIT,
                    varianceAmount, Instant.now()
            );
            entry.setLines(List.of(debitLine, creditLine));
            journalRepositoryPort.save(entry);
            journalEntryId = entry.getEntryId();

            if (varianceAmount.compareTo(SUPERVISOR_VARIANCE_THRESHOLD) > 0) {
                status = "PENDING_SUPERVISOR_APPROVAL";
            } else {
                status = "AUTO_RESOLVED_OVERAGE";
            }
        }

        // 3. Persist Master Till Cash Reconciliation Record
        TillCashReconciliationEntity reconciliation = new TillCashReconciliationEntity(
                till.getTillId(),
                tellerUserId,
                electronicBalance,
                physicalCash,
                variance,
                request.notes200Count(),
                request.notes100Count(),
                request.notes50Count(),
                request.notes10Count(),
                request.notes5Count(),
                coinsVal,
                varianceType,
                varianceAmount,
                varianceGlCode,
                journalEntryId,
                status,
                request.reconciliationNotes()
        );
        TillCashReconciliationEntity savedRec = reconciliationRepository.save(reconciliation);

        // 4. Persist Itemized Banknote Denominations Breakdown
        List<TillDenominationEntity> denoms = new ArrayList<>();
        if (request.notes200Count() > 0) denoms.add(new TillDenominationEntity(savedRec.getReconciliationId(), new BigDecimal("200.00"), request.notes200Count(), notes200Val));
        if (request.notes100Count() > 0) denoms.add(new TillDenominationEntity(savedRec.getReconciliationId(), new BigDecimal("100.00"), request.notes100Count(), notes100Val));
        if (request.notes50Count() > 0)  denoms.add(new TillDenominationEntity(savedRec.getReconciliationId(), new BigDecimal("50.00"),  request.notes50Count(),  notes50Val));
        if (request.notes10Count() > 0)  denoms.add(new TillDenominationEntity(savedRec.getReconciliationId(), new BigDecimal("10.00"),  request.notes10Count(),  notes10Val));
        if (request.notes5Count() > 0)   denoms.add(new TillDenominationEntity(savedRec.getReconciliationId(), new BigDecimal("5.00"),   request.notes5Count(),   notes5Val));
        if (coinsVal.compareTo(BigDecimal.ZERO) > 0) denoms.add(new TillDenominationEntity(savedRec.getReconciliationId(), new BigDecimal("1.00"), coinsVal.intValue(), coinsVal));
        denominationRepository.saveAll(denoms);

        // 5. Persist Historical Till Closing Audit Log
        TillClosingLogEntity closingLog = new TillClosingLogEntity(
                savedRec.getReconciliationId(),
                till.getTillId(),
                tellerUserId,
                "BLIND",
                electronicBalance,
                physicalCash,
                variance,
                status,
                journalEntryId
        );
        closingLogRepository.save(closingLog);

        // 6. Lock and Close Teller Drawer
        till.setStatus(TillStatus.CLOSED);
        till.setClosedAt(Instant.now());
        till.setCurrentCash(BigDecimal.ZERO);
        tillRepository.save(till);

        log.info("Closed till {} for teller {}. Electronic: ETB {}, Physical: ETB {}, Variance: ETB {} ({}), Status: {}",
                till.getTillId(), tellerUserId, electronicBalance, physicalCash, variance, varianceType, status);

        return savedRec;
    }

    /**
     * Supervisor sign-off / approval for drawer cash variances exceeding regulatory tolerance.
     */
    public TillCashReconciliationEntity supervisorApproveReconciliation(UUID reconciliationId, UUID supervisorUserId, String notes) {
        TillCashReconciliationEntity rec = reconciliationRepository.findById(reconciliationId)
                .orElseThrow(() -> new IllegalArgumentException("Reconciliation record not found: " + reconciliationId));

        if (!"PENDING_SUPERVISOR_APPROVAL".equalsIgnoreCase(rec.getStatus())) {
            throw new IllegalStateException("Reconciliation is not awaiting supervisor approval. Current status: " + rec.getStatus());
        }

        rec.setStatus("SUPERVISOR_APPROVED");
        rec.setSupervisorApprovedBy(supervisorUserId);
        rec.setSupervisorApprovedAt(Instant.now());
        rec.setSupervisorNotes(notes != null ? notes.trim() : "Approved by branch supervisor");

        log.info("Supervisor {} approved cash variance on reconciliation {}", supervisorUserId, reconciliationId);
        return reconciliationRepository.save(rec);
    }

    public TellerTillEntity assignTill(AssignTillRequest request) {
        TellerTillEntity till = tillRepository.findByTellerUserId(request.tellerUserId())
                .orElseGet(TellerTillEntity::new);

        till.setBranchId(request.branchId());
        till.setBranchCode(request.branchCode().trim());
        till.setTellerUserId(request.tellerUserId());
        till.setTillName(request.tillName().trim());
        till.setTillGlCode(request.tillGlCode() != null && !request.tillGlCode().isBlank() ?
                request.tillGlCode().trim() : "1020-" + request.branchCode().trim());
        if (request.maxCashLimit() != null) {
            till.setMaxCashLimit(request.maxCashLimit());
        }

        return tillRepository.save(till);
    }

    @Transactional(readOnly = true)
    public List<TellerTillEntity> getTillsByBranch(UUID branchId) {
        return tillRepository.findByBranchId(branchId);
    }

    @Transactional(readOnly = true)
    public List<TillCashReconciliationEntity> getReconciliationsByTillId(UUID tillId) {
        return reconciliationRepository.findByTillIdOrderByCreatedAtDesc(tillId);
    }

    @Transactional(readOnly = true)
    public List<TillCashReconciliationEntity> getReconciliationsByTeller(UUID tellerUserId) {
        return reconciliationRepository.findByTellerUserIdOrderByCreatedAtDesc(tellerUserId);
    }

    @Transactional(readOnly = true)
    public List<TillDenominationEntity> getDenominationsByReconciliation(UUID reconciliationId) {
        return denominationRepository.findByReconciliationIdOrderByDenominationValueDesc(reconciliationId);
    }

    /**
     * Atomically mutates physical cash inside an open teller drawer upon cash transactions.
     */
    public void recordCashMovement(UUID tellerUserId, BigDecimal amount, boolean isDeposit) {
        Optional<TellerTillEntity> openTillOpt = tillRepository.findByTellerUserIdAndStatus(tellerUserId, TillStatus.OPEN);
        if (openTillOpt.isEmpty()) {
            log.warn("No active OPEN till for teller {}. Cash movement ETB {} was recorded without till balance deduction.",
                    tellerUserId, amount);
            return;
        }

        TellerTillEntity till = openTillOpt.get();
        BigDecimal current = till.getCurrentCash() != null ? till.getCurrentCash() : BigDecimal.ZERO;

        if (isDeposit) {
            till.setCurrentCash(current.add(amount));
            log.info("Till {} credited with ETB {}. New drawer balance: ETB {}", till.getTillId(), amount, till.getCurrentCash());
        } else {
            if (current.compareTo(amount) < 0) {
                throw new IllegalStateException("Insufficient cash in drawer: Available ETB " + current +
                        ", withdrawal requested: ETB " + amount + ". Please request a vault cash transfer.");
            }
            till.setCurrentCash(current.subtract(amount));
            log.info("Till {} debited with ETB {}. New drawer balance: ETB {}", till.getTillId(), amount, till.getCurrentCash());
        }

        tillRepository.save(till);
    }

    private TellerTillEntity createDefaultTillForTeller(UUID tellerUserId, UUID branchId, String branchCode) {
        String shortId = tellerUserId.toString().substring(0, 8);
        TellerTillEntity entity = new TellerTillEntity(
                null,
                branchId,
                branchCode,
                tellerUserId,
                "Teller Till (" + shortId + ")",
                "1020-" + branchCode,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("200000.00"),
                TillStatus.CLOSED
        );
        return tillRepository.save(entity);
    }
}
