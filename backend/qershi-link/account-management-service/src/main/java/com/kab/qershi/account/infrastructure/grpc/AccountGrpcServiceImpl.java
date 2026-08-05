package com.kab.qershi.account.infrastructure.grpc;

import com.kab.qershi.account.domain.model.Account;
import com.kab.qershi.account.domain.model.AccountProduct;
import com.kab.qershi.account.domain.ports.inbound.AccountOpeningUseCase;
import com.kab.qershi.account.domain.ports.inbound.ProductManagementUseCase;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * gRPC Service Server implementation exposing high-speed inter-service RPCs for Core Account management.
 * Allows transaction and loan services to validate account balances and freeze states.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@GrpcService
public class AccountGrpcServiceImpl extends AccountGrpcServiceGrpc.AccountGrpcServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(AccountGrpcServiceImpl.class);

    private final AccountOpeningUseCase accountOpeningUseCase;
    private final ProductManagementUseCase productManagementUseCase;

    public AccountGrpcServiceImpl(AccountOpeningUseCase accountOpeningUseCase,
                                  ProductManagementUseCase productManagementUseCase) {
        this.accountOpeningUseCase = accountOpeningUseCase;
        this.productManagementUseCase = productManagementUseCase;
    }

    @Override
    public void getAccountByNo(AccountNoRequest request, StreamObserver<AccountProtoResponse> responseObserver) {
        log.debug("gRPC GetAccountByNo request received for accountNo: {}, schema: {}", request.getAccountNo(), request.getTenantSchema());
        try {
            if (request.getTenantSchema() != null && !request.getTenantSchema().isBlank()) {
                com.kab.qershi.account.infrastructure.config.TenantContext.setTenantSchema(request.getTenantSchema().trim());
            }
            Account account = accountOpeningUseCase.getAccountByNo(request.getAccountNo());
            AccountProduct product = productManagementUseCase.getProductByCode(account.getProductCode());
            BigDecimal minBalance = product != null ? product.getMinOperatingBalance() : BigDecimal.ZERO;
            BigDecimal available = account.getAvailableBalance(minBalance);

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
            Account account = accountOpeningUseCase.getAccountByNo(request.getAccountNo());
            AccountProduct product = productManagementUseCase.getProductByCode(account.getProductCode());
            BigDecimal minBalance = product != null ? product.getMinOperatingBalance() : BigDecimal.ZERO;
            BigDecimal amount = new BigDecimal(request.getAmount());

            boolean canDebit = account.canPerformDebit(amount, minBalance);
            BigDecimal available = account.getAvailableBalance(minBalance);

            String message = canDebit ? "Debit validation successful." : "Debit rejected due to insufficient available balance or freeze controls.";

            ValidationProtoResponse response = ValidationProtoResponse.newBuilder()
                    .setIsValid(canDebit)
                    .setMessage(message)
                    .setAvailableBalance(available.toPlainString())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
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
            Account account = accountOpeningUseCase.getAccountByNo(request.getAccountNo());
            BigDecimal amount = new BigDecimal(request.getAmount());

            boolean canCredit = account.canPerformCredit(amount);
            AccountProduct product = productManagementUseCase.getProductByCode(account.getProductCode());
            BigDecimal minBalance = product != null ? product.getMinOperatingBalance() : BigDecimal.ZERO;
            BigDecimal available = account.getAvailableBalance(minBalance);

            String message = canCredit ? "Credit validation successful." : "Credit rejected due to account status or credit freeze controls.";

            ValidationProtoResponse response = ValidationProtoResponse.newBuilder()
                    .setIsValid(canCredit)
                    .setMessage(message)
                    .setAvailableBalance(available.toPlainString())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
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
}
