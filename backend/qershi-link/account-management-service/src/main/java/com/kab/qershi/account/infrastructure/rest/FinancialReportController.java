package com.kab.qershi.account.infrastructure.rest;

import com.kab.qershi.account.application.usecase.FinancialReportService;
import com.kab.qershi.account.infrastructure.rest.dto.BalanceSheetReportDto;
import com.kab.qershi.account.infrastructure.rest.dto.ProfitLossReportDto;
import com.kab.qershi.account.infrastructure.rest.dto.TrialBalanceReportDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * REST API Controller for Core Banking Financial Statements and Regulatory Reports.
 * Generates dynamic Trial Balance, Balance Sheet, and Profit & Loss (Income Statement) reports.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/accounting/reports")
@Tag(name = "Financial Accounting Reports", description = "Endpoints for Trial Balance, Balance Sheet, and Profit & Loss statements")
public class FinancialReportController {

    private final FinancialReportService reportService;

    public FinancialReportController(FinancialReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/trial-balance")
    @PreAuthorize("hasAnyAuthority('FINANCIAL_REPORT_VIEW', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @Operation(summary = "Generate Trial Balance", description = "Aggregates all General Ledger debit and credit balances and verifies accounting equilibrium.")
    public ResponseEntity<TrialBalanceReportDto> getTrialBalance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        TrialBalanceReportDto report = reportService.generateTrialBalance(asOfDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/balance-sheet")
    @PreAuthorize("hasAnyAuthority('FINANCIAL_REPORT_VIEW', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @Operation(summary = "Generate Balance Sheet", description = "Generates the institutional Balance Sheet evaluating Assets = Liabilities + Member Equity.")
    public ResponseEntity<BalanceSheetReportDto> getBalanceSheet(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        BalanceSheetReportDto report = reportService.generateBalanceSheet(asOfDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/profit-loss")
    @PreAuthorize("hasAnyAuthority('FINANCIAL_REPORT_VIEW', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @Operation(summary = "Generate Profit & Loss (Income Statement)", description = "Computes Operating Revenue minus Operating Expenses to evaluate Net Surplus over a period.")
    public ResponseEntity<ProfitLossReportDto> getProfitLoss(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ProfitLossReportDto report = reportService.generateProfitLoss(startDate, endDate);
        return ResponseEntity.ok(report);
    }
}
