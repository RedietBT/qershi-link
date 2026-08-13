import React from 'react';
import { Building2, Layers, Coins, CheckCircle2 } from 'lucide-react';

export const SaccoStatsBar = ({ saccos = [] }) => {
  const totalCount = saccos.length;
  const unionCount = saccos.filter(s => s.union).length;
  const primaryCount = totalCount - unionCount;
  const activeCount = saccos.filter(s => (s.status || '').toUpperCase() === 'ACTIVE').length;

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      {/* Total Workspaces */}
      <div className="bdae-card p-5 flex items-center space-x-4 border border-[var(--bdae-border)] shadow-md">
        <div 
          className="w-12 h-12 rounded-2xl flex items-center justify-center text-white shadow-md shrink-0"
          style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
        >
          <Building2 className="w-6 h-6" />
        </div>
        <div>
          <p className="text-xs text-[var(--bdae-text-secondary)] font-semibold uppercase tracking-wider">
            Total Tenants
          </p>
          <p className="text-2xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
            {totalCount}
          </p>
        </div>
      </div>

      {/* Union Federations */}
      <div className="bdae-card p-5 flex items-center space-x-4 border border-[var(--bdae-border)] shadow-md">
        <div className="w-12 h-12 rounded-2xl bg-cyan-500/10 text-cyan-600 border border-cyan-500/30 flex items-center justify-center shrink-0">
          <Layers className="w-6 h-6" />
        </div>
        <div>
          <p className="text-xs text-[var(--bdae-text-secondary)] font-semibold uppercase tracking-wider">
            Union Federations
          </p>
          <p className="text-2xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
            {unionCount}
          </p>
        </div>
      </div>

      {/* Primary SACCOs */}
      <div className="bdae-card p-5 flex items-center space-x-4 border border-[var(--bdae-border)] shadow-md">
        <div className="w-12 h-12 rounded-2xl bg-amber-500/10 text-amber-600 border border-amber-500/30 flex items-center justify-center shrink-0">
          <Coins className="w-6 h-6" />
        </div>
        <div>
          <p className="text-xs text-[var(--bdae-text-secondary)] font-semibold uppercase tracking-wider">
            Primary SACCOs
          </p>
          <p className="text-2xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
            {primaryCount}
          </p>
        </div>
      </div>

      {/* Active Workspaces */}
      <div className="bdae-card p-5 flex items-center space-x-4 border border-[var(--bdae-border)] shadow-md">
        <div className="w-12 h-12 rounded-2xl bg-emerald-500/10 text-emerald-600 border border-emerald-500/30 flex items-center justify-center shrink-0">
          <CheckCircle2 className="w-6 h-6" />
        </div>
        <div>
          <p className="text-xs text-[var(--bdae-text-secondary)] font-semibold uppercase tracking-wider">
            Active Ready
          </p>
          <p className="text-2xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
            {activeCount}
          </p>
        </div>
      </div>
    </div>
  );
};
