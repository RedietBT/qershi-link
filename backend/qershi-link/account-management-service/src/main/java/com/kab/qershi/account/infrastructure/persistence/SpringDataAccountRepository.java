package com.kab.qershi.account.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for Accounts table.
 * Includes tenant-isolated account lookup by member phone number.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataAccountRepository extends JpaRepository<AccountEntity, UUID> {

    Optional<AccountEntity> findByAccountNo(String accountNo);

    List<AccountEntity> findByUserId(UUID userId);

    long countBySaccoCodeAndProductCode(String saccoCode, String productCode);

    boolean existsByAccountNo(String accountNo);

    /**
     * Native query executing tenant-isolated phone number to account lookup by joining master_schema.users identity table.
     */
    @Query(value = "SELECT a.* FROM accounts a " +
            "JOIN master_schema.users u ON u.user_id = a.user_id " +
            "WHERE u.msisdn = :phoneNumber OR REPLACE(u.msisdn, '+', '') = REPLACE(:phoneNumber, '+', '')", nativeQuery = true)
    List<AccountEntity> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);
}
