package com.kab.qershi.auth.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Infrastructure JPA Entity mapping to the tenant-isolated roles table.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@Entity
@Table(name = "roles") // Stored inside individual schemas, schema name is controlled dynamically by search_path
@Getter
@Setter
public class RoleEntity {

    @Id
    @Column(name = "role_id", updatable = false, nullable = false)
    private UUID roleId;

    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName;

    @Column(name = "is_system_defined", nullable = false)
    private boolean isSystemDefined;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id")
    )
    @Column(name = "permission_code")
    private Set<String> permissions = new HashSet<>();
}