package com.kab.qershi.account.application.usecase;

import com.kab.qershi.account.domain.model.AccountStatus;
import com.kab.qershi.account.infrastructure.persistence.AccountEntity;
import com.kab.qershi.account.infrastructure.persistence.AccountProductEntity;
import com.kab.qershi.account.infrastructure.persistence.SpringDataAccountRepository;
import com.kab.qershi.account.infrastructure.persistence.SpringDataProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Core Banking Daily Interest Accrual and Month-End Capitalization Engine.
 * Implements actual/365 daily accrual computation per member savings product.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class InterestAccrualService {

    private static final Logger log = LoggerFactory.getLogger(InterestAccrualService.class);
    private static final BigDecimal DAYS_IN_YEAR = new BigDecimal("365");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final SpringDataAccountRepository accountRepository;
    private final SpringDataProductRepository productRepository;

    public InterestAccrualService(SpringDataAccountRepository accountRepository,
                                  SpringDataProductRepository productRepository) {
        this.accountRepository = accountRepository;
        this.productRepository = productRepository;
    }

    public record AccrualResult(int accountsAccrued, BigDecimal totalInterestAccrued, int accountsCapitalized, BigDecimal totalCapitalized) {}

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
        BigDecimal totalCapitalized = BigDecimal.ZERO;

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

            // Month-end capitalization routine
            if (isMonthEnd && account.getAccruedInterestPayable() != null && account.getAccruedInterestPayable().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal payout = account.getAccruedInterestPayable();
                account.setBookBalance(account.getBookBalance().add(payout));
                account.setAccruedInterestPayable(BigDecimal.ZERO);
                account.setLastCapitalizationDate(businessDate);
                account.setUpdatedAt(LocalDateTime.now());

                totalCapitalized = totalCapitalized.add(payout);
                capitalizedCount++;
            }
        }

        accountRepository.saveAll(activeAccounts);

        log.info("Completed Interest Accrual. Accrued: {} accounts ({} ETB), Capitalized: {} accounts ({} ETB)",
                accruedCount, totalAccrued, capitalizedCount, totalCapitalized);

        return new AccrualResult(accruedCount, totalAccrued, capitalizedCount, totalCapitalized);
    }
}
