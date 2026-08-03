package com.kab.qershi.account.domain.ports.inbound;

import com.kab.qershi.account.domain.model.Account;
import com.kab.qershi.account.domain.model.AccountLien;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Inbound Use Case interface for Lien Holds and Administrative Freeze controls.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface LienManagementUseCase {

    AccountLien placeLien(String accountNo, BigDecimal amount, String reason, String referenceNo, UUID officerUserId);

    AccountLien releaseLien(UUID lienId, UUID officerUserId);

    Account freezeAccount(String accountNo, String freezeStatus, UUID officerUserId, String reason);

    List<AccountLien> getActiveLiensForAccount(String accountNo);

    List<AccountLien> getAllLiensForAccount(String accountNo);
}
