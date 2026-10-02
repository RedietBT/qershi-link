package com.kab.qershi.transaction.domain.ports.inbound;

import com.kab.qershi.transaction.domain.model.TellerTill;
import com.kab.qershi.transaction.domain.model.TillCashReconciliation;
import com.kab.qershi.transaction.domain.model.TillDenomination;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Inbound port exposing Teller Cash Drawer (Till) lifecycle,
 * daily Blind Till Balancing, and banknote denomination reconciliation use cases.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface TellerTillUseCase {

    record CloseTillCommand(
            BigDecimal physicalCashCounted,
            int notes200Count,
            int notes100Count,
            int notes50Count,
            int notes10Count,
            int notes5Count,
            BigDecimal coinsAmount,
            String reconciliationNotes
    ) {}

    record AssignTillCommand(
            UUID branchId,
            String branchCode,
            UUID tellerUserId,
            String tillName,
            String tillGlCode,
            BigDecimal maxCashLimit
    ) {}

    TellerTill getTillByTellerUserId(UUID tellerUserId);

    Optional<TellerTill> findOpenTillByTellerUserId(UUID tellerUserId);

    TellerTill openTill(UUID tellerUserId, BigDecimal openingCash, UUID branchId, String branchCode);

    TillCashReconciliation closeAndReconcileTill(UUID tellerUserId, CloseTillCommand command);

    TillCashReconciliation supervisorApproveReconciliation(UUID reconciliationId, UUID supervisorUserId, String notes);

    TellerTill assignTill(AssignTillCommand command);

    List<TellerTill> getTillsByBranch(UUID branchId);

    List<TillCashReconciliation> getReconciliationsByTillId(UUID tillId);

    List<TillCashReconciliation> getReconciliationsByTeller(UUID tellerUserId);

    List<TillDenomination> getDenominationsByReconciliation(UUID reconciliationId);

    void recordCashMovement(UUID tellerUserId, BigDecimal amount, boolean isDeposit);
}
