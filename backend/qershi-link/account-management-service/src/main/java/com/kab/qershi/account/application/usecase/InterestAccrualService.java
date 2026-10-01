package com.kab.qershi.account.application.usecase;

import com.kab.qershi.account.domain.model.AccountStatus;
import com.kab.qershi.account.infrastructure.persistence.AccountEntity;
import com.kab.qershi.account.infrastructure.persistence.AccountProductEntity;
import com.kab.qershi.account.infrastructure.persistence.InterestTaxDeductionLogEntity;
import com.kab.qershi.account.infrastructure.persistence.SpringDataAccountRepository;
import com.kab.qershi.account.infrastructure.persistence.SpringDataChartOfAccountRepository;
import com.kab.qershi.account.infrastructure.persistence.SpringDataInterestTaxDeductionLogRepository;
import com.kab.qershi.account.infrastructure.persistence.SpringDataProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Core Banking Daily Interest Accrual and Month-End Capitalization Engine.
 * Implements actual/365 daily accrual computation per member savings product
 * with statutory 5% Withholding Tax (WHT) deduction at source during capitalization.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class InterestAccrualService {

    private static final Logger log = LoggerFactory.getLogger(InterestAccrualService.class);
    private static final BigDecimal DAYS_IN_YEAR = new BigDecimal("365");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal WHT_RATE_PCT = new BigDecimal("5.00");
    private static final BigDecimal WHT_FRACTION = new BigDecimal("0.05");

    private final SpringDataAccountRepository accountRepository;
    private final SpringDataProductRepository productRepository;
    private final SpringDataInterestTaxDeductionLogRepository taxLogRepository;
    private final SpringDataChartOfAccountRepository coaRepository;

    public InterestAccrualService(SpringDataAccountRepository accountRepository,
                                  SpringDataProductRepository productRepository,
                                  SpringDataInterestTaxDeductionLogRepository taxLogRepository,
                                  SpringDataChartOfAccountRepository coaRepository) {
        this.accountRepository = accountRepository;
        this.productRepository = productRepository;
        this.taxLogRepository = taxLogRepository;
        this.coaRepository = coaRepository;
    }

    public record AccrualResult(
            int accountsAccrued,
            BigDecimal totalInterestAccrued,
            int accountsCapitalized,
            BigDecimal totalGrossCapitalized,
            BigDecimal totalTaxWithheld,
            BigDecimal totalNetCapitalized
    ) {
        public BigDecimal totalCapitalized() {
            return totalGrossCapitalized;
        }
    }

    @Transactional
    public AccrualResult runDailyAccrual(LocalDate businessDate, boolean isMonthEnd) {
        log.info("Starting Daily Interest Accrual for business date: {}, isMonthEnd: {}", businessDate, isMonthEnd);

        List<AccountProductEntity> products = productRepository.findAll();
        Map<String, AccountProductEntity> productMap = products.stream()
                .collect(Collectors.toMap(AccountProductEntity::getProductCode, p -> p, (p1, p2) -> p1));

        List<AccountEntity> activeAccounts = accountRepository.findByStatus(AccountStatus.ACTIVE);

        int accruedCount = 0;
        BigDecimal totalAccrued = BigDecimal.ZERO;
        int capitalizedCount = 0;
        BigDecimal totalGrossCapitalized = BigDecimal.ZERO;
        BigDecimal totalTaxWithheld = BigDecimal.ZERO;
        BigDecimal totalNetCapitalized = BigDecimal.ZERO;

        List<InterestTaxDeductionLogEntity> taxLogsToSave = new ArrayList<>();

        for (AccountEntity account : activeAccounts) {
            AccountProductEntity product = productMap.get(account.getProductCode());
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
                    BigDecimal currentAccrued = account.getAccruedInterestPayable() != null ? account.getAccruedInterestPayable() : BigDecimal.ZERO;
                    account.setAccruedInterestPayable(currentAccrued.add(dailyAccrual));
                    account.setLastInterestAccrualDate(businessDate);
                    account.setUpdatedAt(LocalDateTime.now());

                    totalAccrued = totalAccrued.add(dailyAccrual);
                    accruedCount++;
                }
            }

            // Month-end capitalization routine with statutory 5% Withholding Tax (WHT)
            if (isMonthEnd && account.getAccruedInterestPayable() != null && account.getAccruedInterestPayable().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal grossPayout = account.getAccruedInterestPayable();
                BigDecimal taxWithheld = grossPayout.multiply(WHT_FRACTION).setScale(2, RoundingMode.HALF_UP);
                BigDecimal netPayout = grossPayout.subtract(taxWithheld).setScale(2, RoundingMode.HALF_UP);

                account.setBookBalance(account.getBookBalance().add(netPayout));
                account.setAccruedInterestPayable(BigDecimal.ZERO);
                account.setLastCapitalizationDate(businessDate);
                account.setUpdatedAt(LocalDateTime.now());

                InterestTaxDeductionLogEntity taxLog = new InterestTaxDeductionLogEntity(
                        account.getAccountNo(),
                        businessDate,
                        grossPayout,
                        WHT_RATE_PCT,
                        taxWithheld,
                        netPayout,
                        "2091"
                );
                taxLogsToSave.add(taxLog);

                totalGrossCapitalized = totalGrossCapitalized.add(grossPayout);
                totalTaxWithheld = totalTaxWithheld.add(taxWithheld);
                totalNetCapitalized = totalNetCapitalized.add(netPayout);
                capitalizedCount++;
            }
        }

        accountRepository.saveAll(activeAccounts);

        if (!taxLogsToSave.isEmpty()) {
            taxLogRepository.saveAll(taxLogsToSave);
            log.info("Persisted {} statutory 5% Withholding Tax deduction records.", taxLogsToSave.size());

            // General Ledger balancing for month-end capitalization:
            // 1. Debit GL 2051 (Accrued Savings Interest Payable) with gross accrued interest settled
            final BigDecimal finalGross = totalGrossCapitalized;
            coaRepository.findByGlCode("2051").ifPresent(gl -> {
                gl.setBalance(gl.getBalance().subtract(finalGross));
                coaRepository.save(gl);
            });

            // 2. Credit GL 2091 (Withholding Tax Payable) with 5% tax withheld for statutory authority remittance
            final BigDecimal finalTax = totalTaxWithheld;
            coaRepository.findByGlCode("2091").ifPresent(gl -> {
                gl.setBalance(gl.getBalance().add(finalTax));
                coaRepository.save(gl);
            });
        }

        log.info("Completed Interest Accrual. Accrued: {} accounts ({} ETB), Capitalized: {} accounts (Gross: {} ETB, WHT 5%: {} ETB, Net: {} ETB)",
                accruedCount, totalAccrued, capitalizedCount, totalGrossCapitalized, totalTaxWithheld, totalNetCapitalized);

        return new AccrualResult(accruedCount, totalAccrued, capitalizedCount, totalGrossCapitalized, totalTaxWithheld, totalNetCapitalized);
    }
}
