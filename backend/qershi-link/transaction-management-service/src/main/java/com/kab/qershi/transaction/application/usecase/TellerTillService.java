package com.kab.qershi.transaction.application.usecase;

import com.kab.qershi.transaction.domain.model.*;
import com.kab.qershi.transaction.domain.ports.inbound.TellerTillUseCase;
import com.kab.qershi.transaction.domain.ports.outbound.JournalRepositoryPort;
import com.kab.qershi.transaction.domain.ports.outbound.TellerTillRepositoryPort;
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
 * Pure Hexagonal Application Use Case implementation.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
@Transactional
public class TellerTillService implements TellerTillUseCase {

    private static final Logger log = LoggerFactory.getLogger(TellerTillService.class);
    private static final UUID DEFAULT_HEAD_OFFICE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final BigDecimal SUPERVISOR_VARIANCE_THRESHOLD = new BigDecimal("100.00");

    private final TellerTillRepositoryPort tillRepositoryPort;
    private final JournalRepositoryPort journalRepositoryPort;

    public TellerTillService(TellerTillRepositoryPort tillRepositoryPort,
                             JournalRepositoryPort journalRepositoryPort) {
        this.tillRepositoryPort = tillRepositoryPort;
        this.journalRepositoryPort = journalRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public TellerTill getTillByTellerUserId(UUID tellerUserId) {
        return tillRepositoryPort.findTillByTellerUserId(tellerUserId)
                .orElseGet(() -> createDefaultTillForTeller(tellerUserId, DEFAULT_HEAD_OFFICE_ID, "001"));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TellerTill> findOpenTillByTellerUserId(UUID tellerUserId) {
        return tillRepositoryPort.findTillByTellerUserIdAndStatus(tellerUserId, TillStatus.OPEN);
    }

    @Override
    public TellerTill openTill(UUID tellerUserId, BigDecimal openingCash, UUID branchId, String branchCode) {
        TellerTill till = tillRepositoryPort.findTillByTellerUserId(tellerUserId)
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
        return tillRepositoryPort.saveTill(till);
    }

    /**
     * Core CBS Blind Till Balancing & Banknote Reconciliation implementation.
     * Tellers submit physical banknote counts without seeing expected balance.
     * Discrepancies generate balanced GL entries (5090 Shortage or 4090 Overage) and flag supervisor sign-off if needed.
     */
    @Override
    public TillCashReconciliation closeAndReconcileTill(UUID tellerUserId, CloseTillCommand command) {
        TellerTill till = tillRepositoryPort.findTillByTellerUserId(tellerUserId)
                .orElseThrow(() -> new IllegalArgumentException("No till found for teller user: " + tellerUserId));

        if (till.getStatus() != TillStatus.OPEN) {
            throw new IllegalStateException("Cannot close drawer: till is currently " + till.getStatus() + ". Must be OPEN.");
        }

        // 1. Calculate physical total strictly from counted denominations & coins
        BigDecimal notes200Val = new BigDecimal(command.notes200Count()).multiply(new BigDecimal("200.00"));
        BigDecimal notes100Val = new BigDecimal(command.notes100Count()).multiply(new BigDecimal("100.00"));
        BigDecimal notes50Val  = new BigDecimal(command.notes50Count()).multiply(new BigDecimal("50.00"));
        BigDecimal notes10Val  = new BigDecimal(command.notes10Count()).multiply(new BigDecimal("10.00"));
        BigDecimal notes5Val   = new BigDecimal(command.notes5Count()).multiply(new BigDecimal("5.00"));
        BigDecimal coinsVal    = command.coinsAmount() != null ? command.coinsAmount() : BigDecimal.ZERO;

        BigDecimal calculatedFromNotes = notes200Val.add(notes100Val).add(notes50Val).add(notes10Val).add(notes5Val).add(coinsVal);

        BigDecimal physicalCash = (command.physicalCashCounted() != null && command.physicalCashCounted().compareTo(BigDecimal.ZERO) > 0)
                ? command.physicalCashCounted()
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
        TillCashReconciliation reconciliation = new TillCashReconciliation(
                null,
                till.getTillId(),
                tellerUserId,
                electronicBalance,
                physicalCash,
                variance,
                command.notes200Count(),
                command.notes100Count(),
                command.notes50Count(),
                command.notes10Count(),
                command.notes5Count(),
                coinsVal,
                varianceType,
                varianceAmount,
                varianceGlCode,
                journalEntryId,
                status,
                null,
                null,
                null,
                command.reconciliationNotes(),
                Instant.now()
        );
        TillCashReconciliation savedRec = tillRepositoryPort.saveReconciliation(reconciliation);

        // 4. Persist Itemized Banknote Denominations Breakdown
        List<TillDenomination> denoms = new ArrayList<>();
        if (command.notes200Count() > 0) denoms.add(new TillDenomination(null, savedRec.getReconciliationId(), new BigDecimal("200.00"), command.notes200Count(), notes200Val, Instant.now()));
        if (command.notes100Count() > 0) denoms.add(new TillDenomination(null, savedRec.getReconciliationId(), new BigDecimal("100.00"), command.notes100Count(), notes100Val, Instant.now()));
        if (command.notes50Count() > 0)  denoms.add(new TillDenomination(null, savedRec.getReconciliationId(), new BigDecimal("50.00"),  command.notes50Count(),  notes50Val,  Instant.now()));
        if (command.notes10Count() > 0)  denoms.add(new TillDenomination(null, savedRec.getReconciliationId(), new BigDecimal("10.00"),  command.notes10Count(),  notes10Val,  Instant.now()));
        if (command.notes5Count() > 0)   denoms.add(new TillDenomination(null, savedRec.getReconciliationId(), new BigDecimal("5.00"),   command.notes5Count(),   notes5Val,   Instant.now()));
        if (coinsVal.compareTo(BigDecimal.ZERO) > 0) denoms.add(new TillDenomination(null, savedRec.getReconciliationId(), new BigDecimal("1.00"), coinsVal.intValue(), coinsVal, Instant.now()));
        tillRepositoryPort.saveDenominations(denoms);

        // 5. Persist Historical Till Closing Audit Log
        TillClosingLog closingLog = new TillClosingLog(
                null,
                savedRec.getReconciliationId(),
                till.getTillId(),
                tellerUserId,
                "BLIND",
                electronicBalance,
                physicalCash,
                variance,
                status,
                journalEntryId,
                Instant.now()
        );
        tillRepositoryPort.saveClosingLog(closingLog);

        // 6. Lock and Close Teller Drawer
        till.setStatus(TillStatus.CLOSED);
        till.setClosedAt(Instant.now());
        till.setCurrentCash(BigDecimal.ZERO);
        tillRepositoryPort.saveTill(till);

        log.info("Closed till {} for teller {}. Electronic: ETB {}, Physical: ETB {}, Variance: ETB {} ({}), Status: {}",
                till.getTillId(), tellerUserId, electronicBalance, physicalCash, variance, varianceType, status);

        return savedRec;
    }

    /**
     * Supervisor sign-off / approval for drawer cash variances exceeding regulatory tolerance.
     */
    @Override
    public TillCashReconciliation supervisorApproveReconciliation(UUID reconciliationId, UUID supervisorUserId, String notes) {
        TillCashReconciliation rec = tillRepositoryPort.findReconciliationById(reconciliationId)
                .orElseThrow(() -> new IllegalArgumentException("Reconciliation record not found: " + reconciliationId));

        if (!"PENDING_SUPERVISOR_APPROVAL".equalsIgnoreCase(rec.getStatus())) {
            throw new IllegalStateException("Reconciliation is not awaiting supervisor approval. Current status: " + rec.getStatus());
        }

        rec.setStatus("SUPERVISOR_APPROVED");
        rec.setSupervisorApprovedBy(supervisorUserId);
        rec.setSupervisorApprovedAt(Instant.now());
        rec.setSupervisorNotes(notes != null ? notes.trim() : "Approved by branch supervisor");

        log.info("Supervisor {} approved cash variance on reconciliation {}", supervisorUserId, reconciliationId);
        return tillRepositoryPort.saveReconciliation(rec);
    }

    @Override
    public TellerTill assignTill(AssignTillCommand command) {
        TellerTill till = tillRepositoryPort.findTillByTellerUserId(command.tellerUserId())
                .orElseGet(TellerTill::new);

        till.setBranchId(command.branchId());
        till.setBranchCode(command.branchCode().trim());
        till.setTellerUserId(command.tellerUserId());
        till.setTillName(command.tillName().trim());
        till.setTillGlCode(command.tillGlCode() != null && !command.tillGlCode().isBlank() ?
                command.tillGlCode().trim() : "1020-" + command.branchCode().trim());
        if (command.maxCashLimit() != null) {
            till.setMaxCashLimit(command.maxCashLimit());
        }

        return tillRepositoryPort.saveTill(till);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TellerTill> getTillsByBranch(UUID branchId) {
        return tillRepositoryPort.findTillsByBranchId(branchId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TillCashReconciliation> getReconciliationsByTillId(UUID tillId) {
        return tillRepositoryPort.findReconciliationsByTillId(tillId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TillCashReconciliation> getReconciliationsByTeller(UUID tellerUserId) {
        return tillRepositoryPort.findReconciliationsByTellerUserId(tellerUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TillDenomination> getDenominationsByReconciliation(UUID reconciliationId) {
        return tillRepositoryPort.findDenominationsByReconciliationId(reconciliationId);
    }

    /**
     * Atomically mutates physical cash inside an open teller drawer upon cash transactions.
     */
    @Override
    public void recordCashMovement(UUID tellerUserId, BigDecimal amount, boolean isDeposit) {
        Optional<TellerTill> openTillOpt = tillRepositoryPort.findTillByTellerUserIdAndStatus(tellerUserId, TillStatus.OPEN);
        if (openTillOpt.isEmpty()) {
            log.warn("No active OPEN till for teller {}. Cash movement ETB {} was recorded without till balance deduction.",
                    tellerUserId, amount);
            return;
        }

        TellerTill till = openTillOpt.get();
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

        tillRepositoryPort.saveTill(till);
    }

    private TellerTill createDefaultTillForTeller(UUID tellerUserId, UUID branchId, String branchCode) {
        String shortId = tellerUserId.toString().substring(0, 8);
        TellerTill domain = new TellerTill(
                null,
                branchId,
                branchCode,
                tellerUserId,
                "Teller Till (" + shortId + ")",
                "1020-" + branchCode,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("200000.00"),
                TillStatus.CLOSED,
                null,
                null,
                Instant.now()
        );
        return tillRepositoryPort.saveTill(domain);
    }
}
