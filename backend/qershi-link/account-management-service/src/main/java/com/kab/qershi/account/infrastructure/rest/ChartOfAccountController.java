package com.kab.qershi.account.infrastructure.rest;

import com.kab.qershi.account.application.usecase.ChartOfAccountService;
import com.kab.qershi.account.infrastructure.persistence.ChartOfAccountEntity;
import com.kab.qershi.account.infrastructure.rest.dto.ChartOfAccountNodeDto;
import com.kab.qershi.account.infrastructure.rest.dto.CreateChartOfAccountRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API Controller for Managing the General Ledger Chart of Accounts (COA).
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/accounting/coa")
@Tag(name = "Chart of Accounts Management", description = "Endpoints for hierarchical General Ledger structure and GL accounts")
public class ChartOfAccountController {

    private final ChartOfAccountService coaService;

    public ChartOfAccountController(ChartOfAccountService coaService) {
        this.coaService = coaService;
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAnyAuthority('COA_VIEW', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @Operation(summary = "Get Chart of Accounts Hierarchy Tree", description = "Returns the full nested hierarchical tree of General Ledger accounts with aggregated balances.")
    public ResponseEntity<List<ChartOfAccountNodeDto>> getCoaTree() {
        List<ChartOfAccountNodeDto> tree = coaService.getCoaTree();
        return ResponseEntity.ok(tree);
    }

    @GetMapping("/flat")
    @PreAuthorize("hasAnyAuthority('COA_VIEW', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @Operation(summary = "Get Flat Chart of Accounts List", description = "Returns all General Ledger accounts in flat order, useful for dropdown selectors.")
    public ResponseEntity<List<ChartOfAccountEntity>> getAllFlat() {
        List<ChartOfAccountEntity> list = coaService.getAllFlat();
        return ResponseEntity.ok(list);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('COA_MANAGE', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @Operation(summary = "Create General Ledger Account", description = "Creates a new custom GL account under an existing parent category or node.")
    public ResponseEntity<ChartOfAccountEntity> createAccount(@Valid @RequestBody CreateChartOfAccountRequest request) {
        ChartOfAccountEntity created = coaService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
