package com.kab.qershi.loan.origination.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity mapping loan_groups table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "loan_groups")
public class LoanGroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "group_id")
    private UUID groupId;

    @Column(name = "group_name", nullable = false, length = 100)
    private String groupName;

    @Column(name = "is_formal", nullable = false)
    private boolean formal = false;

    @Column(name = "license_no", length = 50)
    private String licenseNo;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id")
    private List<LoanGroupMemberEntity> members = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public LoanGroupEntity() {}

    public LoanGroupEntity(UUID groupId, String groupName, boolean formal, String licenseNo,
                           List<LoanGroupMemberEntity> members, Instant createdAt, Instant updatedAt) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.formal = formal;
        this.licenseNo = licenseNo;
        this.members = members != null ? members : new ArrayList<>();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public boolean isFormal() {
        return formal;
    }

    public void setFormal(boolean formal) {
        this.formal = formal;
    }

    public String getLicenseNo() {
        return licenseNo;
    }

    public void setLicenseNo(String licenseNo) {
        this.licenseNo = licenseNo;
    }

    public List<LoanGroupMemberEntity> getMembers() {
        return members;
    }

    public void setMembers(List<LoanGroupMemberEntity> members) {
        this.members = members;
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
