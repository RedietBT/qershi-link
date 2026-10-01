package com.kab.qershi.loan.management.infrastructure.adapters;

import com.kab.qershi.account.infrastructure.grpc.AccountGrpcServiceGrpc;
import com.kab.qershi.account.infrastructure.grpc.AccountNoRequest;
import com.kab.qershi.account.infrastructure.grpc.AccountProtoResponse;
import com.kab.qershi.account.infrastructure.grpc.CreditValidationProtoRequest;
import com.kab.qershi.account.infrastructure.grpc.DebitValidationProtoRequest;
import com.kab.qershi.account.infrastructure.grpc.PlaceLienProtoRequest;
import com.kab.qershi.account.infrastructure.grpc.PlaceLienProtoResponse;
import com.kab.qershi.account.infrastructure.grpc.PostTransactionRequest;
import com.kab.qershi.account.infrastructure.grpc.PostTransactionResponse;
import com.kab.qershi.account.infrastructure.grpc.ReleaseLienProtoRequest;
import com.kab.qershi.account.infrastructure.grpc.ReleaseLienProtoResponse;
import com.kab.qershi.account.infrastructure.grpc.ValidationProtoResponse;
import com.kab.qershi.loan.management.domain.port.out.AccountClientPort;
import com.kab.qershi.loan.management.infrastructure.config.TenantContext;
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
            throw new RuntimeException("Account service unavailable for account: " + accountNo, ex);
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
            return new ValidationResult(res.getIsValid(), res.getMessage(), parseDecimal(res.getAvailableBalance()));
        } catch (Exception ex) {
            log.error("gRPC call ValidateAccountForDebit failed for accountNo {}: {}", accountNo, ex.getMessage());
            return new ValidationResult(false, "RPC failed: " + ex.getMessage(), BigDecimal.ZERO);
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
            return new ValidationResult(res.getIsValid(), res.getMessage(), parseDecimal(res.getAvailableBalance()));
        } catch (Exception ex) {
            log.error("gRPC call ValidateAccountForCredit failed for accountNo {}: {}", accountNo, ex.getMessage());
            return new ValidationResult(false, "RPC failed: " + ex.getMessage(), BigDecimal.ZERO);
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

    @Override
    public LienResult placeLien(String accountNo, BigDecimal amount, String reason, String referenceNo, String officerUserId) {
        log.debug("Calling gRPC PlaceLien for accountNo: {}, amount: {}, ref: {}", accountNo, amount, referenceNo);
        try {
            String schema = TenantContext.getTenantSchema();
            PlaceLienProtoRequest request = PlaceLienProtoRequest.newBuilder()
                    .setAccountNo(accountNo)
                    .setAmount(amount.toPlainString())
                    .setReason(reason != null ? reason : "Peer Guarantor Savings Pledge")
                    .setReferenceNo(referenceNo != null ? referenceNo : "")
                    .setOfficerUserId(officerUserId != null ? officerUserId : "")
                    .setTenantSchema(schema != null ? schema : "")
                    .build();

            PlaceLienProtoResponse res = accountGrpcStub.placeLien(request);
            return new LienResult(res.getIsSuccess(), res.getLienId(), res.getMessage());
        } catch (Exception ex) {
            log.error("gRPC call PlaceLien failed for accountNo {}: {}", accountNo, ex.getMessage());
            return new LienResult(false, null, "RPC error: " + ex.getMessage());
        }
    }

    @Override
    public boolean releaseLien(String lienId, String officerUserId) {
        log.debug("Calling gRPC ReleaseLien for lienId: {}", lienId);
        try {
            String schema = TenantContext.getTenantSchema();
            ReleaseLienProtoRequest request = ReleaseLienProtoRequest.newBuilder()
                    .setLienId(lienId)
                    .setOfficerUserId(officerUserId != null ? officerUserId : "")
                    .setTenantSchema(schema != null ? schema : "")
                    .build();

            ReleaseLienProtoResponse res = accountGrpcStub.releaseLien(request);
            return res.getIsSuccess();
        } catch (Exception ex) {
            log.error("gRPC call ReleaseLien failed for lienId {}: {}", lienId, ex.getMessage());
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
