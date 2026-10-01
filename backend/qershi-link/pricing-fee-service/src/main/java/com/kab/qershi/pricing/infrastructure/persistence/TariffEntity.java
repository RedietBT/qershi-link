package com.kab.qershi.pricing.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity mapping tariffs table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "tariffs", indexes = {
        @Index(name = "idx_tariffs_txn_type", columnList = "transaction_type"),
        @Index(name = "idx_tariffs_is_active", columnList = "is_active")
})
public class TariffEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "tariff_id", nullable = false, updatable = false)
    private UUID tariffId;

    @Column(name = "tariff_code", nullable = false, unique = true, length = 50)
    private String tariffCode;

    @Column(name = "tariff_name", nullable = false, length = 150)
    private String tariffName;

    @Column(name = "transaction_type", nullable = false, length = 50)
    private String transactionType;

    @Column(name = "fee_type", nullable = false, length = 20)
    private String feeType = "FLAT";

    @Column(name = "fee_value", nullable = false, precision = 15, scale = 4)
    private BigDecimal feeValue = BigDecimal.ZERO;

    @Column(name = "min_fee", precision = 15, scale = 2)
    private BigDecimal minFee;

    @Column(name = "max_fee", precision = 15, scale = 2)
    private BigDecimal maxFee;

    @Column(name = "fee_gl_code", nullable = false, length = 50)
    private String feeGlCode = "4020";

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "ETB";

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "tariff", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("slabOrder ASC, fromAmount ASC")
    private java.util.List<TariffSlabEntity> slabs = new java.util.ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public TariffEntity() {}

    public TariffEntity(String tariffCode, String tariffName, String transactionType,
                        String feeType, BigDecimal feeValue, BigDecimal minFee, BigDecimal maxFee,
                        String feeGlCode, String description) {
        this(null, tariffCode, tariffName, transactionType, feeType, feeValue, minFee, maxFee, feeGlCode, "ETB", true, description);
    }

    public TariffEntity(UUID tariffId, String tariffCode, String tariffName, String transactionType,
                        String feeType, BigDecimal feeValue, BigDecimal minFee, BigDecimal maxFee,
                        String feeGlCode, String currency, boolean active, String description) {
        this.tariffId = tariffId;
        this.tariffCode = tariffCode;
        this.tariffName = tariffName;
        this.transactionType = transactionType;
        this.feeType = feeType != null ? feeType : "FLAT";
        this.feeValue = feeValue != null ? feeValue : BigDecimal.ZERO;
        this.minFee = minFee;
        this.maxFee = maxFee;
        this.feeGlCode = feeGlCode != null ? feeGlCode : "4020";
        this.currency = currency != null ? currency : "ETB";
        this.active = active;
        this.description = description;
        this.slabs = new java.util.ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void addSlab(TariffSlabEntity slab) {
        if (slab != null) {
            slab.setTariff(this);
            this.slabs.add(slab);
        }
    }

    public void clearSlabs() {
        this.slabs.clear();
    }

    public void setSlabs(java.util.List<TariffSlabEntity> newSlabs) {
        this.slabs.clear();
        if (newSlabs != null) {
            for (TariffSlabEntity s : newSlabs) {
                s.setTariff(this);
                this.slabs.add(s);
            }
        }
    }

    public java.util.List<TariffSlabEntity> getSlabs() { return slabs; }

    public UUID getTariffId() { return tariffId; }
    public void setTariffId(UUID tariffId) { this.tariffId = tariffId; }

    public String getTariffCode() { return tariffCode; }
    public void setTariffCode(String tariffCode) { this.tariffCode = tariffCode; }

    public String getTariffName() { return tariffName; }
    public void setTariffName(String tariffName) { this.tariffName = tariffName; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getFeeType() { return feeType; }
    public void setFeeType(String feeType) { this.feeType = feeType; }

    public BigDecimal getFeeValue() { return feeValue; }
    public void setFeeValue(BigDecimal feeValue) { this.feeValue = feeValue; }

    public BigDecimal getMinFee() { return minFee; }
    public void setMinFee(BigDecimal minFee) { this.minFee = minFee; }

    public BigDecimal getMaxFee() { return maxFee; }
    public void setMaxFee(BigDecimal maxFee) { this.maxFee = maxFee; }

    public String getFeeGlCode() { return feeGlCode; }
    public void setFeeGlCode(String feeGlCode) { this.feeGlCode = feeGlCode; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
