import React from 'react';
import { ShieldCheck, CheckCircle2, ShieldAlert, Globe } from 'lucide-react';

export const AuditLogStatsBar = ({ logs = [] }) => {
  const totalCount = logs.length;
  const successCount = logs.filter(l => (l.status || '').toUpperCase() === 'SUCCESS').length;
  const failedCount = logs.filter(l => (l.status || '').toUpperCase() === 'FAILED').length;
  const uniqueIPs = new Set(logs.map(l => l.ipAddress).filter(Boolean)).size;

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      {/* Total Audit Events */}
      <div className="bdae-card p-5 flex items-center space-x-4 border border-[var(--bdae-border)] shadow-md">
        <div 
          className="w-12 h-12 rounded-2xl flex items-center justify-center text-white shadow-md shrink-0"
          style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
        >
          <ShieldCheck className="w-6 h-6" />
        </div>
        <div>
          <p className="text-xs text-[var(--bdae-text-secondary)] font-semibold uppercase tracking-wider">
            Total Security Events
          </p>
          <p className="text-2xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
            {totalCount}
          </p>
        </div>
      </div>

      {/* Successful Operations */}
      <div className="bdae-card p-5 flex items-center space-x-4 border border-[var(--bdae-border)] shadow-md">
        <div className="w-12 h-12 rounded-2xl bg-emerald-500/10 text-emerald-600 border border-emerald-500/30 flex items-center justify-center shrink-0">
          <CheckCircle2 className="w-6 h-6" />
        </div>
        <div>
          <p className="text-xs text-[var(--bdae-text-secondary)] font-semibold uppercase tracking-wider">
            Successful Actions
          </p>
          <p className="text-2xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
            {successCount}
          </p>
        </div>
      </div>

      {/* Security Alerts / Failures */}
      <div className="bdae-card p-5 flex items-center space-x-4 border border-[var(--bdae-border)] shadow-md">
        <div className="w-12 h-12 rounded-2xl bg-red-500/10 text-red-600 border border-red-500/30 flex items-center justify-center shrink-0">
          <ShieldAlert className="w-6 h-6" />
        </div>
        <div>
          <p className="text-xs text-[var(--bdae-text-secondary)] font-semibold uppercase tracking-wider">
            Security Alerts
          </p>
          <p className="text-2xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
            {failedCount}
          </p>
        </div>
      </div>

      {/* Unique IP Addresses */}
      <div className="bdae-card p-5 flex items-center space-x-4 border border-[var(--bdae-border)] shadow-md">
        <div className="w-12 h-12 rounded-2xl bg-cyan-500/10 text-cyan-600 border border-cyan-500/30 flex items-center justify-center shrink-0">
          <Globe className="w-6 h-6" />
        </div>
        <div>
          <p className="text-xs text-[var(--bdae-text-secondary)] font-semibold uppercase tracking-wider">
            Active IP Sources
          </p>
          <p className="text-2xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
            {uniqueIPs}
          </p>
        </div>
      </div>
    </div>
  );
};
