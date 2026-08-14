package com.kab.qershi.transaction.infrastructure.grpc;

import com.kab.qershi.account.infrastructure.grpc.AccountGrpcServiceGrpc;
import com.kab.qershi.account.infrastructure.grpc.AccountNoRequest;
import com.kab.qershi.account.infrastructure.grpc.AccountProtoResponse;
import com.kab.qershi.account.infrastructure.grpc.CreditValidationProtoRequest;
import com.kab.qershi.account.infrastructure.grpc.DebitValidationProtoRequest;
import com.kab.qershi.account.infrastructure.grpc.ValidationProtoResponse;
import com.kab.qershi.account.infrastructure.grpc.PostTransactionRequest;
import com.kab.qershi.account.infrastructure.grpc.PostTransactionResponse;
import com.kab.qershi.transaction.domain.ports.outbound.AccountClientPort;
import com.kab.qershi.transaction.infrastructure.config.TenantContext;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Infrastructure client adapter implementing AccountClientPort via gRPC.
 * Calls account-management-service on gRPC port 9082.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class AccountGrpcClientAdapter implements AccountClientPort {

    private static final Logger log = LoggerFactory.getLogger(AccountGrpcClientAdapter.class);

    @GrpcClient("account-service")
    private AccountGrpcServiceGrpc.AccountGrpcServiceBlockingStub accountGrpcStub;

    @Override
    public AccountInfo getAccountInfo(String accountNo) {
        log.debug("Calling gRPC GetAccountByNo for accountNo: {}", accountNo);
        try {
            String schema = TenantContext.getTenantSchema();
            AccountNoRequest request = AccountNoRequest.newBuilder()
                    .setAccountNo(accountNo)
                    .setTenantSchema(schema != null ? schema : "")
                    .build();
            AccountProtoResponse res = accountGrpcStub.getAccountByNo(request);

            return new AccountInfo(
                    res.getAccountId(),
                    res.getAccountNo(),
                    res.getUserId(),
                    res.getSaccoCode(),
                    res.getBranchCode(),
                    res.getProductCode(),
                    parseDecimal(res.getBookBalance()),
                    parseDecimal(res.getLienHoldAmount()),
                    parseDecimal(res.getAvailableBalance()),
                    res.getStatus(),
                    res.getFreezeStatus(),
                    res.getPhoneNumber(),
                    res.getFullName()
            );
        } catch (Exception ex) {
            log.error("gRPC call GetAccountByNo failed for accountNo {}: {}", accountNo, ex.getMessage());
            throw new RuntimeException("Failed retrieving account details via gRPC: " + ex.getMessage(), ex);
        }
    }

    @Override
    public ValidationResult validateDebit(String accountNo, BigDecimal amount) {
        log.debug("Calling gRPC ValidateAccountForDebit for accountNo: {}, amount: {}", accountNo, amount);
        try {
            String schema = TenantContext.getTenantSchema();
            DebitValidationProtoRequest request = DebitValidationProtoRequest.newBuilder()
                    .setAccountNo(accountNo)
                    .setAmount(amount.toPlainString())
                    .setTenantSchema(schema != null ? schema : "")
                    .build();
            ValidationProtoResponse res = accountGrpcStub.validateAccountForDebit(request);

            return new ValidationResult(
                    res.getIsValid(),
                    res.getMessage(),
                    parseDecimal(res.getAvailableBalance())
            );
        } catch (Exception ex) {
            log.error("gRPC call ValidateAccountForDebit failed for accountNo {}: {}", accountNo, ex.getMessage());
            return new ValidationResult(false, "gRPC debit validation failed: " + ex.getMessage(), BigDecimal.ZERO);
        }
    }

    @Override
    public ValidationResult validateCredit(String accountNo, BigDecimal amount) {
        log.debug("Calling gRPC ValidateAccountForCredit for accountNo: {}, amount: {}", accountNo, amount);
        try {
            String schema = TenantContext.getTenantSchema();
            CreditValidationProtoRequest request = CreditValidationProtoRequest.newBuilder()
                    .setAccountNo(accountNo)
                    .setAmount(amount.toPlainString())
                    .setTenantSchema(schema != null ? schema : "")
                    .build();
            ValidationProtoResponse res = accountGrpcStub.validateAccountForCredit(request);

            return new ValidationResult(
                    res.getIsValid(),
                    res.getMessage(),
                    parseDecimal(res.getAvailableBalance())
            );
        } catch (Exception ex) {
            log.error("gRPC call ValidateAccountForCredit failed for accountNo {}: {}", accountNo, ex.getMessage());
            return new ValidationResult(false, "gRPC credit validation failed: " + ex.getMessage(), BigDecimal.ZERO);
        }
    }

    @Override
    public boolean postTransaction(String accountNo, BigDecimal amount, String transactionType) {
        log.debug("Calling gRPC PostTransaction for accountNo: {}, amount: {}, type: {}", accountNo, amount, transactionType);
        try {
            String schema = TenantContext.getTenantSchema();
            PostTransactionRequest request = PostTransactionRequest.newBuilder()
                    .setAccountNo(accountNo)
                    .setAmount(amount.toPlainString())
                    .setTransactionType(transactionType)
                    .setTenantSchema(schema != null ? schema : "")
                    .build();

            PostTransactionResponse res = accountGrpcStub.postTransaction(request);

            if (!res.getIsSuccess()) {
                log.warn("gRPC PostTransaction failed remotely: {}", res.getMessage());
            }
            return res.getIsSuccess();
        } catch (Exception ex) {
            log.error("gRPC call PostTransaction failed for accountNo {}: {}", accountNo, ex.getMessage());
            return false;
        }
    }

    private BigDecimal parseDecimal(String val) {
        if (val == null || val.isBlank()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(val.trim());
        } catch (Exception ignored) {
            return BigDecimal.ZERO;
        }
    }
}
