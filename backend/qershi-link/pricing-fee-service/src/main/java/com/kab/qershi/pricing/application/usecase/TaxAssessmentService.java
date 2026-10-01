package com.kab.qershi.pricing.application.usecase;

import com.kab.qershi.pricing.domain.model.WithholdingTaxResult;
import com.kab.qershi.pricing.domain.ports.inbound.TaxAssessmentUseCase;
import com.kab.qershi.pricing.domain.ports.outbound.TaxLogRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Service calculating and recording statutory 5% Withholding Tax (WHT) deductions.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Service
public class TaxAssessmentService implements TaxAssessmentUseCase {

    private static final Logger log = LoggerFactory.getLogger(TaxAssessmentService.class);
    private static final BigDecimal WHT_RATE_PCT = new BigDecimal("5.00");
    private static final BigDecimal WHT_FRACTION = new BigDecimal("0.05");

    private final TaxLogRepositoryPort taxLogRepositoryPort;

    public TaxAssessmentService(TaxLogRepositoryPort taxLogRepositoryPort) {
        this.taxLogRepositoryPort = taxLogRepositoryPort;
    }

    @Override
    @Transactional
    public WithholdingTaxResult assessSavingsInterestTax(String accountNo, LocalDate businessDate, BigDecimal grossInterest) {
        if (grossInterest == null || grossInterest.compareTo(BigDecimal.ZERO) <= 0) {
            return new WithholdingTaxResult(accountNo, businessDate, BigDecimal.ZERO, WHT_RATE_PCT, BigDecimal.ZERO, BigDecimal.ZERO, "2091");
        }

        BigDecimal taxWithheld = grossInterest.multiply(WHT_FRACTION).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netInterest = grossInterest.subtract(taxWithheld).setScale(2, RoundingMode.HALF_UP);

        WithholdingTaxResult result = new WithholdingTaxResult(
                accountNo,
                businessDate,
                grossInterest,
                WHT_RATE_PCT,
                taxWithheld,
                netInterest,
                "2091"
        );

        taxLogRepositoryPort.save(result);
        log.info("Recorded 5% WHT deduction for account {}: gross={}, tax={}, net={}",
                accountNo, grossInterest, taxWithheld, netInterest);

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WithholdingTaxResult> listTaxLogs(String accountNo) {
        if (accountNo != null && !accountNo.isBlank()) {
            return taxLogRepositoryPort.findByAccountNo(accountNo.trim());
        }
        return taxLogRepositoryPort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumTaxWithheldBetween(LocalDate startDate, LocalDate endDate) {
        return taxLogRepositoryPort.sumTaxWithheldBetween(startDate, endDate);
    }
}
