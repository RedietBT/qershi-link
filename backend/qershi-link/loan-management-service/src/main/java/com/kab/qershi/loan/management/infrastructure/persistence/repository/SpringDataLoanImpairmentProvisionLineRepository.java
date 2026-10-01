package com.kab.qershi.loan.management.infrastructure.persistence.repository;

import com.kab.qershi.loan.management.infrastructure.persistence.entity.LoanImpairmentProvisionLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for per-loan IFRS 9 provision detail lines.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataLoanImpairmentProvisionLineRepository
        extends JpaRepository<LoanImpairmentProvisionLineEntity, UUID> {

    List<LoanImpairmentProvisionLineEntity> findByRunId(UUID runId);

    List<LoanImpairmentProvisionLineEntity> findByRunIdAndIfrs9Stage(UUID runId, String ifrs9Stage);

    long countByRunId(UUID runId);
}
