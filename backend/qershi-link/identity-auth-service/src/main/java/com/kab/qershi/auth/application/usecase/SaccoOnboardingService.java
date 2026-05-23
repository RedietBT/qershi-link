package com.kab.qershi.auth.application.usecase;

import com.kab.qershi.auth.domain.model.Role;
import com.kab.qershi.auth.domain.model.Sacco;
import com.kab.qershi.auth.domain.model.User;
import com.kab.qershi.auth.domain.model.GlobalRole;
import com.kab.qershi.auth.domain.ports.inbound.SaccoOnboardingUseCase;
import com.kab.qershi.auth.domain.ports.outbound.SaccoRepositoryPort;
import com.kab.qershi.auth.domain.ports.outbound.UserRepositoryPort;
import com.kab.qershi.auth.domain.ports.outbound.TenantProvisioningPort;
import com.kab.qershi.auth.domain.service.IdentityDomainService;
import com.kab.qershi.auth.domain.service.RbacDomainService;

import java.util.UUID;

/**
 * Service implementation handling the registration and configuration tasks of a new SACCO or Union.
 * Coordinates master data writing with physical schema isolation setups.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
public class SaccoOnboardingService implements SaccoOnboardingUseCase {

    private final SaccoRepositoryPort saccoRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final TenantProvisioningPort tenantProvisioningPort;
    private final IdentityDomainService identityDomainService;
    private final RbacDomainService rbacDomainService;

    /**
     * Constructs the onboarding service with required outbound boundaries and domain calculators.
     */
    public SaccoOnboardingService(
            SaccoRepositoryPort saccoRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            TenantProvisioningPort tenantProvisioningPort,
            IdentityDomainService identityDomainService,
            RbacDomainService rbacDomainService) {
        this.saccoRepositoryPort = saccoRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.tenantProvisioningPort = tenantProvisioningPort;
        this.identityDomainService = identityDomainService;
        this.rbacDomainService = rbacDomainService;
    }

    /**
     * Executes the orchestration flow for onboarding a new tenant.
     * Enforces the Zero-Orphan architectural policy by destroying partial setups on failure.
     *
     * @param command The input specification containing the registration criteria.
     * @return OnboardResult Emitted details of successful infrastructure tracking states.
     * @throws IllegalStateException If duplicate logical identifiers exist in registries.
     * @throws RuntimeException If an environment exception forces infrastructure rollback.
     */
    @Override
    public OnboardResult onboardSacco(OnboardCommand command) {
        // 1. Verify that the requested legal name is globally unique
        if (saccoRepositoryPort.existsBySaccoName(command.saccoName())) {
            throw new IllegalStateException("A SACCO with this legal name is already registered.");
        }

        // 2. Validate phone formatting and ensure it isn't linked to an existing identity
        identityDomainService.validateUniqueUserIdentifiers(command.adminMsisdn());

        // 3. Compute and sanitize the structural database schema tag name to prevent SQL Injection
        String sanitizedSchemaName = identityDomainService.generateAndSanitizeSchemaName(
                command.saccoName(),
                command.isUnion()
        );

        UUID saccoId = UUID.randomUUID();
        Sacco newSacco = new Sacco(
                saccoId,
                command.saccoName(),
                sanitizedSchemaName,
                command.isUnion(),
                command.minShareRequirement()
        );

        // Map platform role hierarchy based on tenant designation
        GlobalRole assignedGlobalRole = command.isUnion() ? GlobalRole.UNION_ADMIN : GlobalRole.SACCO_USER;

        User adminUser = new User(
                UUID.randomUUID(),
                command.adminMsisdn(),
                saccoId,
                "TEMPORARY_INITIAL_HASH_PLACEHOLDER",
                assignedGlobalRole
        );

        // 4. Transaction-wrapped physical setup logic mimicking the Zero-Orphan Policy rule
        try {
            // Save metadata registries inside master_schema
            saccoRepositoryPort.save(newSacco);
            userRepositoryPort.save(adminUser);

            // Execute programmatic CREATE SCHEMA DDL migrations for "The Vault"
            tenantProvisioningPort.provisionTenantSchema(sanitizedSchemaName);

            // Create and seed system capabilities into the default core ADMIN role
            Role defaultAdminRole = new Role(UUID.randomUUID(), "ADMIN", true);
            rbacDomainService.seedAdminRolePermissions(defaultAdminRole);

            // Persist the seeded RBAC layout inside the newly created isolated vault
            tenantProvisioningPort.seedTenantRbac(sanitizedSchemaName, defaultAdminRole);

            // Everything passed without a crash; elevate setup status safely
            newSacco.activate();
            saccoRepositoryPort.save(newSacco);

        } catch (Exception ex) {
            // CRITICAL: Enforcement of the Zero-Orphan Policy.
            // If the schema creation or seeding crashes, completely drop the structural schema namespace.
            tenantProvisioningPort.dropTenantSchema(sanitizedSchemaName);
            throw new RuntimeException("Onboarding failed due to environment provisioning exceptions. Rollback triggered.", ex);
        }

        return new OnboardResult(
                newSacco.getSaccoId(),
                newSacco.getSchemaName(),
                adminUser.getUserId(),
                newSacco.getStatus().name(),
                newSacco.getCreatedAt()
        );
    }
}