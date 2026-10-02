package com.kab.qershi.account.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataStandingOrderRepository
        extends JpaRepository<StandingOrderEntity, UUID> {

    Optional<StandingOrderEntity> findByStandingOrderNo(String standingOrderNo);

    List<StandingOrderEntity> findByMemberId(UUID memberId);

    List<StandingOrderEntity> findBySourceAccountId(UUID sourceAccountId);

    /** Fetch all ACTIVE orders due for sweep on or before the given date */
    @Query("SELECT s FROM StandingOrderEntity s WHERE s.status = 'ACTIVE' AND s.nextRunDate <= :asOfDate")
    List<StandingOrderEntity> findDueOrders(@Param("asOfDate") LocalDate asOfDate);
}
