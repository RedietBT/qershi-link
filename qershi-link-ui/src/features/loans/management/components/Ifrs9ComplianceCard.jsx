import React, { useState, useEffect } from 'react';
import {
  BookOpen,
  RefreshCw,
  RotateCw,
  CheckCircle2,
  AlertTriangle,
  TrendingDown,
  ShieldCheck,
  ChevronDown,
  ChevronUp,
  FileText
} from 'lucide-react';
import { loanMgmtHttpClient } from '../../../../common/api/httpClient';
import { formatCurrency } from '../../../../common/utils/currency';

/**
 * IFRS 9 / NBE Regulatory Loan Loss Provisioning Compliance Card.
 *
 * Displays the latest month-end ECL provision run with:
 *   - Five NBE risk stage breakdown (Pass → Loss)
 *   - GL posting reference (DEBIT 5030 / CREDIT 1039)
 *   - Manual trigger for SACCO_ADMIN / SUPER_ADMIN
 */
export const Ifrs9ComplianceCard = ({ canTrigger }) => {
  const [run, setRun] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isRunning, setIsRunning] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [expanded, setExpanded] = useState(false);

  const fetchLatest = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const res = await loanMgmtHttpClient.get('/loans/ifrs9-provisioning/latest');
      setRun(res.data || null);
    } catch (err) {
      if (err.response?.status === 204) {
        setRun(null); // No run yet — that's fine
      } else {
        setError(err.response?.data?.message || 'Failed to load IFRS 9 provisioning data.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchLatest();
  }, []);

  const handleTriggerRun = async () => {
    if (isRunning) return;
    setIsRunning(true);
    setError(null);
    setSuccess(null);
    try {
      const res = await loanMgmtHttpClient.post('/loans/ifrs9-provisioning/run');
      setSuccess(
        `IFRS 9 provisioning completed. GL Ref: ${res.data?.glPostingRef}. ` +
        `Total provision: ${formatCurrency(res.data?.totalProvisionRequired)} ETB ` +
        `(DEBIT GL 5030 / CREDIT GL 1039).`
      );
      await fetchLatest();
    } catch (err) {
      setError(err.response?.data?.message || 'IFRS 9 provisioning run failed. Check audit logs.');
    } finally {
      setIsRunning(false);
    }
  };

  // ── NBE Risk Stage Configuration ──────────────────────────────────────────
  const stages = run
    ? [
        {
          label: 'Pass',
          subtitle: '0–29 DPD',
          rate: '1%',
          balance: run.passBalance,
          provision: run.passProvision,
          color: 'emerald',
          barColor: 'bg-emerald-500',
          textColor: 'text-emerald-600 dark:text-emerald-400',
          bgColor: 'bg-emerald-500/10',
          borderColor: 'border-emerald-500/20',
        },
        {
          label: 'Special Mention',
          subtitle: '30–89 DPD',
          rate: '5%',
          balance: run.specialMentionBalance,
          provision: run.specialMentionProvision,
          color: 'yellow',
          barColor: 'bg-yellow-500',
          textColor: 'text-yellow-600 dark:text-yellow-400',
          bgColor: 'bg-yellow-500/10',
          borderColor: 'border-yellow-500/20',
        },
        {
          label: 'Substandard',
          subtitle: '90–179 DPD',
          rate: '20%',
          balance: run.substandardBalance,
          provision: run.substandardProvision,
          color: 'orange',
          barColor: 'bg-orange-500',
          textColor: 'text-orange-600 dark:text-orange-400',
          bgColor: 'bg-orange-500/10',
          borderColor: 'border-orange-500/20',
        },
        {
          label: 'Doubtful',
          subtitle: '180–359 DPD',
          rate: '50%',
          balance: run.doubtfulBalance,
          provision: run.doubtfulProvision,
          color: 'red',
          barColor: 'bg-red-500',
          textColor: 'text-red-600 dark:text-red-400',
          bgColor: 'bg-red-500/10',
          borderColor: 'border-red-500/20',
        },
        {
          label: 'Loss',
          subtitle: '360+ DPD',
          rate: '100%',
          balance: run.lossBalance,
          provision: run.lossProvision,
          color: 'rose',
          barColor: 'bg-rose-700',
          textColor: 'text-rose-700 dark:text-rose-400',
          bgColor: 'bg-rose-700/10',
          borderColor: 'border-rose-700/20',
        },
      ]
    : [];

  const totalPortfolio = run?.totalPortfolioBalance ?? 0;
  const totalProvision = run?.totalProvisionRequired ?? 0;
  const provisionCoverageRatio =
    totalPortfolio > 0 ? ((totalProvision / totalPortfolio) * 100).toFixed(2) : '—';

  return (
    <div className="bdae-card rounded-2xl border border-[var(--bdae-border)] overflow-hidden animate-fadeIn">
      {/* ── Card Header ── */}
      <div className="px-6 py-5 border-b border-[var(--bdae-border)] flex flex-col md:flex-row md:items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-violet-500/10 border border-violet-500/20">
            <BookOpen className="w-5 h-5 text-violet-500" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-sm font-bold text-[var(--bdae-text-primary)]">
                IFRS 9 / NBE Loan Loss Provisioning
              </h2>
              {run?.status === 'COMPLETED' && (
                <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/20">
                  GL POSTED
                </span>
              )}
            </div>
            <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-0.5">
              Regulatory ECL impairment reserve per NBE Directive — DEBIT GL 5030 / CREDIT GL 1039
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2 shrink-0">
          <button
            onClick={fetchLatest}
            disabled={isLoading || isRunning}
            className="p-2 rounded-xl border border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5 text-[var(--bdae-text-secondary)] transition-all disabled:opacity-50"
            title="Refresh"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />
          </button>

          {canTrigger && (
            <button
              id="ifrs9-trigger-btn"
              onClick={handleTriggerRun}
              disabled={isRunning}
              className="px-4 py-2 rounded-xl text-xs font-bold bg-violet-600 hover:bg-violet-700 text-white flex items-center gap-2 shadow-lg shadow-violet-600/20 transition-all disabled:opacity-50"
            >
              <RotateCw className={`w-3.5 h-3.5 ${isRunning ? 'animate-spin' : ''}`} />
              <span>{isRunning ? 'Running Provision...' : 'Run Month-End Provision'}</span>
            </button>
          )}
        </div>
      </div>

      {/* ── Feedback ── */}
      {error && (
        <div className="mx-6 mt-4 p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 text-xs flex items-start gap-2">
          <AlertTriangle className="w-4 h-4 shrink-0 mt-0.5" />
          <span>{error}</span>
        </div>
      )}
      {success && (
        <div className="mx-6 mt-4 p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-600 dark:text-emerald-400 text-xs flex items-start gap-2 animate-fadeIn">
          <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5" />
          <span>{success}</span>
        </div>
      )}

      {/* ── Body ── */}
      <div className="px-6 py-5">
        {isLoading ? (
          <div className="flex items-center justify-center py-12 text-[var(--bdae-text-secondary)] text-sm">
            <RefreshCw className="w-5 h-5 animate-spin mr-2" />
            Loading provision data...
          </div>
        ) : !run ? (
          <div className="flex flex-col items-center justify-center py-10 gap-3 text-center">
            <div className="p-4 rounded-2xl bg-violet-500/10 border border-violet-500/20">
              <TrendingDown className="w-8 h-8 text-violet-400" />
            </div>
            <p className="text-sm font-semibold text-[var(--bdae-text-primary)]">No Provision Run Yet</p>
            <p className="text-xs text-[var(--bdae-text-secondary)] max-w-xs">
              No IFRS 9 month-end provisioning run found. The EOD batch auto-triggers this on month-end, or you can run it manually.
            </p>
          </div>
        ) : (
          <>
            {/* ── Top Summary Row ── */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
              <div className="rounded-xl bg-[var(--bdae-bg-secondary)] border border-[var(--bdae-border)] p-4">
                <p className="text-[11px] font-semibold text-[var(--bdae-text-secondary)] uppercase tracking-wide">Total Portfolio</p>
                <p className="text-xl font-black font-mono text-[var(--bdae-text-primary)] mt-1">{formatCurrency(totalPortfolio)}</p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-0.5">{run.totalLoansEvaluated} loans evaluated</p>
              </div>
              <div className="rounded-xl bg-violet-600/10 border border-violet-500/20 p-4">
                <p className="text-[11px] font-semibold text-violet-500 uppercase tracking-wide">Total ECL Provision Required</p>
                <p className="text-xl font-black font-mono text-violet-600 dark:text-violet-400 mt-1">{formatCurrency(totalProvision)}</p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-0.5">Coverage ratio: {provisionCoverageRatio}%</p>
              </div>
              <div className="rounded-xl bg-[var(--bdae-bg-secondary)] border border-[var(--bdae-border)] p-4">
                <p className="text-[11px] font-semibold text-[var(--bdae-text-secondary)] uppercase tracking-wide">Business Date</p>
                <p className="text-xl font-black font-mono text-[var(--bdae-text-primary)] mt-1">{run.businessDate}</p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-0.5">Run type: {run.runType}</p>
              </div>
            </div>

            {/* ── NBE Stage Breakdown ── */}
            <div className="space-y-2.5">
              <p className="text-xs font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider mb-3">
                NBE / IFRS 9 Risk Stage Classification
              </p>
              {stages.map((stage) => {
                const balanceNum = parseFloat(stage.balance) || 0;
                const pct = totalPortfolio > 0 ? (balanceNum / totalPortfolio) * 100 : 0;
                return (
                  <div
                    key={stage.label}
                    className={`rounded-xl border ${stage.borderColor} ${stage.bgColor} p-4`}
                  >
                    <div className="flex items-center justify-between mb-2">
                      <div className="flex items-center gap-3">
                        <div className={`w-2.5 h-2.5 rounded-full ${stage.barColor} shrink-0`} />
                        <div>
                          <span className={`text-sm font-bold ${stage.textColor}`}>{stage.label}</span>
                          <span className="ml-2 text-[11px] text-[var(--bdae-text-secondary)]">
                            {stage.subtitle}
                          </span>
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="text-xs font-mono font-bold text-[var(--bdae-text-primary)]">
                          {formatCurrency(stage.balance)} ETB
                        </div>
                        <div className={`text-[11px] font-semibold ${stage.textColor}`}>
                          @ {stage.rate} → {formatCurrency(stage.provision)} provision
                        </div>
                      </div>
                    </div>
                    {/* Progress bar */}
                    <div className="h-1.5 rounded-full bg-black/10 dark:bg-white/10 overflow-hidden">
                      <div
                        className={`h-full rounded-full ${stage.barColor} transition-all duration-700`}
                        style={{ width: `${Math.min(pct, 100).toFixed(1)}%` }}
                      />
                    </div>
                    <div className="text-[10px] text-[var(--bdae-text-secondary)] mt-1">
                      {pct.toFixed(1)}% of portfolio
                    </div>
                  </div>
                );
              })}
            </div>

            {/* ── GL Posting Reference (Expandable) ── */}
            <div className="mt-5 rounded-xl border border-[var(--bdae-border)] overflow-hidden">
              <button
                onClick={() => setExpanded((v) => !v)}
                className="w-full flex items-center justify-between px-4 py-3 text-xs font-semibold text-[var(--bdae-text-secondary)] hover:bg-black/5 dark:hover:bg-white/5 transition-all"
              >
                <div className="flex items-center gap-2">
                  <FileText className="w-4 h-4" />
                  <span>GL Journal Entry Details</span>
                  <span className="px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/20 text-[10px] font-bold">
                    POSTED
                  </span>
                </div>
                {expanded ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
              </button>

              {expanded && (
                <div className="px-4 pb-4 border-t border-[var(--bdae-border)] animate-fadeIn">
                  <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
                    <div className="rounded-lg bg-red-500/5 border border-red-500/15 p-3">
                      <p className="text-[11px] font-bold text-red-600 dark:text-red-400 uppercase tracking-wide mb-1.5">DEBIT</p>
                      <p className="font-mono font-bold text-[var(--bdae-text-primary)]">GL {run.glDebitAccount}</p>
                      <p className="text-[var(--bdae-text-secondary)]">Loan Impairment Loss Expense</p>
                      <p className="font-mono font-bold text-red-600 dark:text-red-400 mt-2">{formatCurrency(totalProvision)} ETB</p>
                    </div>
                    <div className="rounded-lg bg-emerald-500/5 border border-emerald-500/15 p-3">
                      <p className="text-[11px] font-bold text-emerald-600 dark:text-emerald-400 uppercase tracking-wide mb-1.5">CREDIT</p>
                      <p className="font-mono font-bold text-[var(--bdae-text-primary)]">GL {run.glCreditAccount}</p>
                      <p className="text-[var(--bdae-text-secondary)]">Allowance for Credit Losses</p>
                      <p className="font-mono font-bold text-emerald-600 dark:text-emerald-400 mt-2">{formatCurrency(totalProvision)} ETB</p>
                    </div>
                  </div>
                  <div className="mt-3 flex flex-wrap gap-3 text-xs">
                    <div className="flex items-center gap-2 rounded-lg bg-[var(--bdae-bg-secondary)] border border-[var(--bdae-border)] px-3 py-2">
                      <ShieldCheck className="w-3.5 h-3.5 text-violet-500" />
                      <span className="text-[var(--bdae-text-secondary)]">Reference:</span>
                      <span className="font-mono font-bold text-[var(--bdae-text-primary)]">{run.glPostingRef}</span>
                    </div>
                    {run.glPostedAt && (
                      <div className="flex items-center gap-2 rounded-lg bg-[var(--bdae-bg-secondary)] border border-[var(--bdae-border)] px-3 py-2">
                        <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500" />
                        <span className="text-[var(--bdae-text-secondary)]">Posted at:</span>
                        <span className="font-mono text-[var(--bdae-text-primary)]">
                          {new Date(run.glPostedAt).toLocaleString()}
                        </span>
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>
          </>
        )}
      </div>
    </div>
  );
};
