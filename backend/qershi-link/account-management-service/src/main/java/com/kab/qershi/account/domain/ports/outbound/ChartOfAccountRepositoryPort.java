package com.kab.qershi.account.domain.ports.outbound;

import com.kab.qershi.account.domain.model.ChartOfAccount;
import com.kab.qershi.account.domain.model.GlAccountType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound repository port for Chart of Accounts (GL) persistence.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface ChartOfAccountRepositoryPort {

    ChartOfAccount save(ChartOfAccount chartOfAccount);

    Optional<ChartOfAccount> findById(UUID accountId);

    Optional<ChartOfAccount> findByGlCode(String glCode);

    List<ChartOfAccount> findByParentGlCode(String parentGlCode);

    List<ChartOfAccount> findAllOrderByGlCodeAsc();

    List<ChartOfAccount> findByAccountTypeOrderByGlCodeAsc(GlAccountType accountType);

    boolean existsByGlCode(String glCode);
}
