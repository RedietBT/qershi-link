package com.kab.qershi.account.domain.ports.inbound;

import com.kab.qershi.account.domain.model.TermDepositContract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Inbound Use Case Port for Term Deposit Operations.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface TermDepositUseCase {

    record OpenFdResult(
            UUID contractId,
            String contractNo,
            LocalDate maturityDate,
            BigDecimal principal,
            BigDecimal ratePercent,
            String openingGlRef,
            String status
    ) {}

    record MaturityProcessResult(
            int contractsProcessed,
            int autoRolledOver,
            int closedNormal,
            BigDecimal totalPrincipalPaid,
            BigDecimal totalInterestPaid
    ) {}

    record EarlyBreakResult(
            String contractNo,
            BigDecimal principal,
            BigDecimal penaltyCharged,
            BigDecimal netPayout,
            String destinationAccountNo,
            String glRef,
            String status
    ) {}

    record DailyAccrualResult(
            int contractsAccrued,
            BigDecimal totalInterestAccruedToday,
            LocalDate businessDate
    ) {}

    OpenFdResult openTermDeposit(
            String accountNo,
            UUID userId,
            String saccoCode,
            String branchCode,
            BigDecimal principal,
            int tenorMonths,
            BigDecimal interestRatePa,
            boolean autoRollover,
            Integer rolloverTenorMonths,
            UUID makerUserId,
            String makerNotes,
            boolean autoApprove
    );

    MaturityProcessResult processMaturitySweep(LocalDate businessDate);

    DailyAccrualResult runDailyInterestAccrual(LocalDate businessDate);

    EarlyBreakResult breakTermDepositEarly(String contractNo, String reason, UUID authorizedByUserId);

    Optional<TermDepositContract> getContract(String contractNo);

    List<TermDepositContract> getContractsByAccount(String accountNo);

    List<TermDepositContract> getContractsByUser(UUID userId);

    List<TermDepositContract> getPendingApprovals();

    void approveContract(String contractNo, UUID checkerUserId, String checkerNotes);
}
