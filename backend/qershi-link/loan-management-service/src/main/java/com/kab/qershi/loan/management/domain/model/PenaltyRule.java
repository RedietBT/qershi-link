package com.kab.qershi.loan.management.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Pure Domain Entity representing dynamic SACCO Loan Penalty Policy configuration.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class PenaltyRule {

    private UUID configId;
    private String policyCode;
    private String policyName;
    private Integer gracePeriodDays;
    private BigDecimal penaltyRatePct;
    private Boolean active;
    private OffsetDateTime createdAt;

    public PenaltyRule() {}

    public PenaltyRule(UUID configId, String policyCode, String policyName, Integer gracePeriodDays,
                       BigDecimal penaltyRatePct, Boolean active, OffsetDateTime createdAt) {
        this.configId = configId;
        this.policyCode = policyCode;
        this.policyName = policyName;
        this.gracePeriodDays = gracePeriodDays;
        this.penaltyRatePct = penaltyRatePct;
        this.active = active;
        this.createdAt = createdAt;
    }

    public UUID getConfigId() {
        return configId;
    }

    public void setConfigId(UUID configId) {
        this.configId = configId;
    }

    public String getPolicyCode() {
        return policyCode;
    }

    public void setPolicyCode(String policyCode) {
        this.policyCode = policyCode;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public Integer getGracePeriodDays() {
        return gracePeriodDays;
    }

    public void setGracePeriodDays(Integer gracePeriodDays) {
        this.gracePeriodDays = gracePeriodDays;
    }

    public BigDecimal getPenaltyRatePct() {
        return penaltyRatePct;
    }

    public void setPenaltyRatePct(BigDecimal penaltyRatePct) {
        this.penaltyRatePct = penaltyRatePct;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
