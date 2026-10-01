package com.kab.qershi.pricing.domain.ports.inbound;

import com.kab.qershi.pricing.domain.model.WithholdingTaxResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Inbound port for statutory withholding tax calculation and audit log retrieval.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface TaxAssessmentUseCase {
    WithholdingTaxResult assessSavingsInterestTax(String accountNo, LocalDate businessDate, BigDecimal grossInterest);
    List<WithholdingTaxResult> listTaxLogs(String accountNo);
    BigDecimal sumTaxWithheldBetween(LocalDate startDate, LocalDate endDate);
}
