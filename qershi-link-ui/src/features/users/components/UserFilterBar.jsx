import React from 'react';
import { Search, Filter, CheckCircle2, Clock, Lock } from 'lucide-react';

export const UserFilterBar = ({ searchTerm, setSearchTerm, statusFilter, setStatusFilter }) => {
  return (
    <div className="bdae-card p-4 border border-[var(--bdae-border)] flex flex-col md:flex-row items-center justify-between gap-4">
      {/* Search Input */}
      <div className="relative w-full md:w-96">
        <input
          type="text"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          placeholder="Filter by phone number (+251...), role, user ID..."
          className="w-full pl-10 pr-4 py-2 rounded-xl border border-[var(--bdae-border)] focus:border-[var(--bdae-secondary)] text-xs bg-transparent outline-none text-[var(--bdae-text-primary)] transition-all font-mono"
        />
        <Search className="w-4 h-4 text-[var(--bdae-text-secondary)] absolute left-3.5 top-2.5" />
      </div>

      {/* Status Filter Buttons */}
      <div className="flex items-center space-x-2 text-xs w-full md:w-auto justify-end">
        <span className="text-[11px] font-bold text-[var(--bdae-text-secondary)] flex items-center gap-1">
          <Filter className="w-3.5 h-3.5" /> Status:
        </span>

        <button
          onClick={() => setStatusFilter('ALL')}
          className={`px-3 py-1.5 rounded-xl font-bold transition-all ${
            statusFilter === 'ALL'
              ? 'bg-[var(--bdae-primary)] text-white shadow-sm'
              : 'border border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] hover:bg-black/5 dark:hover:bg-white/5'
          }`}
        >
          All
        </button>

        <button
          onClick={() => setStatusFilter('ACTIVE')}
          className={`px-3 py-1.5 rounded-xl font-bold flex items-center gap-1 transition-all ${
            statusFilter === 'ACTIVE'
              ? 'bg-emerald-600 text-white shadow-sm'
              : 'border border-emerald-500/30 text-emerald-600 hover:bg-emerald-500/10'
          }`}
        >
          <CheckCircle2 className="w-3.5 h-3.5" /> Active
        </button>

        <button
          onClick={() => setStatusFilter('PENDING')}
          className={`px-3 py-1.5 rounded-xl font-bold flex items-center gap-1 transition-all ${
            statusFilter === 'PENDING'
              ? 'bg-amber-600 text-white shadow-sm'
              : 'border border-amber-500/30 text-amber-600 hover:bg-amber-500/10'
          }`}
        >
          <Clock className="w-3.5 h-3.5" /> Pending
        </button>

        <button
          onClick={() => setStatusFilter('LOCKED')}
          className={`px-3 py-1.5 rounded-xl font-bold flex items-center gap-1 transition-all ${
            statusFilter === 'LOCKED'
              ? 'bg-red-600 text-white shadow-sm'
              : 'border border-red-500/30 text-red-600 hover:bg-red-500/10'
          }`}
        >
          <Lock className="w-3.5 h-3.5" /> Locked
        </button>
      </div>
    </div>
  );
};
