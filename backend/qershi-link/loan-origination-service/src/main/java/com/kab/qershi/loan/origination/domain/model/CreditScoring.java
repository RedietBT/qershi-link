package com.kab.qershi.loan.origination.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing pre-eligibility multi-factor credit scoring.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class CreditScoring {

    private final UUID scoringId;
    private final UUID applicationId;
    private final BigDecimal savingsConsistency;
    private final BigDecimal historicalYield;
    private final BigDecimal projectedYield;
    private final BigDecimal landSizeHectares;
    private final BigDecimal calculatedScore;
    private final boolean passedEligibility;
    private final Instant createdAt;

    public CreditScoring(UUID scoringId, UUID applicationId, BigDecimal savingsConsistency,
                         BigDecimal historicalYield, BigDecimal projectedYield, BigDecimal landSizeHectares,
                         BigDecimal calculatedScore, boolean passedEligibility, Instant createdAt) {
        this.scoringId = scoringId != null ? scoringId : UUID.randomUUID();
        this.applicationId = applicationId;
        this.savingsConsistency = savingsConsistency;
        this.historicalYield = historicalYield;
        this.projectedYield = projectedYield;
        this.landSizeHectares = landSizeHectares;
        this.calculatedScore = calculatedScore;
        this.passedEligibility = passedEligibility;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public UUID getScoringId() {
        return scoringId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public BigDecimal getSavingsConsistency() {
        return savingsConsistency;
    }

    public BigDecimal getHistoricalYield() {
        return historicalYield;
    }

    public BigDecimal getProjectedYield() {
        return projectedYield;
    }

    public BigDecimal getLandSizeHectares() {
        return landSizeHectares;
    }

    public BigDecimal getCalculatedScore() {
        return calculatedScore;
    }

    public boolean isPassedEligibility() {
        return passedEligibility;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
