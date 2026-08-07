package com.kab.qershi.loan.origination.infrastructure.persistence.repository;

import com.kab.qershi.loan.origination.infrastructure.persistence.entity.LoanGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for loan_groups table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataLoanGroupRepository extends JpaRepository<LoanGroupEntity, UUID> {

    boolean existsByLicenseNo(String licenseNo);
}
