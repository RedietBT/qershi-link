package com.kab.qershi.loan.management.domain.port.out;

import com.kab.qershi.loan.management.domain.model.LoanRepayment;

import java.util.List;
import java.util.UUID;

/**
 * Outbound Repository Port for LoanRepayment persistence.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface LoanRepaymentRepositoryPort {

    LoanRepayment save(LoanRepayment repayment);

    List<LoanRepayment> findByAccountId(UUID accountId);
}
