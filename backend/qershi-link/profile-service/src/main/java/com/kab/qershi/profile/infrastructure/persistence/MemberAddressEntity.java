package com.kab.qershi.profile.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@AllArgsConstructor
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
}
