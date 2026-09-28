package com.kab.qershi.transaction.application.usecase;

import com.kab.qershi.transaction.domain.model.TillStatus;
import com.kab.qershi.transaction.infrastructure.persistence.SpringDataTellerTillRepository;
import com.kab.qershi.transaction.infrastructure.persistence.SpringDataTillCashReconciliationRepository;
import com.kab.qershi.transaction.infrastructure.persistence.TellerTillEntity;
import com.kab.qershi.transaction.infrastructure.persistence.TillCashReconciliationEntity;
import com.kab.qershi.transaction.infrastructure.rest.dto.AssignTillRequest;
import com.kab.qershi.transaction.infrastructure.rest.dto.CloseTillRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service managing teller cash drawers (tills), daily opening/closing routines,
 * and physical banknote counting reconciliations.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
@Transactional
public class TellerTillService {

    private static final Logger log = LoggerFactory.getLogger(TellerTillService.class);
    private static final UUID DEFAULT_HEAD_OFFICE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final SpringDataTellerTillRepository tillRepository;
    private final SpringDataTillCashReconciliationRepository reconciliationRepository;

    public TellerTillService(SpringDataTellerTillRepository tillRepository,
                             SpringDataTillCashReconciliationRepository reconciliationRepository) {
        this.tillRepository = tillRepository;
        this.reconciliationRepository = reconciliationRepository;
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

    public TillCashReconciliationEntity closeAndReconcileTill(UUID tellerUserId, CloseTillRequest request) {
        TellerTillEntity till = tillRepository.findByTellerUserId(tellerUserId)
                .orElseThrow(() -> new IllegalArgumentException("No till found for teller user: " + tellerUserId));

        if (till.getStatus() != TillStatus.OPEN) {
            throw new IllegalStateException("Cannot close drawer: till is currently " + till.getStatus() + ". Must be OPEN.");
        }

        BigDecimal electronicBalance = till.getCurrentCash() != null ? till.getCurrentCash() : BigDecimal.ZERO;
        BigDecimal physicalCash = request.physicalCashCounted() != null ? request.physicalCashCounted() : BigDecimal.ZERO;
        BigDecimal variance = physicalCash.subtract(electronicBalance);

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
                request.reconciliationNotes()
        );

        TillCashReconciliationEntity savedReconciliation = reconciliationRepository.save(reconciliation);

        till.setStatus(TillStatus.CLOSED);
        till.setClosedAt(Instant.now());
        till.setCurrentCash(BigDecimal.ZERO);
        tillRepository.save(till);

        log.info("Closed till {} for teller {}. Electronic: ETB {}, Physical: ETB {}, Variance: ETB {}",
                till.getTillId(), tellerUserId, electronicBalance, physicalCash, variance);

        return savedReconciliation;
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
