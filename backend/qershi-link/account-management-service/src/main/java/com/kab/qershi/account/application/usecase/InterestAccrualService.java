package com.kab.qershi.account.application.usecase;

import com.kab.qershi.account.domain.model.Account;
import com.kab.qershi.account.domain.model.AccountProduct;
import com.kab.qershi.account.domain.model.AccountStatus;
import com.kab.qershi.account.domain.ports.outbound.AccountRepositoryPort;
import com.kab.qershi.account.domain.ports.outbound.ProductRepositoryPort;
import com.kab.qershi.account.infrastructure.config.TenantContext;
import com.kab.qershi.pricing.infrastructure.grpc.PricingGrpcServiceGrpc;
import com.kab.qershi.pricing.infrastructure.grpc.TaxCalculationProtoRequest;
import com.kab.qershi.pricing.infrastructure.grpc.TaxCalculationProtoResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Core Banking Daily Interest Accrual and Month-End Capitalization Engine.
 * Implements actual/365 daily accrual computation per member savings product.
 * Delegates 5% Withholding Tax (WHT) statutory deduction to pricing-fee-service.
 *
 * @author KAB Digital Solution PLC
 * @version 1.2.0
 */
@Service
public class InterestAccrualService {

    private static final Logger log = LoggerFactory.getLogger(InterestAccrualService.class);
    private static final BigDecimal DAYS_IN_YEAR = new BigDecimal("365");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final AccountRepositoryPort accountRepository;
    private final ProductRepositoryPort productRepository;

    @GrpcClient("pricing-service")
    private PricingGrpcServiceGrpc.PricingGrpcServiceBlockingStub pricingStub;

    public InterestAccrualService(AccountRepositoryPort accountRepository,
                                  ProductRepositoryPort productRepository) {
        this.accountRepository = accountRepository;
        this.productRepository = productRepository;
    }

    public record AccrualResult(int accountsAccrued, BigDecimal totalInterestAccrued, int accountsCapitalized, BigDecimal totalCapitalized) {}

    @Transactional
    public AccrualResult runDailyAccrual(LocalDate businessDate, boolean isMonthEnd) {
        log.info("Starting Daily Interest Accrual for business date: {}, isMonthEnd: {}", businessDate, isMonthEnd);

        List<AccountProduct> products = productRepository.findAllProducts();
        Map<String, AccountProduct> productMap = products.stream()
                .collect(Collectors.toMap(AccountProduct::getProductCode, p -> p, (p1, p2) -> p1));

        List<Account> activeAccounts = accountRepository.findByStatus(AccountStatus.ACTIVE);

        int accruedCount = 0;
        BigDecimal totalAccrued = BigDecimal.ZERO;
        int capitalizedCount = 0;
        BigDecimal totalCapitalized = BigDecimal.ZERO;

        for (Account account : activeAccounts) {
            AccountProduct product = productMap.get(account.getProductCode());
            if (product == null || product.getInterestRatePa() == null || product.getInterestRatePa().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal balance = account.getBookBalance() != null ? account.getBookBalance() : BigDecimal.ZERO;
            BigDecimal minBalance = product.getMinOperatingBalance() != null ? product.getMinOperatingBalance() : BigDecimal.ZERO;

            if (balance.compareTo(minBalance) >= 0) {
                // Formula: (balance * (interestRatePa / 100)) / 365
                BigDecimal rateFraction = product.getInterestRatePa().divide(ONE_HUNDRED, 8, RoundingMode.HALF_UP);
                BigDecimal annualInterest = balance.multiply(rateFraction);
                BigDecimal dailyAccrual = annualInterest.divide(DAYS_IN_YEAR, 4, RoundingMode.HALF_UP);

                if (dailyAccrual.compareTo(BigDecimal.ZERO) > 0) {
                    account.accrueDailyInterest(dailyAccrual, businessDate);
                    totalAccrued = totalAccrued.add(dailyAccrual);
                    accruedCount++;
                }
            }

            // Month-end capitalization routine
            if (isMonthEnd && account.getAccruedInterestPayable() != null && account.getAccruedInterestPayable().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal grossPayout = account.getAccruedInterestPayable();
                BigDecimal netPayout = grossPayout;

                if (pricingStub != null) {
                    try {
                        String tenant = TenantContext.getTenantSchema();
                        TaxCalculationProtoRequest taxReq = TaxCalculationProtoRequest.newBuilder()
                                .setTaxType("WHT_SAVINGS_INTEREST")
                                .setGrossAmount(grossPayout.toPlainString())
                                .setAccountNo(account.getAccountNo())
                                .setBusinessDate(businessDate.toString())
                                .setTenantSchema(tenant != null ? tenant : "")
                                .build();
                        TaxCalculationProtoResponse taxRes = pricingStub.calculateWithholdingTax(taxReq);
                        if (taxRes != null && !taxRes.getNetCredited().isBlank()) {
                            netPayout = new BigDecimal(taxRes.getNetCredited());
                            log.info("WHT 5% assessed via pricing-service for account {}: gross={}, tax={}, net={}",
                                    account.getAccountNo(), grossPayout, taxRes.getTaxWithheld(), netPayout);
                        }
                    } catch (Exception ex) {
                        log.warn("Pricing service WHT assessment failed for account {}, proceeding with gross: {}",
                                account.getAccountNo(), ex.getMessage());
                    }
                }

                account.capitalizeAccruedInterest(netPayout, businessDate);
                totalCapitalized = totalCapitalized.add(netPayout);
                capitalizedCount++;
            }
        }

        accountRepository.saveAll(activeAccounts);

        log.info("Completed Interest Accrual. Accrued: {} accounts ({} ETB), Capitalized: {} accounts ({} ETB)",
                accruedCount, totalAccrued, capitalizedCount, totalCapitalized);

        return new AccrualResult(accruedCount, totalAccrued, capitalizedCount, totalCapitalized);
    }
}
