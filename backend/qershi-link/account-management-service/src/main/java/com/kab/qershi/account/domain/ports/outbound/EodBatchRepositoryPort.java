package com.kab.qershi.account.domain.ports.outbound;

import com.kab.qershi.account.domain.model.EodBatchExecution;
import com.kab.qershi.account.domain.model.EodBatchStepLog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound repository port for managing EOD batch executions and step logs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface EodBatchRepositoryPort {

    EodBatchExecution saveExecution(EodBatchExecution execution);

    Optional<EodBatchExecution> findExecutionById(UUID batchId);

    Optional<EodBatchExecution> findLatestExecution();

    List<EodBatchExecution> findAllExecutionsOrderByStartedAtDesc();

    EodBatchStepLog saveStepLog(EodBatchStepLog stepLog);

    List<EodBatchStepLog> findStepLogsByBatchId(UUID batchId);
}
