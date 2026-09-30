package com.kab.qershi.account.infrastructure.persistence;

import com.kab.qershi.account.domain.model.GlAccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for ChartOfAccountEntity.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Repository
public interface SpringDataChartOfAccountRepository extends JpaRepository<ChartOfAccountEntity, UUID> {

    Optional<ChartOfAccountEntity> findByGlCode(String glCode);

    List<ChartOfAccountEntity> findByParentGlCode(String parentGlCode);

    List<ChartOfAccountEntity> findAllByOrderByGlCodeAsc();

    List<ChartOfAccountEntity> findByAccountTypeOrderByGlCodeAsc(GlAccountType accountType);

    boolean existsByGlCode(String glCode);
}
