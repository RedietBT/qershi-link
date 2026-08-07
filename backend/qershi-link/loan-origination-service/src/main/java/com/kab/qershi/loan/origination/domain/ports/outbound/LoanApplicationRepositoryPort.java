package com.kab.qershi.loan.origination.domain.ports.outbound;

import com.kab.qershi.loan.origination.domain.model.LoanApplication;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound repository port for loan applications aggregate persistence.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface LoanApplicationRepositoryPort {

    LoanApplication save(LoanApplication application);

    Optional<LoanApplication> findById(UUID applicationId);

    Optional<LoanApplication> findByApplicationNo(String applicationNo);

    List<LoanApplication> findByUserId(UUID userId);

    long countApplicationsCreatedInCurrentYear();
}
