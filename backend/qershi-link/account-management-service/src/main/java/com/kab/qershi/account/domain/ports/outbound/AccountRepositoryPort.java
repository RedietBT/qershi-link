package com.kab.qershi.account.domain.ports.outbound;

import com.kab.qershi.account.domain.model.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound repository port for managing member core account persistence.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface AccountRepositoryPort {

    Account save(Account account);

    Optional<Account> findByAccountId(UUID accountId);

    Optional<Account> findByAccountNo(String accountNo);

    List<Account> findByUserId(UUID userId);

    List<Account> findByPhoneNumber(String phoneNumber);

    List<Account> findAllAccounts();

    long countAccountsBySaccoAndProduct(String saccoCode, String productCode);

    boolean existsByAccountNo(String accountNo);
}
