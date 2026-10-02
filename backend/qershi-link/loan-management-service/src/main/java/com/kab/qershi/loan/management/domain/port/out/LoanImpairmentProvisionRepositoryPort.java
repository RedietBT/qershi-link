package com.kab.qershi.loan.management.domain.port.out;

import com.kab.qershi.loan.management.domain.model.LoanImpairmentProvisionLine;
import com.kab.qershi.loan.management.domain.model.LoanImpairmentProvisionRun;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Repository Port for IFRS 9 / NBE Loan Impairment Provision Runs and Lines.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface LoanImpairmentProvisionRepositoryPort {

    LoanImpairmentProvisionRun saveRun(LoanImpairmentProvisionRun run);

    Optional<LoanImpairmentProvisionRun> findRunByBusinessDate(LocalDate businessDate);

    Optional<LoanImpairmentProvisionRun> findLatestCompletedRun();

    List<LoanImpairmentProvisionRun> findTop12CompletedRuns();

    List<LoanImpairmentProvisionRun> findAllRuns();

    List<LoanImpairmentProvisionLine> saveAllLines(List<LoanImpairmentProvisionLine> lines);

    List<LoanImpairmentProvisionLine> findLinesByRunId(UUID runId);

    List<LoanImpairmentProvisionLine> findLinesByRunIdAndIfrs9Stage(UUID runId, String stage);

    long countLinesByRunId(UUID runId);
}
