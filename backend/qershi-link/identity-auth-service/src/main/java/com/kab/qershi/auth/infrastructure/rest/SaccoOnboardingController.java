package com.kab.qershi.auth.infrastructure.rest;

// 🛠️ FIXED: Pointed these imports to the correct inbound port package location
import com.kab.qershi.auth.domain.ports.inbound.SaccoOnboardingUseCase;
import com.kab.qershi.auth.domain.ports.inbound.SaccoOnboardingUseCase.OnboardCommand;
import com.kab.qershi.auth.domain.ports.inbound.SaccoOnboardingUseCase.OnboardResult;
import com.kab.qershi.auth.infrastructure.rest.dto.OnboardSaccoRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Inbound Edge Adapter exposing RESTful system administrative workspace orchestration handlers.
 * Translates structural registrations down to clean isolated transactional spaces.
 *
 * @author KAB Digital Solution PLC
 * @version 1.1.0
 */
@RestController
@RequestMapping("/api/v1/sacco")
@Tag(name = "SACCO Management Engine", description = "Administrative commands interface coordinating physical tenant isolation workspace configurations")
public class SaccoOnboardingController {

    private final SaccoOnboardingUseCase saccoOnboardingUseCase;

    /**
     * Constructs the onboarding endpoint handler mapping parameters to infrastructure registries.
     *
     * @param saccoOnboardingUseCase The core orchestrator managing physical space deployments.
     */
    public SaccoOnboardingController(SaccoOnboardingUseCase saccoOnboardingUseCase) {
        this.saccoOnboardingUseCase = saccoOnboardingUseCase;
    }

    /**
     * Section 1.1.1.4.1: Multi-Tenant Infrastructure Provisioning Endpoint
     * Onboards a new operational workspace instance and safely allocates private tables components.
     *
     * @param request The validated operational configuration boundaries criteria payload.
     * @return ResponseEntity Enclosing data descriptors confirming operational readiness states.
     */
    @PostMapping("/onboard")
    @Operation(
            summary = "Provision Isolated Workspace Infrastructure Domain",
            description = "Registers organizational metrics inside the shared database, and executes structural CREATE SCHEMA routines safely mapped via Zero-Orphan rules."
    )
    @ApiResponse(responseCode = "201", description = "Multi-tenant tracking structures and storage spaces initialized successfully.",
            content = @Content(schema = @Schema(implementation = OnboardResult.class)))
    @ApiResponse(responseCode = "400", description = "Structural schema input parameters fail validation constraints checking rules.")
    @ApiResponse(responseCode = "409", description = "Resource registration conflict: The legal corporate name or dynamic database schema key mapping collides with active setups.")
    @ApiResponse(responseCode = "500", description = "Critical environmental infrastructure exception caught during space allocations. Database rollback rules executed cleanly.")
    public ResponseEntity<OnboardResult> onboardSacco(@Valid @RequestBody OnboardSaccoRequest request) {

        // 🛠️ FIXED: Filled out all 7 arguments required by OnboardCommand using safe defaults for missing REST payload properties
        OnboardCommand command = new OnboardCommand(
                request.saccoName(),
                request.isUnion(),
                request.minShareRequirement(),
                request.adminMsisdn(),
                request.adminName(),
                request.region(),
                java.util.Map.of()
        );

        OnboardResult result = saccoOnboardingUseCase.onboardSacco(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}