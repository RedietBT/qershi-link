package com.kab.qershi.loan.management.infrastructure.persistence.adapter;

import com.kab.qershi.loan.management.domain.model.LoanRepayment;
import com.kab.qershi.loan.management.domain.port.out.LoanRepaymentRepositoryPort;
import com.kab.qershi.loan.management.infrastructure.persistence.entity.LoanRepaymentEntity;
import com.kab.qershi.loan.management.infrastructure.persistence.repository.SpringDataLoanRepaymentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Outbound Repository Adapter implementing LoanRepaymentRepositoryPort via JPA.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class LoanRepaymentRepositoryAdapter implements LoanRepaymentRepositoryPort {

    private final SpringDataLoanRepaymentRepository repository;

    public LoanRepaymentRepositoryAdapter(SpringDataLoanRepaymentRepository repository) {
        this.repository = repository;
    }

    @Override
    public LoanRepayment save(LoanRepayment repayment) {
        LoanRepaymentEntity entity = toEntity(repayment);
        LoanRepaymentEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<LoanRepayment> findByAccountId(UUID accountId) {
        return repository.findByAccountId(accountId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private LoanRepaymentEntity toEntity(LoanRepayment domain) {
        if (domain == null) return null;
        LoanRepaymentEntity entity = new LoanRepaymentEntity();
        entity.setRepaymentId(domain.getRepaymentId());
        entity.setAccountId(domain.getAccountId());
        entity.setTransactionRef(domain.getTransactionRef());
        entity.setAmountPaid(domain.getAmountPaid());
        entity.setPrincipalPortion(domain.getPrincipalPortion());
        entity.setInterestPortion(domain.getInterestPortion());
        entity.setPenaltyPortion(domain.getPenaltyPortion());
        entity.setPaymentDate(domain.getPaymentDate());
        entity.setPaymentChannel(domain.getPaymentChannel());
        entity.setRemarks(domain.getRemarks());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    private LoanRepayment toDomain(LoanRepaymentEntity entity) {
        if (entity == null) return null;
        return new LoanRepayment(
                entity.getRepaymentId(),
                entity.getAccountId(),
                entity.getTransactionRef(),
                entity.getAmountPaid(),
                entity.getPrincipalPortion(),
                entity.getInterestPortion(),
                entity.getPenaltyPortion(),
                entity.getPaymentDate(),
                entity.getPaymentChannel(),
                entity.getRemarks(),
                entity.getCreatedAt()
        );
    }
}
