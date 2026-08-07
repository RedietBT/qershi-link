package com.kab.qershi.loan.management.domain.port.out;

import com.kab.qershi.loan.management.domain.model.LoanAccount;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Repository Port for LoanAccount persistence.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface LoanAccountRepositoryPort {

    LoanAccount save(LoanAccount account);

    Optional<LoanAccount> findById(UUID id);

    Optional<LoanAccount> findByApplicationId(UUID applicationId);

    List<LoanAccount> findByUserId(UUID userId);
}
