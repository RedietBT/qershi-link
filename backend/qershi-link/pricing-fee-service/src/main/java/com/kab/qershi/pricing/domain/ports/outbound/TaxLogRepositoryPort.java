package com.kab.qershi.pricing.domain.ports.outbound;

import com.kab.qershi.pricing.domain.model.WithholdingTaxResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Outbound SPI port for persisting statutory tax logs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface TaxLogRepositoryPort {
    WithholdingTaxResult save(WithholdingTaxResult taxLog);
    List<WithholdingTaxResult> findAll();
    List<WithholdingTaxResult> findByAccountNo(String accountNo);
    BigDecimal sumTaxWithheldBetween(LocalDate startDate, LocalDate endDate);
}
