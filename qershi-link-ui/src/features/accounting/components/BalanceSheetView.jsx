import React from 'react';
import { Scale } from 'lucide-react';
import { formatCurrency } from '../../../common/utils/currency';
import { BalanceStatusBanner } from './BalanceStatusBanner';

export const BalanceSheetView = ({ report }) => {
  if (!report) return null;

  const totalAssets = report.totalAssets || 0;
  const totalLiabilities = report.totalLiabilities || 0;
  const totalEquity = report.totalEquity || 0;
  const totalLiabEquity = report.totalLiabilitiesAndEquity || totalLiabilities + totalEquity;
  const isBalanced = report.isBalanced;
  const variance = report.variance || Math.abs(totalAssets - totalLiabEquity);

  return (
    <div className="space-y-4 animate-fadeIn">
      {/* Fundamental Equation Status Banner */}
      <BalanceStatusBanner
        isBalanced={isBalanced}
        title="Balance Sheet Accounting Equation (Assets = Liabilities + Equity)"
        successMessage={`Total Institutional Assets (${formatCurrency(totalAssets)}) equal Total Liabilities & Member Equity (${formatCurrency(totalLiabEquity)}).`}
        difference={variance}
        leftLabel="Total Assets"
        leftValue={totalAssets}
        rightLabel="Liabilities + Equity"
        rightValue={totalLiabEquity}
      />

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Left: Assets */}
        <div className="bdae-card rounded-2xl border border-[var(--bdae-border)] p-6 space-y-4 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between border-b border-[var(--bdae-border)] pb-3">
              <h3 className="text-sm font-black text-emerald-600 dark:text-emerald-400 uppercase tracking-wider">
                1. Assets (Portfolio & Reserves)
              </h3>
              <span className="font-mono text-xs font-bold text-[var(--bdae-text-secondary)]">
                {report.assets?.length || 0} Lines
              </span>
            </div>

            <div className="divide-y divide-[var(--bdae-border)] mt-2">
              {(report.assets || []).map((line) => (
                <div key={line.glCode} className="py-2.5 flex items-center justify-between text-xs">
                  <div>
                    <span className="font-mono font-bold text-[var(--bdae-primary)] mr-2">
                      {line.glCode}
                    </span>
                    <span className="font-medium text-[var(--bdae-text-primary)]">
                      {line.accountName}
                    </span>
                  </div>
                  <span className="font-mono font-bold text-[var(--bdae-text-primary)]">
                    {formatCurrency(line.amount)}
                  </span>
                </div>
              ))}
            </div>
          </div>

          <div className="pt-4 border-t-2 border-[var(--bdae-border)] flex items-center justify-between">
            <span className="text-xs font-black uppercase text-[var(--bdae-text-primary)]">
              Total Assets
            </span>
            <span className="font-mono text-base font-black text-emerald-600 dark:text-emerald-400">
              {formatCurrency(totalAssets)}
            </span>
          </div>
        </div>

        {/* Right: Liabilities & Equity */}
        <div className="space-y-6">
          {/* Liabilities */}
          <div className="bdae-card rounded-2xl border border-[var(--bdae-border)] p-6 space-y-4 shadow-sm">
            <div className="flex items-center justify-between border-b border-[var(--bdae-border)] pb-3">
              <h3 className="text-sm font-black text-amber-600 dark:text-amber-400 uppercase tracking-wider">
                2. Liabilities (Member Deposits)
              </h3>
              <span className="font-mono text-xs font-bold text-[var(--bdae-text-secondary)]">
                {report.liabilities?.length || 0} Lines
              </span>
            </div>

            <div className="divide-y divide-[var(--bdae-border)]">
              {(report.liabilities || []).map((line) => (
                <div key={line.glCode} className="py-2.5 flex items-center justify-between text-xs">
                  <div>
                    <span className="font-mono font-bold text-[var(--bdae-primary)] mr-2">
                      {line.glCode}
                    </span>
                    <span className="font-medium text-[var(--bdae-text-primary)]">
                      {line.accountName}
                    </span>
                  </div>
                  <span className="font-mono font-bold text-[var(--bdae-text-primary)]">
                    {formatCurrency(line.amount)}
                  </span>
                </div>
              ))}
            </div>

            <div className="pt-3 border-t border-[var(--bdae-border)] flex items-center justify-between">
              <span className="text-xs font-bold uppercase text-[var(--bdae-text-secondary)]">
                Total Liabilities
              </span>
              <span className="font-mono text-sm font-black text-amber-600 dark:text-amber-400">
                {formatCurrency(totalLiabilities)}
              </span>
            </div>
          </div>

          {/* Equity */}
          <div className="bdae-card rounded-2xl border border-[var(--bdae-border)] p-6 space-y-4 shadow-sm">
            <div className="flex items-center justify-between border-b border-[var(--bdae-border)] pb-3">
              <h3 className="text-sm font-black text-purple-600 dark:text-purple-400 uppercase tracking-wider">
                3. Member Equity & Reserves
              </h3>
              <span className="font-mono text-xs font-bold text-[var(--bdae-text-secondary)]">
                {report.equity?.length || 0} Lines
              </span>
            </div>

            <div className="divide-y divide-[var(--bdae-border)]">
              {(report.equity || []).map((line) => (
                <div key={line.glCode} className="py-2.5 flex items-center justify-between text-xs">
                  <div>
                    <span className="font-mono font-bold text-[var(--bdae-primary)] mr-2">
                      {line.glCode}
                    </span>
                    <span className="font-medium text-[var(--bdae-text-primary)]">
                      {line.accountName}
                    </span>
                  </div>
                  <span className="font-mono font-bold text-[var(--bdae-text-primary)]">
                    {formatCurrency(line.amount)}
                  </span>
                </div>
              ))}
            </div>

            <div className="pt-3 border-t border-[var(--bdae-border)] flex items-center justify-between">
              <span className="text-xs font-bold uppercase text-[var(--bdae-text-secondary)]">
                Total Equity
              </span>
              <span className="font-mono text-sm font-black text-purple-600 dark:text-purple-400">
                {formatCurrency(totalEquity)}
              </span>
            </div>

            <div className="pt-3 border-t-2 border-[var(--bdae-border)] flex items-center justify-between">
              <span className="text-xs font-black uppercase text-[var(--bdae-text-primary)]">
                Total Liabilities & Equity
              </span>
              <span className="font-mono text-base font-black text-[var(--bdae-primary)]">
                {formatCurrency(totalLiabEquity)}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
