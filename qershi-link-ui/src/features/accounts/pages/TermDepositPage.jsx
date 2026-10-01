import React, { useState, useEffect, useCallback } from 'react';
import {
  Landmark,
  Plus,
  RefreshCw,
  TrendingUp,
  Calendar,
  Scissors,
  CheckCircle2,
  AlertTriangle,
  Clock,
  ChevronRight,
  DollarSign,
  ShieldCheck
} from 'lucide-react';
import { accountHttpClient } from '../../../common/api/httpClient';
import { useAuthStore } from '../../../common/store/useAuthStore';
import { formatCurrency } from '../../../common/utils/currency';
import { OpenTermDepositModal } from '../components/OpenTermDepositModal';
import { TermDepositCard } from '../components/TermDepositCard';

/**
 * Fixed Term Deposit Management Dashboard.
 * Gap 5: Temenos Transact / Finacle FD contract lifecycle.
 */
export const TermDepositPage = () => {
  const [contracts, setContracts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isOpenModalVisible, setIsOpenModalVisible] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [statusFilter, setStatusFilter] = useState('ALL');

  const user = useAuthStore((s) => s.user);
  const canManage =
    user?.globalRole === 'SUPER_ADMIN' ||
    user?.permissions?.includes('TERM_DEPOSIT_MANAGE') ||
    user?.globalRole === 'SACCO_ADMIN';

  const fetchContracts = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const res = await accountHttpClient.get('/term-deposits/active');
      setContracts(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load term deposit contracts.');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchContracts();
  }, [fetchContracts]);

  const handleOpenSuccess = (msg) => {
    setSuccess(msg || 'Term Deposit opened successfully.');
    setIsOpenModalVisible(false);
    fetchContracts();
    setTimeout(() => setSuccess(null), 6000);
  };

  const handleEarlyBreak = async (contractId) => {
    if (!window.confirm('Are you sure you want to prematurely break this Term Deposit? A penalty will be applied.')) return;
    try {
      const res = await accountHttpClient.post(`/term-deposits/${contractId}/break-early`);
      setSuccess(
        `Contract broken early. Net payout: ${formatCurrency(res.data?.netPayoutAmount)} ETB. GL Ref: ${res.data?.closingGlRef}`
      );
      fetchContracts();
      setTimeout(() => setSuccess(null), 8000);
    } catch (err) {
      setError(err.response?.data?.message || 'Early break failed. Check authorization or contract status.');
    }
  };

  // Portfolio summary metrics
  const totalPrincipal = contracts.reduce((s, c) => s + parseFloat(c.principalAmount || 0), 0);
  const totalAccrued   = contracts.reduce((s, c) => s + parseFloat(c.accruedInterest || 0), 0);
  const activeCount    = contracts.filter(c => c.status === 'ACTIVE').length;
  const maturedCount   = contracts.filter(c => c.status === 'MATURED').length;

  const filtered = statusFilter === 'ALL'
    ? contracts
    : contracts.filter(c => c.status === statusFilter);

  const STATUS_LABELS = ['ALL', 'ACTIVE', 'MATURED', 'CLOSED_NORMAL', 'CLOSED_EARLY', 'ROLLED_OVER'];

  const STATUS_COLORS = {
    ACTIVE:        'text-emerald-600 dark:text-emerald-400 bg-emerald-500/10 border-emerald-500/20',
    MATURED:       'text-amber-600 dark:text-amber-400 bg-amber-500/10 border-amber-500/20',
    CLOSED_NORMAL: 'text-blue-600 dark:text-blue-400 bg-blue-500/10 border-blue-500/20',
    CLOSED_EARLY:  'text-rose-600 dark:text-rose-400 bg-rose-500/10 border-rose-500/20',
    ROLLED_OVER:   'text-violet-600 dark:text-violet-400 bg-violet-500/10 border-violet-500/20',
    PENDING_APPROVAL: 'text-gray-600 dark:text-gray-400 bg-gray-500/10 border-gray-500/20',
  };

  return (
    <div className="space-y-6 animate-fadeIn pb-12">
      {/* ── Page Header ── */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
            <Landmark className="w-4 h-4 text-violet-500" />
            <span>Savings &amp; Deposits</span>
          </div>
          <h1 className="text-2xl font-black tracking-tight text-[var(--bdae-text-primary)]">
            Fixed Term Deposits (FD)
          </h1>
          <p className="text-xs text-[var(--bdae-text-secondary)]">
            Member FD contracts — Temenos / Finacle standard with penalty-break engine and GL posting.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={fetchContracts}
            disabled={isLoading}
            className="p-2 rounded-xl border border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5 text-[var(--bdae-text-secondary)] transition-all"
          >
            <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
          </button>
          {canManage && (
            <button
              id="open-fd-btn"
              onClick={() => setIsOpenModalVisible(true)}
              className="px-4 py-2 rounded-xl text-xs font-bold bg-violet-600 hover:bg-violet-700 text-white flex items-center gap-2 shadow-lg shadow-violet-600/20 transition-all"
            >
              <Plus className="w-4 h-4" />
              Open Term Deposit
            </button>
          )}
        </div>
      </div>

      {/* ── Feedback ── */}
      {error && (
        <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 text-xs flex items-start gap-2">
          <AlertTriangle className="w-4 h-4 shrink-0 mt-0.5" />
          <span>{error}</span>
        </div>
      )}
      {success && (
        <div className="p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-600 dark:text-emerald-400 text-xs flex items-start gap-2 animate-fadeIn">
          <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5" />
          <span>{success}</span>
        </div>
      )}

      {/* ── Portfolio Summary Cards ── */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {[
          { label: 'Total FD Principal', value: formatCurrency(totalPrincipal) + ' ETB', icon: DollarSign, color: 'violet', sub: `${activeCount} active contracts` },
          { label: 'Total Accrued Interest', value: formatCurrency(totalAccrued) + ' ETB', icon: TrendingUp, color: 'emerald', sub: 'Pending capitalization' },
          { label: 'Active Contracts', value: activeCount, icon: ShieldCheck, color: 'blue', sub: 'Funds currently locked' },
          { label: 'Matured (Pending)', value: maturedCount, icon: Clock, color: 'amber', sub: 'Awaiting rollover / payout' },
        ].map(({ label, value, icon: Icon, color, sub }) => (
          <div key={label} className="bdae-card p-5 rounded-2xl border border-[var(--bdae-border)] flex flex-col justify-between">
            <div className={`flex items-center justify-between text-xs font-semibold text-[var(--bdae-text-secondary)]`}>
              <span>{label}</span>
              <Icon className={`w-4 h-4 text-${color}-500`} />
            </div>
            <div className="mt-3">
              <div className={`text-2xl font-black font-mono tracking-tight text-${color}-600 dark:text-${color}-400`}>
                {value}
              </div>
              <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">{sub}</p>
            </div>
          </div>
        ))}
      </div>

      {/* ── GL Info Banner ── */}
      <div className="rounded-xl border border-violet-500/20 bg-violet-500/5 px-4 py-3 flex items-center gap-3 text-xs">
        <ShieldCheck className="w-4 h-4 text-violet-500 shrink-0" />
        <span className="text-[var(--bdae-text-secondary)]">
          <strong className="text-[var(--bdae-text-primary)]">GL Postings:</strong>&nbsp;
          Open → <span className="font-mono text-violet-500">DEBIT GL 1010</span> / <span className="font-mono text-violet-500">CREDIT GL 2060</span>&nbsp;|&nbsp;
          Close → <span className="font-mono text-emerald-500">DEBIT GL 2060</span> / <span className="font-mono text-emerald-500">CREDIT GL 1010</span>&nbsp;|&nbsp;
          Daily Accrual → <span className="font-mono text-amber-500">DEBIT GL 2055</span> / <span className="font-mono text-amber-500">CREDIT GL 2051</span>
        </span>
      </div>

      {/* ── Status Filter Tabs ── */}
      <div className="flex flex-wrap gap-2">
        {STATUS_LABELS.map((s) => (
          <button
            key={s}
            onClick={() => setStatusFilter(s)}
            className={`px-3 py-1.5 rounded-xl text-xs font-semibold border transition-all ${
              statusFilter === s
                ? 'bg-violet-600 text-white border-violet-600 shadow-lg shadow-violet-600/20'
                : 'border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] hover:bg-black/5 dark:hover:bg-white/5'
            }`}
          >
            {s === 'ALL' ? `All (${contracts.length})` : s.replace('_', ' ')}
          </button>
        ))}
      </div>

      {/* ── Contract Cards Grid ── */}
      {isLoading ? (
        <div className="flex items-center justify-center py-16 text-[var(--bdae-text-secondary)]">
          <RefreshCw className="w-5 h-5 animate-spin mr-2" /> Loading contracts...
        </div>
      ) : filtered.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-16 gap-3 text-center">
          <div className="p-5 rounded-2xl bg-violet-500/10 border border-violet-500/20">
            <Landmark className="w-10 h-10 text-violet-400" />
          </div>
          <p className="text-sm font-bold text-[var(--bdae-text-primary)]">No Term Deposits Found</p>
          <p className="text-xs text-[var(--bdae-text-secondary)] max-w-sm">
            {statusFilter === 'ALL'
              ? 'No Fixed Term Deposit contracts exist yet. Open a new FD contract to lock funds at a preferential rate.'
              : `No contracts with status "${statusFilter}".`}
          </p>
          {canManage && statusFilter === 'ALL' && (
            <button
              onClick={() => setIsOpenModalVisible(true)}
              className="mt-2 px-4 py-2 rounded-xl text-xs font-bold bg-violet-600 hover:bg-violet-700 text-white flex items-center gap-2 transition-all"
            >
              <Plus className="w-4 h-4" /> Open First Term Deposit
            </button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-5">
          {filtered.map((contract) => (
            <TermDepositCard
              key={contract.contractId}
              contract={contract}
              canManage={canManage}
              statusColors={STATUS_COLORS}
              onEarlyBreak={() => handleEarlyBreak(contract.contractId)}
            />
          ))}
        </div>
      )}

      {/* ── Open FD Modal ── */}
      {isOpenModalVisible && (
        <OpenTermDepositModal
          onClose={() => setIsOpenModalVisible(false)}
          onSuccess={handleOpenSuccess}
        />
      )}
    </div>
  );
};
