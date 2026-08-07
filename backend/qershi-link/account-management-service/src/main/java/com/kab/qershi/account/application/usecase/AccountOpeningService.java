package com.kab.qershi.account.application.usecase;

import com.kab.qershi.account.domain.model.Account;
import com.kab.qershi.account.domain.model.AccountProduct;
import com.kab.qershi.account.domain.model.AccountStatus;
import com.kab.qershi.account.domain.model.FreezeStatus;
import com.kab.qershi.account.domain.ports.inbound.AccountOpeningUseCase;
import com.kab.qershi.account.domain.ports.outbound.AccountRepositoryPort;
import com.kab.qershi.account.domain.ports.outbound.ProductRepositoryPort;
import com.kab.qershi.account.domain.ports.outbound.ProfileValidationPort;
import com.kab.qershi.account.domain.service.AccountNumberGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Application service implementing AccountOpeningUseCase.
 * Handles core account creation with Luhn check-digit generation, Four-Eye Maker-Checker approval,
 * and tenant-isolated phone number search.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
@Transactional
public class AccountOpeningService implements AccountOpeningUseCase {

    private final AccountRepositoryPort accountRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final ProfileValidationPort profileValidationPort;
    private final AccountNumberGenerator accountNumberGenerator;
    private final com.kab.qershi.account.infrastructure.adapters.NotificationGrpcClientAdapter notificationAdapter;

    public AccountOpeningService(AccountRepositoryPort accountRepositoryPort,
                                 ProductRepositoryPort productRepositoryPort,
                                 ProfileValidationPort profileValidationPort,
                                 AccountNumberGenerator accountNumberGenerator,
                                 com.kab.qershi.account.infrastructure.adapters.NotificationGrpcClientAdapter notificationAdapter) {
        this.accountRepositoryPort = accountRepositoryPort;
        this.productRepositoryPort = productRepositoryPort;
        this.profileValidationPort = profileValidationPort;
        this.accountNumberGenerator = accountNumberGenerator;
        this.notificationAdapter = notificationAdapter;
    }

    @Override
    public Account openAccount(UUID userId, String saccoCode, String branchCode, String productCode) {
        // 1. Validate member active status
        if (!profileValidationPort.isMemberActive(userId)) {
            throw new IllegalStateException("Cannot open account for member ID " + userId + ". Member status must be active.");
        }

        // 2. Validate product existence & active state
        AccountProduct product = productRepositoryPort.findByProductCode(productCode)
                .orElseThrow(() -> new IllegalArgumentException("Product not found for code: " + productCode));

        if (!product.isActive()) {
            throw new IllegalStateException("Product " + productCode + " is currently inactive.");
        }

        // 3. Generate sequential Luhn account number
        long count = accountRepositoryPort.countAccountsBySaccoAndProduct(saccoCode, productCode);
        long sequenceNumber = count + 1;
        String accountNo = accountNumberGenerator.generateAccountNo(saccoCode, branchCode, productCode, sequenceNumber);
        while (accountRepositoryPort.existsByAccountNo(accountNo)) {
            sequenceNumber++;
            accountNo = accountNumberGenerator.generateAccountNo(saccoCode, branchCode, productCode, sequenceNumber);
        }

        // 4. Construct Account aggregate root
        Account account = new Account(
                UUID.randomUUID(),
                accountNo,
                userId,
                saccoCode,
                branchCode,
                productCode,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                AccountStatus.PENDING_APPROVAL,
                FreezeStatus.NONE,
                LocalDateTime.now(),
                null,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        return accountRepositoryPort.save(account);
    }

    @Override
    public Account approveAccount(String accountNo, UUID checkerUserId) {
        Account account = getAccountByNo(accountNo);
        account.approveAccount(checkerUserId);
        Account approved = accountRepositoryPort.save(account);

        try {
            AccountProduct product = productRepositoryPort.findByProductCode(approved.getProductCode()).orElse(null);
            String prodName = product != null ? product.getProductName() : approved.getProductCode();
            notificationAdapter.sendAccountOpenedNotification("", "Member " + approved.getUserId().toString().substring(0, 8), approved.getAccountNo(), prodName);
        } catch (Exception ex) {
            org.slf4j.LoggerFactory.getLogger(AccountOpeningService.class).warn("Failed dispatching account opened SMS: {}", ex.getMessage());
        }

        return approved;
    }

    @Override
    @Transactional(readOnly = true)
    public Account getAccountByNo(String accountNo) {
        return accountRepositoryPort.findByAccountNo(accountNo)
                .orElseThrow(() -> new IllegalArgumentException("Account not found for account number: " + accountNo));
    }

    @Override
    @Transactional(readOnly = true)
    public Account getAccountById(UUID accountId) {
        return accountRepositoryPort.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found for ID: " + accountId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getAccountsByUserId(UUID userId) {
        return accountRepositoryPort.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getAccountsByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number is required for account search.");
        }
        return accountRepositoryPort.findByPhoneNumber(phoneNumber.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        return accountRepositoryPort.findAllAccounts();
    }
}
