package com.kab.qershi.account.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataDividendDistributionRepository
        extends JpaRepository<DividendDistributionEntity, UUID> {

    Optional<DividendDistributionEntity> findByFiscalYear(Integer fiscalYear);
}
