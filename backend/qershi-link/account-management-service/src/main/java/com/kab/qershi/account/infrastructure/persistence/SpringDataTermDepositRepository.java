package com.kab.qershi.account.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Fixed Term Deposit contracts.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataTermDepositRepository
        extends JpaRepository<TermDepositContractEntity, UUID> {

    Optional<TermDepositContractEntity> findByContractNo(String contractNo);

    List<TermDepositContractEntity> findByAccountNo(String accountNo);

    List<TermDepositContractEntity> findByUserId(UUID userId);

    List<TermDepositContractEntity> findByStatus(TermDepositContractEntity.TermDepositStatus status);

    // All ACTIVE contracts that have reached or passed maturity date (for EOD sweep)
    @Query("SELECT t FROM TermDepositContractEntity t WHERE t.status = 'ACTIVE' AND t.maturityDate <= :businessDate")
    List<TermDepositContractEntity> findMaturedContracts(@Param("businessDate") LocalDate businessDate);

    // All ACTIVE contracts for daily interest accrual
    List<TermDepositContractEntity> findByStatusAndMaturityDateAfter(
            TermDepositContractEntity.TermDepositStatus status, LocalDate businessDate);

    // Pending approvals
    List<TermDepositContractEntity> findByStatusOrderByCreatedAtAsc(
            TermDepositContractEntity.TermDepositStatus status);

    // Count active by account
    long countByAccountNoAndStatus(String accountNo, TermDepositContractEntity.TermDepositStatus status);
}
