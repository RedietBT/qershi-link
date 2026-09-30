import React from 'react';
import { TrendingUp, TrendingDown, ArrowUpRight } from 'lucide-react';
import { formatCurrency } from '../../../common/utils/currency';

export const ProfitLossView = ({ report }) => {
  if (!report) return null;

  const totalRevenue = report.totalRevenue || 0;
  const totalExpense = report.totalExpense || 0;
  const netSurplus = report.netSurplusOrDeficit != null ? report.netSurplusOrDeficit : totalRevenue - totalExpense;
  const isSurplus = netSurplus >= 0;

  return (
    <div className="space-y-4 animate-fadeIn">
      {/* Net Surplus Banner */}
      <div
        className={`p-5 rounded-2xl border flex items-center justify-between gap-4 ${
          isSurplus
            ? 'bg-emerald-500/10 border-emerald-500/20 text-emerald-700 dark:text-emerald-400'
            : 'bg-rose-500/10 border-rose-500/20 text-rose-700 dark:text-rose-400'
        }`}
      >
        <div className="flex items-center gap-3">
          <div
            className={`w-10 h-10 rounded-xl flex items-center justify-center text-white ${
              isSurplus ? 'bg-emerald-500' : 'bg-rose-500'
            }`}
          >
            {isSurplus ? <TrendingUp className="w-5 h-5" /> : <TrendingDown className="w-5 h-5" />}
          </div>
          <div>
            <h4 className="text-xs font-black uppercase tracking-wide">
              {isSurplus ? 'Operating Net Surplus Generated' : 'Operating Net Deficit Incurred'}
            </h4>
            <p className="text-[11px] opacity-90">
              Period: {report.startDate} to {report.endDate}
            </p>
          </div>
        </div>

        <div className="text-right">
          <span className="text-[10px] block uppercase font-bold opacity-75">
            {isSurplus ? 'Net Operating Surplus' : 'Net Deficit'}
          </span>
          <span className="font-mono text-xl font-black">
            {formatCurrency(netSurplus)}
          </span>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Operating Revenue */}
        <div className="bdae-card rounded-2xl border border-[var(--bdae-border)] p-6 space-y-4 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between border-b border-[var(--bdae-border)] pb-3">
              <h3 className="text-sm font-black text-blue-600 dark:text-blue-400 uppercase tracking-wider flex items-center gap-1.5">
                <ArrowUpRight className="w-4 h-4" />
                Operating Revenues & Income
              </h3>
              <span className="font-mono text-xs font-bold text-[var(--bdae-text-secondary)]">
                {report.revenues?.length || 0} Lines
              </span>
            </div>

            <div className="divide-y divide-[var(--bdae-border)] mt-2">
              {(report.revenues || []).map((line) => (
                <div key={line.glCode} className="py-2.5 flex items-center justify-between text-xs">
                  <div>
                    <span className="font-mono font-bold text-[var(--bdae-primary)] mr-2">
                      {line.glCode}
                    </span>
                    <span className="font-medium text-[var(--bdae-text-primary)]">
                      {line.accountName}
                    </span>
                  </div>
                  <span className="font-mono font-bold text-blue-600 dark:text-blue-400">
                    {formatCurrency(line.amount)}
                  </span>
                </div>
              ))}
            </div>
          </div>

          <div className="pt-4 border-t-2 border-[var(--bdae-border)] flex items-center justify-between">
            <span className="text-xs font-black uppercase text-[var(--bdae-text-primary)]">
              Total Operating Revenues
            </span>
            <span className="font-mono text-base font-black text-blue-600 dark:text-blue-400">
              {formatCurrency(totalRevenue)}
            </span>
          </div>
        </div>

        {/* Operating Expenses */}
        <div className="bdae-card rounded-2xl border border-[var(--bdae-border)] p-6 space-y-4 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between border-b border-[var(--bdae-border)] pb-3">
              <h3 className="text-sm font-black text-rose-600 dark:text-rose-400 uppercase tracking-wider flex items-center gap-1.5">
                <TrendingDown className="w-4 h-4" />
                Operating & Financial Expenses
              </h3>
              <span className="font-mono text-xs font-bold text-[var(--bdae-text-secondary)]">
                {report.expenses?.length || 0} Lines
              </span>
            </div>

            <div className="divide-y divide-[var(--bdae-border)] mt-2">
              {(report.expenses || []).map((line) => (
                <div key={line.glCode} className="py-2.5 flex items-center justify-between text-xs">
                  <div>
                    <span className="font-mono font-bold text-[var(--bdae-primary)] mr-2">
                      {line.glCode}
                    </span>
                    <span className="font-medium text-[var(--bdae-text-primary)]">
                      {line.accountName}
                    </span>
                  </div>
                  <span className="font-mono font-bold text-rose-600 dark:text-rose-400">
                    {formatCurrency(line.amount)}
                  </span>
                </div>
              ))}
            </div>
          </div>

          <div className="pt-4 border-t-2 border-[var(--bdae-border)] flex items-center justify-between">
            <span className="text-xs font-black uppercase text-[var(--bdae-text-primary)]">
              Total Operating Expenses
            </span>
            <span className="font-mono text-base font-black text-rose-600 dark:text-rose-400">
              {formatCurrency(totalExpense)}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};
