package com.kab.qershi.pricing.domain.ports.inbound;

import com.kab.qershi.pricing.domain.model.FeeCalculationResult;
import java.math.BigDecimal;

/**
 * Inbound port for dynamic fee and tariff calculation.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface TariffCalculationUseCase {
    FeeCalculationResult calculateFee(String transactionType, BigDecimal amount);
}
