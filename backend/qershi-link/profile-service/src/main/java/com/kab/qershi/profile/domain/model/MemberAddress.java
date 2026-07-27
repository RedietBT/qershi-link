package com.kab.qershi.profile.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Domain entity representing a member's contact handles and physical residence location.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Getter
@Setter
public class MemberAddress {

    private final UUID addressId;
    private final UUID userId;
    private String primaryPhone;
    private String secondaryPhone;
    private String email;
    private String region;
    private String zoneSubcity;
    private String woreda;
    private String houseNumber;

    public MemberAddress(UUID addressId, UUID userId, String primaryPhone, String secondaryPhone,
                         String email, String region, String zoneSubcity, String woreda, String houseNumber) {
        this.addressId = addressId != null ? addressId : UUID.randomUUID();
        this.userId = userId;
        setPrimaryPhone(primaryPhone);
        this.secondaryPhone = secondaryPhone;
        this.email = email;
        this.region = region;
        this.zoneSubcity = zoneSubcity;
        this.woreda = woreda;
        this.houseNumber = houseNumber;
    }

    public UUID getAddressId() { return addressId; }
    public UUID getUserId() { return userId; }

    public String getPrimaryPhone() { return primaryPhone; }
    public void setPrimaryPhone(String primaryPhone) {
        if (primaryPhone == null || !primaryPhone.matches("^\\+251\\d{9}$")) {
            throw new IllegalArgumentException("Phone number must comply with E.164 format (+251XXXXXXXXX)");
        }
        this.primaryPhone = primaryPhone;
    }

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
