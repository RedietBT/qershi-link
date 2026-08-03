package com.kab.qershi.account.infrastructure.persistence;

import com.kab.qershi.account.domain.model.LienStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository for Account Liens table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataLienRepository extends JpaRepository<AccountLienEntity, UUID> {

    List<AccountLienEntity> findByAccountNo(String accountNo);

    List<AccountLienEntity> findByAccountNoAndStatus(String accountNo, LienStatus status);
}
