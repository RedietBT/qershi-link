package com.kab.qershi.account.infrastructure.rest;

import com.kab.qershi.account.infrastructure.persistence.SaccoConfigEntity;
import com.kab.qershi.account.infrastructure.persistence.SpringDataSaccoConfigRepository;
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
 * @version 1.1.0
 */
@RestController
@RequestMapping("/api/v1/sacco-config")
@Tag(name = "SACCO Configuration", description = "Endpoints for configuring tenant SACCO code once for core account generation.")
@SecurityRequirement(name = "bearerAuth")
public class SaccoConfigController {

    private final SpringDataSaccoConfigRepository saccoConfigRepository;

    public SaccoConfigController(SpringDataSaccoConfigRepository saccoConfigRepository) {
        this.saccoConfigRepository = saccoConfigRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('ROLE_MANAGE')")
    @Operation(summary = "Create or Set SACCO Code", description = "Sets the unique SACCO identification code for account opening once per SACCO tenant.")
    public ResponseEntity<ApiResponse<SaccoConfigEntity>> createSaccoConfig(@Valid @RequestBody SaccoConfigRequest request) {
        return saveOrUpdateConfig(request);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('ROLE_MANAGE')")
    @Operation(summary = "Update SACCO Code", description = "Updates the SACCO identification code for account generation.")
    public ResponseEntity<ApiResponse<SaccoConfigEntity>> updateSaccoConfig(@Valid @RequestBody SaccoConfigRequest request) {
        return saveOrUpdateConfig(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SACCO_ADMIN', 'ADMIN') or hasAuthority('ACCOUNT_VIEW')")
    @Operation(summary = "Get SACCO Code Configuration", description = "Retrieves the active SACCO identification code for this tenant.")
    public ResponseEntity<ApiResponse<SaccoConfigEntity>> getSaccoConfig() {
        SaccoConfigEntity config = saccoConfigRepository.findFirstByOrderByCreatedAtAsc()
                .orElseGet(() -> new SaccoConfigEntity(null, "0001", "Default SACCO", "0001"));
        return ResponseEntity.ok(ApiResponse.success(config, "SACCO code configuration retrieved successfully."));
    }

    private ResponseEntity<ApiResponse<SaccoConfigEntity>> saveOrUpdateConfig(SaccoConfigRequest request) {
        SaccoConfigEntity config = saccoConfigRepository.findFirstByOrderByCreatedAtAsc()
                .orElseGet(SaccoConfigEntity::new);

        config.setSaccoCode(request.saccoCode().trim());
        if (request.saccoName() != null && !request.saccoName().isBlank()) {
            config.setSaccoName(request.saccoName().trim());
        }
        if (config.getBranchCode() == null) {
            config.setBranchCode("0001");
        }

        SaccoConfigEntity saved = saccoConfigRepository.save(config);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(saved, "SACCO code configured successfully. Code: " + saved.getSaccoCode()));
    }
}
