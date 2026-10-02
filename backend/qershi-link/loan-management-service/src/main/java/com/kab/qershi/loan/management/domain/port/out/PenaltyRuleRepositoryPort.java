package com.kab.qershi.loan.management.domain.port.out;

import com.kab.qershi.loan.management.domain.model.PenaltyRule;

import java.util.List;
import java.util.Optional;

/**
 * Outbound Repository Port for PenaltyRule persistence.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public interface PenaltyRuleRepositoryPort {

    Optional<PenaltyRule> findByPolicyCode(String policyCode);

    List<PenaltyRule> findByActiveTrue();
}
