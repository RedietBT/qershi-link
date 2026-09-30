import React from 'react';
import { Layers } from 'lucide-react';
import { formatCurrency } from '../../../../common/utils/currency';

export const DelinquencyAgingBuckets = ({
  bucketSummaries = [],
  selectedBucket,
  onSelectBucket
}) => {
  return (
    <div className="space-y-3 animate-fadeIn">
      <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
        <Layers className="w-4 h-4 text-[var(--bdae-primary)]" />
        <span>Statutory Aging Buckets</span>
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
        <button
          onClick={() => onSelectBucket('ALL')}
          className={`p-3.5 rounded-xl border text-left transition-all ${
            selectedBucket === 'ALL'
              ? 'border-[var(--bdae-primary)] bg-[var(--bdae-primary)]/10 shadow-sm'
              : 'border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5'
          }`}
        >
          <div className="text-[10px] font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
            All Portfolios
          </div>
          <div className="text-base font-black text-[var(--bdae-text-primary)] mt-1 font-mono">
            {bucketSummaries.reduce((acc, b) => acc + (b.loanCount || 0), 0)} Loans
          </div>
          <div className="text-[10px] text-[var(--bdae-text-secondary)] mt-0.5 truncate">
            {formatCurrency(
              bucketSummaries.reduce((acc, b) => acc + (b.totalOutstandingPrincipal || 0), 0)
            )}
          </div>
        </button>

        {bucketSummaries.map((b) => {
          const isSelected = selectedBucket === b.bucket;
          return (
            <button
              key={b.bucket}
              onClick={() => onSelectBucket(b.bucket)}
              className={`p-3.5 rounded-xl border text-left transition-all ${
                isSelected
                  ? 'border-[var(--bdae-primary)] bg-[var(--bdae-primary)]/10 shadow-sm'
                  : 'border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5'
              }`}
            >
              <div className="text-[10px] font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] truncate">
                {b.displayName || b.bucket}
              </div>
              <div className="text-base font-black text-[var(--bdae-text-primary)] mt-1 font-mono">
                {b.loanCount || 0} Loans
              </div>
              <div className="text-[10px] text-[var(--bdae-text-secondary)] mt-0.5 truncate">
                {formatCurrency(b.totalOutstandingPrincipal || 0)}
              </div>
            </button>
          );
        })}
      </div>
    </div>
  );
};
