package com.kab.qershi.loan.origination.domain.ports.outbound;

import java.math.BigDecimal;

/**
 * Outbound port for querying account information from account-management-service via gRPC.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface AccountClientPort {

    AccountInfo getAccountInfo(String accountNo);

    record AccountInfo(
            String accountId,
            String accountNo,
            String userId,
            String saccoCode,
            String branchCode,
            String productCode,
            BigDecimal bookBalance,
            BigDecimal lienHoldAmount,
            BigDecimal availableBalance,
            String status,
            String freezeStatus,
            String phoneNumber,
            String fullName
    ) {}
}
