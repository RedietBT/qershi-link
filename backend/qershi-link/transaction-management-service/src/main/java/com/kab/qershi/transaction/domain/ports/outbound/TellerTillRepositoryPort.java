package com.kab.qershi.transaction.domain.ports.outbound;

import com.kab.qershi.transaction.domain.model.TellerTill;
import com.kab.qershi.transaction.domain.model.TillCashReconciliation;
import com.kab.qershi.transaction.domain.model.TillClosingLog;
import com.kab.qershi.transaction.domain.model.TillDenomination;
import com.kab.qershi.transaction.domain.model.TillStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for persisting and querying teller drawer (till) domain aggregates,
 * reconciliation statements, banknote breakdowns, and closing logs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface TellerTillRepositoryPort {

    TellerTill saveTill(TellerTill till);

    Optional<TellerTill> findTillByTellerUserId(UUID tellerUserId);

    Optional<TellerTill> findTillByTellerUserIdAndStatus(UUID tellerUserId, TillStatus status);

    List<TellerTill> findTillsByBranchId(UUID branchId);

    TillCashReconciliation saveReconciliation(TillCashReconciliation reconciliation);

    Optional<TillCashReconciliation> findReconciliationById(UUID reconciliationId);

    List<TillCashReconciliation> findReconciliationsByTillId(UUID tillId);

    List<TillCashReconciliation> findReconciliationsByTellerUserId(UUID tellerUserId);

    void saveDenominations(List<TillDenomination> denominations);

    List<TillDenomination> findDenominationsByReconciliationId(UUID reconciliationId);

    TillClosingLog saveClosingLog(TillClosingLog closingLog);
}
