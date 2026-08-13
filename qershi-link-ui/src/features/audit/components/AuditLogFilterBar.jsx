import React from 'react';
import { Search, Filter, ShieldCheck, ShieldAlert } from 'lucide-react';

export const AuditLogFilterBar = ({ searchTerm, setSearchTerm, statusFilter, setStatusFilter }) => {
  return (
    <div className="bdae-card p-4 border border-[var(--bdae-border)] flex flex-col md:flex-row items-center justify-between gap-4">
      {/* Search Input */}
      <div className="relative w-full md:w-96">
        <input
          type="text"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          placeholder="Filter by action, resource, IP address, user ID..."
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
          onClick={() => setStatusFilter('SUCCESS')}
          className={`px-3 py-1.5 rounded-xl font-bold flex items-center gap-1 transition-all ${
            statusFilter === 'SUCCESS'
              ? 'bg-emerald-600 text-white shadow-sm'
              : 'border border-emerald-500/30 text-emerald-600 hover:bg-emerald-500/10'
          }`}
        >
          <ShieldCheck className="w-3.5 h-3.5" /> Success
        </button>

        <button
          onClick={() => setStatusFilter('FAILED')}
          className={`px-3 py-1.5 rounded-xl font-bold flex items-center gap-1 transition-all ${
            statusFilter === 'FAILED'
              ? 'bg-red-600 text-white shadow-sm'
              : 'border border-red-500/30 text-red-600 hover:bg-red-500/10'
          }`}
        >
          <ShieldAlert className="w-3.5 h-3.5" /> Security Alerts
        </button>
      </div>
    </div>
  );
};
