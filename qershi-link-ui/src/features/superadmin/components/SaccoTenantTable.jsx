import React from 'react';
import { SaccoStatusBadge } from './SaccoStatusBadge';
import { Eye, Building2, Layers, Calendar, Database, RefreshCw, AlertCircle, KeyRound } from 'lucide-react';

export const SaccoTenantTable = ({ saccos = [], isLoading, error, onInspect, onRefresh }) => {
  if (isLoading) {
    return (
      <div className="bdae-card p-12 text-center space-y-3 border border-[var(--bdae-border)]">
        <RefreshCw className="w-8 h-8 text-[var(--bdae-secondary)] animate-spin mx-auto" />
        <p className="text-xs font-semibold text-[var(--bdae-text-secondary)]">
          Fetching SACCO Registry Workspaces (GET /api/v1/saccos)...
        </p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bdae-card p-8 border border-red-500/30 bg-red-500/5 text-center space-y-3">
        <AlertCircle className="w-8 h-8 text-red-500 mx-auto" />
        <p className="text-xs font-bold text-red-600 dark:text-red-400">{error}</p>
        <button
          onClick={onRefresh}
          className="bdae-btn-primary px-4 py-2 text-xs font-bold rounded-xl inline-flex items-center gap-1.5"
        >
          <RefreshCw className="w-3.5 h-3.5" /> Retry Load
        </button>
      </div>
    );
  }

  if (saccos.length === 0) {
    return (
      <div className="bdae-card p-12 text-center space-y-3 border border-[var(--bdae-border)]">
        <Building2 className="w-10 h-10 text-[var(--bdae-text-secondary)] mx-auto opacity-50" />
        <p className="text-sm font-bold text-[var(--bdae-text-primary)]">No SACCO Workspaces Registered</p>
        <p className="text-xs text-[var(--bdae-text-secondary)]">Onboard a SACCO tenant to get started.</p>
      </div>
    );
  }

  return (
    <div className="bdae-card border border-[var(--bdae-border)] shadow-xl overflow-hidden rounded-2xl">
      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs border-collapse">
          <thead>
            <tr className="border-b border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 text-[11px] font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider">
              <th className="py-3.5 px-4">SACCO Tenant Name</th>
              <th className="py-3.5 px-4">PostgreSQL Schema</th>
              <th className="py-3.5 px-4">Classification</th>
              <th className="py-3.5 px-4">Min Share (ETB)</th>
              <th className="py-3.5 px-4">Status</th>
              <th className="py-3.5 px-4">Created Date</th>
              <th className="py-3.5 px-4 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[var(--bdae-border)]">
            {saccos.map((sacco) => {
              const formattedDate = sacco.createdAt
                ? new Date(sacco.createdAt).toLocaleDateString('en-US', {
                    year: 'numeric',
                    month: 'short',
                    day: 'numeric'
                  })
                : 'N/A';

              return (
                <tr 
                  key={sacco.saccoId}
                  className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors group"
                >
                  {/* SACCO Name & UUID */}
                  <td className="py-3.5 px-4">
                    <div className="flex items-center space-x-3">
                      <div 
                        className="w-8 h-8 rounded-lg flex items-center justify-center text-white text-xs font-bold shadow-sm shrink-0"
                        style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
                      >
                        <Building2 className="w-4 h-4" />
                      </div>
                      <div>
                        <p className="font-bold text-[var(--bdae-text-primary)] text-xs">
                          {sacco.saccoName}
                        </p>
                        <p className="text-[10px] text-[var(--bdae-text-secondary)] font-mono">
                          {sacco.saccoId}
                        </p>
                      </div>
                    </div>
                  </td>

                  {/* Schema Name */}
                  <td className="py-3.5 px-4">
                    <span className="inline-flex items-center space-x-1 font-mono text-[11px] bg-black/5 dark:bg-white/5 px-2 py-1 rounded-md border border-[var(--bdae-border)] text-[var(--bdae-text-primary)]">
                      <Database className="w-3 h-3 text-[var(--bdae-secondary)]" />
                      <span>{sacco.schemaName}</span>
                    </span>
                  </td>

                  {/* Classification Badge */}
                  <td className="py-3.5 px-4">
                    {sacco.union ? (
                      <span className="inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-cyan-500/10 text-cyan-600 border border-cyan-500/30">
                        <Layers className="w-3 h-3" />
                        <span>Union Federation</span>
                      </span>
                    ) : (
                      <span className="inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-slate-500/10 text-slate-600 border border-slate-500/20">
                        <span>Primary SACCO</span>
                      </span>
                    )}
                  </td>

                  {/* Min Share Requirement */}
                  <td className="py-3.5 px-4 font-mono font-bold text-xs">
                    ETB {Number(sacco.minShareRequirement || 0).toLocaleString()}
                  </td>

                  {/* Status Badge */}
                  <td className="py-3.5 px-4">
                    <SaccoStatusBadge status={sacco.status} />
                  </td>

                  {/* Created Date */}
                  <td className="py-3.5 px-4 text-[11px] text-[var(--bdae-text-secondary)] font-mono">
                    <div className="flex items-center space-x-1">
                      <Calendar className="w-3 h-3 opacity-60" />
                      <span>{formattedDate}</span>
                    </div>
                  </td>

                  {/* Actions */}
                  <td className="py-3.5 px-4 text-right space-x-2">
                    <button
                      onClick={() => onInspect(sacco.saccoId)}
                      className="px-3 py-1.5 rounded-xl border border-[var(--bdae-border)] hover:border-[var(--bdae-secondary)] hover:bg-[var(--bdae-secondary)]/10 text-[var(--bdae-secondary)] text-xs font-bold inline-flex items-center gap-1.5 transition-all shadow-sm"
                    >
                      <Eye className="w-3.5 h-3.5" />
                      <span>View Profile</span>
                    </button>

                    <button
                      onClick={() => onResendPin(sacco.saccoId)}
                      className="px-3 py-1.5 rounded-xl border border-amber-500/30 hover:border-amber-500 bg-amber-500/5 hover:bg-amber-500/10 text-amber-600 dark:text-amber-400 text-xs font-bold inline-flex items-center gap-1.5 transition-all shadow-sm"
                      title="Resend Initial PIN via SMS (POST /api/v1/pin/resend/{userId})"
                    >
                      <KeyRound className="w-3.5 h-3.5" />
                      <span>Resend PIN</span>
                    </button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};
