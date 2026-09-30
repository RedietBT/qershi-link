import React from 'react';
import { TrendingDown, ShieldAlert, Percent, DollarSign } from 'lucide-react';
import { formatCurrency } from '../../../../common/utils/currency';

export const DelinquencyMetricsDeck = ({ summary }) => {
  const par30Ratio = summary?.par30RatioPct ?? 0;
  const par90Ratio = summary?.par90RatioPct ?? 0;
  const totalOverdue = summary?.totalOverdueAmount ?? 0;
  const provision = summary?.totalProvisionRequired ?? 0;

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 animate-fadeIn">
      <div className="bdae-card p-5 rounded-2xl border border-[var(--bdae-border)] flex flex-col justify-between">
        <div className="flex items-center justify-between text-xs text-[var(--bdae-text-secondary)] font-semibold">
          <span>PAR 30 Ratio</span>
          <Percent className="w-4 h-4 text-amber-500" />
        </div>
        <div className="mt-3">
          <div className="text-2xl font-black tracking-tight text-amber-600 dark:text-amber-400 font-mono">
            {par30Ratio.toFixed(2)}%
          </div>
          <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
            Overdue &gt; 30 Days (Watchlist)
          </p>
        </div>
      </div>

      <div className="bdae-card p-5 rounded-2xl border border-[var(--bdae-border)] flex flex-col justify-between">
        <div className="flex items-center justify-between text-xs text-[var(--bdae-text-secondary)] font-semibold">
          <span>PAR 90 / NPL Ratio</span>
          <TrendingDown className="w-4 h-4 text-red-500" />
        </div>
        <div className="mt-3">
          <div className="text-2xl font-black tracking-tight text-red-600 dark:text-red-400 font-mono">
            {par90Ratio.toFixed(2)}%
          </div>
          <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
            Non-Performing Loans (&gt; 90 DPD)
          </p>
        </div>
      </div>

      <div className="bdae-card p-5 rounded-2xl border border-[var(--bdae-border)] flex flex-col justify-between">
        <div className="flex items-center justify-between text-xs text-[var(--bdae-text-secondary)] font-semibold">
          <span>Total Overdue Portfolio</span>
          <DollarSign className="w-4 h-4 text-rose-500" />
        </div>
        <div className="mt-3">
          <div className="text-2xl font-black tracking-tight text-[var(--bdae-text-primary)] font-mono">
            {formatCurrency(totalOverdue)}
          </div>
          <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
            Aggregated Arrears at Risk
          </p>
        </div>
      </div>

      <div className="bdae-card p-5 rounded-2xl border border-[var(--bdae-border)] flex flex-col justify-between">
        <div className="flex items-center justify-between text-xs text-[var(--bdae-text-secondary)] font-semibold">
          <span>Statutory Loss Provision</span>
          <ShieldAlert className="w-4 h-4 text-purple-500" />
        </div>
        <div className="mt-3">
          <div className="text-2xl font-black tracking-tight text-purple-600 dark:text-purple-400 font-mono">
            {formatCurrency(provision)}
          </div>
          <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
            Required Impairment Reserve
          </p>
        </div>
      </div>
    </div>
  );
};
