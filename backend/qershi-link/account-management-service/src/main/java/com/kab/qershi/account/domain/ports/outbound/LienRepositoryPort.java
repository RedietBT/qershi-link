package com.kab.qershi.account.domain.ports.outbound;

import com.kab.qershi.account.domain.model.AccountLien;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound repository port for managing monetary lien hold persistence.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface LienRepositoryPort {

    AccountLien save(AccountLien lien);

    Optional<AccountLien> findByLienId(UUID lienId);

    List<AccountLien> findByAccountNo(String accountNo);

    List<AccountLien> findActiveLiensByAccountNo(String accountNo);
}
