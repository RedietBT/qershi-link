package com.kab.qershi.pricing.application.usecase;

import com.kab.qershi.pricing.domain.model.FeeCalculationResult;
import com.kab.qershi.pricing.domain.model.Tariff;
import com.kab.qershi.pricing.domain.ports.inbound.TariffCalculationUseCase;
import com.kab.qershi.pricing.domain.ports.outbound.TariffRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Core Banking Enterprise Pricing Calculation Engine.
 * Evaluates flat and percentage tariffs with min/max caps.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class PricingCalculationService implements TariffCalculationUseCase {

    private static final Logger log = LoggerFactory.getLogger(PricingCalculationService.class);
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final TariffRepositoryPort tariffRepositoryPort;

    public PricingCalculationService(TariffRepositoryPort tariffRepositoryPort) {
        this.tariffRepositoryPort = tariffRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public FeeCalculationResult calculateFee(String transactionType, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return FeeCalculationResult.zero(transactionType, BigDecimal.ZERO);
        }

        List<Tariff> activeTariffs = tariffRepositoryPort.findActiveByTransactionType(transactionType);
        if (activeTariffs.isEmpty()) {
            return FeeCalculationResult.zero(transactionType, amount);
        }

        Tariff tariff = activeTariffs.get(0);
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

        return new FeeCalculationResult(
                fee.compareTo(BigDecimal.ZERO) > 0,
                tariff.getTariffCode(),
                tariff.getTariffName(),
                transactionType,
                tariff.getFeeType(),
                tariff.getFeeValue(),
                fee.setScale(2, RoundingMode.HALF_UP),
                tariff.getMinFee(),
                tariff.getMaxFee(),
                tariff.getFeeGlCode(),
                total.setScale(2, RoundingMode.HALF_UP)
        );
    }
}
