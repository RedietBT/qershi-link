package com.kab.qershi.loan.origination.domain.ports.outbound;

import com.kab.qershi.loan.origination.domain.model.LoanGroup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound repository port for loan groups persistence operations.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface LoanGroupRepositoryPort {

    LoanGroup save(LoanGroup group);

    Optional<LoanGroup> findById(UUID groupId);

    boolean existsByLicenseNo(String licenseNo);

    List<LoanGroup> findAll();
}
