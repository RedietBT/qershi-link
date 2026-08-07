package com.kab.qershi.loan.origination.infrastructure.persistence.repository;

import com.kab.qershi.loan.origination.infrastructure.persistence.entity.LoanApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for loan_applications table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataLoanApplicationRepository extends JpaRepository<LoanApplicationEntity, UUID> {

    Optional<LoanApplicationEntity> findByApplicationNo(String applicationNo);

    List<LoanApplicationEntity> findByUserId(UUID userId);
}
