package com.kab.qershi.account.domain.ports.inbound;

import com.kab.qershi.account.domain.model.Account;

import java.util.List;
import java.util.UUID;

/**
 * Inbound Use Case interface for Member Core Account Lifecycle management.
 * Includes tenant-isolated account lookup by phone number.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface AccountOpeningUseCase {

    Account openAccount(UUID userId, String saccoCode, String branchCode, String productCode);

    Account approveAccount(String accountNo, UUID checkerUserId);

    Account getAccountByNo(String accountNo);

    Account getAccountById(UUID accountId);

    List<Account> getAccountsByUserId(UUID userId);

    List<Account> getAccountsByPhoneNumber(String phoneNumber);

    List<Account> getAllAccounts();
}
