package com.kab.qershi.loan.management.infrastructure.persistence.repository;

import com.kab.qershi.loan.management.infrastructure.persistence.entity.LoanDelinquencySnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for LoanDelinquencySnapshotEntity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataLoanDelinquencySnapshotRepository extends JpaRepository<LoanDelinquencySnapshotEntity, UUID> {

    List<LoanDelinquencySnapshotEntity> findByBusinessDate(LocalDate businessDate);

    Optional<LoanDelinquencySnapshotEntity> findByAccountIdAndBusinessDate(UUID accountId, LocalDate businessDate);

    @Query("SELECT s FROM LoanDelinquencySnapshotEntity s WHERE s.businessDate = " +
           "(SELECT MAX(s2.businessDate) FROM LoanDelinquencySnapshotEntity s2) ORDER BY s.daysPastDue DESC")
    List<LoanDelinquencySnapshotEntity> findLatestSnapshots();

    @Query("SELECT s FROM LoanDelinquencySnapshotEntity s WHERE s.parBucket != 'CURRENT' AND s.businessDate = " +
           "(SELECT MAX(s2.businessDate) FROM LoanDelinquencySnapshotEntity s2) ORDER BY s.daysPastDue DESC")
    List<LoanDelinquencySnapshotEntity> findLatestDelinquentSnapshots();
}
