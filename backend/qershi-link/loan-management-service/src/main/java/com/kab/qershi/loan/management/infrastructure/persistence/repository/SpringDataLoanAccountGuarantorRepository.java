package com.kab.qershi.loan.management.infrastructure.persistence.repository;

import com.kab.qershi.loan.management.infrastructure.persistence.entity.LoanAccountGuarantorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository for Loan Account Guarantors.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataLoanAccountGuarantorRepository extends JpaRepository<LoanAccountGuarantorEntity, UUID> {

    List<LoanAccountGuarantorEntity> findByAccountId(UUID accountId);

    List<LoanAccountGuarantorEntity> findByAccountIdAndStatus(UUID accountId, String status);

    List<LoanAccountGuarantorEntity> findByApplicationId(UUID applicationId);
}
