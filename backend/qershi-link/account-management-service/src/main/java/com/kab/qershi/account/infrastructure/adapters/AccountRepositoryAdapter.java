package com.kab.qershi.account.infrastructure.adapters;

import com.kab.qershi.account.domain.model.Account;
import com.kab.qershi.account.domain.ports.outbound.AccountRepositoryPort;
import com.kab.qershi.account.infrastructure.persistence.AccountEntity;
import com.kab.qershi.account.infrastructure.persistence.SpringDataAccountRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Infrastructure persistence adapter implementing AccountRepositoryPort.
 * Bridges domain models and Spring Data JPA entities.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class AccountRepositoryAdapter implements AccountRepositoryPort {

    private final SpringDataAccountRepository accountRepository;

    public AccountRepositoryAdapter(SpringDataAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account save(Account account) {
        AccountEntity entity = toEntity(account);
        AccountEntity saved = accountRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Account> findByAccountId(UUID accountId) {
        return accountRepository.findById(accountId).map(this::toDomain);
    }

    @Override
    public Optional<Account> findByAccountNo(String accountNo) {
        return accountRepository.findByAccountNo(accountNo).map(this::toDomain);
    }

    @Override
    public List<Account> findByUserId(UUID userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Account> findByPhoneNumber(String phoneNumber) {
        return accountRepository.findByPhoneNumber(phoneNumber).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Account> findAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countAccountsBySaccoAndProduct(String saccoCode, String productCode) {
        return accountRepository.countBySaccoCodeAndProductCode(saccoCode, productCode);
    }

    @Override
    public boolean existsByAccountNo(String accountNo) {
        return accountRepository.existsByAccountNo(accountNo);
    }

    private AccountEntity toEntity(Account domain) {
        if (domain == null) return null;
        return new AccountEntity(
                domain.getAccountId(),
                domain.getAccountNo(),
                domain.getUserId(),
                domain.getSaccoCode(),
                domain.getBranchCode(),
                domain.getProductCode(),
                domain.getBookBalance(),
                domain.getLienHoldAmount(),
                domain.getStatus(),
                domain.getFreezeStatus(),
                domain.getOpenedDate(),
                domain.getApprovedByUserId(),
                domain.getApprovalDate(),
                domain.getClosedDate(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    private Account toDomain(AccountEntity entity) {
        if (entity == null) return null;
        return new Account(
                entity.getAccountId(),
                entity.getAccountNo(),
                entity.getUserId(),
                entity.getSaccoCode(),
                entity.getBranchCode(),
                entity.getProductCode(),
                entity.getBookBalance(),
                entity.getLienHoldAmount(),
                entity.getStatus(),
                entity.getFreezeStatus(),
                entity.getOpenedDate(),
                entity.getApprovedByUserId(),
                entity.getApprovalDate(),
                entity.getClosedDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
