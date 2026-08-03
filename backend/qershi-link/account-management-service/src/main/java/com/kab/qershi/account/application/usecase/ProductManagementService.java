package com.kab.qershi.account.application.usecase;

import com.kab.qershi.account.domain.model.AccountProduct;
import com.kab.qershi.account.domain.model.InterestPostingFrequency;
import com.kab.qershi.account.domain.ports.inbound.ProductManagementUseCase;
import com.kab.qershi.account.domain.ports.outbound.ProductRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Application service implementing ProductManagementUseCase.
 * Handles dynamic product factory configuration with auto-assigned 3-digit product codes.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
@Transactional
public class ProductManagementService implements ProductManagementUseCase {

    private final ProductRepositoryPort productRepositoryPort;

    public ProductManagementService(ProductRepositoryPort productRepositoryPort) {
        this.productRepositoryPort = productRepositoryPort;
    }

    @Override
    public AccountProduct createProduct(String productName, String category, String currency,
                                         BigDecimal interestRatePa, String postingFrequency,
                                         BigDecimal minOperatingBalance, BigDecimal minMonthlyContribution,
                                         Integer termPeriodMonths, BigDecimal earlyWithdrawalPenaltyPct) {
        // Auto-generate next 3-digit product code (e.g. 101, 102, 103...)
        long totalProducts = productRepositoryPort.countTotalProducts();
        long nextSeq = 101 + totalProducts;
        String productCode = String.valueOf(nextSeq);
        while (productRepositoryPort.existsByProductCode(productCode)) {
            nextSeq++;
            productCode = String.valueOf(nextSeq);
        }

        InterestPostingFrequency frequency;
        try {
            frequency = postingFrequency != null ? InterestPostingFrequency.valueOf(postingFrequency.toUpperCase()) : InterestPostingFrequency.MONTHLY;
        } catch (Exception ex) {
            frequency = InterestPostingFrequency.MONTHLY;
        }

        AccountProduct product = new AccountProduct(
                UUID.randomUUID(),
                productCode,
                productName,
                category.toUpperCase(),
                currency != null ? currency.toUpperCase() : "ETB",
                interestRatePa != null ? interestRatePa : BigDecimal.ZERO,
                frequency,
                minOperatingBalance != null ? minOperatingBalance : BigDecimal.ZERO,
                minMonthlyContribution != null ? minMonthlyContribution : BigDecimal.ZERO,
                termPeriodMonths,
                earlyWithdrawalPenaltyPct != null ? earlyWithdrawalPenaltyPct : BigDecimal.ZERO,
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        return productRepositoryPort.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountProduct getProductByCode(String productCode) {
        return productRepositoryPort.findByProductCode(productCode)
                .orElseThrow(() -> new IllegalArgumentException("Account product not found for code: " + productCode));
    }

    @Override
    @Transactional(readOnly = true)
    public AccountProduct getProductById(UUID productId) {
        return productRepositoryPort.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Account product not found for ID: " + productId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountProduct> getAllProducts() {
        return productRepositoryPort.findAllProducts();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountProduct> getAllActiveProducts() {
        return productRepositoryPort.findAllActiveProducts();
    }
}
