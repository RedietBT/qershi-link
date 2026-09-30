import React from 'react';
import { Search, Loader2 } from 'lucide-react';

export const UnderwritingLookupCard = ({
  searchId,
  setSearchId,
  loading,
  onLookup
}) => {
  return (
    <div className="bdae-card p-5 border border-[var(--bdae-border)] space-y-4 rounded-2xl">
      <h2 className="text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
        Inquire Loan Application for Underwriting Decision
      </h2>

      <form onSubmit={onLookup} className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <input
            type="text"
            placeholder="Enter Loan Application UUID or Borrower User ID..."
            value={searchId}
            onChange={(e) => setSearchId(e.target.value)}
            className="bdae-input font-mono text-xs pl-10 font-bold"
          />
          <Search className="w-4 h-4 text-[var(--bdae-text-secondary)] absolute left-3.5 top-3" />
        </div>

        <button
          type="submit"
          disabled={loading || !searchId.trim()}
          className="px-5 py-2.5 bg-purple-600 text-white rounded-xl text-xs font-bold flex items-center justify-center gap-2 hover:bg-purple-700 disabled:opacity-50 shadow-md transition-all shrink-0"
        >
          {loading ? (
            <Loader2 className="w-4 h-4 animate-spin" />
          ) : (
            <Search className="w-4 h-4" />
          )}
          Inquire Application
        </button>
      </form>
    </div>
  );
};
