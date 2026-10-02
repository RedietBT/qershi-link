package com.kab.qershi.account.infrastructure.rest;

import com.kab.qershi.account.domain.model.SaccoConfig;
import com.kab.qershi.account.domain.ports.outbound.SaccoConfigRepositoryPort;
import com.kab.qershi.account.infrastructure.rest.dto.SaccoConfigRequest;
import com.kab.qershi.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing tenant SACCO Code.
 *
 * @author KAB Digital Solution PLC
 * @version 1.2.0
 */
@RestController
@RequestMapping("/api/v1/sacco-config")
@Tag(name = "SACCO Configuration", description = "Endpoints for configuring tenant SACCO code once for core account generation.")
@SecurityRequirement(name = "bearerAuth")
public class SaccoConfigController {

    private final SaccoConfigRepositoryPort saccoConfigRepository;

    public SaccoConfigController(SaccoConfigRepositoryPort saccoConfigRepository) {
        this.saccoConfigRepository = saccoConfigRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('ROLE_MANAGE', 'SACCO_CONFIG')")
    @Operation(summary = "Create or Set SACCO Code", description = "Sets the unique SACCO identification code for account opening once per SACCO tenant.")
    public ResponseEntity<ApiResponse<SaccoConfig>> createSaccoConfig(@Valid @RequestBody SaccoConfigRequest request) {
        return saveOrUpdateConfig(request);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('ROLE_MANAGE', 'SACCO_CONFIG')")
    @Operation(summary = "Update SACCO Code", description = "Updates the SACCO identification code for account generation.")
    public ResponseEntity<ApiResponse<SaccoConfig>> updateSaccoConfig(@Valid @RequestBody SaccoConfigRequest request) {
        return saveOrUpdateConfig(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAnyAuthority('ACCOUNT_VIEW', 'SACCO_CONFIG')")
    @Operation(summary = "Get SACCO Code Configuration", description = "Retrieves the active SACCO identification code for this tenant.")
    public ResponseEntity<ApiResponse<SaccoConfig>> getSaccoConfig() {
        SaccoConfig config = saccoConfigRepository.findFirst()
                .orElseGet(() -> new SaccoConfig(null, "0001", "Default SACCO", "0001", null, null));
        return ResponseEntity.ok(ApiResponse.success(config, "SACCO code configuration retrieved successfully."));
    }

    private ResponseEntity<ApiResponse<SaccoConfig>> saveOrUpdateConfig(SaccoConfigRequest request) {
        SaccoConfig config = saccoConfigRepository.findFirst()
                .orElseGet(SaccoConfig::new);

        config.update(
                request.saccoCode(),
                request.saccoName(),
                config.getBranchCode() != null ? config.getBranchCode() : "0001"
        );

        SaccoConfig saved = saccoConfigRepository.save(config);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(saved, "SACCO code configured successfully. Code: " + saved.getSaccoCode()));
    }
}
