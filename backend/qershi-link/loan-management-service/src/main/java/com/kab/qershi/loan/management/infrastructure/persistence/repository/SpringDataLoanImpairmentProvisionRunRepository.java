package com.kab.qershi.loan.management.infrastructure.persistence.repository;

import com.kab.qershi.loan.management.infrastructure.persistence.entity.LoanImpairmentProvisionRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for IFRS 9 provision run headers.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataLoanImpairmentProvisionRunRepository
        extends JpaRepository<LoanImpairmentProvisionRunEntity, UUID> {

    Optional<LoanImpairmentProvisionRunEntity> findByBusinessDate(LocalDate businessDate);

    List<LoanImpairmentProvisionRunEntity> findTop12ByStatusOrderByBusinessDateDesc(String status);

    @Query("SELECT r FROM LoanImpairmentProvisionRunEntity r ORDER BY r.businessDate DESC")
    List<LoanImpairmentProvisionRunEntity> findAllOrderedByDateDesc();

    @Query("SELECT r FROM LoanImpairmentProvisionRunEntity r WHERE r.status = 'COMPLETED' ORDER BY r.businessDate DESC LIMIT 1")
    Optional<LoanImpairmentProvisionRunEntity> findLatestCompleted();
}
