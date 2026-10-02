package com.kab.qershi.account.infrastructure.rest;

import com.kab.qershi.account.domain.model.Branch;
import com.kab.qershi.account.domain.ports.inbound.BranchUseCase;
import com.kab.qershi.account.infrastructure.rest.dto.CreateBranchRequest;
import com.kab.qershi.account.infrastructure.rest.dto.UpdateBranchRequest;
import com.kab.qershi.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for managing SACCO Branches and operational hierarchies.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/accounts/branches")
@Tag(name = "Branch Management", description = "Endpoints for administering SACCO branch network, vault codes, and limits.")
@SecurityRequirement(name = "bearerAuth")
public class BranchController {

    private final BranchUseCase branchUseCase;

    public BranchController(BranchUseCase branchUseCase) {
        this.branchUseCase = branchUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAuthority('BRANCH_MANAGE')")
    @Operation(summary = "Create SACCO Branch", description = "Onboards a new physical or digital branch with dedicated vault GL code and lending limits.")
    public ResponseEntity<ApiResponse<Branch>> createBranch(@Valid @RequestBody CreateBranchRequest request) {
        Branch created = branchUseCase.createBranch(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Branch '" + created.getBranchName() + "' created successfully with code " + created.getBranchCode()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'BRANCH_MANAGER', 'TELLER', 'AUDITOR') or hasAnyAuthority('BRANCH_VIEW', 'ACCOUNT_VIEW')")
    @Operation(summary = "List Branches", description = "Retrieves all branches within the SACCO tenant, optionally filtered by status.")
    public ResponseEntity<ApiResponse<List<Branch>>> getAllBranches(@RequestParam(required = false) String status) {
        List<Branch> branches = branchUseCase.getAllBranches(status);
        return ResponseEntity.ok(ApiResponse.success(branches, "Retrieved " + branches.size() + " branches successfully."));
    }

    @GetMapping("/{branchId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'BRANCH_MANAGER', 'TELLER', 'AUDITOR') or hasAnyAuthority('BRANCH_VIEW', 'ACCOUNT_VIEW')")
    @Operation(summary = "Get Branch by ID", description = "Retrieves specific branch details.")
    public ResponseEntity<ApiResponse<Branch>> getBranchById(@PathVariable UUID branchId) {
        Branch branch = branchUseCase.getBranchById(branchId);
        return ResponseEntity.ok(ApiResponse.success(branch, "Branch details retrieved successfully."));
    }

    @GetMapping("/code/{branchCode}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'BRANCH_MANAGER', 'TELLER', 'AUDITOR') or hasAnyAuthority('BRANCH_VIEW', 'ACCOUNT_VIEW')")
    @Operation(summary = "Get Branch by Code", description = "Retrieves branch details by unique code (e.g. 001).")
    public ResponseEntity<ApiResponse<Branch>> getBranchByCode(@PathVariable String branchCode) {
        Branch branch = branchUseCase.getBranchByCode(branchCode);
        return ResponseEntity.ok(ApiResponse.success(branch, "Branch details retrieved successfully."));
    }

    @PutMapping("/{branchId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAuthority('BRANCH_MANAGE')")
    @Operation(summary = "Update Branch", description = "Updates branch details, address, manager, vault GL code, or limits.")
    public ResponseEntity<ApiResponse<Branch>> updateBranch(@PathVariable UUID branchId,
                                                            @Valid @RequestBody UpdateBranchRequest request) {
        Branch updated = branchUseCase.updateBranch(branchId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Branch updated successfully."));
    }

    @PatchMapping("/{branchId}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAuthority('BRANCH_MANAGE')")
    @Operation(summary = "Update Branch Status", description = "Activates or deactivates an operational branch.")
    public ResponseEntity<ApiResponse<Branch>> updateStatus(@PathVariable UUID branchId,
                                                            @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status field is required (ACTIVE or INACTIVE).");
        }
        Branch updated = branchUseCase.updateBranchStatus(branchId, status);
        return ResponseEntity.ok(ApiResponse.success(updated, "Branch status updated to " + updated.getStatus()));
    }
}
