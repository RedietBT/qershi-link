import React, { useState, useEffect } from 'react';
import {
  FileText,
  Calendar,
  RefreshCw,
  AlertTriangle,
  Scale,
  TrendingUp,
  Printer
} from 'lucide-react';
import { accountingApi } from '../api/accountingApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';
import { TrialBalanceView } from '../components/TrialBalanceView';
import { BalanceSheetView } from '../components/BalanceSheetView';
import { ProfitLossView } from '../components/ProfitLossView';

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
      setError(
        err.response?.data?.message || err.message || 'Failed to generate financial statement.'
      );
      setReportData(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReport();
  }, [activeTab, asOfDate, startDate, endDate]);

  const handlePrint = () => {
    window.print();
  };

  return (
    <div className="space-y-6 animate-fadeIn pb-12">
      {/* ── Header ── */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-[var(--bdae-border)] pb-4">
        <div>
          <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
            <FileText className="w-4 h-4 text-[var(--bdae-primary)]" />
            <span>Statutory Financial Accounting</span>
          </div>
          <h1 className="text-2xl font-black tracking-tight text-[var(--bdae-text-primary)]">
            Financial Statements & Regulatory Reports
          </h1>
          <p className="text-xs text-[var(--bdae-text-secondary)]">
            Double-entry Trial Balance equilibrium, Institutional Balance Sheet, and Income Statements.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={fetchReport}
            disabled={loading}
            className="px-3 py-2 rounded-xl text-xs font-semibold border border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5 text-[var(--bdae-text-primary)] flex items-center gap-1.5 transition-all"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            <span>Regenerate</span>
          </button>

          <button
            onClick={handlePrint}
            className="px-3 py-2 rounded-xl text-xs font-semibold border border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5 text-[var(--bdae-text-primary)] flex items-center gap-1.5 transition-all"
          >
            <Printer className="w-3.5 h-3.5" />
            <span>Print Report</span>
          </button>
        </div>
      </div>

      {/* ── Report Selector Navigation ── */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center p-1 rounded-xl bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] text-xs font-bold">
          <button
            onClick={() => setActiveTab('TRIAL_BALANCE')}
            className={`px-4 py-2 rounded-lg transition-all flex items-center gap-1.5 ${
              activeTab === 'TRIAL_BALANCE'
                ? 'bg-[var(--bdae-primary)] text-white shadow-sm'
                : 'text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]'
            }`}
          >
            <Scale className="w-3.5 h-3.5" />
            <span>1. Trial Balance</span>
          </button>

          <button
            onClick={() => setActiveTab('BALANCE_SHEET')}
            className={`px-4 py-2 rounded-lg transition-all flex items-center gap-1.5 ${
              activeTab === 'BALANCE_SHEET'
                ? 'bg-[var(--bdae-primary)] text-white shadow-sm'
                : 'text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]'
            }`}
          >
            <FileText className="w-3.5 h-3.5" />
            <span>2. Balance Sheet</span>
          </button>

          <button
            onClick={() => setActiveTab('PROFIT_LOSS')}
            className={`px-4 py-2 rounded-lg transition-all flex items-center gap-1.5 ${
              activeTab === 'PROFIT_LOSS'
                ? 'bg-[var(--bdae-primary)] text-white shadow-sm'
                : 'text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]'
            }`}
          >
            <TrendingUp className="w-3.5 h-3.5" />
            <span>3. Profit & Loss</span>
          </button>
        </div>

        {/* Date Filters */}
        <div className="flex items-center gap-3 text-xs">
          {activeTab === 'PROFIT_LOSS' ? (
            <div className="flex items-center gap-2">
              <span className="text-[10px] font-bold uppercase text-[var(--bdae-text-secondary)]">
                Range:
              </span>
              <input
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                className="bdae-input text-xs py-1 px-2.5 font-mono"
              />
              <span className="text-[var(--bdae-text-secondary)]">to</span>
              <input
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                className="bdae-input text-xs py-1 px-2.5 font-mono"
              />
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <span className="text-[10px] font-bold uppercase text-[var(--bdae-text-secondary)]">
                As of Date:
              </span>
              <input
                type="date"
                value={asOfDate}
                onChange={(e) => setAsOfDate(e.target.value)}
                className="bdae-input text-xs py-1 px-2.5 font-mono"
              />
            </div>
          )}
        </div>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 text-xs flex items-center gap-2.5">
          <AlertTriangle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* ── Report Views ── */}
      <PermissionGuard
        permissions={[PERMISSIONS.FINANCIAL_REPORT_VIEW]}
        fallback={
          <div className="p-12 text-center text-xs text-[var(--bdae-text-secondary)]">
            Financial reporting permission required.
          </div>
        }
      >
        {activeTab === 'TRIAL_BALANCE' && <TrialBalanceView report={reportData} />}
        {activeTab === 'BALANCE_SHEET' && <BalanceSheetView report={reportData} />}
        {activeTab === 'PROFIT_LOSS' && <ProfitLossView report={reportData} />}
      </PermissionGuard>
    </div>
  );
};
