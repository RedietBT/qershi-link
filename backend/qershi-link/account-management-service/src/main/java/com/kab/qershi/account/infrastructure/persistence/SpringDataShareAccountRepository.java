package com.kab.qershi.account.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for Share Accounts.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataShareAccountRepository extends JpaRepository<ShareAccountEntity, UUID> {

    Optional<ShareAccountEntity> findByMemberId(UUID memberId);

    Optional<ShareAccountEntity> findByAccountNumber(String accountNumber);

    boolean existsByMemberId(UUID memberId);

    List<ShareAccountEntity> findByStatus(String status);

    List<ShareAccountEntity> findBySaccoCodeAndStatus(String saccoCode, String status);
}
