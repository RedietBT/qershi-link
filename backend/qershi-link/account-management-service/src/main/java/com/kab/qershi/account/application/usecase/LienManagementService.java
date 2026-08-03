package com.kab.qershi.account.application.usecase;

import com.kab.qershi.account.domain.model.Account;
import com.kab.qershi.account.domain.model.AccountLien;
import com.kab.qershi.account.domain.model.FreezeStatus;
import com.kab.qershi.account.domain.model.LienStatus;
import com.kab.qershi.account.domain.ports.inbound.LienManagementUseCase;
import com.kab.qershi.account.domain.ports.outbound.AccountRepositoryPort;
import com.kab.qershi.account.domain.ports.outbound.LienRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Application service implementing LienManagementUseCase.
 * Handles monetary collateral holds and administrative freeze states.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
@Transactional
public class LienManagementService implements LienManagementUseCase {

    private final AccountRepositoryPort accountRepositoryPort;
    private final LienRepositoryPort lienRepositoryPort;

    public LienManagementService(AccountRepositoryPort accountRepositoryPort,
                                 LienRepositoryPort lienRepositoryPort) {
        this.accountRepositoryPort = accountRepositoryPort;
        this.lienRepositoryPort = lienRepositoryPort;
    }

    @Override
    public AccountLien placeLien(String accountNo, BigDecimal amount, String reason, String referenceNo, UUID officerUserId) {
        Account account = accountRepositoryPort.findByAccountNo(accountNo)
                .orElseThrow(() -> new IllegalArgumentException("Account not found for account number: " + accountNo));

        account.placeLien(amount);
        accountRepositoryPort.save(account);

        AccountLien lien = new AccountLien(
                UUID.randomUUID(),
                accountNo,
                amount,
                reason,
                referenceNo,
                officerUserId,
                null,
                LienStatus.ACTIVE,
                LocalDateTime.now(),
                null
        );

        return lienRepositoryPort.save(lien);
    }

    @Override
    public AccountLien releaseLien(UUID lienId, UUID officerUserId) {
        AccountLien lien = lienRepositoryPort.findByLienId(lienId)
                .orElseThrow(() -> new IllegalArgumentException("Lien hold not found for ID: " + lienId));

        if (lien.getStatus() != LienStatus.ACTIVE) {
            throw new IllegalStateException("Lien hold " + lienId + " is already released or expired.");
        }

        Account account = accountRepositoryPort.findByAccountNo(lien.getAccountNo())
                .orElseThrow(() -> new IllegalArgumentException("Account not found for account number: " + lien.getAccountNo()));

        account.releaseLien(lien.getLienAmount());
        accountRepositoryPort.save(account);

        lien.release(officerUserId);
        return lienRepositoryPort.save(lien);
    }

    @Override
    public Account freezeAccount(String accountNo, String freezeStatusStr, UUID officerUserId, String reason) {
        Account account = accountRepositoryPort.findByAccountNo(accountNo)
                .orElseThrow(() -> new IllegalArgumentException("Account not found for account number: " + accountNo));

        FreezeStatus freezeStatus;
        try {
            freezeStatus = FreezeStatus.valueOf(freezeStatusStr.toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid freeze status: " + freezeStatusStr + ". Allowed: NONE, DEBIT_FREEZE, CREDIT_FREEZE, FULL_FREEZE");
        }

        account.setFreezeStatus(freezeStatus);
        account.setUpdatedAt(LocalDateTime.now());
        return accountRepositoryPort.save(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountLien> getActiveLiensForAccount(String accountNo) {
        return lienRepositoryPort.findActiveLiensByAccountNo(accountNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountLien> getAllLiensForAccount(String accountNo) {
        return lienRepositoryPort.findByAccountNo(accountNo);
    }
}
