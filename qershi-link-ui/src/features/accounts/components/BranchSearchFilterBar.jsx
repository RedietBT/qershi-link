import React from 'react';
import { Search, Filter } from 'lucide-react';

export const BranchSearchFilterBar = ({
  searchQuery,
  setSearchQuery,
  statusFilter,
  setStatusFilter
}) => {
  return (
    <div className="bdae-card p-4 rounded-2xl border border-[var(--bdae-border)] flex flex-col sm:flex-row items-center justify-between gap-4">
      <div className="relative w-full sm:w-80">
        <Search className="w-4 h-4 text-[var(--bdae-text-secondary)] absolute left-3 top-2.5" />
        <input
          type="text"
          placeholder="Filter branches by name, code, region..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="bdae-input text-xs pl-9 w-full"
        />
      </div>

      <div className="flex items-center gap-2 self-start sm:self-auto text-xs">
        <span className="text-[10px] font-bold uppercase text-[var(--bdae-text-secondary)] flex items-center gap-1">
          <Filter className="w-3 h-3" /> Status:
        </span>
        {['ALL', 'ACTIVE', 'INACTIVE'].map((st) => (
          <button
            key={st}
            type="button"
            onClick={() => setStatusFilter(st)}
            className={`px-3 py-1 rounded-lg text-[11px] font-bold transition-all ${
              statusFilter === st
                ? 'bg-[var(--bdae-primary)] text-white shadow-sm'
                : 'bg-black/5 dark:bg-white/5 text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]'
            }`}
          >
            {st}
          </button>
        ))}
      </div>
    </div>
  );
};
