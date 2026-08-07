package com.kab.qershi.loan.origination.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Root Aggregate domain entity for SACCO Loan Applications.
 * Dynamic scoringType string permits custom SACCO-configured pre-eligibility engines.
 *
 * @author KAB Digital Solution PLC
 * @version 1.1.0
 */
public class LoanApplication {

    private final UUID applicationId;
    private final String applicationNo;
    private final UUID userId;
    private final UUID groupId;
    private final UUID productId;
    private final String scoringType;
    private final BigDecimal amountRequested;
    private BigDecimal amountApproved;
    private ApplicationStatus status;
    private CreditScoring creditScoring;
    private final List<Collateral> collaterals;
    private final List<ApprovalLog> approvalLogs;
    private final Instant createdAt;
    private Instant updatedAt;

    public LoanApplication(UUID applicationId, String applicationNo, UUID userId, UUID groupId,
                           UUID productId, String scoringType, BigDecimal amountRequested,
                           BigDecimal amountApproved, ApplicationStatus status, CreditScoring creditScoring,
                           List<Collateral> collaterals, List<ApprovalLog> approvalLogs,
                           Instant createdAt, Instant updatedAt) {
        this.applicationId = applicationId != null ? applicationId : UUID.randomUUID();
        this.applicationNo = applicationNo;
        this.userId = userId;
        this.groupId = groupId;
        this.productId = productId;
        this.scoringType = scoringType != null ? scoringType.toUpperCase().trim() : "SAVINGS";
        this.amountRequested = amountRequested;
        this.amountApproved = amountApproved;
        this.status = status != null ? status : ApplicationStatus.DRAFT;
        this.creditScoring = creditScoring;
        this.collaterals = collaterals != null ? new ArrayList<>(collaterals) : new ArrayList<>();
        this.approvalLogs = approvalLogs != null ? new ArrayList<>(approvalLogs) : new ArrayList<>();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public String getApplicationNo() {
        return applicationNo;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getScoringType() {
        return scoringType;
    }

    public BigDecimal getAmountRequested() {
        return amountRequested;
    }

    public BigDecimal getAmountApproved() {
        return amountApproved;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public CreditScoring getCreditScoring() {
        return creditScoring;
    }

    public List<Collateral> getCollaterals() {
        return Collections.unmodifiableList(collaterals);
    }

    public List<ApprovalLog> getApprovalLogs() {
        return Collections.unmodifiableList(approvalLogs);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setAmountApproved(BigDecimal amountApproved) {
        this.amountApproved = amountApproved;
        this.updatedAt = Instant.now();
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public void setCreditScoring(CreditScoring creditScoring) {
        this.creditScoring = creditScoring;
        this.updatedAt = Instant.now();
    }

    public void addCollateral(Collateral collateral) {
        if (collateral != null) {
            this.collaterals.add(collateral);
            this.updatedAt = Instant.now();
        }
    }

    public void addApprovalLog(ApprovalLog log) {
        if (log != null) {
            this.approvalLogs.add(log);
            this.updatedAt = Instant.now();
        }
    }
}
