package com.kab.qershi.account.infrastructure.grpc;

import com.kab.qershi.account.domain.model.Account;
import com.kab.qershi.account.domain.model.AccountProduct;
import com.kab.qershi.account.domain.ports.inbound.AccountOpeningUseCase;
import com.kab.qershi.account.domain.ports.inbound.ProductManagementUseCase;
import com.kab.qershi.account.domain.ports.outbound.AccountRepositoryPort;
import com.kab.qershi.account.domain.ports.outbound.ProfileValidationPort;
import com.kab.qershi.account.infrastructure.persistence.ProductMakerCheckerRuleEntity;
import com.kab.qershi.account.infrastructure.persistence.SpringDataProductMakerCheckerRuleRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * gRPC Service Server implementation exposing high-speed inter-service RPCs for Core Account management.
 * Allows transaction and loan services to validate account balances, product risk limits, and freeze states.
 *
 * @author KAB Digital Solution PLC
 * @version 1.1.0
 */
@GrpcService
public class AccountGrpcServiceImpl extends AccountGrpcServiceGrpc.AccountGrpcServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(AccountGrpcServiceImpl.class);

    private final AccountOpeningUseCase accountOpeningUseCase;
    private final ProductManagementUseCase productManagementUseCase;
    private final AccountRepositoryPort accountRepositoryPort;
    private final ProfileValidationPort profileValidationPort;
    private final SpringDataProductMakerCheckerRuleRepository productRuleRepository;

    public AccountGrpcServiceImpl(AccountOpeningUseCase accountOpeningUseCase,
                                  ProductManagementUseCase productManagementUseCase,
                                  AccountRepositoryPort accountRepositoryPort,
                                  ProfileValidationPort profileValidationPort,
                                  SpringDataProductMakerCheckerRuleRepository productRuleRepository) {
        this.accountOpeningUseCase = accountOpeningUseCase;
        this.productManagementUseCase = productManagementUseCase;
        this.accountRepositoryPort = accountRepositoryPort;
        this.profileValidationPort = profileValidationPort;
        this.productRuleRepository = productRuleRepository;
    }

    private Account resolveAccount(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Account identifier cannot be null or blank.");
        }
        try {
            java.util.UUID id = java.util.UUID.fromString(identifier.trim());
            return accountOpeningUseCase.getAccountById(id);
        } catch (IllegalArgumentException notUuid) {
            return accountOpeningUseCase.getAccountByNo(identifier.trim());
        }
    }

    @Override
    public void getAccountByNo(AccountNoRequest request, StreamObserver<AccountProtoResponse> responseObserver) {
        log.debug("gRPC GetAccountByNo request received for accountNo: {}, schema: {}", request.getAccountNo(), request.getTenantSchema());
        try {
            if (request.getTenantSchema() != null && !request.getTenantSchema().isBlank()) {
                com.kab.qershi.account.infrastructure.config.TenantContext.setTenantSchema(request.getTenantSchema().trim());
            }
            Account account = resolveAccount(request.getAccountNo());
            AccountProduct product = productManagementUseCase.getProductByCode(account.getProductCode());
            BigDecimal minBalance = product != null ? product.getMinOperatingBalance() : BigDecimal.ZERO;
            BigDecimal available = account.getAvailableBalance(minBalance);
            
            ProfileValidationPort.ProfileContact contact = profileValidationPort.getProfileContact(account.getUserId());

            AccountProtoResponse response = AccountProtoResponse.newBuilder()
                    .setAccountId(account.getAccountId().toString())
                    .setAccountNo(account.getAccountNo())
                    .setUserId(account.getUserId().toString())
                    .setSaccoCode(account.getSaccoCode())
                    .setBranchCode(account.getBranchCode())
                    .setProductCode(account.getProductCode())
                    .setBookBalance(account.getBookBalance().toPlainString())
                    .setLienHoldAmount(account.getLienHoldAmount().toPlainString())
                    .setAvailableBalance(available.toPlainString())
                    .setStatus(account.getStatus().name())
                    .setFreezeStatus(account.getFreezeStatus().name())
                    .setPhoneNumber(contact.phoneNumber() != null ? contact.phoneNumber() : "")
                    .setFullName(contact.fullName() != null ? contact.fullName() : "Member")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("gRPC GetAccountByNo failed for accountNo {}: {}", request.getAccountNo(), ex.getMessage());
            responseObserver.onError(ex);
        } finally {
            com.kab.qershi.account.infrastructure.config.TenantContext.clear();
        }
    }

    @Override
    public void validateAccountForDebit(DebitValidationProtoRequest request, StreamObserver<ValidationProtoResponse> responseObserver) {
        log.debug("gRPC ValidateAccountForDebit request for accountNo: {}, amount: {}, schema: {}",
                request.getAccountNo(), request.getAmount(), request.getTenantSchema());
        try {
            if (request.getTenantSchema() != null && !request.getTenantSchema().isBlank()) {
                com.kab.qershi.account.infrastructure.config.TenantContext.setTenantSchema(request.getTenantSchema().trim());
            }
            Account account = resolveAccount(request.getAccountNo());
            BigDecimal amount = new BigDecimal(request.getAmount());

            Optional<ProductMakerCheckerRuleEntity> ruleOpt = productRuleRepository.findByProductCode(account.getProductCode());
            BigDecimal minBalance = BigDecimal.ZERO;
            if (ruleOpt.isPresent() && ruleOpt.get().getMinOperatingBalance() != null) {
                minBalance = ruleOpt.get().getMinOperatingBalance();
            } else {
                AccountProduct product = productManagementUseCase.getProductByCode(account.getProductCode());
                if (product != null && product.getMinOperatingBalance() != null) {
                    minBalance = product.getMinOperatingBalance();
                }
            }

            BigDecimal available = account.getAvailableBalance(minBalance);

            // 1. Basic debitability and minimum operating floor balance check
            if (!account.canPerformDebit(amount, minBalance)) {
                String reason = "Debit rejected: Insufficient available funds. Required minimum operating balance: " 
                        + minBalance.setScale(2, java.math.RoundingMode.HALF_UP) + " ETB, Current Available: " 
                        + available.setScale(2, java.math.RoundingMode.HALF_UP) + " ETB.";
                if (account.getFreezeStatus() != null && account.getFreezeStatus().blocksDebit()) {
                    reason = "Debit rejected: Account debit is blocked by freeze status (" + account.getFreezeStatus() + ").";
                } else if (account.getStatus() != com.kab.qershi.account.domain.model.AccountStatus.ACTIVE) {
                    reason = "Debit rejected: Account is not active (status: " + account.getStatus() + ").";
                }
                ValidationProtoResponse response = ValidationProtoResponse.newBuilder()
                        .setIsValid(false)
                        .setMessage(reason)
                        .setAvailableBalance(available.toPlainString())
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            // 2. Single Transaction Supervisor Threshold Check (Product-Specific Maker-Checker)
            if (ruleOpt.isPresent()) {
                ProductMakerCheckerRuleEntity rule = ruleOpt.get();
                if (rule.isEnableMakerChecker() && rule.getSingleWithdrawalLimit() != null 
                        && rule.getSingleWithdrawalLimit().compareTo(BigDecimal.ZERO) > 0 
                        && amount.compareTo(rule.getSingleWithdrawalLimit()) > 0) {
                    ValidationProtoResponse response = ValidationProtoResponse.newBuilder()
                            .setIsValid(false)
                            .setMessage("Withdrawal amount of " + amount.setScale(2, java.math.RoundingMode.HALF_UP) 
                                    + " ETB exceeds the single transaction supervisor threshold (" 
                                    + rule.getSingleWithdrawalLimit().setScale(2, java.math.RoundingMode.HALF_UP) 
                                    + " ETB) for " + rule.getProductName() + ". Requires Maker-Checker Branch Manager authorization.")
                            .setAvailableBalance(available.toPlainString())
                            .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                    return;
                }

                // 3. Daily Cumulative Withdrawal Limit Check
                BigDecimal alreadyWithdrawnToday = account.getDailyWithdrawnAmountToday();
                BigDecimal projectedDaily = alreadyWithdrawnToday.add(amount);
                if (rule.getDailyWithdrawalLimit() != null 
                        && rule.getDailyWithdrawalLimit().compareTo(BigDecimal.ZERO) > 0 
                        && projectedDaily.compareTo(rule.getDailyWithdrawalLimit()) > 0) {
                    ValidationProtoResponse response = ValidationProtoResponse.newBuilder()
                            .setIsValid(false)
                            .setMessage("Withdrawal rejected: Cumulative daily withdrawals (" 
                                    + projectedDaily.setScale(2, java.math.RoundingMode.HALF_UP) 
                                    + " ETB) would exceed the daily limit of " 
                                    + rule.getDailyWithdrawalLimit().setScale(2, java.math.RoundingMode.HALF_UP) 
                                    + " ETB for " + rule.getProductName() 
                                    + " (Already withdrawn today: " + alreadyWithdrawnToday.setScale(2, java.math.RoundingMode.HALF_UP) + " ETB).")
                            .setAvailableBalance(available.toPlainString())
                            .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                    return;
                }
            }

            ValidationProtoResponse response = ValidationProtoResponse.newBuilder()
                    .setIsValid(true)
                    .setMessage("Debit validation successful.")
                    .setAvailableBalance(available.toPlainString())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Debit validation failed with unexpected error for account {}: {}", request.getAccountNo(), ex.getMessage(), ex);
            ValidationProtoResponse response = ValidationProtoResponse.newBuilder()
                    .setIsValid(false)
                    .setMessage("Validation error: " + ex.getMessage())
                    .setAvailableBalance("0.0000")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } finally {
            com.kab.qershi.account.infrastructure.config.TenantContext.clear();
        }
    }

    @Override
    public void validateAccountForCredit(CreditValidationProtoRequest request, StreamObserver<ValidationProtoResponse> responseObserver) {
        log.debug("gRPC ValidateAccountForCredit request for accountNo: {}, amount: {}, schema: {}",
                request.getAccountNo(), request.getAmount(), request.getTenantSchema());
        try {
            if (request.getTenantSchema() != null && !request.getTenantSchema().isBlank()) {
                com.kab.qershi.account.infrastructure.config.TenantContext.setTenantSchema(request.getTenantSchema().trim());
            }
            Account account = resolveAccount(request.getAccountNo());
            BigDecimal amount = new BigDecimal(request.getAmount());

            boolean canCredit = account.canPerformCredit(amount);
            AccountProduct product = productManagementUseCase.getProductByCode(account.getProductCode());
            BigDecimal minBalance = product != null ? product.getMinOperatingBalance() : BigDecimal.ZERO;
            BigDecimal available = account.getAvailableBalance(minBalance);

            if (!canCredit) {
                String reason = "Credit rejected due to account status (" + account.getStatus() + ") or credit freeze controls.";
                ValidationProtoResponse response = ValidationProtoResponse.newBuilder()
                        .setIsValid(false)
                        .setMessage(reason)
                        .setAvailableBalance(available.toPlainString())
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            // Storage Ceiling Enforcement: Maximum Storing Balance Limit
            Optional<ProductMakerCheckerRuleEntity> ruleOpt = productRuleRepository.findByProductCode(account.getProductCode());
            if (ruleOpt.isPresent()) {
                ProductMakerCheckerRuleEntity rule = ruleOpt.get();
                BigDecimal maxLimit = rule.getMaxBalanceLimit();
                BigDecimal resultingBalance = account.getBookBalance().add(amount);
                if (maxLimit != null && maxLimit.compareTo(BigDecimal.ZERO) > 0 && resultingBalance.compareTo(maxLimit) > 0) {
                    ValidationProtoResponse response = ValidationProtoResponse.newBuilder()
                            .setIsValid(false)
                            .setMessage("Deposit rejected: Resulting account balance (" 
                                    + resultingBalance.setScale(2, java.math.RoundingMode.HALF_UP) 
                                    + " ETB) would exceed the maximum storing balance limit of " 
                                    + maxLimit.setScale(2, java.math.RoundingMode.HALF_UP) 
                                    + " ETB for " + rule.getProductName() + ".")
                            .setAvailableBalance(available.toPlainString())
                            .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                    return;
                }
            }

            ValidationProtoResponse response = ValidationProtoResponse.newBuilder()
                    .setIsValid(true)
                    .setMessage("Credit validation successful.")
                    .setAvailableBalance(available.toPlainString())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Credit validation failed with unexpected error for account {}: {}", request.getAccountNo(), ex.getMessage(), ex);
            ValidationProtoResponse response = ValidationProtoResponse.newBuilder()
                    .setIsValid(false)
                    .setMessage("Validation error: " + ex.getMessage())
                    .setAvailableBalance("0.0000")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } finally {
            com.kab.qershi.account.infrastructure.config.TenantContext.clear();
        }
    }

    @Override
    public void postTransaction(PostTransactionRequest request, StreamObserver<PostTransactionResponse> responseObserver) {
        log.info("gRPC PostTransaction request for accountNo: {}, type: {}, amount: {}, schema: {}",
                request.getAccountNo(), request.getTransactionType(), request.getAmount(), request.getTenantSchema());
        try {
            if (request.getTenantSchema() != null && !request.getTenantSchema().isBlank()) {
                com.kab.qershi.account.infrastructure.config.TenantContext.setTenantSchema(request.getTenantSchema().trim());
            }

            Account account = resolveAccount(request.getAccountNo());
            BigDecimal amount = new BigDecimal(request.getAmount());

            if ("CREDIT".equalsIgnoreCase(request.getTransactionType())) {
                account.credit(amount);
            } else if ("DEBIT".equalsIgnoreCase(request.getTransactionType())) {
                AccountProduct product = productManagementUseCase.getProductByCode(account.getProductCode());
                BigDecimal minBalance = product != null ? product.getMinOperatingBalance() : BigDecimal.ZERO;
                account.debit(amount, minBalance);
            } else {
                throw new IllegalArgumentException("Unknown transaction type: " + request.getTransactionType());
            }

            Account saved = accountRepositoryPort.save(account);

            PostTransactionResponse response = PostTransactionResponse.newBuilder()
                    .setIsSuccess(true)
                    .setMessage("Transaction applied successfully.")
                    .setNewBalance(saved.getBookBalance().toPlainString())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Failed to apply gRPC transaction for accountNo: {}", request.getAccountNo(), ex);
            PostTransactionResponse response = PostTransactionResponse.newBuilder()
                    .setIsSuccess(false)
                    .setMessage(ex.getMessage())
                    .setNewBalance("0.00")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } finally {
            com.kab.qershi.account.infrastructure.config.TenantContext.clear();
        }
    }
}
