package com.kab.qershi.pricing.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository for InterestTaxDeductionLogEntity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataInterestTaxDeductionLogRepository extends JpaRepository<InterestTaxDeductionLogEntity, UUID> {

    List<InterestTaxDeductionLogEntity> findByAccountNo(String accountNo);

    List<InterestTaxDeductionLogEntity> findByBusinessDate(LocalDate businessDate);

    @Query("SELECT COALESCE(SUM(l.taxWithheld), 0) FROM InterestTaxDeductionLogEntity l WHERE l.businessDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTaxWithheldBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
