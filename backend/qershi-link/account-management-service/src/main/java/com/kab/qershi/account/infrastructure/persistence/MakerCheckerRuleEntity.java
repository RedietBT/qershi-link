package com.kab.qershi.account.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA Entity mapping sacco_xxx.sacco_maker_checker_rules database table.
 * Stores Four-Eyes operational policies, transaction thresholds, and anti-self-approval enforcement.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "sacco_maker_checker_rules")
public class MakerCheckerRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rule_id", nullable = false, updatable = false)
    private UUID ruleId;

    @Column(name = "sacco_code", nullable = false, length = 20)
    private String saccoCode = "0001";

    @Column(name = "enable_member_onboarding_checker", nullable = false)
    private boolean enableMemberOnboardingChecker = true;

    @Column(name = "enable_account_opening_checker", nullable = false)
    private boolean enableAccountOpeningChecker = true;

    @Column(name = "enable_account_freeze_checker", nullable = false)
    private boolean enableAccountFreezeChecker = true;

    @Column(name = "enable_loan_approval_checker", nullable = false)
    private boolean enableLoanApprovalChecker = true;

    @Column(name = "enable_loan_disbursement_checker", nullable = false)
    private boolean enableLoanDisbursementChecker = true;

    @Column(name = "transaction_checker_threshold", nullable = false, precision = 18, scale = 4)
    private BigDecimal transactionCheckerThreshold = new BigDecimal("50000.0000");

    @Column(name = "daily_account_limit_threshold", nullable = false, precision = 18, scale = 4)
    private BigDecimal dailyAccountLimitThreshold = new BigDecimal("200000.0000");

    @Column(name = "enforce_anti_self_approval", nullable = false)
    private boolean enforceAntiSelfApproval = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public MakerCheckerRuleEntity() {}

    public UUID getRuleId() { return ruleId; }
    public void setRuleId(UUID ruleId) { this.ruleId = ruleId; }

    public String getSaccoCode() { return saccoCode; }
    public void setSaccoCode(String saccoCode) { this.saccoCode = saccoCode; }

    public boolean isEnableMemberOnboardingChecker() { return enableMemberOnboardingChecker; }
    public void setEnableMemberOnboardingChecker(boolean enableMemberOnboardingChecker) { this.enableMemberOnboardingChecker = enableMemberOnboardingChecker; }

    public boolean isEnableAccountOpeningChecker() { return enableAccountOpeningChecker; }
    public void setEnableAccountOpeningChecker(boolean enableAccountOpeningChecker) { this.enableAccountOpeningChecker = enableAccountOpeningChecker; }

    public boolean isEnableAccountFreezeChecker() { return enableAccountFreezeChecker; }
    public void setEnableAccountFreezeChecker(boolean enableAccountFreezeChecker) { this.enableAccountFreezeChecker = enableAccountFreezeChecker; }

    public boolean isEnableLoanApprovalChecker() { return enableLoanApprovalChecker; }
    public void setEnableLoanApprovalChecker(boolean enableLoanApprovalChecker) { this.enableLoanApprovalChecker = enableLoanApprovalChecker; }

    public boolean isEnableLoanDisbursementChecker() { return enableLoanDisbursementChecker; }
    public void setEnableLoanDisbursementChecker(boolean enableLoanDisbursementChecker) { this.enableLoanDisbursementChecker = enableLoanDisbursementChecker; }

    public BigDecimal getTransactionCheckerThreshold() { return transactionCheckerThreshold; }
    public void setTransactionCheckerThreshold(BigDecimal transactionCheckerThreshold) { this.transactionCheckerThreshold = transactionCheckerThreshold; }

    public BigDecimal getDailyAccountLimitThreshold() { return dailyAccountLimitThreshold; }
    public void setDailyAccountLimitThreshold(BigDecimal dailyAccountLimitThreshold) { this.dailyAccountLimitThreshold = dailyAccountLimitThreshold; }

    public boolean isEnforceAntiSelfApproval() { return enforceAntiSelfApproval; }
    public void setEnforceAntiSelfApproval(boolean enforceAntiSelfApproval) { this.enforceAntiSelfApproval = enforceAntiSelfApproval; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
