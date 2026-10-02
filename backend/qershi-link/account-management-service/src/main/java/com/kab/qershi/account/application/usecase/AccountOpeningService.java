package com.kab.qershi.account.application.usecase;

import com.kab.qershi.account.domain.model.Account;
import com.kab.qershi.account.domain.model.AccountAuditLog;
import com.kab.qershi.account.domain.model.AccountProduct;
import com.kab.qershi.account.domain.model.AccountStatus;
import com.kab.qershi.account.domain.model.FreezeStatus;
import com.kab.qershi.account.domain.model.SaccoConfig;
import com.kab.qershi.account.domain.ports.inbound.AccountOpeningUseCase;
import com.kab.qershi.account.domain.ports.outbound.AccountAuditLogRepositoryPort;
import com.kab.qershi.account.domain.ports.outbound.AccountRepositoryPort;
import com.kab.qershi.account.domain.ports.outbound.ProductRepositoryPort;
import com.kab.qershi.account.domain.ports.outbound.ProfileValidationPort;
import com.kab.qershi.account.domain.ports.outbound.SaccoConfigRepositoryPort;
import com.kab.qershi.account.domain.service.AccountNumberGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @version 1.2.0
 */
@Service
@Transactional
public class AccountOpeningService implements AccountOpeningUseCase {

    private static final Logger log = LoggerFactory.getLogger(AccountOpeningService.class);

    private final AccountRepositoryPort accountRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final ProfileValidationPort profileValidationPort;
    private final AccountNumberGenerator accountNumberGenerator;
    private final com.kab.qershi.account.infrastructure.adapters.NotificationGrpcClientAdapter notificationAdapter;
    private final AccountAuditLogRepositoryPort auditLogRepositoryPort;
    private final SaccoConfigRepositoryPort saccoConfigRepositoryPort;

    public AccountOpeningService(AccountRepositoryPort accountRepositoryPort,
                                 ProductRepositoryPort productRepositoryPort,
                                 ProfileValidationPort profileValidationPort,
                                 AccountNumberGenerator accountNumberGenerator,
                                 com.kab.qershi.account.infrastructure.adapters.NotificationGrpcClientAdapter notificationAdapter,
                                 AccountAuditLogRepositoryPort auditLogRepositoryPort,
                                 SaccoConfigRepositoryPort saccoConfigRepositoryPort) {
        this.accountRepositoryPort = accountRepositoryPort;
        this.productRepositoryPort = productRepositoryPort;
        this.profileValidationPort = profileValidationPort;
        this.accountNumberGenerator = accountNumberGenerator;
        this.notificationAdapter = notificationAdapter;
        this.auditLogRepositoryPort = auditLogRepositoryPort;
        this.saccoConfigRepositoryPort = saccoConfigRepositoryPort;
    }

    @Override
    public Account openAccount(UUID userId, String branchCode, String productCode) {
        // 1. Resolve tenant SACCO Code & Branch Code configuration
        SaccoConfig saccoConfig = saccoConfigRepositoryPort.findFirst()
                .orElseGet(() -> new SaccoConfig(null, "0001", "Default SACCO", "0001", null, null));

        String saccoCode = saccoConfig.getSaccoCode();
        String finalBranchCode = (branchCode != null && !branchCode.isBlank()) ? branchCode.trim() : saccoConfig.getBranchCode();

        // 2. Validate member active status
        if (!profileValidationPort.isMemberActive(userId)) {
            throw new IllegalStateException("Cannot open account for member ID " + userId + ". Member status must be active.");
        }

        // 3. Validate product existence & active state
        AccountProduct product = productRepositoryPort.findByProductCode(productCode)
                .orElseThrow(() -> new IllegalArgumentException("Product not found for code: " + productCode));

        if (!product.isActive()) {
            throw new IllegalStateException("Product " + productCode + " is currently inactive.");
        }

        // 4. Generate sequential Luhn account number
        long count = accountRepositoryPort.countAccountsBySaccoAndProduct(saccoCode, productCode);
        long sequenceNumber = count + 1;
        String accountNo = accountNumberGenerator.generateAccountNo(saccoCode, finalBranchCode, productCode, sequenceNumber);
        while (accountRepositoryPort.existsByAccountNo(accountNo)) {
            sequenceNumber++;
            accountNo = accountNumberGenerator.generateAccountNo(saccoCode, finalBranchCode, productCode, sequenceNumber);
        }

        // 5. Construct Account aggregate root
        Account account = new Account(
                UUID.randomUUID(),
                accountNo,
                userId,
                saccoCode,
                finalBranchCode,
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
        account.setLastActivityDate(java.time.LocalDate.now());

        Account saved = accountRepositoryPort.save(account);

        try {
            auditLogRepositoryPort.save(new AccountAuditLog(
                    UUID.randomUUID(),
                    saved.getAccountNo(),
                    saved.getUserId(),
                    userId,
                    "ACCOUNT_OPENED",
                    "status",
                    null,
                    "PENDING_APPROVAL",
                    LocalDateTime.now()
            ));
        } catch (Exception ex) {
            log.warn("Failed writing account open audit log: {}", ex.getMessage());
        }

        return saved;
    }

    @Override
    public Account approveAccount(String accountNo, UUID checkerUserId) {
        Account account = getAccountByNo(accountNo);
        account.approveAccount(checkerUserId);
        Account approved = accountRepositoryPort.save(account);

        try {
            auditLogRepositoryPort.save(new AccountAuditLog(
                    UUID.randomUUID(),
                    approved.getAccountNo(),
                    approved.getUserId(),
                    checkerUserId,
                    "ACCOUNT_APPROVED",
                    "status",
                    "PENDING_APPROVAL",
                    "ACTIVE",
                    LocalDateTime.now()
            ));
        } catch (Exception ex) {
            log.warn("Failed writing account approval audit log: {}", ex.getMessage());
        }

        try {
            AccountProduct product = productRepositoryPort.findByProductCode(approved.getProductCode()).orElse(null);
            String prodName = product != null ? product.getProductName() : approved.getProductCode();

            // Fetch member phone number & full name via domain port
            ProfileValidationPort.ProfileContact contact = profileValidationPort.getProfileContact(approved.getUserId());
            String recipientPhone = (contact != null) ? contact.phoneNumber() : null;
            String memberName = (contact != null && contact.fullName() != null && !contact.fullName().isBlank())
                    ? contact.fullName().trim()
                    : "Valued Member";

            // Fetch SACCO Name from sacco config
            String saccoName = saccoConfigRepositoryPort.findFirst()
                    .map(SaccoConfig::getSaccoName)
                    .orElse("SACCO");

            if (recipientPhone != null && !recipientPhone.isBlank()) {
                notificationAdapter.sendAccountOpenedNotification(recipientPhone, memberName, approved.getAccountNo(), prodName, saccoName);
            } else {
                log.warn("Skipping account opening SMS dispatch for user {}: Recipient phone number not found.", approved.getUserId());
            }
        } catch (Exception ex) {
            log.warn("Failed dispatching account opened SMS: {}", ex.getMessage());
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
