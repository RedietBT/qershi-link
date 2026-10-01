import React from 'react';
import { Calendar, TrendingUp, Scissors, RotateCw, AlertTriangle, CheckCircle2 } from 'lucide-react';
import { formatCurrency } from '../../../common/utils/currency';

const STATUS_ICONS = {
  ACTIVE:        <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500" />,
  MATURED:       <Calendar className="w-3.5 h-3.5 text-amber-500" />,
  CLOSED_NORMAL: <CheckCircle2 className="w-3.5 h-3.5 text-blue-500" />,
  CLOSED_EARLY:  <Scissors className="w-3.5 h-3.5 text-rose-500" />,
  ROLLED_OVER:   <RotateCw className="w-3.5 h-3.5 text-violet-500" />,
  PENDING_APPROVAL: <AlertTriangle className="w-3.5 h-3.5 text-gray-500" />,
};

export const TermDepositCard = ({ contract, canManage, statusColors, onEarlyBreak }) => {
  const c = contract;
  const statusClass = statusColors[c.status] || 'text-gray-500 bg-gray-500/10 border-gray-500/20';

  const principal    = parseFloat(c.principalAmount || 0);
  const accrued      = parseFloat(c.accruedInterest || 0);
  const totalValue   = principal + accrued;
  const daysToMat    = Math.max(0, Math.ceil((new Date(c.maturityDate) - new Date()) / (1000 * 60 * 60 * 24)));
  const tenor        = c.tenorMonths || 0;
  const progressPct  = Math.min(100, Math.max(0, ((tenor * 30 - daysToMat) / (tenor * 30)) * 100));

  return (
    <div className="bdae-card rounded-2xl border border-[var(--bdae-border)] overflow-hidden flex flex-col transition-all hover:shadow-xl hover:shadow-black/10 hover:-translate-y-0.5">
      {/* ── Card Header ── */}
      <div className="px-5 py-4 border-b border-[var(--bdae-border)] flex items-start justify-between">
        <div>
          <p className="text-[11px] font-mono text-[var(--bdae-text-secondary)]">{c.contractNo}</p>
          <p className="text-sm font-bold text-[var(--bdae-text-primary)] mt-0.5">{c.accountNo}</p>
        </div>
        <span className={`flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-bold border ${statusClass}`}>
          {STATUS_ICONS[c.status]}
          {c.status?.replace('_', ' ')}
        </span>
      </div>

      {/* ── Financials ── */}
      <div className="px-5 py-4 space-y-3 flex-1">
        {/* Principal */}
        <div className="flex items-center justify-between">
          <span className="text-xs text-[var(--bdae-text-secondary)]">Principal</span>
          <span className="text-sm font-bold font-mono text-[var(--bdae-text-primary)]">{formatCurrency(principal)} ETB</span>
        </div>

        {/* Accrued Interest */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-1.5 text-xs text-[var(--bdae-text-secondary)]">
            <TrendingUp className="w-3.5 h-3.5 text-emerald-500" />
            <span>Accrued Interest</span>
          </div>
          <span className="text-sm font-bold font-mono text-emerald-600 dark:text-emerald-400">
            +{formatCurrency(accrued)} ETB
          </span>
        </div>

        {/* Total Value */}
        <div className="flex items-center justify-between rounded-lg bg-[var(--bdae-bg-secondary)] border border-[var(--bdae-border)] px-3 py-2">
          <span className="text-xs font-semibold text-[var(--bdae-text-secondary)]">Current Value</span>
          <span className="text-sm font-black font-mono text-violet-600 dark:text-violet-400">{formatCurrency(totalValue)} ETB</span>
        </div>

        {/* Rate & Penalty */}
        <div className="flex gap-3">
          <div className="flex-1 rounded-lg bg-emerald-500/5 border border-emerald-500/15 px-3 py-2 text-center">
            <p className="text-[10px] text-[var(--bdae-text-secondary)] font-semibold uppercase tracking-wide">Rate</p>
            <p className="text-sm font-black font-mono text-emerald-600 dark:text-emerald-400">{c.agreedInterestRatePa}%</p>
            <p className="text-[10px] text-[var(--bdae-text-secondary)]">p.a.</p>
          </div>
          <div className="flex-1 rounded-lg bg-rose-500/5 border border-rose-500/15 px-3 py-2 text-center">
            <p className="text-[10px] text-[var(--bdae-text-secondary)] font-semibold uppercase tracking-wide">Penalty</p>
            <p className="text-sm font-black font-mono text-rose-600 dark:text-rose-400">{c.earlyBreakPenaltyPct}%</p>
            <p className="text-[10px] text-[var(--bdae-text-secondary)]">early break</p>
          </div>
          <div className="flex-1 rounded-lg bg-blue-500/5 border border-blue-500/15 px-3 py-2 text-center">
            <p className="text-[10px] text-[var(--bdae-text-secondary)] font-semibold uppercase tracking-wide">Tenor</p>
            <p className="text-sm font-black font-mono text-blue-600 dark:text-blue-400">{c.tenorMonths}M</p>
            <p className="text-[10px] text-[var(--bdae-text-secondary)]">duration</p>
          </div>
        </div>

        {/* Dates */}
        <div className="space-y-1.5 text-xs">
          <div className="flex justify-between text-[var(--bdae-text-secondary)]">
            <div className="flex items-center gap-1.5">
              <Calendar className="w-3.5 h-3.5" />
              <span>Start</span>
            </div>
            <span className="font-mono text-[var(--bdae-text-primary)]">{c.startDate}</span>
          </div>
          <div className="flex justify-between text-[var(--bdae-text-secondary)]">
            <div className="flex items-center gap-1.5">
              <Calendar className="w-3.5 h-3.5 text-amber-500" />
              <span>Maturity</span>
            </div>
            <span className="font-mono font-bold text-amber-600 dark:text-amber-400">{c.maturityDate}</span>
          </div>
        </div>

        {/* Progress to maturity */}
        {c.status === 'ACTIVE' && (
          <div>
            <div className="flex justify-between text-[11px] text-[var(--bdae-text-secondary)] mb-1">
              <span>Maturity Progress</span>
              <span>{daysToMat} days remaining</span>
            </div>
            <div className="h-1.5 rounded-full bg-black/10 dark:bg-white/10 overflow-hidden">
              <div
                className="h-full rounded-full bg-violet-500 transition-all duration-700"
                style={{ width: `${progressPct.toFixed(0)}%` }}
              />
            </div>
          </div>
        )}

        {/* Auto-rollover badge */}
        {c.autoRollover && (
          <div className="flex items-center gap-1.5 text-[11px] text-violet-600 dark:text-violet-400">
            <RotateCw className="w-3.5 h-3.5" />
            <span className="font-semibold">Auto-Rollover Enabled</span>
          </div>
        )}

        {/* GL Refs */}
        {c.openingGlRef && (
          <div className="text-[10px] font-mono text-[var(--bdae-text-secondary)] truncate" title={c.openingGlRef}>
            GL Ref: {c.openingGlRef}
          </div>
        )}
      </div>

      {/* ── Actions ── */}
      {canManage && c.status === 'ACTIVE' && (
        <div className="px-5 py-3 border-t border-[var(--bdae-border)]">
          <button
            onClick={onEarlyBreak}
            className="w-full flex items-center justify-center gap-2 px-4 py-2 rounded-xl text-xs font-bold border border-rose-500/30 text-rose-600 dark:text-rose-400 hover:bg-rose-500/10 transition-all"
          >
            <Scissors className="w-3.5 h-3.5" />
            Break Early (Penalty Applies)
          </button>
        </div>
      )}
    </div>
  );
};
