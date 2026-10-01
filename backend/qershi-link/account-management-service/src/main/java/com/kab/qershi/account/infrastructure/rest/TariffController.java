package com.kab.qershi.account.infrastructure.rest;

import com.kab.qershi.account.application.usecase.TariffEngineService;
import com.kab.qershi.account.application.usecase.TariffEngineService.FeeCalculation;
import com.kab.qershi.account.infrastructure.persistence.InterestTaxDeductionLogEntity;
import com.kab.qershi.account.infrastructure.persistence.SpringDataInterestTaxDeductionLogRepository;
import com.kab.qershi.account.infrastructure.persistence.TariffEntity;
import com.kab.qershi.account.infrastructure.rest.dto.TariffRequest;
import com.kab.qershi.common.dto.ApiResponse;
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
 * REST Controller for Managing Transaction Tariffs and Withholding Tax Logs.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/tariffs")
@Tag(name = "Fee & Tariff Engine", description = "Endpoints for configuring transaction fees, live tariff simulation, and 5% WHT audit records.")
@SecurityRequirement(name = "bearerAuth")
public class TariffController {

    private final TariffEngineService tariffEngineService;
    private final SpringDataInterestTaxDeductionLogRepository taxLogRepository;

    public TariffController(TariffEngineService tariffEngineService,
                            SpringDataInterestTaxDeductionLogRepository taxLogRepository) {
        this.tariffEngineService = tariffEngineService;
        this.taxLogRepository = taxLogRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER', 'BRANCH_MANAGER', 'AUDITOR') or hasAuthority('ACCOUNT_VIEW')")
    @Operation(summary = "List All Tariffs", description = "Retrieves all configured transaction tariff schedules.")
    public ResponseEntity<ApiResponse<List<TariffEntity>>> listTariffs() {
        List<TariffEntity> tariffs = tariffEngineService.listTariffs();
        return ResponseEntity.ok(ApiResponse.success(tariffs, "Retrieved " + tariffs.size() + " tariff rules."));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAuthority('TARIFF_MANAGE')")
    @Operation(summary = "Create Tariff Rule", description = "Creates a new transaction tariff rule with flat or percentage fee logic.")
    public ResponseEntity<ApiResponse<TariffEntity>> createTariff(@Valid @RequestBody TariffRequest request) {
        TariffEntity entity = new TariffEntity(
                request.getTariffCode(),
                request.getTariffName(),
                request.getTransactionType(),
                request.getFeeType(),
                request.getFeeValue(),
                request.getMinFee(),
                request.getMaxFee(),
                request.getFeeGlCode() != null ? request.getFeeGlCode() : "4020",
                request.getDescription()
        );
        if (request.getIsActive() != null) {
            entity.setActive(request.getIsActive());
        }
        if (request.getCurrency() != null) {
            entity.setCurrency(request.getCurrency());
        }
        TariffEntity saved = tariffEngineService.createTariff(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(saved, "Tariff rule " + saved.getTariffCode() + " created successfully."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAuthority('TARIFF_MANAGE')")
    @Operation(summary = "Update Tariff Rule", description = "Updates an existing tariff configuration.")
    public ResponseEntity<ApiResponse<TariffEntity>> updateTariff(@PathVariable UUID id,
                                                                 @Valid @RequestBody TariffRequest request) {
        TariffEntity entity = new TariffEntity(
                request.getTariffCode(),
                request.getTariffName(),
                request.getTransactionType(),
                request.getFeeType(),
                request.getFeeValue(),
                request.getMinFee(),
                request.getMaxFee(),
                request.getFeeGlCode() != null ? request.getFeeGlCode() : "4020",
                request.getDescription()
        );
        if (request.getIsActive() != null) {
            entity.setActive(request.getIsActive());
        }
        TariffEntity updated = tariffEngineService.updateTariff(id, entity);
        return ResponseEntity.ok(ApiResponse.success(updated, "Tariff rule updated successfully."));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN') or hasAuthority('TARIFF_MANAGE')")
    @Operation(summary = "Toggle Tariff Active Status", description = "Enables or disables an existing tariff rule.")
    public ResponseEntity<ApiResponse<TariffEntity>> toggleTariff(@PathVariable UUID id,
                                                                 @RequestParam boolean active) {
        TariffEntity updated = tariffEngineService.toggleStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success(updated, "Tariff status updated to " + (active ? "ACTIVE" : "INACTIVE")));
    }

    @GetMapping("/calculate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER', 'BRANCH_MANAGER', 'AUDITOR') or hasAuthority('ACCOUNT_VIEW')")
    @Operation(summary = "Live Tariff Calculation Simulator", description = "Simulates fee deduction for a given transaction type and amount.")
    public ResponseEntity<ApiResponse<FeeCalculation>> calculateFee(
            @RequestParam String transactionType,
            @RequestParam BigDecimal amount) {
        FeeCalculation calc = tariffEngineService.calculateFee(transactionType, amount);
        return ResponseEntity.ok(ApiResponse.success(calc, "Fee calculated successfully."));
    }

    @GetMapping("/tax-logs")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'AUDITOR', 'BRANCH_MANAGER') or hasAuthority('ACCOUNT_VIEW')")
    @Operation(summary = "List Statutory WHT Tax Deduction Logs", description = "Retrieves audit records of 5% withholding tax withheld during monthly interest capitalizations.")
    public ResponseEntity<ApiResponse<List<InterestTaxDeductionLogEntity>>> listTaxLogs(
            @RequestParam(required = false) String accountNo) {
        List<InterestTaxDeductionLogEntity> logs;
        if (accountNo != null && !accountNo.isBlank()) {
            logs = taxLogRepository.findByAccountNo(accountNo.trim());
        } else {
            logs = taxLogRepository.findAll();
        }
        return ResponseEntity.ok(ApiResponse.success(logs, "Retrieved " + logs.size() + " tax deduction records."));
    }

    @GetMapping("/tax-logs/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'AUDITOR') or hasAuthority('ACCOUNT_VIEW')")
    @Operation(summary = "Tax Withheld Cumulative Summary", description = "Calculates total statutory 5% WHT withheld within a date range.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTaxSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        BigDecimal totalTax = taxLogRepository.sumTaxWithheldBetween(startDate, endDate);
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
