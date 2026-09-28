import React, { useState, useEffect } from 'react';
import {
  AlertTriangle,
  TrendingDown,
  ShieldAlert,
  Percent,
  DollarSign,
  Filter,
  RefreshCw,
  Search,
  CheckCircle2,
  PieChart,
  Layers,
  ArrowUpRight,
  Clock,
  Banknote,
  FileSpreadsheet
} from 'lucide-react';
import { loanMgmtHttpClient } from '../../../../common/api/httpClient';
import { useAuthStore } from '../../../../common/store/useAuthStore';
import { PERMISSIONS } from '../../../../common/constants/permissions';

export const LoanDelinquencyDashboard = () => {
  const [summary, setSummary] = useState(null);
  const [loans, setLoans] = useState([]);
  const [selectedBucket, setSelectedBucket] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isEvaluating, setIsEvaluating] = useState(false);
  const [error, setError] = useState(null);
  const [feedback, setFeedback] = useState(null);

  const user = useAuthStore((state) => state.user);
  const userPermissions = user?.permissions || [];
  const canEvaluate = user?.globalRole === 'SUPER_ADMIN' ||
    user?.globalRole === 'SACCO_ADMIN' ||
    userPermissions.includes(PERMISSIONS.LOAN_DELINQUENCY_VIEW);

  const fetchData = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [summaryRes, loansRes] = await Promise.all([
        loanMgmtHttpClient.get('/loans/delinquency/summary'),
        loanMgmtHttpClient.get('/loans/delinquency/loans')
      ]);
      setSummary(summaryRes.data);
      setLoans(loansRes.data || []);
    } catch (err) {
      console.error('Failed to load PAR delinquency data:', err);
      setError(err.response?.data?.message || 'Failed to load Portfolio at Risk (PAR) data.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleTriggerEvaluation = async () => {
    setIsEvaluating(true);
    setError(null);
    setFeedback(null);
    try {
      const res = await loanMgmtHttpClient.post('/loans/delinquency/evaluate');
      setFeedback(`Delinquency aging completed. Evaluated ${res.data.totalLoansEvaluated} active loans.`);
      await fetchData();
    } catch (err) {
      console.error('Failed to trigger PAR evaluation:', err);
      setError(err.response?.data?.message || 'Error executing PAR aging evaluation.');
    } finally {
      setIsEvaluating(false);
    }
  };

  const formatCurrency = (amount) => {
    if (amount === undefined || amount === null) return '0.00 ETB';
    return Number(amount).toLocaleString('en-ET', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' ETB';
  };

  const filteredLoans = loans.filter((loan) => {
    const matchesBucket = selectedBucket === 'ALL' || loan.parBucket === selectedBucket;
    const matchesSearch = searchQuery === '' ||
      loan.accountNo?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      loan.accountId?.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesBucket && matchesSearch;
  });

  const getBucketBadge = (bucket) => {
    switch (bucket) {
      case 'CURRENT':
        return <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/15 text-emerald-600 dark:text-emerald-400">Current (0 DPD)</span>;
      case 'WATCHLIST_PAR_30':
        return <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/15 text-amber-600 dark:text-amber-400">PAR 1-30</span>;
      case 'SUBSTANDARD_PAR_60':
        return <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-orange-500/15 text-orange-600 dark:text-orange-400">PAR 31-60</span>;
      case 'DOUBTFUL_PAR_90':
        return <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-rose-500/15 text-rose-600 dark:text-rose-400">PAR 61-90</span>;
      case 'LOSS_PAR_90_PLUS':
        return <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-red-500/15 text-red-600 dark:text-red-400">NPL / Loss (90+ DPD)</span>;
      default:
        return <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-gray-500/15 text-gray-600">{bucket}</span>;
    }
  };

  return (
    <div className="space-y-6 animate-fadeIn pb-12">
      {/* ── Page Header ── */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
            <AlertTriangle className="w-4 h-4 text-amber-500" />
            <span>Credit & Risk Governance</span>
          </div>
          <h1 className="text-2xl font-black tracking-tight text-[var(--bdae-text-primary)]">
            Portfolio at Risk (PAR) & Delinquency Aging
          </h1>
          <p className="text-xs text-[var(--bdae-text-secondary)]">
            Inspect Days Past Due (DPD), non-performing loans (NPL), and statutory loan loss provisioning reserves.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={fetchData}
            disabled={isLoading || isEvaluating}
            className="px-3 py-2 rounded-xl text-xs font-semibold border border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5 text-[var(--bdae-text-primary)] flex items-center gap-1.5 transition-all"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />
            <span>Refresh</span>
          </button>

          {canEvaluate && (
            <button
              onClick={handleTriggerEvaluation}
              disabled={isEvaluating}
              className="px-4 py-2 rounded-xl text-xs font-bold bg-[var(--bdae-primary)] hover:opacity-90 text-white flex items-center gap-2 shadow-lg shadow-[var(--bdae-primary)]/20 transition-all disabled:opacity-50"
            >
              <RotateCw className={`w-3.5 h-3.5 ${isEvaluating ? 'animate-spin' : ''}`} />
              <span>{isEvaluating ? 'Evaluating Aging...' : 'Recalculate PAR Aging'}</span>
            </button>
          )}
        </div>
      </div>

      {/* ── Feedback Alerts ── */}
      {error && (
        <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 text-xs flex items-center gap-2.5">
          <ShieldAlert className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}
      {feedback && (
        <div className="p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-600 dark:text-emerald-400 text-xs flex items-center gap-2.5">
          <CheckCircle2 className="w-4 h-4 shrink-0" />
          <span>{feedback}</span>
        </div>
      )}

      {/* ── Top Metric KPI Cards ── */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Total Portfolio */}
        <div className="bdae-card p-5 rounded-2xl border border-[var(--bdae-border)] space-y-2">
          <div className="flex items-center justify-between text-[var(--bdae-text-secondary)]">
            <span className="text-[10px] font-bold uppercase tracking-wider">Total Active Loan Book</span>
            <Banknote className="w-4 h-4 text-[var(--bdae-primary)]" />
          </div>
          <p className="text-xl font-black font-mono text-[var(--bdae-text-primary)]">
            {formatCurrency(summary?.totalOutstandingPrincipal)}
          </p>
          <p className="text-[11px] text-[var(--bdae-text-secondary)]">
            Total Loans: <span className="font-bold text-[var(--bdae-text-primary)]">{summary?.totalLoans || 0}</span>
          </p>
        </div>

        {/* Performing / Current */}
        <div className="bdae-card p-5 rounded-2xl border border-[var(--bdae-border)] space-y-2">
          <div className="flex items-center justify-between text-emerald-600 dark:text-emerald-400">
            <span className="text-[10px] font-bold uppercase tracking-wider">Current / Performing</span>
            <CheckCircle2 className="w-4 h-4" />
          </div>
          <p className="text-xl font-black font-mono text-emerald-600 dark:text-emerald-400">
            {formatCurrency(summary?.currentAmount)}
          </p>
          <p className="text-[11px] text-[var(--bdae-text-secondary)]">
            <span className="font-bold text-emerald-600 dark:text-emerald-400">{summary?.currentCount || 0}</span> loans (0 DPD)
          </p>
        </div>

        {/* NPL Ratio */}
        <div className="bdae-card p-5 rounded-2xl border border-[var(--bdae-border)] space-y-2">
          <div className="flex items-center justify-between text-red-500">
            <span className="text-[10px] font-bold uppercase tracking-wider">NPL Ratio (PAR 90+)</span>
            <TrendingDown className="w-4 h-4" />
          </div>
          <p className="text-xl font-black font-mono text-red-500">
            {summary?.nplRatioPct !== undefined ? `${summary.nplRatioPct}%` : '0.00%'}
          </p>
          <p className="text-[11px] text-[var(--bdae-text-secondary)]">
            NPL Volume: <span className="font-bold text-red-500">{formatCurrency(summary?.lossAmount)}</span>
          </p>
        </div>

        {/* Provision Reserves */}
        <div className="bdae-card p-5 rounded-2xl border border-[var(--bdae-border)] space-y-2">
          <div className="flex items-center justify-between text-purple-600 dark:text-purple-400">
            <span className="text-[10px] font-bold uppercase tracking-wider">Provision Reserves</span>
            <Percent className="w-4 h-4" />
          </div>
          <p className="text-xl font-black font-mono text-purple-600 dark:text-purple-400">
            {formatCurrency(summary?.totalProvisionReserve)}
          </p>
          <p className="text-[11px] text-[var(--bdae-text-secondary)]">
            Statutory loan loss reserve held
          </p>
        </div>
      </div>

      {/* ── PAR Breakdown Risk Distribution ── */}
      <div className="bdae-card p-6 rounded-2xl border border-[var(--bdae-border)] space-y-4">
        <h2 className="text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] flex items-center gap-2">
          <Layers className="w-4 h-4 text-[var(--bdae-primary)]" />
          <span>Regulatory Delinquency Aging Classification</span>
        </h2>

        <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
          {/* Current */}
          <div className="p-3.5 rounded-xl border border-[var(--bdae-border)] bg-emerald-500/5 space-y-1">
            <span className="text-[10px] font-bold uppercase text-emerald-600 dark:text-emerald-400">
              Current (0 DPD)
            </span>
            <p className="text-base font-black font-mono text-[var(--bdae-text-primary)]">
              {summary?.currentCount || 0}
            </p>
            <p className="text-[11px] font-mono text-[var(--bdae-text-secondary)]">
              {formatCurrency(summary?.currentAmount)}
            </p>
            <span className="text-[9px] font-semibold text-emerald-600">1% General Reserve</span>
          </div>

          {/* Watchlist PAR 30 */}
          <div className="p-3.5 rounded-xl border border-[var(--bdae-border)] bg-amber-500/5 space-y-1">
            <span className="text-[10px] font-bold uppercase text-amber-600 dark:text-amber-400">
              Watchlist (1-30 DPD)
            </span>
            <p className="text-base font-black font-mono text-[var(--bdae-text-primary)]">
              {summary?.par30Count || 0}
            </p>
            <p className="text-[11px] font-mono text-[var(--bdae-text-secondary)]">
              {formatCurrency(summary?.par30Amount)}
            </p>
            <span className="text-[9px] font-semibold text-amber-600">5% Reserve</span>
          </div>

          {/* Substandard PAR 60 */}
          <div className="p-3.5 rounded-xl border border-[var(--bdae-border)] bg-orange-500/5 space-y-1">
            <span className="text-[10px] font-bold uppercase text-orange-600 dark:text-orange-400">
              Substandard (31-60 DPD)
            </span>
            <p className="text-base font-black font-mono text-[var(--bdae-text-primary)]">
              {summary?.par60Count || 0}
            </p>
            <p className="text-[11px] font-mono text-[var(--bdae-text-secondary)]">
              {formatCurrency(summary?.par60Amount)}
            </p>
            <span className="text-[9px] font-semibold text-orange-600">25% Reserve</span>
          </div>

          {/* Doubtful PAR 90 */}
          <div className="p-3.5 rounded-xl border border-[var(--bdae-border)] bg-rose-500/5 space-y-1">
            <span className="text-[10px] font-bold uppercase text-rose-600 dark:text-rose-400">
              Doubtful (61-90 DPD)
            </span>
            <p className="text-base font-black font-mono text-[var(--bdae-text-primary)]">
              {summary?.par90Count || 0}
            </p>
            <p className="text-[11px] font-mono text-[var(--bdae-text-secondary)]">
              {formatCurrency(summary?.par90Amount)}
            </p>
            <span className="text-[9px] font-semibold text-rose-600">50% Reserve</span>
          </div>

          {/* Loss / NPL */}
          <div className="p-3.5 rounded-xl border border-[var(--bdae-border)] bg-red-500/5 space-y-1">
            <span className="text-[10px] font-bold uppercase text-red-600 dark:text-red-400">
              Loss / NPL (90+ DPD)
            </span>
            <p className="text-base font-black font-mono text-[var(--bdae-text-primary)]">
              {summary?.lossCount || 0}
            </p>
            <p className="text-[11px] font-mono text-[var(--bdae-text-secondary)]">
              {formatCurrency(summary?.lossAmount)}
            </p>
            <span className="text-[9px] font-semibold text-red-600">100% Reserve</span>
          </div>
        </div>
      </div>

      {/* ── Delinquent Loans Roster ── */}
      <div className="bdae-card p-6 rounded-2xl border border-[var(--bdae-border)] space-y-4">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div className="flex items-center gap-2 overflow-x-auto pb-2 md:pb-0">
            {[
              { id: 'ALL', label: 'All Evaluated' },
              { id: 'CURRENT', label: 'Current' },
              { id: 'WATCHLIST_PAR_30', label: 'PAR 1-30' },
              { id: 'SUBSTANDARD_PAR_60', label: 'PAR 31-60' },
              { id: 'DOUBTFUL_PAR_90', label: 'PAR 61-90' },
              { id: 'LOSS_PAR_90_PLUS', label: 'Loss / NPL' }
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => setSelectedBucket(tab.id)}
                className={`px-3 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap transition-all ${
                  selectedBucket === tab.id
                    ? 'bg-[var(--bdae-primary)] text-white shadow-md'
                    : 'text-[var(--bdae-text-secondary)] hover:bg-black/5 dark:hover:bg-white/5'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>

          <div className="relative w-full md:w-64">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-[var(--bdae-text-secondary)]" />
            <input
              type="text"
              placeholder="Search by loan account..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-9 pr-3 py-1.5 rounded-xl text-xs border border-[var(--bdae-border)] bg-transparent text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)]"
            />
          </div>
        </div>

        {filteredLoans.length === 0 ? (
          <div className="text-center py-12 text-xs text-[var(--bdae-text-secondary)] space-y-2">
            <CheckCircle2 className="w-8 h-8 mx-auto text-emerald-500 opacity-60" />
            <p>No loans match the selected risk filter.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="border-b border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] uppercase text-[10px] font-bold tracking-wider">
                  <th className="py-3 px-3">Loan Account</th>
                  <th className="py-3 px-3">Principal</th>
                  <th className="py-3 px-3">DPD (Days)</th>
                  <th className="py-3 px-3">Risk Classification</th>
                  <th className="py-3 px-3">Overdue Principal</th>
                  <th className="py-3 px-3">Overdue Interest</th>
                  <th className="py-3 px-3">Total Overdue</th>
                  <th className="py-3 px-3">Provision Rate</th>
                  <th className="py-3 px-3 text-right">Required Reserve</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[var(--bdae-border)]">
                {filteredLoans.map((loan) => (
                  <tr
                    key={loan.snapshotId || loan.accountId}
                    className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
                  >
                    <td className="py-3 px-3 font-mono font-bold text-[var(--bdae-text-primary)]">
                      {loan.accountNo}
                    </td>
                    <td className="py-3 px-3 font-mono text-[var(--bdae-text-primary)]">
                      {formatCurrency(loan.principalAmount)}
                    </td>
                    <td className="py-3 px-3 font-mono font-bold">
                      <span className={loan.daysPastDue > 0 ? 'text-red-500' : 'text-emerald-500'}>
                        {loan.daysPastDue} days
                      </span>
                    </td>
                    <td className="py-3 px-3">
                      {getBucketBadge(loan.parBucket)}
                    </td>
                    <td className="py-3 px-3 font-mono text-[var(--bdae-text-secondary)]">
                      {formatCurrency(loan.overduePrincipal)}
                    </td>
                    <td className="py-3 px-3 font-mono text-[var(--bdae-text-secondary)]">
                      {formatCurrency(loan.overdueInterest)}
                    </td>
                    <td className="py-3 px-3 font-mono font-bold text-red-500">
                      {formatCurrency(loan.totalOverdue)}
                    </td>
                    <td className="py-3 px-3 font-semibold text-[var(--bdae-text-secondary)]">
                      {loan.provisionRatePct}%
                    </td>
                    <td className="py-3 px-3 text-right font-mono font-bold text-purple-600 dark:text-purple-400">
                      {formatCurrency(loan.provisionAmount)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
