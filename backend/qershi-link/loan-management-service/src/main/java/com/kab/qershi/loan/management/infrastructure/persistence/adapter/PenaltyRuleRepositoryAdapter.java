package com.kab.qershi.loan.management.infrastructure.persistence.adapter;

import com.kab.qershi.loan.management.domain.model.PenaltyRule;
import com.kab.qershi.loan.management.domain.port.out.PenaltyRuleRepositoryPort;
import com.kab.qershi.loan.management.infrastructure.persistence.entity.PenaltyRuleEntity;
import com.kab.qershi.loan.management.infrastructure.persistence.repository.SpringDataPenaltyRuleRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Outbound Repository Adapter implementing PenaltyRuleRepositoryPort via Spring Data JPA.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Component
public class PenaltyRuleRepositoryAdapter implements PenaltyRuleRepositoryPort {

    private final SpringDataPenaltyRuleRepository repository;

    public PenaltyRuleRepositoryAdapter(SpringDataPenaltyRuleRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PenaltyRule> findByPolicyCode(String policyCode) {
        return repository.findByPolicyCode(policyCode).map(this::toDomain);
    }

    @Override
    public List<PenaltyRule> findByActiveTrue() {
        return repository.findByActiveTrue().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private PenaltyRule toDomain(PenaltyRuleEntity entity) {
        if (entity == null) return null;
        return new PenaltyRule(
                entity.getConfigId(),
                entity.getPolicyCode(),
                entity.getPolicyName(),
                entity.getGracePeriodDays(),
                entity.getPenaltyRatePct(),
                entity.getActive(),
                entity.getCreatedAt()
        );
    }
}
