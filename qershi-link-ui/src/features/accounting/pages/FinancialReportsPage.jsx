import React, { useState, useEffect } from 'react';
import {
  FileText, Calendar, Download, Printer, RefreshCw, CheckCircle2,
  AlertTriangle, ArrowDownRight, ArrowUpRight, Scale, TrendingUp, TrendingDown,
  Layers, ChevronRight, ShieldCheck
} from 'lucide-react';
import { accountingApi } from '../api/accountingApi';
import { formatCurrency, formatNumber } from '../../../common/utils/currency';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS, ROLES } from '../../../common/constants/permissions';

/**
 * Balance Indicator Banner for Balance Sheet and Trial Balance
 */
const BalanceStatusBanner = ({ isBalanced, title, successMessage, difference, leftLabel, leftValue, rightLabel, rightValue }) => {
  if (isBalanced) {
    return (
      <div className="p-4 rounded-2xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-700 dark:text-emerald-400 flex items-center justify-between gap-4 animate-fadeIn">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-emerald-500/20 text-emerald-600 dark:text-emerald-400 flex items-center justify-center shrink-0">
            <CheckCircle2 className="w-5 h-5" />
          </div>
          <div>
            <h4 className="text-xs font-extrabold uppercase tracking-wide">
              {title || 'Accounting Equation Verified & Balanced'}
            </h4>
            <p className="text-[11px] opacity-90">
              {successMessage || `${leftLabel}: ${formatCurrency(leftValue)} = ${rightLabel}: ${formatCurrency(rightValue)}`}
            </p>
          </div>
        </div>
        <div className="hidden sm:flex items-center gap-3 text-right">
          <div className="border-r border-emerald-500/20 pr-3">
            <span className="text-[10px] block uppercase font-bold opacity-75">{leftLabel}</span>
            <span className="font-mono text-xs font-bold">{formatCurrency(leftValue)}</span>
          </div>
          <div>
            <span className="text-[10px] block uppercase font-bold opacity-75">{rightLabel}</span>
            <span className="font-mono text-xs font-bold">{formatCurrency(rightValue)}</span>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="p-4 rounded-2xl bg-rose-500/10 border border-rose-500/20 text-rose-700 dark:text-rose-400 flex items-center justify-between gap-4 animate-fadeIn">
      <div className="flex items-center gap-3">
        <div className="w-9 h-9 rounded-xl bg-rose-500/20 text-rose-600 dark:text-rose-400 flex items-center justify-center shrink-0">
          <AlertTriangle className="w-5 h-5" />
        </div>
        <div>
          <h4 className="text-xs font-extrabold uppercase tracking-wide">
            Accounting Disequilibrium Detected!
          </h4>
          <p className="text-[11px] opacity-90">
            {leftLabel} ({formatCurrency(leftValue)}) does not equal {rightLabel} ({formatCurrency(rightValue)}). Variance: {formatCurrency(difference)}
          </p>
        </div>
      </div>
      <div className="text-right">
        <span className="text-[10px] block uppercase font-bold text-rose-500">Net Variance</span>
        <span className="font-mono text-xs font-bold">{formatCurrency(difference)}</span>
      </div>
    </div>
  );
};

export const FinancialReportsPage = () => {
  const [activeTab, setActiveTab] = useState('TRIAL_BALANCE'); // TRIAL_BALANCE | BALANCE_SHEET | PROFIT_LOSS
  const [asOfDate, setAsOfDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [startDate, setStartDate] = useState(() => {
    const now = new Date();
    return `${now.getFullYear()}-01-01`;
  });
  const [endDate, setEndDate] = useState(() => new Date().toISOString().split('T')[0]);

  const [reportData, setReportData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Load active report
  const fetchReport = async () => {
    setLoading(true);
    setError(null);
    try {
      let data = null;
      if (activeTab === 'TRIAL_BALANCE') {
        data = await accountingApi.getTrialBalance(asOfDate);
      } else if (activeTab === 'BALANCE_SHEET') {
        data = await accountingApi.getBalanceSheet(asOfDate);
      } else if (activeTab === 'PROFIT_LOSS') {
        data = await accountingApi.getProfitLoss(startDate, endDate);
      }
      setReportData(data);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to generate financial statement.');
      setReportData(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReport();
  }, [activeTab]);

  // Handle browser native print
  const handlePrint = () => {
    window.print();
  };

  // CSV Exporter for each report type
  const handleDownloadCsv = () => {
    if (!reportData) return;
    let csvContent = 'data:text/csv;charset=utf-8,';

    if (activeTab === 'TRIAL_BALANCE') {
      csvContent += `TRIAL BALANCE REPORT\r\nAs of Date: ${reportData.asOfDate}\r\nGenerated: ${reportData.generatedAt}\r\n\r\n`;
      csvContent += 'GL Code,Account Name,Account Type,Debit (ETB),Credit (ETB)\r\n';
      (reportData.lines || []).forEach((line) => {
        csvContent += `"${line.glCode}","${line.accountName}","${line.accountType}",${line.debitAmount || 0},${line.creditAmount || 0}\r\n`;
      });
      csvContent += `\r\nTOTALS,,,${reportData.totalDebits || 0},${reportData.totalCredits || 0}\r\n`;
      csvContent += `STATUS,,,${reportData.isBalanced ? 'BALANCED' : 'UNBALANCED'},\r\n`;
    } else if (activeTab === 'BALANCE_SHEET') {
      csvContent += `BALANCE SHEET STATEMENT\r\nAs of Date: ${reportData.asOfDate}\r\nGenerated: ${reportData.generatedAt}\r\n\r\n`;
      csvContent += 'Section,GL Code,Account Name,Amount (ETB)\r\n';
      (reportData.assets || []).forEach((l) => {
        csvContent += `"Assets","${l.glCode}","${l.accountName}",${l.amount || 0}\r\n`;
      });
      csvContent += `"Assets Total",,,${reportData.totalAssets || 0}\r\n\r\n`;

      (reportData.liabilities || []).forEach((l) => {
        csvContent += `"Liabilities","${l.glCode}","${l.accountName}",${l.amount || 0}\r\n`;
      });
      csvContent += `"Liabilities Total",,,${reportData.totalLiabilities || 0}\r\n\r\n`;

      (reportData.equity || []).forEach((l) => {
        csvContent += `"Equity","${l.glCode}","${l.accountName}",${l.amount || 0}\r\n`;
      });
      csvContent += `"Current Year Net Surplus",,,${reportData.currentYearSurplus || 0}\r\n`;
      csvContent += `"Equity Total (Incl. Surplus)",,,${reportData.totalEquity || 0}\r\n\r\n`;
      csvContent += `"TOTAL LIABILITIES & EQUITY",,,${reportData.totalLiabilitiesAndEquity || 0}\r\n`;
    } else if (activeTab === 'PROFIT_LOSS') {
      csvContent += `PROFIT AND LOSS STATEMENT (INCOME STATEMENT)\r\nPeriod: ${reportData.startDate} to ${reportData.endDate}\r\nGenerated: ${reportData.generatedAt}\r\n\r\n`;
      csvContent += 'Section,GL Code,Account Name,Amount (ETB)\r\n';
      (reportData.operatingRevenue || []).forEach((l) => {
        csvContent += `"Revenue","${l.glCode}","${l.accountName}",${l.amount || 0}\r\n`;
      });
      csvContent += `"Total Revenue",,,${reportData.totalRevenue || 0}\r\n\r\n`;

      (reportData.operatingExpenses || []).forEach((l) => {
        csvContent += `"Expenses","${l.glCode}","${l.accountName}",${l.amount || 0}\r\n`;
      });
      csvContent += `"Total Expenses",,,${reportData.totalExpenses || 0}\r\n\r\n`;
      csvContent += `"NET OPERATING SURPLUS / (DEFICIT)",,,${reportData.netSurplus || 0}\r\n`;
    }

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `${activeTab.toLowerCase()}_${asOfDate || endDate}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="space-y-6 animate-fadeIn pb-12">
      {/* Top Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-[var(--bdae-border)] pb-4 print:hidden">
        <div className="flex items-center gap-3">
          <div
            className="w-10 h-10 rounded-xl flex items-center justify-center text-white shadow-md shrink-0"
            style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
          >
            <FileText className="w-5 h-5" />
          </div>
          <div>
            <h1 className="text-xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
              Core Banking Financial Statements
            </h1>
            <p className="text-xs text-[var(--bdae-text-secondary)]">
              Double-entry Trial Balance, institutional Balance Sheet, and period Profit & Loss.
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={handlePrint}
            title="Print Statement (A4 formatted)"
            className="px-3.5 py-2 rounded-xl border border-[var(--bdae-border)] text-xs font-semibold text-[var(--bdae-text-primary)] hover:bg-black/5 dark:hover:bg-white/5 flex items-center gap-1.5 transition-colors shadow-xs"
          >
            <Printer className="w-3.5 h-3.5 text-[var(--bdae-text-secondary)]" />
            <span className="hidden sm:inline">Print</span>
          </button>

          <button
            type="button"
            onClick={handleDownloadCsv}
            disabled={!reportData || loading}
            title="Download CSV Spreadsheet"
            className="px-3.5 py-2 rounded-xl border border-[var(--bdae-border)] text-xs font-semibold text-[var(--bdae-text-primary)] hover:bg-black/5 dark:hover:bg-white/5 flex items-center gap-1.5 transition-colors disabled:opacity-50 shadow-xs"
          >
            <Download className="w-3.5 h-3.5 text-[var(--bdae-text-secondary)]" />
            <span className="hidden sm:inline">CSV Export</span>
          </button>
        </div>
      </div>

      {/* 3-Tab Navigator */}
      <div className="flex items-center gap-2 border-b border-[var(--bdae-border)] pb-1 print:hidden">
        <button
          type="button"
          onClick={() => setActiveTab('TRIAL_BALANCE')}
          className={`px-4 py-2.5 rounded-xl text-xs font-bold flex items-center gap-2 transition-all ${
            activeTab === 'TRIAL_BALANCE'
              ? 'bg-[var(--bdae-primary)] text-white shadow-md'
              : 'text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)] hover:bg-black/5 dark:hover:bg-white/5'
          }`}
        >
          <Scale className="w-4 h-4" />
          <span>Trial Balance</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('BALANCE_SHEET')}
          className={`px-4 py-2.5 rounded-xl text-xs font-bold flex items-center gap-2 transition-all ${
            activeTab === 'BALANCE_SHEET'
              ? 'bg-[var(--bdae-primary)] text-white shadow-md'
              : 'text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)] hover:bg-black/5 dark:hover:bg-white/5'
          }`}
        >
          <Layers className="w-4 h-4" />
          <span>Balance Sheet</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('PROFIT_LOSS')}
          className={`px-4 py-2.5 rounded-xl text-xs font-bold flex items-center gap-2 transition-all ${
            activeTab === 'PROFIT_LOSS'
              ? 'bg-[var(--bdae-primary)] text-white shadow-md'
              : 'text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)] hover:bg-black/5 dark:hover:bg-white/5'
          }`}
        >
          <TrendingUp className="w-4 h-4" />
          <span>Profit & Loss (P&L)</span>
        </button>
      </div>

      {/* Parameter Filter Bar */}
      <div className="bdae-card p-4 rounded-2xl border border-[var(--bdae-border)] flex flex-wrap items-center justify-between gap-4 text-xs print:hidden shadow-xs">
        <div className="flex flex-wrap items-center gap-3">
          {activeTab !== 'PROFIT_LOSS' ? (
            <div className="flex items-center gap-2">
              <label className="font-bold text-[var(--bdae-text-secondary)]">As Of Date:</label>
              <div className="relative">
                <input
                  type="date"
                  value={asOfDate}
                  onChange={(e) => setAsOfDate(e.target.value)}
                  className="py-1.5 px-3 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] font-mono text-xs focus:outline-none focus:border-[var(--bdae-primary)]"
                />
              </div>
            </div>
          ) : (
            <div className="flex flex-wrap items-center gap-3">
              <div className="flex items-center gap-2">
                <label className="font-bold text-[var(--bdae-text-secondary)]">From:</label>
                <input
                  type="date"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  className="py-1.5 px-3 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] font-mono text-xs focus:outline-none focus:border-[var(--bdae-primary)]"
                />
              </div>
              <div className="flex items-center gap-2">
                <label className="font-bold text-[var(--bdae-text-secondary)]">To:</label>
                <input
                  type="date"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  className="py-1.5 px-3 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] font-mono text-xs focus:outline-none focus:border-[var(--bdae-primary)]"
                />
              </div>
            </div>
          )}
        </div>

        <button
          type="button"
          onClick={fetchReport}
          disabled={loading}
          className="bdae-btn-primary px-4 py-2 rounded-xl text-white font-bold flex items-center gap-1.5 text-xs shadow-sm"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          <span>Generate Statement</span>
        </button>
      </div>

      {/* Printable Report Header */}
      <div className="hidden print:block border-b-2 border-black pb-4 mb-6">
        <h2 className="text-xl font-bold uppercase tracking-tight">Qershi-Link Core Banking</h2>
        <h3 className="text-base font-semibold">
          {activeTab === 'TRIAL_BALANCE' && 'General Ledger Trial Balance'}
          {activeTab === 'BALANCE_SHEET' && 'Statement of Financial Position (Balance Sheet)'}
          {activeTab === 'PROFIT_LOSS' && 'Statement of Comprehensive Income (Profit & Loss)'}
        </h3>
        <p className="text-xs text-gray-600">
          {activeTab !== 'PROFIT_LOSS' ? `As of: ${asOfDate}` : `Period: ${startDate} to ${endDate}`}
          {' | Generated at: ' + new Date().toLocaleString()}
        </p>
      </div>

      {/* Main Report Body */}
      {loading ? (
        <div className="bdae-card p-16 rounded-2xl border border-[var(--bdae-border)] text-center text-[var(--bdae-text-secondary)]">
          <RefreshCw className="w-8 h-8 animate-spin mx-auto mb-3 text-[var(--bdae-primary)]" />
          <p className="font-bold text-sm text-[var(--bdae-text-primary)]">Aggregating General Ledger Balances...</p>
          <p className="text-xs mt-1">Applying double-entry validation rules.</p>
        </div>
      ) : error ? (
        <div className="bdae-card p-8 rounded-2xl border border-red-500/20 text-center text-red-500 space-y-2">
          <AlertTriangle className="w-8 h-8 mx-auto" />
          <p className="font-bold text-sm">{error}</p>
        </div>
      ) : !reportData ? (
        <div className="bdae-card p-12 rounded-2xl border border-[var(--bdae-border)] text-center text-[var(--bdae-text-secondary)] text-xs">
          Select parameters and click "Generate Statement" to run the report.
        </div>
      ) : (
        <div className="space-y-6">
          {/* TAB 1: TRIAL BALANCE */}
          {activeTab === 'TRIAL_BALANCE' && (
            <div className="space-y-4">
              <BalanceStatusBanner
                isBalanced={reportData.isBalanced}
                title={reportData.isBalanced ? 'Trial Balance Equilibrium Verified' : 'Trial Balance Out of Equilibrium!'}
                successMessage={`Debit and Credit accounts balance at ${formatCurrency(reportData.totalDebits)}.`}
                difference={reportData.difference}
                leftLabel="Total Debits"
                leftValue={reportData.totalDebits}
                rightLabel="Total Credits"
                rightValue={reportData.totalCredits}
              />

              <div className="bdae-card rounded-2xl border border-[var(--bdae-border)] overflow-hidden shadow-sm">
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-xs">
                    <thead className="bg-black/5 dark:bg-white/5 border-b border-[var(--bdae-border)] text-[10px] font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider">
                      <tr>
                        <th className="py-3 px-4">GL Code</th>
                        <th className="py-3 px-4">Account Description</th>
                        <th className="py-3 px-3">Category</th>
                        <th className="py-3 px-4 text-right">Debit Balance (ETB)</th>
                        <th className="py-3 px-4 text-right">Credit Balance (ETB)</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-[var(--bdae-border)]">
                      {(reportData.lines || []).map((line) => (
                        <tr key={line.accountId} className="hover:bg-black/[0.02] dark:hover:bg-white/[0.02]">
                          <td className="py-2.5 px-4 font-mono font-bold text-[var(--bdae-text-primary)]">
                            {line.glCode}
                          </td>
                          <td className="py-2.5 px-4 font-medium text-[var(--bdae-text-primary)]">
                            {line.accountName}
                          </td>
                          <td className="py-2.5 px-3">
                            <span className="text-[10px] px-2 py-0.5 rounded-full font-bold bg-black/5 dark:bg-white/5 text-[var(--bdae-text-secondary)]">
                              {line.accountType}
                            </span>
                          </td>
                          <td className="py-2.5 px-4 text-right font-mono text-[var(--bdae-text-primary)]">
                            {line.debitAmount > 0 ? formatNumber(line.debitAmount) : '-'}
                          </td>
                          <td className="py-2.5 px-4 text-right font-mono text-[var(--bdae-text-primary)]">
                            {line.creditAmount > 0 ? formatNumber(line.creditAmount) : '-'}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                    <tfoot className="bg-black/5 dark:bg-white/5 font-extrabold border-t-2 border-[var(--bdae-border)] text-xs">
                      <tr>
                        <td colSpan={3} className="py-3.5 px-4 uppercase tracking-wider text-[var(--bdae-text-primary)]">
                          Total Balances
                        </td>
                        <td className="py-3.5 px-4 text-right font-mono text-sm text-[var(--bdae-text-primary)]">
                          {formatCurrency(reportData.totalDebits)}
                        </td>
                        <td className="py-3.5 px-4 text-right font-mono text-sm text-[var(--bdae-text-primary)]">
                          {formatCurrency(reportData.totalCredits)}
                        </td>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              </div>
            </div>
          )}

          {/* TAB 2: BALANCE SHEET */}
          {activeTab === 'BALANCE_SHEET' && (
            <div className="space-y-4">
              <BalanceStatusBanner
                isBalanced={reportData.isBalanced}
                title={reportData.isBalanced ? 'Balance Sheet Equilibrium Verified' : 'Balance Sheet Equation Mismatch!'}
                successMessage={`Total Institutional Assets (${formatCurrency(reportData.totalAssets)}) match Total Liabilities & Member Equity.`}
                difference={reportData.difference}
                leftLabel="Total Assets"
                leftValue={reportData.totalAssets}
                rightLabel="Total Liabilities + Equity"
                rightValue={reportData.totalLiabilitiesAndEquity}
              />

              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* LEFT: ASSETS */}
                <div className="bdae-card rounded-2xl border border-[var(--bdae-border)] overflow-hidden shadow-sm flex flex-col justify-between">
                  <div>
                    <div className="p-4 bg-emerald-500/10 border-b border-emerald-500/20 flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <span className="w-2.5 h-2.5 rounded-full bg-emerald-500" />
                        <h3 className="font-extrabold text-xs uppercase tracking-wider text-emerald-700 dark:text-emerald-400">
                          1000 · Institutional Assets
                        </h3>
                      </div>
                      <span className="font-mono font-extrabold text-sm text-emerald-700 dark:text-emerald-400">
                        {formatCurrency(reportData.totalAssets)}
                      </span>
                    </div>

                    <table className="w-full text-left text-xs">
                      <thead className="border-b border-[var(--bdae-border)] bg-black/[0.02] dark:bg-white/[0.02] text-[10px] text-[var(--bdae-text-secondary)] uppercase">
                        <tr>
                          <th className="py-2 px-4">Code</th>
                          <th className="py-2 px-4">Account</th>
                          <th className="py-2 px-4 text-right">Amount (ETB)</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-[var(--bdae-border)]">
                        {(reportData.assets || []).map((line) => (
                          <tr key={line.accountId} className="hover:bg-black/[0.02] dark:hover:bg-white/[0.02]">
                            <td className="py-2.5 px-4 font-mono font-bold text-[var(--bdae-text-primary)]">
                              {line.glCode}
                            </td>
                            <td className="py-2.5 px-4 font-medium text-[var(--bdae-text-primary)]">
                              {line.accountName}
                            </td>
                            <td className="py-2.5 px-4 text-right font-mono font-semibold text-[var(--bdae-text-primary)]">
                              {formatCurrency(line.amount)}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  <div className="p-4 border-t border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 flex items-center justify-between font-extrabold text-xs">
                    <span>Total Assets</span>
                    <span className="font-mono text-sm text-emerald-600 dark:text-emerald-400">
                      {formatCurrency(reportData.totalAssets)}
                    </span>
                  </div>
                </div>

                {/* RIGHT: LIABILITIES + EQUITY */}
                <div className="space-y-6">
                  {/* Liabilities Table */}
                  <div className="bdae-card rounded-2xl border border-[var(--bdae-border)] overflow-hidden shadow-sm">
                    <div className="p-4 bg-amber-500/10 border-b border-amber-500/20 flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <span className="w-2.5 h-2.5 rounded-full bg-amber-500" />
                        <h3 className="font-extrabold text-xs uppercase tracking-wider text-amber-700 dark:text-amber-400">
                          2000 · Institutional Liabilities
                        </h3>
                      </div>
                      <span className="font-mono font-extrabold text-sm text-amber-700 dark:text-amber-400">
                        {formatCurrency(reportData.totalLiabilities)}
                      </span>
                    </div>

                    <table className="w-full text-left text-xs">
                      <thead className="border-b border-[var(--bdae-border)] bg-black/[0.02] dark:bg-white/[0.02] text-[10px] text-[var(--bdae-text-secondary)] uppercase">
                        <tr>
                          <th className="py-2 px-4">Code</th>
                          <th className="py-2 px-4">Account</th>
                          <th className="py-2 px-4 text-right">Amount (ETB)</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-[var(--bdae-border)]">
                        {(reportData.liabilities || []).map((line) => (
                          <tr key={line.accountId} className="hover:bg-black/[0.02] dark:hover:bg-white/[0.02]">
                            <td className="py-2.5 px-4 font-mono font-bold text-[var(--bdae-text-primary)]">
                              {line.glCode}
                            </td>
                            <td className="py-2.5 px-4 font-medium text-[var(--bdae-text-primary)]">
                              {line.accountName}
                            </td>
                            <td className="py-2.5 px-4 text-right font-mono font-semibold text-[var(--bdae-text-primary)]">
                              {formatCurrency(line.amount)}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                      <tfoot className="border-t border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 font-bold text-xs">
                        <tr>
                          <td colSpan={2} className="py-2.5 px-4">Total Liabilities</td>
                          <td className="py-2.5 px-4 text-right font-mono">
                            {formatCurrency(reportData.totalLiabilities)}
                          </td>
                        </tr>
                      </tfoot>
                    </table>
                  </div>

                  {/* Equity & Reserves Table */}
                  <div className="bdae-card rounded-2xl border border-[var(--bdae-border)] overflow-hidden shadow-sm">
                    <div className="p-4 bg-purple-500/10 border-b border-purple-500/20 flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <span className="w-2.5 h-2.5 rounded-full bg-purple-500" />
                        <h3 className="font-extrabold text-xs uppercase tracking-wider text-purple-700 dark:text-purple-400">
                          3000 · Member Equity & Retained Earnings
                        </h3>
                      </div>
                      <span className="font-mono font-extrabold text-sm text-purple-700 dark:text-purple-400">
                        {formatCurrency(reportData.totalEquity)}
                      </span>
                    </div>

                    <table className="w-full text-left text-xs">
                      <thead className="border-b border-[var(--bdae-border)] bg-black/[0.02] dark:bg-white/[0.02] text-[10px] text-[var(--bdae-text-secondary)] uppercase">
                        <tr>
                          <th className="py-2 px-4">Code</th>
                          <th className="py-2 px-4">Account</th>
                          <th className="py-2 px-4 text-right">Amount (ETB)</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-[var(--bdae-border)]">
                        {(reportData.equity || []).map((line) => (
                          <tr key={line.accountId} className="hover:bg-black/[0.02] dark:hover:bg-white/[0.02]">
                            <td className="py-2.5 px-4 font-mono font-bold text-[var(--bdae-text-primary)]">
                              {line.glCode}
                            </td>
                            <td className="py-2.5 px-4 font-medium text-[var(--bdae-text-primary)]">
                              {line.accountName}
                            </td>
                            <td className="py-2.5 px-4 text-right font-mono font-semibold text-[var(--bdae-text-primary)]">
                              {formatCurrency(line.amount)}
                            </td>
                          </tr>
                        ))}
                        {/* Current Year Net Surplus line */}
                        <tr className="bg-emerald-500/5 font-semibold text-emerald-700 dark:text-emerald-400">
                          <td className="py-2.5 px-4 font-mono font-bold">P&L</td>
                          <td className="py-2.5 px-4">Current Year Operating Surplus / (Deficit)</td>
                          <td className="py-2.5 px-4 text-right font-mono font-bold">
                            {formatCurrency(reportData.currentYearSurplus)}
                          </td>
                        </tr>
                      </tbody>
                      <tfoot className="border-t border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 font-bold text-xs">
                        <tr>
                          <td colSpan={2} className="py-2.5 px-4">Total Equity & Surplus</td>
                          <td className="py-2.5 px-4 text-right font-mono">
                            {formatCurrency(reportData.totalEquity)}
                          </td>
                        </tr>
                      </tfoot>
                    </table>
                  </div>

                  {/* Summary Total Liabilities + Equity Bar */}
                  <div className="bdae-card p-4 rounded-2xl border-2 border-[var(--bdae-border)] flex items-center justify-between font-extrabold text-xs shadow-xs">
                    <span className="uppercase tracking-wider">Total Liabilities & Equity</span>
                    <span className="font-mono text-base text-[var(--bdae-primary)]">
                      {formatCurrency(reportData.totalLiabilitiesAndEquity)}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* TAB 3: PROFIT & LOSS */}
          {activeTab === 'PROFIT_LOSS' && (
            <div className="space-y-6">
              {/* Net Surplus Headline Banner */}
              <div
                className={`p-6 rounded-2xl border shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4 ${
                  reportData.netSurplus >= 0
                    ? 'bg-emerald-500/10 border-emerald-500/20 text-emerald-800 dark:text-emerald-300'
                    : 'bg-rose-500/10 border-rose-500/20 text-rose-800 dark:text-rose-300'
                }`}
              >
                <div className="flex items-center gap-3.5">
                  <div
                    className={`w-12 h-12 rounded-2xl flex items-center justify-center text-white shrink-0 shadow-md ${
                      reportData.netSurplus >= 0 ? 'bg-emerald-500' : 'bg-rose-500'
                    }`}
                  >
                    {reportData.netSurplus >= 0 ? (
                      <TrendingUp className="w-6 h-6" />
                    ) : (
                      <TrendingDown className="w-6 h-6" />
                    )}
                  </div>
                  <div>
                    <span className="text-[10px] font-extrabold uppercase tracking-widest opacity-80">
                      {reportData.netSurplus >= 0 ? 'Net Operating Surplus (Profit)' : 'Net Operating Deficit (Loss)'}
                    </span>
                    <h3 className="text-2xl font-black font-mono">
                      {formatCurrency(reportData.netSurplus)}
                    </h3>
                  </div>
                </div>

                <div className="flex items-center gap-6 text-xs font-semibold">
                  <div>
                    <span className="text-[10px] uppercase block opacity-75">Operating Revenue</span>
                    <span className="font-mono font-bold text-sm text-blue-600 dark:text-blue-400">
                      {formatCurrency(reportData.totalRevenue)}
                    </span>
                  </div>
                  <div className="border-l border-current pl-6">
                    <span className="text-[10px] uppercase block opacity-75">Operating Expenses</span>
                    <span className="font-mono font-bold text-sm text-rose-600 dark:text-rose-400">
                      {formatCurrency(reportData.totalExpenses)}
                    </span>
                  </div>
                </div>
              </div>

              {/* Revenue & Expense Detailed Breakdown */}
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* 4000: Operating Revenue */}
                <div className="bdae-card rounded-2xl border border-[var(--bdae-border)] overflow-hidden shadow-sm flex flex-col justify-between">
                  <div>
                    <div className="p-4 bg-blue-500/10 border-b border-blue-500/20 flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <span className="w-2.5 h-2.5 rounded-full bg-blue-500" />
                        <h3 className="font-extrabold text-xs uppercase tracking-wider text-blue-700 dark:text-blue-400">
                          4000 · Operating Income & Revenue
                        </h3>
                      </div>
                      <span className="font-mono font-extrabold text-sm text-blue-700 dark:text-blue-400">
                        {formatCurrency(reportData.totalRevenue)}
                      </span>
                    </div>

                    <table className="w-full text-left text-xs">
                      <thead className="border-b border-[var(--bdae-border)] bg-black/[0.02] dark:bg-white/[0.02] text-[10px] text-[var(--bdae-text-secondary)] uppercase">
                        <tr>
                          <th className="py-2 px-4">Code</th>
                          <th className="py-2 px-4">Revenue Stream</th>
                          <th className="py-2 px-4 text-right">Amount (ETB)</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-[var(--bdae-border)]">
                        {(reportData.operatingRevenue || []).map((line) => (
                          <tr key={line.accountId} className="hover:bg-black/[0.02] dark:hover:bg-white/[0.02]">
                            <td className="py-2.5 px-4 font-mono font-bold text-[var(--bdae-text-primary)]">
                              {line.glCode}
                            </td>
                            <td className="py-2.5 px-4 font-medium text-[var(--bdae-text-primary)]">
                              {line.accountName}
                            </td>
                            <td className="py-2.5 px-4 text-right font-mono font-semibold text-[var(--bdae-text-primary)]">
                              {formatCurrency(line.amount)}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  <div className="p-4 border-t border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 flex items-center justify-between font-extrabold text-xs">
                    <span>Total Operating Income</span>
                    <span className="font-mono text-sm text-blue-600 dark:text-blue-400">
                      {formatCurrency(reportData.totalRevenue)}
                    </span>
                  </div>
                </div>

                {/* 5000: Operating Expenses */}
                <div className="bdae-card rounded-2xl border border-[var(--bdae-border)] overflow-hidden shadow-sm flex flex-col justify-between">
                  <div>
                    <div className="p-4 bg-rose-500/10 border-b border-rose-500/20 flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <span className="w-2.5 h-2.5 rounded-full bg-rose-500" />
                        <h3 className="font-extrabold text-xs uppercase tracking-wider text-rose-700 dark:text-rose-400">
                          5000 · Operating Expenses & Provisions
                        </h3>
                      </div>
                      <span className="font-mono font-extrabold text-sm text-rose-700 dark:text-rose-400">
                        {formatCurrency(reportData.totalExpenses)}
                      </span>
                    </div>

                    <table className="w-full text-left text-xs">
                      <thead className="border-b border-[var(--bdae-border)] bg-black/[0.02] dark:bg-white/[0.02] text-[10px] text-[var(--bdae-text-secondary)] uppercase">
                        <tr>
                          <th className="py-2 px-4">Code</th>
                          <th className="py-2 px-4">Expense Head</th>
                          <th className="py-2 px-4 text-right">Amount (ETB)</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-[var(--bdae-border)]">
                        {(reportData.operatingExpenses || []).map((line) => (
                          <tr key={line.accountId} className="hover:bg-black/[0.02] dark:hover:bg-white/[0.02]">
                            <td className="py-2.5 px-4 font-mono font-bold text-[var(--bdae-text-primary)]">
                              {line.glCode}
                            </td>
                            <td className="py-2.5 px-4 font-medium text-[var(--bdae-text-primary)]">
                              {line.accountName}
                            </td>
                            <td className="py-2.5 px-4 text-right font-mono font-semibold text-[var(--bdae-text-primary)]">
                              {formatCurrency(line.amount)}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  <div className="p-4 border-t border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 flex items-center justify-between font-extrabold text-xs">
                    <span>Total Operating Expenses</span>
                    <span className="font-mono text-sm text-rose-600 dark:text-rose-400">
                      {formatCurrency(reportData.totalExpenses)}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default FinancialReportsPage;
