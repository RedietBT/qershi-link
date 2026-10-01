package com.kab.qershi.pricing.infrastructure.rest;

import com.kab.qershi.common.dto.ApiResponse;
import com.kab.qershi.pricing.domain.model.FeeCalculationResult;
import com.kab.qershi.pricing.domain.model.Tariff;
import com.kab.qershi.pricing.domain.model.WithholdingTaxResult;
import com.kab.qershi.pricing.domain.ports.inbound.TariffCalculationUseCase;
import com.kab.qershi.pricing.domain.ports.inbound.TariffManagementUseCase;
import com.kab.qershi.pricing.domain.ports.inbound.TaxAssessmentUseCase;
import com.kab.qershi.pricing.infrastructure.rest.dto.TariffRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Managing Transaction Tariffs and Withholding Tax Logs in pricing-fee-service.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/tariffs")
@Tag(name = "Enterprise Pricing & Tariff Engine", description = "Endpoints for configuring transaction fees, live tariff simulation, and statutory 5% WHT audit records.")
@SecurityRequirement(name = "bearerAuth")
public class PricingTariffController {

    private final TariffCalculationUseCase tariffCalculationUseCase;
    private final TariffManagementUseCase tariffManagementUseCase;
    private final TaxAssessmentUseCase taxAssessmentUseCase;

    public PricingTariffController(TariffCalculationUseCase tariffCalculationUseCase,
                                   TariffManagementUseCase tariffManagementUseCase,
                                   TaxAssessmentUseCase taxAssessmentUseCase) {
        this.tariffCalculationUseCase = tariffCalculationUseCase;
        this.tariffManagementUseCase = tariffManagementUseCase;
        this.taxAssessmentUseCase = taxAssessmentUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER', 'BRANCH_MANAGER', 'AUDITOR') or hasAuthority('ACCOUNT_VIEW')")
    @Operation(summary = "List All Tariffs", description = "Retrieves all configured transaction tariff schedules.")
    public ResponseEntity<ApiResponse<List<Tariff>>> listTariffs() {
        List<Tariff> tariffs = tariffManagementUseCase.listAllTariffs();
        return ResponseEntity.ok(ApiResponse.success(tariffs, "Retrieved " + tariffs.size() + " tariff rules."));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAuthority('TARIFF_MANAGE')")
    @Operation(summary = "Create Tariff Rule", description = "Creates a new transaction tariff rule with flat, percentage, or tiered slab fee logic.")
    public ResponseEntity<ApiResponse<Tariff>> createTariff(@Valid @RequestBody TariffRequest request) {
        List<com.kab.qershi.pricing.domain.model.TariffSlab> slabs = mapSlabs(request.getSlabs(), null);
        Tariff tariff = new Tariff(
                null,
                request.getTariffCode(),
                request.getTariffName(),
                request.getTransactionType(),
                request.getFeeType(),
                request.getFeeValue(),
                request.getMinFee(),
                request.getMaxFee(),
                request.getFeeGlCode() != null ? request.getFeeGlCode() : "4020",
                request.getCurrency() != null ? request.getCurrency() : "ETB",
                request.getIsActive() != null ? request.getIsActive() : true,
                request.getDescription(),
                slabs
        );
        Tariff saved = tariffManagementUseCase.createTariff(tariff);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(saved, "Tariff rule " + saved.getTariffCode() + " created successfully."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAuthority('TARIFF_MANAGE')")
    @Operation(summary = "Update Tariff Rule", description = "Updates an existing tariff configuration.")
    public ResponseEntity<ApiResponse<Tariff>> updateTariff(@PathVariable UUID id,
                                                            @Valid @RequestBody TariffRequest request) {
        List<com.kab.qershi.pricing.domain.model.TariffSlab> slabs = mapSlabs(request.getSlabs(), id);
        Tariff tariff = new Tariff(
                id,
                request.getTariffCode(),
                request.getTariffName(),
                request.getTransactionType(),
                request.getFeeType(),
                request.getFeeValue(),
                request.getMinFee(),
                request.getMaxFee(),
                request.getFeeGlCode() != null ? request.getFeeGlCode() : "4020",
                request.getCurrency() != null ? request.getCurrency() : "ETB",
                request.getIsActive() != null ? request.getIsActive() : true,
                request.getDescription(),
                slabs
        );
        Tariff updated = tariffManagementUseCase.updateTariff(id, tariff);
        return ResponseEntity.ok(ApiResponse.success(updated, "Tariff rule updated successfully."));
    }

    private List<com.kab.qershi.pricing.domain.model.TariffSlab> mapSlabs(List<com.kab.qershi.pricing.infrastructure.rest.dto.TariffSlabRequest> requests, UUID tariffId) {
        if (requests == null || requests.isEmpty()) return new java.util.ArrayList<>();
        return requests.stream().map(req -> new com.kab.qershi.pricing.domain.model.TariffSlab(
                null,
                tariffId,
                req.getSlabOrder(),
                req.getFromAmount(),
                req.getToAmount(),
                req.getFeeType(),
                req.getFeeValue(),
                req.getMinFee(),
                req.getMaxFee()
        )).collect(java.util.stream.Collectors.toList());
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAuthority('TARIFF_MANAGE')")
    @Operation(summary = "Toggle Tariff Active Status", description = "Enables or disables an existing tariff rule.")
    public ResponseEntity<ApiResponse<Tariff>> toggleTariff(@PathVariable UUID id,
                                                            @RequestParam boolean active) {
        Tariff updated = tariffManagementUseCase.toggleTariffStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success(updated, "Tariff status updated to " + (active ? "ACTIVE" : "INACTIVE")));
    }

    @GetMapping("/calculate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER', 'BRANCH_MANAGER', 'AUDITOR') or hasAuthority('ACCOUNT_VIEW')")
    @Operation(summary = "Live Tariff Calculation Simulator", description = "Simulates fee deduction for a given transaction type and amount.")
    public ResponseEntity<ApiResponse<FeeCalculationResult>> calculateFee(
            @RequestParam String transactionType,
            @RequestParam BigDecimal amount) {
        FeeCalculationResult calc = tariffCalculationUseCase.calculateFee(transactionType, amount);
        return ResponseEntity.ok(ApiResponse.success(calc, "Fee calculated successfully."));
    }

    @GetMapping("/tax-logs")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'AUDITOR', 'BRANCH_MANAGER') or hasAuthority('ACCOUNT_VIEW')")
    @Operation(summary = "List Statutory WHT Tax Deduction Logs", description = "Retrieves audit records of 5% withholding tax withheld during monthly interest capitalizations.")
    public ResponseEntity<ApiResponse<List<WithholdingTaxResult>>> listTaxLogs(
            @RequestParam(required = false) String accountNo) {
        List<WithholdingTaxResult> logs = taxAssessmentUseCase.listTaxLogs(accountNo);
        return ResponseEntity.ok(ApiResponse.success(logs, "Retrieved " + logs.size() + " tax deduction records."));
    }

    @GetMapping("/tax-logs/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'AUDITOR') or hasAuthority('ACCOUNT_VIEW')")
    @Operation(summary = "Tax Withheld Cumulative Summary", description = "Calculates total statutory 5% WHT withheld within a date range.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTaxSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        BigDecimal totalTax = taxAssessmentUseCase.sumTaxWithheldBetween(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of(
                        "startDate", startDate,
                        "endDate", endDate,
                        "totalTaxWithheld", totalTax,
                        "taxGlCode", "2091",
                        "statutoryRatePct", 5.00
                ),
                "Tax summary calculated."
        ));
    }
}
