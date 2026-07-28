package com.kab.qershi.profile.infrastructure.rest;

import com.kab.qershi.profile.domain.model.NextOfKin;
import com.kab.qershi.profile.domain.ports.inbound.NextOfKinUseCase;
import com.kab.qershi.profile.infrastructure.rest.dto.AddNextOfKinRequest;
import com.kab.qershi.profile.infrastructure.rest.dto.ApiResponse;
import com.kab.qershi.profile.infrastructure.rest.dto.NextOfKinResponse;
import com.kab.qershi.profile.infrastructure.rest.dto.UpdateNextOfKinRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST API Controller for Nominated Beneficiaries (Next of Kin) and Payout Percentage Allocations.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/kin")
@Tag(name = "Next of Kin", description = "Nominated Beneficiaries Payout Allocation Management APIs")
public class NextOfKinController {

    private final NextOfKinUseCase nextOfKinUseCase;

    public NextOfKinController(NextOfKinUseCase nextOfKinUseCase) {
        this.nextOfKinUseCase = nextOfKinUseCase;
    }

    @PostMapping("/{userId}")
    @Operation(summary = "Add Next of Kin", description = "Adds a nominated beneficiary for a member. Enforces total percentage allocation limit <= 100.00%.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Next of Kin beneficiary added successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Total percentage allocation exceeds 100.00% or alphabetic name constraint failure", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cannot add Next of Kin. Member profile not found for user ID", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<NextOfKinResponse>> addNextOfKin(
            @Parameter(description = "UUID of the member user", required = true) @PathVariable UUID userId,
            @Valid @RequestBody AddNextOfKinRequest request) {
        NextOfKin kin = nextOfKinUseCase.addNextOfKin(
                userId,
                request.getFullName(),
                request.getRelationship(),
                request.getPrimaryPhone(),
                request.getIdNumber(),
                request.getPhysicalAddress(),
                request.getAllocationPercentage()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Next of Kin beneficiary added successfully", NextOfKinResponse.fromDomain(kin)));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "List Member Next of Kin", description = "Returns all nominated beneficiaries registered for a member.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Beneficiary list retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<NextOfKinResponse>>> getNextOfKinByUserId(
            @Parameter(description = "UUID of the member user", required = true) @PathVariable UUID userId) {
        List<NextOfKin> list = nextOfKinUseCase.getNextOfKinByUserId(userId);
        List<NextOfKinResponse> responseList = list.stream()
                .map(NextOfKinResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(responseList));
    }

    @PutMapping("/{kinId}")
    @Operation(summary = "Update Next of Kin", description = "Updates nominated beneficiary details and allocation percentage.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Next of Kin beneficiary updated successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Total percentage allocation exceeds 100.00% or invalid payload", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Next of Kin record not found for ID", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<NextOfKinResponse>> updateNextOfKin(
            @Parameter(description = "UUID of the kin entry", required = true) @PathVariable UUID kinId,
            @Valid @RequestBody UpdateNextOfKinRequest request) {
        NextOfKin updated = nextOfKinUseCase.updateNextOfKin(
                kinId,
                request.getFullName(),
                request.getRelationship(),
                request.getPrimaryPhone(),
                request.getIdNumber(),
                request.getPhysicalAddress(),
                request.getAllocationPercentage()
        );
        return ResponseEntity.ok(ApiResponse.ok("Next of Kin beneficiary updated successfully", NextOfKinResponse.fromDomain(updated)));
    }

    @DeleteMapping("/{kinId}")
    @Operation(summary = "Delete Next of Kin", description = "Removes a nominated beneficiary entry.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Next of Kin beneficiary removed successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Next of Kin record not found for ID", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<Void>> deleteNextOfKin(
            @Parameter(description = "UUID of the kin entry", required = true) @PathVariable UUID kinId) {
        nextOfKinUseCase.deleteNextOfKin(kinId);
        return ResponseEntity.ok(ApiResponse.ok("Next of Kin beneficiary removed successfully", null));
    }
}
