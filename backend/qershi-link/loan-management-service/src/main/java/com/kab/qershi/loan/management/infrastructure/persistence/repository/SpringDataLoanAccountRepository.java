package com.kab.qershi.loan.management.infrastructure.persistence.repository;

import com.kab.qershi.loan.management.infrastructure.persistence.entity.LoanAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for LoanAccountEntity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataLoanAccountRepository extends JpaRepository<LoanAccountEntity, UUID> {

    Optional<LoanAccountEntity> findByApplicationId(UUID applicationId);

    List<LoanAccountEntity> findByUserId(UUID userId);
}
