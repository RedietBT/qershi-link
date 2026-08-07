package com.kab.qershi.loan.origination.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapping loan_credit_scoring table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "loan_credit_scoring")
public class LoanCreditScoringEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "scoring_id")
    private UUID scoringId;

    @Column(name = "application_id", nullable = false, unique = true)
    private UUID applicationId;

    @Column(name = "savings_consistency", precision = 5, scale = 2)
    private BigDecimal savingsConsistency;

    @Column(name = "historical_yield", precision = 10, scale = 2)
    private BigDecimal historicalYield;

    @Column(name = "projected_yield", precision = 10, scale = 2)
    private BigDecimal projectedYield;

    @Column(name = "land_size_hectares", precision = 8, scale = 2)
    private BigDecimal landSizeHectares;

    @Column(name = "calculated_score", precision = 5, scale = 2)
    private BigDecimal calculatedScore;

    @Column(name = "passed_eligibility", nullable = false)
    private boolean passedEligibility = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public LoanCreditScoringEntity() {}

    public LoanCreditScoringEntity(UUID scoringId, UUID applicationId, BigDecimal savingsConsistency,
                                  BigDecimal historicalYield, BigDecimal projectedYield, BigDecimal landSizeHectares,
                                  BigDecimal calculatedScore, boolean passedEligibility, Instant createdAt) {
        this.scoringId = scoringId;
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

    public void setScoringId(UUID scoringId) {
        this.scoringId = scoringId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public BigDecimal getSavingsConsistency() {
        return savingsConsistency;
    }

    public void setSavingsConsistency(BigDecimal savingsConsistency) {
        this.savingsConsistency = savingsConsistency;
    }

    public BigDecimal getHistoricalYield() {
        return historicalYield;
    }

    public void setHistoricalYield(BigDecimal historicalYield) {
        this.historicalYield = historicalYield;
    }

    public BigDecimal getProjectedYield() {
        return projectedYield;
    }

    public void setProjectedYield(BigDecimal projectedYield) {
        this.projectedYield = projectedYield;
    }

    public BigDecimal getLandSizeHectares() {
        return landSizeHectares;
    }

    public void setLandSizeHectares(BigDecimal landSizeHectares) {
        this.landSizeHectares = landSizeHectares;
    }

    public BigDecimal getCalculatedScore() {
        return calculatedScore;
    }

    public void setCalculatedScore(BigDecimal calculatedScore) {
        this.calculatedScore = calculatedScore;
    }

    public boolean isPassedEligibility() {
        return passedEligibility;
    }

    public void setPassedEligibility(boolean passedEligibility) {
        this.passedEligibility = passedEligibility;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
