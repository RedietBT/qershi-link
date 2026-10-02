package com.kab.qershi.account.domain.ports.outbound;

import com.kab.qershi.account.domain.model.TermDepositContract;
import com.kab.qershi.account.domain.model.TermDepositStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound repository port for Fixed Term Deposit contract persistence.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface TermDepositRepositoryPort {

    TermDepositContract save(TermDepositContract contract);

    Optional<TermDepositContract> findById(UUID contractId);

    Optional<TermDepositContract> findByContractNo(String contractNo);

    List<TermDepositContract> findByAccountNo(String accountNo);

    List<TermDepositContract> findByUserId(UUID userId);

    List<TermDepositContract> findByStatus(TermDepositStatus status);

    List<TermDepositContract> findMaturedContracts(LocalDate businessDate);

    List<TermDepositContract> findActiveContractsForAccrual(LocalDate businessDate);

    List<TermDepositContract> findPendingApprovals();

    long countActiveByAccountNo(String accountNo);
}
