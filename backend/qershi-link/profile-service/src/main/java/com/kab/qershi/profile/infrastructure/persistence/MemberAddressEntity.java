package com.kab.qershi.profile.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * JPA Entity mapping for member_addresses database table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "member_addresses")
@Getter
@Setter
public class MemberAddressEntity {

    @Id
    @Column(name = "address_id", nullable = false, updatable = false)
    private UUID addressId;

    @Column(name = "user_id", nullable = false, unique = true, updatable = false)
    private UUID userId;

    @Column(name = "primary_phone", nullable = false, unique = true, length = 15)
    private String primaryPhone;

    @Column(name = "secondary_phone", length = 15)
    private String secondaryPhone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "region", nullable = false, length = 100)
    private String region;

    @Column(name = "zone_subcity", nullable = false, length = 100)
    private String zoneSubcity;

    @Column(name = "woreda", nullable = false, length = 100)
    private String woreda;

    @Column(name = "house_number", length = 50)
    private String houseNumber;

    public MemberAddressEntity() {}

    public MemberAddressEntity(UUID addressId, UUID userId, String primaryPhone, String secondaryPhone,
                               String email, String region, String zoneSubcity, String woreda, String houseNumber) {
        this.addressId = addressId;
        this.userId = userId;
        this.primaryPhone = primaryPhone;
        this.secondaryPhone = secondaryPhone;
        this.email = email;
        this.region = region;
        this.zoneSubcity = zoneSubcity;
        this.woreda = woreda;
        this.houseNumber = houseNumber;
    }

    public UUID getAddressId() { return addressId; }
    public void setAddressId(UUID addressId) { this.addressId = addressId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getPrimaryPhone() { return primaryPhone; }
    public void setPrimaryPhone(String primaryPhone) { this.primaryPhone = primaryPhone; }

    public String getSecondaryPhone() { return secondaryPhone; }
    public void setSecondaryPhone(String secondaryPhone) { this.secondaryPhone = secondaryPhone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getZoneSubcity() { return zoneSubcity; }
    public void setZoneSubcity(String zoneSubcity) { this.zoneSubcity = zoneSubcity; }

    public String getWoreda() { return woreda; }
    public void setWoreda(String woreda) { this.woreda = woreda; }

    public String getHouseNumber() { return houseNumber; }
    public void setHouseNumber(String houseNumber) { this.houseNumber = houseNumber; }
}
