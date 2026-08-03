package com.kab.qershi.account.infrastructure.rest;

import com.kab.qershi.account.domain.model.Account;
import com.kab.qershi.account.domain.model.AccountLien;
import com.kab.qershi.account.domain.ports.inbound.LienManagementUseCase;
import com.kab.qershi.account.infrastructure.rest.dto.FreezeAccountRequest;
import com.kab.qershi.account.infrastructure.rest.dto.PlaceLienRequest;
import com.kab.qershi.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller exposing Lien Holds and Administrative Freeze controls.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Lien Holds & Freeze Controls", description = "Endpoints for placing partial balance holds and administrative account freezes.")
@SecurityRequirement(name = "bearerAuth")
public class LienController {

    private final LienManagementUseCase lienManagementUseCase;

    public LienController(LienManagementUseCase lienManagementUseCase) {
        this.lienManagementUseCase = lienManagementUseCase;
    }

    @PostMapping("/{accountNo}/liens")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') or hasAuthority('LIEN_CREATE')")
    @Operation(summary = "Place Partial Lien Hold", description = "Blocks a specific monetary amount on an account (e.g. loan collateral or guarantee block).")
    public ResponseEntity<ApiResponse<AccountLien>> placeLien(@PathVariable String accountNo,
                                                             @Valid @RequestBody PlaceLienRequest request,
                                                             Authentication authentication) {
        UUID officerUserId = parseUserId(authentication);
        AccountLien lien = lienManagementUseCase.placeLien(
                accountNo,
                request.getAmount(),
                request.getReason(),
                request.getReferenceNo(),
                officerUserId
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(lien, "Lien hold of " + request.getAmount() + " placed successfully on account " + accountNo));
    }

    @PutMapping("/liens/{lienId}/release")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') or hasAuthority('LIEN_RELEASE')")
    @Operation(summary = "Release Lien Hold", description = "Releases an active monetary lien hold and restores available balance.")
    public ResponseEntity<ApiResponse<AccountLien>> releaseLien(@PathVariable UUID lienId, Authentication authentication) {
        UUID officerUserId = parseUserId(authentication);
        AccountLien released = lienManagementUseCase.releaseLien(lienId, officerUserId);
        return ResponseEntity.ok(ApiResponse.success(released, "Lien hold " + lienId + " released successfully."));
    }

    @PutMapping("/{accountNo}/freeze")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') or hasAuthority('ACCOUNT_FREEZE')")
    @Operation(summary = "Administrative Freeze / Unfreeze", description = "Sets freeze status on an account (NONE, DEBIT_FREEZE, CREDIT_FREEZE, FULL_FREEZE).")
    public ResponseEntity<ApiResponse<Account>> freezeAccount(@PathVariable String accountNo,
                                                              @Valid @RequestBody FreezeAccountRequest request,
                                                              Authentication authentication) {
        UUID officerUserId = parseUserId(authentication);
        Account frozen = lienManagementUseCase.freezeAccount(accountNo, request.getFreezeStatus(), officerUserId, request.getReason());
        return ResponseEntity.ok(ApiResponse.success(frozen, "Account " + accountNo + " freeze status updated to: " + frozen.getFreezeStatus()));
    }

    @GetMapping("/{accountNo}/liens")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') or hasAuthority('LIEN_VIEW')")
    @Operation(summary = "Get Account Liens", description = "Retrieves active monetary lien holds for a specific account.")
    public ResponseEntity<ApiResponse<List<AccountLien>>> getActiveLiensForAccount(@PathVariable String accountNo) {
        List<AccountLien> liens = lienManagementUseCase.getActiveLiensForAccount(accountNo);
        return ResponseEntity.ok(ApiResponse.success(liens, "Retrieved " + liens.size() + " active lien holds for account " + accountNo));
    }

    private UUID parseUserId(Authentication auth) {
        if (auth == null) return UUID.randomUUID();
        Object principal = auth.getPrincipal();
        if (principal instanceof UUID) {
            return (UUID) principal;
        }
        try {
            return UUID.fromString(principal.toString());
        } catch (Exception ex) {
            return UUID.randomUUID();
        }
    }
}
