package com.kab.qershi.loan.management.domain.port.in;

import com.kab.qershi.loan.management.domain.model.LoanAccount;
import com.kab.qershi.loan.management.domain.model.RepaymentSchedule;

import java.util.List;
import java.util.UUID;

/**
 * Inbound Port for Loan Account & Schedule Inspection Use Case.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface LoanScheduleUseCase {

    LoanAccount getAccount(UUID accountId);

    List<RepaymentSchedule> getAccountSchedule(UUID accountId);

    List<LoanAccount> getUserAccounts(UUID userId);
}
