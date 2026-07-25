package com.kab.qershi.auth.infrastructure.rest;

import com.kab.qershi.auth.infrastructure.persistence.SpringDataSaccoRepository;
import com.kab.qershi.auth.infrastructure.persistence.SaccoEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for executing administrative lookups and lifecycle edits on workspace records.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/saccos")
@Tag(name = "SACCO Registry Management", description = "Allows platform administrative teams to monitor and manage tenant configurations")
public class SaccoManagementController {

    private final SpringDataSaccoRepository saccoRepository;

    public SaccoManagementController(SpringDataSaccoRepository saccoRepository) {
        this.saccoRepository = saccoRepository;
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping
    @Operation(summary = "List all registered SACCO workspaces", description = "Returns a complete high-level metadata index of all ecosystem tenants.")
    public ResponseEntity<List<SaccoEntity>> getAllSaccos() {
        return ResponseEntity.ok(saccoRepository.findAll());
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "Fetch SACCO registry profile by ID")
    public ResponseEntity<SaccoEntity> getSaccoById(@PathVariable UUID id) {
        return saccoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}