package com.kab.qershi.auth.infrastructure.rest;

import com.kab.qershi.auth.application.usecase.SuperAdminService;
import com.kab.qershi.auth.infrastructure.rest.dto.SuperAdminRegistrationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/platform")
@RequiredArgsConstructor
@Tag(name = "Platform Administration", description = "Endpoints for global system management")
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    @PostMapping("/register-admin")
    @Operation(summary = "Register Super Admin", description = "Creates a new system-wide administrative account.")
    public ResponseEntity<String> registerSuperAdmin(@Valid @RequestBody SuperAdminRegistrationRequest request) {
        superAdminService.registerSuperAdmin(request);
        return ResponseEntity.status(201).body("Super Admin registered successfully");
    }
}