package com.kab.qershi.pricing.infrastructure.persistence;

import com.kab.qershi.pricing.domain.model.WithholdingTaxResult;
import com.kab.qershi.pricing.domain.ports.outbound.TaxLogRepositoryPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter implementing TaxLogRepositoryPort via Spring Data JPA.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class TaxLogRepositoryAdapter implements TaxLogRepositoryPort {

    private final SpringDataInterestTaxDeductionLogRepository repository;

    public TaxLogRepositoryAdapter(SpringDataInterestTaxDeductionLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public WithholdingTaxResult save(WithholdingTaxResult taxLog) {
        InterestTaxDeductionLogEntity entity = new InterestTaxDeductionLogEntity(
                taxLog.accountNo(),
                taxLog.businessDate(),
                taxLog.grossInterest(),
                taxLog.taxRatePct(),
                taxLog.taxWithheld(),
                taxLog.netInterest(),
                taxLog.whtGlCode()
        );
        InterestTaxDeductionLogEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<WithholdingTaxResult> findAll() {
        return repository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<WithholdingTaxResult> findByAccountNo(String accountNo) {
        return repository.findByAccountNo(accountNo).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public BigDecimal sumTaxWithheldBetween(LocalDate startDate, LocalDate endDate) {
        return repository.sumTaxWithheldBetween(startDate, endDate);
    }

    private WithholdingTaxResult toDomain(InterestTaxDeductionLogEntity entity) {
        if (entity == null) return null;
        return new WithholdingTaxResult(
                entity.getAccountNo(),
                entity.getBusinessDate(),
                entity.getGrossInterest(),
                entity.getTaxRatePct(),
                entity.getTaxWithheld(),
                entity.getNetInterest(),
                entity.getWhtGlCode()
        );
    }
}
