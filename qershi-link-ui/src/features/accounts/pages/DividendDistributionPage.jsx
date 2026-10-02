import React, { useState, useEffect, useCallback } from 'react';
import {
  TrendingUp, RefreshCw, PlayCircle, CheckCircle2, AlertCircle,
  BarChart3, Users, DollarSign, Percent, BookOpen, ChevronDown,
  ChevronRight, Banknote, ShieldCheck, Calculator,
} from 'lucide-react';
import { dividendApi } from '../api/dividendApi';
import { useAuthStore } from '../../../common/store/useAuthStore';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';
import { formatCurrency } from '../../../common/utils/currency';

const STATUS_PILL = {
  DRAFT:     'bg-gray-500/20 text-gray-400 border-gray-500/30',
  SIMULATED: 'bg-amber-500/20 text-amber-400 border-amber-500/30',
  POSTED:    'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
};

/**
 * DividendDistributionModal — simulate → post two-phase workflow.
 * Uses dividendApi module + PermissionGuard on each action button.
 */
export const DividendDistributionModal = ({ onClose, onSuccess }) => {
  const [fiscalYear, setFiscalYear] = useState(new Date().getFullYear() - 1);
  const [netProfit, setNetProfit] = useState('');
  const [declaredRate, setDeclaredRate] = useState('12');
  const [simulationResult, setSimulationResult] = useState(null);
  const [isSimulating, setIsSimulating] = useState(false);
  const [isPosting, setIsPosting] = useState(false);
  const [error, setError] = useState(null);

  const handleSimulate = async (e) => {
    e.preventDefault();
    setIsSimulating(true);
    setError(null);
    setSimulationResult(null);
    try {
      const data = await dividendApi.simulate({
        fiscalYear: Number(fiscalYear),
        netProfitPool: parseFloat(netProfit),
        declaredRatePercent: parseFloat(declaredRate),
      });
      setSimulationResult(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Simulation failed.');
    } finally {
      setIsSimulating(false);
    }
  };

  const handlePost = async () => {
    if (!simulationResult?.distributionId) return;
    if (!window.confirm(`Post FY${fiscalYear} dividends to ${simulationResult.qualifiedMembersCount} members? This cannot be undone.`)) return;
    setIsPosting(true);
    setError(null);
    try {
      const data = await dividendApi.post(simulationResult.distributionId);
      onSuccess?.(`✅ FY${fiscalYear} dividends posted! GL: ${data.glJournalRef}. Posted: ${data.successfulPostings} members, ${formatCurrency(data.totalNetPosted)} ETB`);
      onClose?.();
    } catch (err) {
      setError(err.response?.data?.message || 'Posting failed.');
    } finally {
      setIsPosting(false);
    }
  };

  const estimatedDividend = netProfit && declaredRate
    ? (parseFloat(netProfit) * parseFloat(declaredRate) / 100) : 0;

  return (
    <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div className="bdae-surface rounded-2xl border border-[var(--bdae-border)] w-full max-w-3xl shadow-2xl max-h-[90vh] flex flex-col">
        <div className="px-6 py-4 border-b border-[var(--bdae-border)] flex items-center justify-between shrink-0">
          <h2 className="font-bold text-[var(--bdae-text-primary)] flex items-center gap-2 text-base">
            <TrendingUp className="w-5 h-5 text-[#00CDDB]" /> AGM Dividend Distribution Engine
          </h2>
          <button onClick={onClose} className="text-[var(--bdae-text-secondary)] text-xl leading-none">✕</button>
        </div>
        <div className="flex-1 overflow-y-auto">
          {/* Config Form */}
          <form onSubmit={handleSimulate} className="p-6 space-y-5 border-b border-[var(--bdae-border)]">
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div>
                <label className="block text-xs font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider mb-1.5">Fiscal Year *</label>
                <input id="div-fiscal-year" type="number" min="2020" max={new Date().getFullYear()} value={fiscalYear}
                  onChange={(e) => setFiscalYear(e.target.value)}
                  className="w-full px-3 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] text-sm focus:outline-none focus:border-[#00CDDB]" required />
              </div>
              <div>
                <label className="block text-xs font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider mb-1.5">Net Profit Pool (ETB) *</label>
                <input id="div-net-profit" type="number" min="1" step="0.01" placeholder="e.g. 5,000,000" value={netProfit}
                  onChange={(e) => setNetProfit(e.target.value)}
                  className="w-full px-3 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] text-sm focus:outline-none focus:border-[#00CDDB]" required />
              </div>
              <div>
                <label className="block text-xs font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider mb-1.5">Declared Rate (%) *</label>
                <input id="div-declared-rate" type="number" min="0.01" max="100" step="0.01" value={declaredRate}
                  onChange={(e) => setDeclaredRate(e.target.value)}
                  className="w-full px-3 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] text-sm focus:outline-none focus:border-[#00CDDB]" required />
              </div>
            </div>

            {estimatedDividend > 0 && (
              <div className="rounded-xl bg-[#00CDDB]/5 border border-[#00CDDB]/20 p-4 flex items-center gap-4 flex-wrap">
                <Calculator className="w-4 h-4 text-[#00CDDB] shrink-0" />
                <div className="flex gap-6 flex-wrap">
                  <div><p className="text-[10px] text-[var(--bdae-text-secondary)] uppercase tracking-wider">Est. Gross Pool</p><p className="text-sm font-bold text-[#00CDDB]">{formatCurrency(estimatedDividend)} ETB</p></div>
                  <div><p className="text-[10px] text-[var(--bdae-text-secondary)] uppercase tracking-wider">Est. WHT (5%)</p><p className="text-sm font-bold text-red-400">−{formatCurrency(estimatedDividend * 0.05)} ETB</p></div>
                  <div><p className="text-[10px] text-[var(--bdae-text-secondary)] uppercase tracking-wider">Est. Net Payout</p><p className="text-sm font-bold text-emerald-400">{formatCurrency(estimatedDividend * 0.95)} ETB</p></div>
                </div>
              </div>
            )}

            {error && (
              <div className="flex items-center gap-3 px-4 py-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-400 text-sm">
                <AlertCircle className="w-4 h-4 shrink-0" /> {error}
              </div>
            )}

            <div className="flex gap-3">
              {/* Simulate — DIVIDEND_SIMULATE */}
              <PermissionGuard
                roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN']}
                permissions={[PERMISSIONS.DIVIDEND_SIMULATE]}
              >
                <button type="submit" id="simulate-dividend-btn" disabled={isSimulating}
                  className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-amber-500 text-white text-sm font-bold hover:bg-amber-500/90 disabled:opacity-50 transition-all">
                  <Calculator className="w-4 h-4" />
                  {isSimulating ? 'Simulating…' : `Simulate FY${fiscalYear}`}
                </button>
              </PermissionGuard>
              {/* Post — DIVIDEND_POST (promoted privilege) */}
              {simulationResult && (
                <PermissionGuard
                  roles={['SUPER_ADMIN', 'SACCO_ADMIN']}
                  permissions={[PERMISSIONS.DIVIDEND_POST]}
                >
                  <button type="button" id="post-dividend-btn" onClick={handlePost} disabled={isPosting}
                    className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-emerald-500 text-white text-sm font-bold hover:bg-emerald-500/90 disabled:opacity-50 transition-all">
                    <ShieldCheck className="w-4 h-4" />
                    {isPosting ? 'Posting…' : 'Post to Member Accounts'}
                  </button>
                </PermissionGuard>
              )}
            </div>
          </form>

          {/* Simulation Results */}
          {simulationResult && (
            <div className="p-6 space-y-5">
              <h3 className="text-sm font-bold text-[var(--bdae-text-primary)] flex items-center gap-2">
                <BarChart3 className="w-4 h-4 text-[#00CDDB]" /> Simulation Results — FY{simulationResult.fiscalYear}
              </h3>
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                {[
                  { icon: Users, label: 'Qualified Members', value: simulationResult.qualifiedMembersCount, color: 'text-[#00CDDB]' },
                  { icon: Banknote, label: 'Gross Pool', value: `${formatCurrency(simulationResult.totalGrossDividend)} ETB`, color: 'text-[var(--bdae-text-primary)]' },
                  { icon: Percent, label: '5% WHT', value: `${formatCurrency(simulationResult.totalTaxWithheld)} ETB`, color: 'text-red-400' },
                  { icon: DollarSign, label: 'Net Payout', value: `${formatCurrency(simulationResult.totalNetPayout)} ETB`, color: 'text-emerald-400' },
                ].map(({ icon: Icon, label, value, color }) => (
                  <div key={label} className="bdae-surface rounded-xl border border-[var(--bdae-border)] p-3 text-center space-y-1">
                    <Icon className={`w-4 h-4 mx-auto ${color}`} />
                    <p className={`text-sm font-bold ${color}`}>{value}</p>
                    <p className="text-[10px] text-[var(--bdae-text-secondary)] uppercase tracking-wider">{label}</p>
                  </div>
                ))}
              </div>
              <div>
                <h4 className="text-xs font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider mb-2 flex items-center gap-2">
                  <BookOpen className="w-3.5 h-3.5" /> Per-Member Preview (top {Math.min(simulationResult.allocations?.length || 0, 20)})
                </h4>
                <div className="rounded-xl border border-[var(--bdae-border)] overflow-hidden">
                  <table className="w-full text-xs">
                    <thead>
                      <tr className="bg-black/[0.02] dark:bg-white/[0.02] border-b border-[var(--bdae-border)]">
                        {['Member ID', 'Shares', 'Gross', '5% WHT', 'Net', 'Dest. Account'].map((h) => (
                          <th key={h} className="px-3 py-2.5 text-left font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider whitespace-nowrap">{h}</th>
                        ))}
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-[var(--bdae-border)]">
                      {(simulationResult.allocations || []).slice(0, 20).map((a) => (
                        <tr key={a.id} className="hover:bg-black/[0.01] dark:hover:bg-white/[0.01]">
                          <td className="px-3 py-2 font-mono text-[10px] text-[var(--bdae-text-secondary)] truncate max-w-[100px]">{a.memberId}</td>
                          <td className="px-3 py-2 font-bold text-[var(--bdae-text-primary)]">{parseFloat(a.weightedAverageShares).toFixed(0)}</td>
                          <td className="px-3 py-2">{formatCurrency(a.grossDividend)}</td>
                          <td className="px-3 py-2 text-red-400">−{formatCurrency(a.taxWithheld)}</td>
                          <td className="px-3 py-2 font-bold text-emerald-400">{formatCurrency(a.netDividendPayout)}</td>
                          <td className="px-3 py-2 font-mono text-[10px] text-[var(--bdae-text-secondary)]">
                            {a.destinationAccountNumber || <span className="text-amber-400">⚠ No account</span>}
                          </td>
                        </tr>
                      ))}
                      {(simulationResult.allocations?.length || 0) > 20 && (
                        <tr>
                          <td colSpan={6} className="px-3 py-2 text-center text-[var(--bdae-text-secondary)] text-[11px]">
                            … and {simulationResult.allocations.length - 20} more members
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

/**
 * DividendDistributionPage — history view with expandable allocations.
 * Uses dividendApi module + PermissionGuard on action buttons.
 */
export const DividendDistributionPage = () => {
  const [distributions, setDistributions] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [expandedId, setExpandedId] = useState(null);
  const [allocations, setAllocations] = useState({});

  const fetchDistributions = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await dividendApi.getAllDistributions();
      setDistributions(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load dividend history.');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => { fetchDistributions(); }, [fetchDistributions]);

  const handleExpandAllocations = async (distributionId) => {
    if (expandedId === distributionId) { setExpandedId(null); return; }
    setExpandedId(distributionId);
    if (!allocations[distributionId]) {
      try {
        const data = await dividendApi.getAllocations(distributionId);
        setAllocations((prev) => ({ ...prev, [distributionId]: data }));
      } catch { /* ignore */ }
    }
  };

  return (
    <div className="p-6 space-y-6 animate-fadeIn">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-extrabold text-[var(--bdae-text-primary)] flex items-center gap-2">
            <TrendingUp className="w-7 h-7 text-[#00CDDB]" /> AGM Dividend Distributions
          </h1>
          <p className="text-sm text-[var(--bdae-text-secondary)] mt-0.5">
            Annual dividend engine — weighted-share calculation with 5% NBE WHT
          </p>
        </div>
        <div className="flex gap-2">
          <button onClick={fetchDistributions} className="p-2 rounded-xl border border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] hover:text-[#00CDDB] transition-colors">
            <RefreshCw className="w-4 h-4" />
          </button>
          {/* New Distribution — DIVIDEND_SIMULATE */}
          <PermissionGuard
            roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN']}
            permissions={[PERMISSIONS.DIVIDEND_SIMULATE]}
          >
            <button
              id="new-dividend-distribution-btn"
              onClick={() => setShowModal(true)}
              className="flex items-center gap-2 px-4 py-2 rounded-xl bg-[#00CDDB] text-white text-xs font-bold hover:bg-[#00CDDB]/90 shadow-sm shadow-[#00CDDB]/30 transition-all"
            >
              <PlayCircle className="w-3.5 h-3.5" /> New Distribution
            </button>
          </PermissionGuard>
        </div>
      </div>

      {error && (
        <div className="flex items-center gap-3 px-4 py-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-400 text-sm">
          <AlertCircle className="w-4 h-4 shrink-0" /> {error}
          <button onClick={() => setError(null)} className="ml-auto text-xs opacity-60 hover:opacity-100">✕</button>
        </div>
      )}
      {success && (
        <div className="flex items-center gap-3 px-4 py-3 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-sm">
          <CheckCircle2 className="w-4 h-4 shrink-0" /> {success}
          <button onClick={() => setSuccess(null)} className="ml-auto text-xs opacity-60 hover:opacity-100">✕</button>
        </div>
      )}

      {isLoading ? (
        <div className="flex items-center justify-center py-20"><RefreshCw className="w-6 h-6 text-[#00CDDB] animate-spin" /></div>
      ) : distributions.length === 0 ? (
        <div className="bdae-surface rounded-2xl border border-[var(--bdae-border)] py-16 text-center">
          <TrendingUp className="w-10 h-10 text-[var(--bdae-text-secondary)] mx-auto mb-3 opacity-40" />
          <p className="text-[var(--bdae-text-secondary)] text-sm">No dividend distributions on record yet.</p>
          <PermissionGuard roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN']} permissions={[PERMISSIONS.DIVIDEND_SIMULATE]}>
            <button onClick={() => setShowModal(true)} className="mt-4 px-5 py-2 rounded-xl bg-[#00CDDB] text-white text-xs font-bold hover:bg-[#00CDDB]/90 transition-all">
              Run First Distribution
            </button>
          </PermissionGuard>
        </div>
      ) : (
        <div className="space-y-3">
          {distributions.map((dist) => (
            <div key={dist.id} className="bdae-surface rounded-2xl border border-[var(--bdae-border)] overflow-hidden">
              <div className="px-5 py-4 flex items-center justify-between flex-wrap gap-3">
                <div className="flex items-center gap-4 flex-wrap">
                  <div className="text-center">
                    <p className="text-2xl font-black text-[#00CDDB]">FY{dist.fiscalYear}</p>
                    <p className="text-[10px] text-[var(--bdae-text-secondary)] uppercase tracking-wider">Fiscal Year</p>
                  </div>
                  <div className="flex gap-4 flex-wrap text-sm">
                    <div><p className="text-[10px] text-[var(--bdae-text-secondary)] uppercase tracking-wider">Declared Rate</p><p className="font-bold text-[var(--bdae-text-primary)]">{dist.declaredRatePercent}%</p></div>
                    <div><p className="text-[10px] text-[var(--bdae-text-secondary)] uppercase tracking-wider">Profit Pool</p><p className="font-bold text-[var(--bdae-text-primary)]">{formatCurrency(dist.netProfitPool)} ETB</p></div>
                    <div><p className="text-[10px] text-[var(--bdae-text-secondary)] uppercase tracking-wider">Net Distributed</p><p className="font-bold text-emerald-400">{formatCurrency((dist.totalDividendDistributed || 0) - (dist.totalTaxWithheld || 0))} ETB</p></div>
                    <div><p className="text-[10px] text-[var(--bdae-text-secondary)] uppercase tracking-wider">Members</p><p className="font-bold text-[var(--bdae-text-primary)]">{dist.qualifiedMembersCount}</p></div>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <span className={`px-2.5 py-1 rounded-full border text-[11px] font-bold uppercase ${STATUS_PILL[dist.status] || 'bg-gray-500/20 text-gray-400'}`}>{dist.status}</span>
                  {/* View allocations — DIVIDEND_VIEW */}
                  <PermissionGuard
                    roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'AUDITOR']}
                    permissions={[PERMISSIONS.DIVIDEND_VIEW, PERMISSIONS.FINANCIAL_REPORT_VIEW]}
                  >
                    {dist.status !== 'DRAFT' && (
                      <button id={`expand-dist-${dist.id}`} onClick={() => handleExpandAllocations(dist.id)}
                        className="flex items-center gap-1 text-[11px] text-[var(--bdae-text-secondary)] hover:text-[#00CDDB] transition-colors">
                        {expandedId === dist.id ? <ChevronDown className="w-3.5 h-3.5" /> : <ChevronRight className="w-3.5 h-3.5" />}
                        Allocations
                      </button>
                    )}
                  </PermissionGuard>
                </div>
              </div>
              {dist.glJournalRef && (
                <div className="px-5 pb-3 text-[11px] text-[var(--bdae-text-secondary)] font-mono">
                  GL Ref: {dist.glJournalRef} · Posted: {dist.postedAt ? new Date(dist.postedAt).toLocaleDateString() : '—'}
                </div>
              )}
              {expandedId === dist.id && allocations[dist.id] && (
                <div className="border-t border-[var(--bdae-border)] overflow-x-auto">
                  <table className="w-full text-xs">
                    <thead>
                      <tr className="bg-black/[0.02] dark:bg-white/[0.02] border-b border-[var(--bdae-border)]">
                        {['Member ID', 'Shares', 'Gross', 'WHT', 'Net', 'Destination', 'Status'].map((h) => (
                          <th key={h} className="px-3 py-2 text-left font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider whitespace-nowrap">{h}</th>
                        ))}
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-[var(--bdae-border)]">
                      {(allocations[dist.id] || []).slice(0, 50).map((a) => (
                        <tr key={a.id} className="hover:bg-black/[0.01] dark:hover:bg-white/[0.01]">
                          <td className="px-3 py-2 font-mono text-[10px] text-[var(--bdae-text-secondary)] truncate max-w-[100px]">{a.memberId}</td>
                          <td className="px-3 py-2 font-bold text-[var(--bdae-text-primary)]">{parseFloat(a.weightedAverageShares).toFixed(0)}</td>
                          <td className="px-3 py-2">{formatCurrency(a.grossDividend)}</td>
                          <td className="px-3 py-2 text-red-400">{formatCurrency(a.taxWithheld)}</td>
                          <td className="px-3 py-2 font-bold text-emerald-400">{formatCurrency(a.netDividendPayout)}</td>
                          <td className="px-3 py-2 font-mono text-[10px] text-[var(--bdae-text-secondary)]">{a.destinationAccountNumber || '—'}</td>
                          <td className="px-3 py-2">
                            <span className={`px-1.5 py-0.5 rounded-full text-[10px] font-bold ${a.status === 'POSTED' ? 'bg-emerald-500/20 text-emerald-400' : a.status === 'FAILED' ? 'bg-red-500/20 text-red-400' : 'bg-amber-500/20 text-amber-400'}`}>{a.status}</span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {showModal && (
        <DividendDistributionModal
          onClose={() => setShowModal(false)}
          onSuccess={(msg) => { setSuccess(msg); setShowModal(false); fetchDistributions(); }}
        />
      )}
    </div>
  );
};
