package com.kab.qershi.account.domain.ports.inbound;

import com.kab.qershi.account.domain.model.AccountProduct;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Inbound Use Case interface for SACCO Deposit Product Catalog management.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface ProductManagementUseCase {

    AccountProduct createProduct(String productName, String category, String currency,
                                 BigDecimal interestRatePa, String postingFrequency,
                                 BigDecimal minOperatingBalance, BigDecimal minMonthlyContribution,
                                 Integer termPeriodMonths, BigDecimal earlyWithdrawalPenaltyPct);

    AccountProduct getProductByCode(String productCode);

    AccountProduct getProductById(UUID productId);

    List<AccountProduct> getAllProducts();

    List<AccountProduct> getAllActiveProducts();
}
