import React from 'react';
import { Search, Loader2, Filter } from 'lucide-react';

/**
 * Filter and Account Inquire Bar for Transaction History
 */
export const TransactionFilterBar = ({
  accountNo,
  onAccountChange,
  loading,
  onSubmit,
  typeFilter,
  onTypeFilterChange,
}) => {
  return (
    <div className="bdae-card p-5 border border-[var(--bdae-border)] space-y-4">
      <form onSubmit={onSubmit} className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <input
            type="text"
            placeholder="Enter Account Number (e.g. ACC-AW-2026-0001)..."
            value={accountNo}
            onChange={(e) => onAccountChange(e.target.value)}
            className="bdae-input font-mono uppercase text-xs font-bold pl-10"
          />
          <Search className="w-4 h-4 text-[var(--bdae-text-secondary)] absolute left-3.5 top-3" />
        </div>

        <button
          type="submit"
          disabled={loading || !accountNo.trim()}
          className="px-5 py-2.5 bg-[var(--bdae-primary)] text-white rounded-xl text-xs font-bold flex items-center justify-center gap-2 hover:opacity-90 disabled:opacity-50 shadow-md transition-all shrink-0"
        >
          {loading ? <Loader2 className="w-4 h-4 animate-spin" /> : <Search className="w-4 h-4" />}
          Inquire Statements
        </button>
      </form>

      {/* Transaction Type Filter Chips */}
      <div className="flex flex-wrap items-center gap-2 pt-2 border-t border-[var(--bdae-border)] text-xs">
        <div className="flex items-center gap-1.5 text-[var(--bdae-text-secondary)] font-bold mr-2 text-[11px]">
          <Filter className="w-3.5 h-3.5" />
          <span>Type:</span>
        </div>
        {[
          { label: 'All Postings', val: 'ALL' },
          { label: 'Deposits', val: 'DEPOSIT' },
          { label: 'Withdrawals', val: 'WITHDRAWAL' },
          { label: 'Transfers', val: 'TRANSFER' },
          { label: 'Disbursements', val: 'LOAN_DISBURSEMENT' },
          { label: 'Repayments', val: 'LOAN_REPAYMENT' },
        ].map((item) => (
          <button
            key={item.val}
            type="button"
            onClick={() => onTypeFilterChange(item.val)}
            className={`px-3 py-1 rounded-lg text-xs font-semibold transition-all ${
              typeFilter === item.val
                ? 'bg-[var(--bdae-primary)] text-white font-bold shadow-xs'
                : 'bg-black/5 dark:bg-white/5 text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]'
            }`}
          >
            {item.label}
          </button>
        ))}
      </div>
    </div>
  );
};
