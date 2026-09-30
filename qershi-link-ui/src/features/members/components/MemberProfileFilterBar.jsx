import React from 'react';
import { Filter, Search } from 'lucide-react';

export const MemberProfileFilterBar = ({
  searchTerm,
  setSearchTerm,
  statusFilter,
  setStatusFilter
}) => {
  return (
    <div className="bdae-card p-4 border border-[var(--bdae-border)] shadow-sm rounded-xl flex flex-col md:flex-row items-center gap-4">
      <div className="flex items-center space-x-2 text-[var(--bdae-text-secondary)] text-xs font-bold shrink-0">
        <Filter className="w-4 h-4" />
        <span>Filters:</span>
      </div>

      <div className="relative flex-grow max-w-sm">
        <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-[var(--bdae-text-secondary)] pointer-events-none" />
        <input
          type="text"
          placeholder="Search by Name or Member No..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full pl-9 pr-4 py-2 text-xs bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] rounded-lg text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)]"
        />
      </div>

      <select
        value={statusFilter}
        onChange={(e) => setStatusFilter(e.target.value)}
        className="px-4 py-2 text-xs bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] rounded-lg text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)]"
      >
        <option value="">All Statuses</option>
        <option value="PENDING_ONBOARDING">Pending Onboarding</option>
        <option value="ACTIVE">Active</option>
        <option value="SUSPENDED">Suspended</option>
        <option value="CLOSED">Closed</option>
      </select>
    </div>
  );
};
