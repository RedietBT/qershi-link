package com.kab.qershi.auth.infrastructure.rest;

import com.kab.qershi.auth.domain.model.Sacco;
import com.kab.qershi.auth.domain.ports.inbound.SaccoManagementUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for executing administrative lookups and lifecycle edits on workspace records.
 * Injects inbound port SaccoManagementUseCase.
 *
 * @author KAB Digital Solution PLC
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/v1/saccos")
@Tag(name = "SACCO Registry Management", description = "Allows platform administrative teams to monitor and manage tenant configurations")
public class SaccoManagementController {

    private final SaccoManagementUseCase saccoManagementUseCase;

    public SaccoManagementController(SaccoManagementUseCase saccoManagementUseCase) {
        this.saccoManagementUseCase = saccoManagementUseCase;
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping
    @Operation(summary = "List all registered SACCO workspaces", description = "Returns a complete high-level metadata index of all ecosystem tenants.")
    public ResponseEntity<List<Sacco>> getAllSaccos() {
        return ResponseEntity.ok(saccoManagementUseCase.getAllSaccos());
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "Fetch SACCO registry profile by ID")
    public ResponseEntity<Sacco> getSaccoById(@PathVariable UUID id) {
        return saccoManagementUseCase.getSaccoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}