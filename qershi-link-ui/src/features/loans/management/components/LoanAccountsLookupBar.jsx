import React from 'react';
import { Search, Loader2 } from 'lucide-react';

export const LoanAccountsLookupBar = ({
  userIdSearch,
  setUserIdSearch,
  loading,
  onSearch
}) => {
  return (
    <div className="bdae-card p-5 border border-[var(--bdae-border)] space-y-4 rounded-2xl">
      <form onSubmit={onSearch} className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <input
            type="text"
            placeholder="Search active loan accounts by Borrower Member User ID or Account UUID..."
            value={userIdSearch}
            onChange={(e) => setUserIdSearch(e.target.value)}
            className="bdae-input font-mono text-xs pl-10"
          />
          <Search className="w-4 h-4 text-[var(--bdae-text-secondary)] absolute left-3.5 top-3" />
        </div>

        <button
          type="submit"
          disabled={loading || !userIdSearch.trim()}
          className="px-5 py-2.5 bg-emerald-600 text-white rounded-xl text-xs font-bold flex items-center justify-center gap-2 hover:bg-emerald-700 disabled:opacity-50 shadow-md transition-all shrink-0"
        >
          {loading ? (
            <Loader2 className="w-4 h-4 animate-spin" />
          ) : (
            <Search className="w-4 h-4" />
          )}
          Inquire Portfolio
        </button>
      </form>
    </div>
  );
};
