package com.kab.qershi.account.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for SystemBusinessDateEntity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataSystemBusinessDateRepository extends JpaRepository<SystemBusinessDateEntity, UUID> {

    @Query("SELECT b FROM SystemBusinessDateEntity b ORDER BY b.updatedAt DESC LIMIT 1")
    Optional<SystemBusinessDateEntity> findCurrentBusinessDate();
}
