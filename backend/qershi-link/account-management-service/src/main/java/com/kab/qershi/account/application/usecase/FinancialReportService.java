package com.kab.qershi.account.application.usecase;

import com.kab.qershi.account.domain.model.ChartOfAccount;
import com.kab.qershi.account.domain.model.GlAccountType;
import com.kab.qershi.account.domain.model.SystemBusinessDate;
import com.kab.qershi.account.domain.ports.outbound.ChartOfAccountRepositoryPort;
import com.kab.qershi.account.domain.ports.outbound.SystemBusinessDateRepositoryPort;
import com.kab.qershi.account.infrastructure.rest.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Core Banking Financial Reporting Generation Engine.
 * Implements standard financial accounting aggregation for:
 * 1. Trial Balance (asserting Debit == Credit equilibrium across all GL accounts)
 * 2. Balance Sheet (verifying Assets == Liabilities + Equity)
 * 3. Profit & Loss Statement (computing Revenue - Expenses = Net Operating Surplus)
 *
 * Enforces strict temporal date bounds and input validation guards.
 *
 * @author KAB Digital Solution PLC
 * @version 1.2.0
 */
@Service
public class FinancialReportService {

    private static final Logger log = LoggerFactory.getLogger(FinancialReportService.class);
    private static final int MIN_YEAR = 2000;
    private static final int MAX_YEAR = 2100;

    private final ChartOfAccountRepositoryPort coaRepository;
    private final SystemBusinessDateRepositoryPort businessDateRepository;

    public FinancialReportService(ChartOfAccountRepositoryPort coaRepository,
                                  SystemBusinessDateRepositoryPort businessDateRepository) {
        this.coaRepository = coaRepository;
        this.businessDateRepository = businessDateRepository;
    }

    /**
     * Resolves and bounds-checks the effective financial date.
     */
    private LocalDate resolveAndValidateDate(LocalDate requestedDate) {
        LocalDate date = requestedDate;
        if (date == null) {
            date = businessDateRepository.findCurrentBusinessDate()
                    .map(SystemBusinessDate::getCurrentBusinessDate)
                    .orElseGet(LocalDate::now);
        }

        if (date.getYear() < MIN_YEAR || date.getYear() > MAX_YEAR) {
            throw new IllegalArgumentException(
                    "Date out of valid financial bounds. Year must be between " + MIN_YEAR + " and " + MAX_YEAR
            );
        }

        return date;
    }

    /**
     * Generates the Trial Balance report.
     * Maps each account's balance to its natural Debit or Credit column.
     * Verifies the fundamental double-entry rule: sum(Debits) == sum(Credits).
     */
    @Transactional(readOnly = true)
    public TrialBalanceReportDto generateTrialBalance(LocalDate asOfDate) {
        LocalDate effectiveDate = resolveAndValidateDate(asOfDate);
        List<ChartOfAccount> accounts = coaRepository.findAllOrderByGlCodeAsc();

        List<TrialBalanceLineDto> lines = new ArrayList<>();
        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (ChartOfAccount account : accounts) {
            BigDecimal bal = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
            BigDecimal debitAmount = BigDecimal.ZERO;
            BigDecimal creditAmount = BigDecimal.ZERO;

            if (account.getAccountType().isDebitNormal()) {
                if (bal.compareTo(BigDecimal.ZERO) >= 0) {
                    debitAmount = bal;
                } else {
                    creditAmount = bal.abs();
                }
            } else {
                if (bal.compareTo(BigDecimal.ZERO) >= 0) {
                    creditAmount = bal;
                } else {
                    debitAmount = bal.abs();
                }
            }

            lines.add(new TrialBalanceLineDto(
                    account.getGlCode(),
                    account.getAccountName(),
                    account.getAccountType(),
                    account.getParentGlCode(),
                    debitAmount,
                    creditAmount
            ));

            totalDebits = totalDebits.add(debitAmount);
            totalCredits = totalCredits.add(creditAmount);
        }

        BigDecimal variance = totalDebits.subtract(totalCredits).abs();
        boolean isBalanced = variance.compareTo(new BigDecimal("0.01")) < 0;

        return new TrialBalanceReportDto(
                effectiveDate,
                lines,
                totalDebits,
                totalCredits,
                isBalanced,
                variance
        );
    }

