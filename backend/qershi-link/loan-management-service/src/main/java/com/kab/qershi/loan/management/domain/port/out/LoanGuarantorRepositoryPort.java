package com.kab.qershi.loan.management.domain.port.out;

import com.kab.qershi.loan.management.domain.model.LoanAccountGuarantor;

import java.util.List;
import java.util.UUID;

/**
 * Outbound Repository Port for LoanAccountGuarantor persistence.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface LoanGuarantorRepositoryPort {

    LoanAccountGuarantor save(LoanAccountGuarantor guarantor);

    List<LoanAccountGuarantor> findByAccountId(UUID accountId);

    List<LoanAccountGuarantor> findByAccountIdAndStatus(UUID accountId, String status);

    List<LoanAccountGuarantor> findByApplicationId(UUID applicationId);
}
