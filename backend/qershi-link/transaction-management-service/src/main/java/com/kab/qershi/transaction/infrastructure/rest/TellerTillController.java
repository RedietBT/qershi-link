package com.kab.qershi.transaction.infrastructure.rest;

import com.kab.qershi.common.dto.ApiResponse;
import com.kab.qershi.transaction.application.usecase.TellerTillService;
import com.kab.qershi.transaction.infrastructure.persistence.TellerTillEntity;
import com.kab.qershi.transaction.infrastructure.persistence.TillCashReconciliationEntity;
import com.kab.qershi.transaction.infrastructure.rest.dto.AssignTillRequest;
import com.kab.qershi.transaction.infrastructure.rest.dto.CloseTillRequest;
import com.kab.qershi.transaction.infrastructure.rest.dto.OpenTillRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Teller Cash Drawer (Till) lifecycle, daily balancing, and banknote reconciliation.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/transactions/tills")
@Tag(name = "Teller Cash Drawer (Till) Operations", description = "Endpoints for opening, live balancing, physical cash counting, and closing teller tills.")
@SecurityRequirement(name = "bearerAuth")
public class TellerTillController {

    private final TellerTillService tillService;

    public TellerTillController(TellerTillService tillService) {
        this.tillService = tillService;
    }

    @GetMapping("/my-till")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER', 'BRANCH_MANAGER') or hasAuthority('TELLER_TILL_VIEW')")
    @Operation(summary = "Get Current Teller Drawer", description = "Retrieves live cash balance and status of the current teller's cash drawer.")
    public ResponseEntity<ApiResponse<TellerTillEntity>> getMyTill() {
        UUID tellerUserId = extractCurrentUserId();
        TellerTillEntity till = tillService.getTillByTellerUserId(tellerUserId);
        return ResponseEntity.ok(ApiResponse.success(till, "Current teller drawer retrieved successfully."));
    }

    @PostMapping("/open")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER') or hasAuthority('TELLER_TILL_MANAGE')")
    @Operation(summary = "Open Teller Drawer", description = "Opens the teller till for business operations with initial opening vault cash.")
    public ResponseEntity<ApiResponse<TellerTillEntity>> openTill(@Valid @RequestBody OpenTillRequest request) {
        UUID tellerUserId = extractCurrentUserId();
        TellerTillEntity till = tillService.openTill(tellerUserId, request.openingCash(), null, "001");
        return ResponseEntity.ok(ApiResponse.success(till, "Teller drawer '" + till.getTillName() + "' opened successfully with ETB " + request.openingCash()));
    }

    @PostMapping("/close")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER') or hasAuthority('TELLER_TILL_MANAGE')")
    @Operation(summary = "Close and Reconcile Teller Drawer", description = "Closes drawer, submits physical banknote denomination counts, calculates variance, and locks till.")
    public ResponseEntity<ApiResponse<TillCashReconciliationEntity>> closeTill(@Valid @RequestBody CloseTillRequest request) {
        UUID tellerUserId = extractCurrentUserId();
        TillCashReconciliationEntity reconciliation = tillService.closeAndReconcileTill(tellerUserId, request);
        return ResponseEntity.ok(ApiResponse.success(reconciliation, "Drawer closed and reconciled successfully. Cash variance: ETB " + reconciliation.getCashVariance()));
    }

    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'BRANCH_MANAGER') or hasAuthority('TELLER_TILL_MANAGE')")
    @Operation(summary = "Assign or Configure Till", description = "Configures a dedicated cash drawer for a branch teller with ceiling limits and GL codes.")
    public ResponseEntity<ApiResponse<TellerTillEntity>> assignTill(@Valid @RequestBody AssignTillRequest request) {
        TellerTillEntity till = tillService.assignTill(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(till, "Till assigned successfully to teller user: " + request.tellerUserId()));
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'BRANCH_MANAGER', 'AUDITOR') or hasAuthority('TELLER_TILL_VIEW')")
    @Operation(summary = "List Branch Tills", description = "Retrieves all teller drawers operating within a specific branch.")
    public ResponseEntity<ApiResponse<List<TellerTillEntity>>> getTillsByBranch(@PathVariable UUID branchId) {
        List<TellerTillEntity> tills = tillService.getTillsByBranch(branchId);
        return ResponseEntity.ok(ApiResponse.success(tills, "Retrieved " + tills.size() + " tills for branch: " + branchId));
    }

    @GetMapping("/my-reconciliations")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER', 'BRANCH_MANAGER') or hasAuthority('TELLER_TILL_VIEW')")
    @Operation(summary = "My Cash Reconciliations", description = "Retrieves historical physical cash reconciliation sheets for the authenticated teller.")
    public ResponseEntity<ApiResponse<List<TillCashReconciliationEntity>>> getMyReconciliations() {
        UUID tellerUserId = extractCurrentUserId();
        List<TillCashReconciliationEntity> recs = tillService.getReconciliationsByTeller(tellerUserId);
        return ResponseEntity.ok(ApiResponse.success(recs, "Retrieved " + recs.size() + " reconciliation records."));
    }

    @GetMapping("/{tillId}/reconciliations")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'BRANCH_MANAGER', 'AUDITOR') or hasAuthority('TELLER_TILL_VIEW')")
    @Operation(summary = "Get Till Reconciliations", description = "Retrieves historical cash reconciliation sheets for a specific drawer ID.")
    public ResponseEntity<ApiResponse<List<TillCashReconciliationEntity>>> getTillReconciliations(@PathVariable UUID tillId) {
        List<TillCashReconciliationEntity> recs = tillService.getReconciliationsByTillId(tillId);
        return ResponseEntity.ok(ApiResponse.success(recs, "Retrieved " + recs.size() + " reconciliation records."));
    }

    private UUID extractCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            if (auth.getPrincipal() instanceof UUID uuid) {
                return uuid;
            }
            if (auth.getDetails() != null) {
                try {
                    return UUID.fromString(auth.getDetails().toString());
                } catch (Exception ignored) {}
            }
            try {
                return UUID.fromString(auth.getName());
            } catch (Exception ignored) {}
        }
        throw new AccessDeniedException("Teller operator identity could not be resolved from authentication token.");
    }
}