    /**
     * Generates the Balance Sheet financial statement.
     * Verifies the core banking equation: Assets = Liabilities + Equity + Current Period Surplus.
     */
    @Transactional(readOnly = true)
    public BalanceSheetReportDto generateBalanceSheet(LocalDate asOfDate) {
        LocalDate effectiveDate = resolveAndValidateDate(asOfDate);
        List<ChartOfAccount> accounts = coaRepository.findAllOrderByGlCodeAsc();

        List<ReportSectionLineDto> assetLines = new ArrayList<>();
        BigDecimal totalAssets = BigDecimal.ZERO;

        List<ReportSectionLineDto> liabilityLines = new ArrayList<>();
        BigDecimal totalLiabilities = BigDecimal.ZERO;

        List<ReportSectionLineDto> equityLines = new ArrayList<>();
        BigDecimal totalEquity = BigDecimal.ZERO;

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (ChartOfAccount account : accounts) {
            BigDecimal bal = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;

            switch (account.getAccountType()) {
                case ASSET -> {
                    assetLines.add(new ReportSectionLineDto(account.getGlCode(), account.getAccountName(), bal));
                    totalAssets = totalAssets.add(bal);
                }
                case LIABILITY -> {
                    liabilityLines.add(new ReportSectionLineDto(account.getGlCode(), account.getAccountName(), bal));
                    totalLiabilities = totalLiabilities.add(bal);
                }
                case EQUITY -> {
                    equityLines.add(new ReportSectionLineDto(account.getGlCode(), account.getAccountName(), bal));
                    totalEquity = totalEquity.add(bal);
                }
                case REVENUE -> totalRevenue = totalRevenue.add(bal);
                case EXPENSE -> totalExpenses = totalExpenses.add(bal);
            }
        }

        // Net Operating Surplus flows into Equity to balance the financial equation
        BigDecimal currentPeriodSurplus = totalRevenue.subtract(totalExpenses);
        BigDecimal totalLiabilitiesAndEquity = totalLiabilities.add(totalEquity).add(currentPeriodSurplus);

        BigDecimal variance = totalAssets.subtract(totalLiabilitiesAndEquity).abs();
        boolean isBalanced = variance.compareTo(new BigDecimal("0.01")) < 0;

        return new BalanceSheetReportDto(
                effectiveDate,
                assetLines,
                totalAssets,
                liabilityLines,
                totalLiabilities,
                equityLines,
                totalEquity,
                currentPeriodSurplus,
                totalLiabilitiesAndEquity,
                isBalanced,
                variance
        );
    }

    /**
     * Generates the Profit and Loss statement (Income Statement).
     * Net Surplus = Total Operating Income - Total Operating Expenses.
     */
    @Transactional(readOnly = true)
    public ProfitLossReportDto generateProfitLoss(LocalDate startDate, LocalDate endDate) {
        LocalDate effectiveStart = startDate != null
                ? resolveAndValidateDate(startDate)
                : resolveAndValidateDate(null).withDayOfMonth(1);
        LocalDate effectiveEnd = resolveAndValidateDate(endDate);

        if (effectiveStart.isAfter(effectiveEnd)) {
            throw new IllegalArgumentException(
                    "Invalid date range: Start date (" + effectiveStart + ") cannot be after end date (" + effectiveEnd + ")."
            );
        }

        List<ChartOfAccount> accounts = coaRepository.findAllOrderByGlCodeAsc();

        List<ReportSectionLineDto> incomeLines = new ArrayList<>();
        BigDecimal totalIncome = BigDecimal.ZERO;

        List<ReportSectionLineDto> expenseLines = new ArrayList<>();
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (ChartOfAccount account : accounts) {
            BigDecimal bal = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;

            if (account.getAccountType() == GlAccountType.REVENUE) {
                incomeLines.add(new ReportSectionLineDto(account.getGlCode(), account.getAccountName(), bal));
                totalIncome = totalIncome.add(bal);
            } else if (account.getAccountType() == GlAccountType.EXPENSE) {
                expenseLines.add(new ReportSectionLineDto(account.getGlCode(), account.getAccountName(), bal));
                totalExpenses = totalExpenses.add(bal);
            }
        }

        BigDecimal netSurplus = totalIncome.subtract(totalExpenses);
        String status = netSurplus.compareTo(BigDecimal.ZERO) >= 0 ? "NET_SURPLUS" : "NET_DEFICIT";

        return new ProfitLossReportDto(
                effectiveStart,
                effectiveEnd,
                incomeLines,
                totalIncome,
                expenseLines,
                totalExpenses,
                netSurplus,
                status
        );
    }
}
