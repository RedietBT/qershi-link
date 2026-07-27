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
public class MemberAddress {

    private final UUID addressId;
    private final UUID userId;
    private String primaryPhone;
    @Setter private String secondaryPhone;
    @Setter private String email;
    @Setter private String region;
    @Setter private String zoneSubcity;
    @Setter private String woreda;
    @Setter private String houseNumber;

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

    public void setPrimaryPhone(String primaryPhone) {
        if (primaryPhone == null || !primaryPhone.matches("^\\+251\\d{9}$")) {
            throw new IllegalArgumentException("Phone number must comply with E.164 format (+251XXXXXXXXX)");
        }
        this.primaryPhone = primaryPhone;
    }
}
