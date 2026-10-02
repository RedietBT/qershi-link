package com.kab.qershi.loan.management.domain.port.in;

import com.kab.qershi.loan.management.domain.model.DelinquentLoanInfo;
import com.kab.qershi.loan.management.domain.model.ParAgingResult;
import com.kab.qershi.loan.management.domain.model.ParSummary;

import java.time.LocalDate;
import java.util.List;

/**
 * Inbound Port for Portfolio at Risk (PAR) aging and Delinquency evaluation.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface LoanDelinquencyUseCase {

    ParAgingResult evaluateParAging(LocalDate businessDate);

    ParSummary getParSummary();

    List<DelinquentLoanInfo> getDelinquentLoans(String bucket);
}
