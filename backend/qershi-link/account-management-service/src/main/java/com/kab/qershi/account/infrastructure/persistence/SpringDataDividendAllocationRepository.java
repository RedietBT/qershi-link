package com.kab.qershi.account.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataDividendAllocationRepository
        extends JpaRepository<DividendAllocationEntity, UUID> {

    List<DividendAllocationEntity> findByDistributionId(UUID distributionId);

    @Modifying
    @Query("DELETE FROM DividendAllocationEntity a WHERE a.distributionId = :distributionId")
    void deleteByDistributionId(@Param("distributionId") UUID distributionId);
}
