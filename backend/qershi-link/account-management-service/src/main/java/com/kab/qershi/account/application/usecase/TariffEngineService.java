package com.kab.qershi.account.application.usecase;

import com.kab.qershi.account.infrastructure.persistence.SpringDataChartOfAccountRepository;
import com.kab.qershi.account.infrastructure.persistence.SpringDataTariffRepository;
import com.kab.qershi.account.infrastructure.persistence.TariffEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Core Banking Tariff Engine & Transaction Fee Calculation Service.
 * Evaluates configured transaction fees (flat & percentage with min/max caps)
 * and directs fee revenues to appropriate Chart of Accounts revenue GLs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class TariffEngineService {

    private static final Logger log = LoggerFactory.getLogger(TariffEngineService.class);
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final SpringDataTariffRepository tariffRepository;
    private final SpringDataChartOfAccountRepository coaRepository;

    public TariffEngineService(SpringDataTariffRepository tariffRepository,
                               SpringDataChartOfAccountRepository coaRepository) {
        this.tariffRepository = tariffRepository;
        this.coaRepository = coaRepository;
    }

    public record FeeCalculation(
            boolean feeApplicable,
            String tariffCode,
            String tariffName,
            String transactionType,
            String feeType,
            BigDecimal rateOrFlatValue,
            BigDecimal calculatedFee,
            String feeGlCode,
            BigDecimal totalDebitRequired
    ) {
        public static FeeCalculation zeroFee(String transactionType, BigDecimal amount) {
            BigDecimal amt = amount != null ? amount : BigDecimal.ZERO;
            return new FeeCalculation(
                    false,
                    null,
                    "No Active Tariff",
                    transactionType,
                    "NONE",
                    BigDecimal.ZERO,
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    "4020",
                    amt.setScale(2, RoundingMode.HALF_UP)
            );
        }
    }

    /**
     * Calculates transaction fee according to active tariff rules.
     */
    @Transactional(readOnly = true)
    public FeeCalculation calculateFee(String transactionType, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return FeeCalculation.zeroFee(transactionType, BigDecimal.ZERO);
        }

        List<TariffEntity> activeTariffs = tariffRepository.findByTransactionTypeAndActiveTrue(transactionType);
        if (activeTariffs.isEmpty()) {
            return FeeCalculation.zeroFee(transactionType, amount);
        }

        TariffEntity tariff = activeTariffs.get(0);
        BigDecimal fee = BigDecimal.ZERO;

        if ("FLAT".equalsIgnoreCase(tariff.getFeeType())) {
            fee = tariff.getFeeValue();
        } else if ("PERCENTAGE".equalsIgnoreCase(tariff.getFeeType())) {
            BigDecimal rateFraction = tariff.getFeeValue().divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP);
            fee = amount.multiply(rateFraction).setScale(2, RoundingMode.HALF_UP);

            if (tariff.getMinFee() != null && fee.compareTo(tariff.getMinFee()) < 0) {
                fee = tariff.getMinFee();
            }
            if (tariff.getMaxFee() != null && fee.compareTo(tariff.getMaxFee()) > 0) {
                fee = tariff.getMaxFee();
            }
        }

        BigDecimal total = amount.add(fee);

        return new FeeCalculation(
                fee.compareTo(BigDecimal.ZERO) > 0,
                tariff.getTariffCode(),
                tariff.getTariffName(),
                transactionType,
                tariff.getFeeType(),
                tariff.getFeeValue(),
                fee.setScale(2, RoundingMode.HALF_UP),
                tariff.getFeeGlCode(),
                total.setScale(2, RoundingMode.HALF_UP)
        );
    }

    @Transactional(readOnly = true)
    public List<TariffEntity> listTariffs() {
        return tariffRepository.findAll();
    }

    @Transactional
    public TariffEntity createTariff(TariffEntity tariff) {
        if (tariff.getTariffCode() == null || tariff.getTariffCode().isBlank()) {
            throw new IllegalArgumentException("Tariff code is required.");
        }
        if (tariffRepository.findByTariffCode(tariff.getTariffCode().trim()).isPresent()) {
            throw new IllegalArgumentException("Tariff with code " + tariff.getTariffCode() + " already exists.");
        }
        tariff.setTariffCode(tariff.getTariffCode().toUpperCase().trim());
        return tariffRepository.save(tariff);
    }

    @Transactional
    public TariffEntity updateTariff(UUID tariffId, TariffEntity updated) {
        TariffEntity existing = tariffRepository.findById(tariffId)
                .orElseThrow(() -> new IllegalArgumentException("Tariff not found with ID: " + tariffId));

        existing.setTariffName(updated.getTariffName());
        existing.setTransactionType(updated.getTransactionType());
        existing.setFeeType(updated.getFeeType());
        existing.setFeeValue(updated.getFeeValue());
        existing.setMinFee(updated.getMinFee());
        existing.setMaxFee(updated.getMaxFee());
        existing.setFeeGlCode(updated.getFeeGlCode());
        existing.setActive(updated.isActive());
        existing.setDescription(updated.getDescription());

        return tariffRepository.save(existing);
    }

    @Transactional
    public TariffEntity toggleStatus(UUID tariffId, boolean active) {
        TariffEntity existing = tariffRepository.findById(tariffId)
                .orElseThrow(() -> new IllegalArgumentException("Tariff not found with ID: " + tariffId));
        existing.setActive(active);
        return tariffRepository.save(existing);
    }
}
