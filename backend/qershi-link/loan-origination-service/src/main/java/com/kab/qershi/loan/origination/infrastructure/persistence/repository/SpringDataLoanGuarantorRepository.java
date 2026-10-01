package com.kab.qershi.loan.origination.infrastructure.persistence.repository;

import com.kab.qershi.loan.origination.infrastructure.persistence.entity.LoanGuarantorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository for Loan Guarantor entities.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataLoanGuarantorRepository extends JpaRepository<LoanGuarantorEntity, UUID> {

    List<LoanGuarantorEntity> findByApplicationId(UUID applicationId);

    List<LoanGuarantorEntity> findByGuarantorUserId(UUID guarantorUserId);

    List<LoanGuarantorEntity> findBySavingsAccountNo(String savingsAccountNo);
}
