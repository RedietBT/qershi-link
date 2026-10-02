package com.kab.qershi.loan.management.domain.port.in;

import com.kab.qershi.loan.management.domain.model.Ifrs9ProvisionResult;
import com.kab.qershi.loan.management.domain.model.LoanImpairmentProvisionLine;
import com.kab.qershi.loan.management.domain.model.LoanImpairmentProvisionRun;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Inbound Port for IFRS 9 / NBE Month-End Loan Impairment Provisioning.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface LoanImpairmentProvisionUseCase {

    Ifrs9ProvisionResult runMonthEndProvisioning(LocalDate businessDate, String triggeredBy, UUID triggeredByUserId);

    Optional<LoanImpairmentProvisionRun> getLatestCompletedRun();

    List<LoanImpairmentProvisionRun> getProvisionHistory();

    List<LoanImpairmentProvisionLine> getRunLines(UUID runId);
}
