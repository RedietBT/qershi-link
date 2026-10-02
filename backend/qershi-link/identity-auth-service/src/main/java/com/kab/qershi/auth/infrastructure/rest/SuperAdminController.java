package com.kab.qershi.auth.infrastructure.rest;

import com.kab.qershi.auth.domain.ports.inbound.SuperAdminUseCase;
import com.kab.qershi.auth.infrastructure.rest.dto.SuperAdminRegistrationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller driving SuperAdminUseCase.
 *
 * @author KAB Digital Solution PLC
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/v1/platform")
@Tag(name = "Platform Administration", description = "Endpoints for global system management")
public class SuperAdminController {

    private final SuperAdminUseCase superAdminUseCase;

    public SuperAdminController(SuperAdminUseCase superAdminUseCase) {
        this.superAdminUseCase = superAdminUseCase;
    }

    @PostMapping("/register-admin")
    @Operation(summary = "Register Super Admin", description = "Creates a new system-wide administrative account.")
    public ResponseEntity<String> registerSuperAdmin(@Valid @RequestBody SuperAdminRegistrationRequest request) {
        superAdminUseCase.registerSuperAdmin(request.msisdn());
        return ResponseEntity.status(201).body("Super Admin registered successfully");
    }
}