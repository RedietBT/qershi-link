package com.kab.qershi.loan.origination.infrastructure.adapters;

import com.kab.qershi.account.infrastructure.grpc.AccountGrpcServiceGrpc;
import com.kab.qershi.account.infrastructure.grpc.AccountNoRequest;
import com.kab.qershi.account.infrastructure.grpc.AccountProtoResponse;
import com.kab.qershi.loan.origination.domain.ports.outbound.AccountClientPort;
import com.kab.qershi.loan.origination.infrastructure.config.TenantContext;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Infrastructure client adapter implementing AccountClientPort via gRPC.
 * Communicates with account-management-service on port 9082.
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

    private BigDecimal parseDecimal(String val) {
        if (val == null || val.isBlank()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(val.trim());
        } catch (Exception ignored) {
            return BigDecimal.ZERO;
        }
    }
}
