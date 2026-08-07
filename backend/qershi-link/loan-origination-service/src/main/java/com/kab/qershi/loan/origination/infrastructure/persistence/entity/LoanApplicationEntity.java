package com.kab.qershi.loan.origination.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity mapping loan_applications table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "loan_applications")
public class LoanApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "application_id")
    private UUID applicationId;

    @Column(name = "application_no", nullable = false, unique = true, length = 50)
    private String applicationNo;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "group_id")
    private UUID groupId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "scoring_type", nullable = false, length = 50)
    private String scoringType;

    @Column(name = "amount_requested", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountRequested;

    @Column(name = "amount_approved", precision = 15, scale = 2)
    private BigDecimal amountApproved;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "application_id", referencedColumnName = "application_id")
    private LoanCreditScoringEntity creditScoring;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "application_id")
    private List<LoanCollateralEntity> collaterals = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "application_id")
    private List<ApprovalWorkflowLogEntity> approvalLogs = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public LoanApplicationEntity() {}

    public LoanApplicationEntity(UUID applicationId, String applicationNo, UUID userId, UUID groupId,
                                UUID productId, String scoringType, BigDecimal amountRequested,
                                BigDecimal amountApproved, String status, LoanCreditScoringEntity creditScoring,
                                List<LoanCollateralEntity> collaterals, List<ApprovalWorkflowLogEntity> approvalLogs,
                                Instant createdAt, Instant updatedAt) {
        this.applicationId = applicationId;
        this.applicationNo = applicationNo;
        this.userId = userId;
        this.groupId = groupId;
        this.productId = productId;
        this.scoringType = scoringType;
        this.amountRequested = amountRequested;
        this.amountApproved = amountApproved;
        this.status = status;
        this.creditScoring = creditScoring;
        this.collaterals = collaterals != null ? collaterals : new ArrayList<>();
        this.approvalLogs = approvalLogs != null ? approvalLogs : new ArrayList<>();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationNo() {
        return applicationNo;
    }

    public void setApplicationNo(String applicationNo) {
        this.applicationNo = applicationNo;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getScoringType() {
        return scoringType;
    }

    public void setScoringType(String scoringType) {
        this.scoringType = scoringType;
    }

    public BigDecimal getAmountRequested() {
        return amountRequested;
    }

    public void setAmountRequested(BigDecimal amountRequested) {
        this.amountRequested = amountRequested;
    }

    public BigDecimal getAmountApproved() {
        return amountApproved;
    }

    public void setAmountApproved(BigDecimal amountApproved) {
        this.amountApproved = amountApproved;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LoanCreditScoringEntity getCreditScoring() {
        return creditScoring;
    }

    public void setCreditScoring(LoanCreditScoringEntity creditScoring) {
        this.creditScoring = creditScoring;
    }

    public List<LoanCollateralEntity> getCollaterals() {
        return collaterals;
    }

    public void setCollaterals(List<LoanCollateralEntity> collaterals) {
        this.collaterals = collaterals;
    }

    public List<ApprovalWorkflowLogEntity> getApprovalLogs() {
        return approvalLogs;
    }

    public void setApprovalLogs(List<ApprovalWorkflowLogEntity> approvalLogs) {
        this.approvalLogs = approvalLogs;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
