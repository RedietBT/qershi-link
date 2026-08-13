import React from 'react';
import { SaccoStatusBadge } from './SaccoStatusBadge';
import { X, Building2, Database, Layers, Coins, Calendar, ShieldCheck, RefreshCw, AlertCircle } from 'lucide-react';

export const SaccoDetailModal = ({ sacco, isLoading, error, onClose }) => {
  if (!sacco && !isLoading && !error) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
      <div className="bdae-card p-6 md:p-8 max-w-lg w-full rounded-3xl shadow-2xl border border-[var(--bdae-border)] space-y-6 relative">
        
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-5 right-5 p-2 rounded-full text-[var(--bdae-text-secondary)] hover:bg-black/10 dark:hover:bg-white/10 transition-all"
        >
          <X className="w-5 h-5" />
        </button>

        {/* Modal Header */}
        <div className="flex items-center space-x-3 border-b border-[var(--bdae-border)] pb-4">
          <div 
            className="w-12 h-12 rounded-2xl flex items-center justify-center text-white shadow-md shrink-0"
            style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
          >
            <Building2 className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-lg font-bold tracking-tight text-[var(--bdae-text-primary)]">
              SACCO Registry Profile
            </h2>
            <p className="text-xs text-[var(--bdae-text-secondary)] font-mono">
              GET /api/v1/saccos/{sacco?.saccoId || 'id'}
            </p>
          </div>
        </div>

        {/* Content Loading State */}
        {isLoading ? (
          <div className="py-12 text-center space-y-2">
            <RefreshCw className="w-8 h-8 text-[var(--bdae-secondary)] animate-spin mx-auto" />
            <p className="text-xs font-semibold text-[var(--bdae-text-secondary)]">Loading SACCO Profile Metadata...</p>
          </div>
        ) : error ? (
          <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/30 text-red-600 dark:text-red-400 text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        ) : sacco ? (
          <div className="space-y-4 text-xs">
            {/* Metadata Grid */}
            <div className="grid grid-cols-2 gap-3">
              
              <div className="p-3.5 rounded-2xl bdae-surface border border-[var(--bdae-border)] space-y-1">
                <p className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)]">SACCO Name</p>
                <p className="font-bold text-sm text-[var(--bdae-text-primary)]">{sacco.saccoName}</p>
              </div>

              <div className="p-3.5 rounded-2xl bdae-surface border border-[var(--bdae-border)] space-y-1">
                <p className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)]">Status</p>
                <div>
                  <SaccoStatusBadge status={sacco.status} />
                </div>
              </div>

              <div className="p-3.5 rounded-2xl bdae-surface border border-[var(--bdae-border)] space-y-1">
                <p className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)]">PostgreSQL Schema</p>
                <p className="font-mono text-xs text-[var(--bdae-secondary)] font-bold flex items-center gap-1">
                  <Database className="w-3.5 h-3.5" />
                  <span>{sacco.schemaName}</span>
                </p>
              </div>

              <div className="p-3.5 rounded-2xl bdae-surface border border-[var(--bdae-border)] space-y-1">
                <p className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)]">Classification</p>
                <p className="font-bold text-xs flex items-center gap-1">
                  <Layers className="w-3.5 h-3.5 text-[var(--bdae-secondary)]" />
                  <span>{sacco.union ? 'Union Federation' : 'Primary SACCO'}</span>
                </p>
              </div>

              <div className="p-3.5 rounded-2xl bdae-surface border border-[var(--bdae-border)] space-y-1">
                <p className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)]">Min Share Requirement</p>
                <p className="font-mono text-xs font-bold text-[var(--bdae-text-primary)] flex items-center gap-1">
                  <Coins className="w-3.5 h-3.5 text-amber-500" />
                  <span>ETB {Number(sacco.minShareRequirement || 0).toLocaleString()}</span>
                </p>
              </div>

              <div className="p-3.5 rounded-2xl bdae-surface border border-[var(--bdae-border)] space-y-1">
                <p className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)]">Created Date</p>
                <p className="font-mono text-xs text-[var(--bdae-text-primary)] flex items-center gap-1">
                  <Calendar className="w-3.5 h-3.5 opacity-60" />
                  <span>{sacco.createdAt ? new Date(sacco.createdAt).toLocaleString() : 'N/A'}</span>
                </p>
              </div>

            </div>

            {/* UUID Box */}
            <div className="p-3 rounded-xl bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] font-mono text-[11px] space-y-1">
              <p className="text-[10px] font-bold text-[var(--bdae-text-secondary)] uppercase">SACCO UUID Identifier</p>
              <p className="text-[var(--bdae-secondary)] select-all font-bold">{sacco.saccoId}</p>
            </div>
          </div>
        ) : null}

        {/* Modal Footer */}
        <div className="pt-2">
          <button
            onClick={onClose}
            className="bdae-btn-primary w-full py-2.5 text-xs font-bold rounded-xl"
          >
            Close Profile Inspector
          </button>
        </div>

      </div>
    </div>
  );
};
