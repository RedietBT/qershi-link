import React from 'react';
import { Play, X, AlertTriangle, Loader2 } from 'lucide-react';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';

export const EodExecutionModal = ({
  isOpen,
  onClose,
  isExecuting,
  currentDate,
  onConfirm
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
      <div className="bdae-card w-full max-w-lg rounded-2xl border border-[var(--bdae-border)] shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="p-5 border-b border-[var(--bdae-border)] flex items-center justify-between bg-purple-600/10">
          <div className="flex items-center gap-2.5">
            <Play className="w-5 h-5 text-purple-600" />
            <div>
              <h3 className="text-sm font-black text-[var(--bdae-text-primary)]">
                Execute End-of-Day (EOD) Batch
              </h3>
              <p className="text-[11px] text-[var(--bdae-text-secondary)]">
                Dual-Control Authorisation & System Day Close
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-[var(--bdae-text-secondary)] hover:bg-black/10 dark:hover:bg-white/10"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Body */}
        <div className="p-6 space-y-4 text-xs">
          <div className="p-4 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-700 dark:text-amber-400 space-y-2">
            <div className="flex items-center gap-2 font-bold">
              <AlertTriangle className="w-4 h-4 shrink-0" />
              <span>Operational Day Close Confirmation</span>
            </div>
            <p className="text-[11px] leading-relaxed">
              Executing the End-of-Day batch will close active cash desk posting windows for{' '}
              <b className="font-mono text-[var(--bdae-text-primary)]">{currentDate}</b>, accrue daily savings/loan interest, evaluate statutory delinquency buckets, roll up General Ledger balances, and advance the core business date.
            </p>
          </div>

          <div className="p-3.5 rounded-xl border border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 space-y-1">
            <span className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)]">
              Automated Operations Included:
            </span>
            <ul className="list-disc list-inside space-y-0.5 text-[11px] text-[var(--bdae-text-secondary)]">
              <li>Posting window cutoff & transaction locks</li>
              <li>Daily compound interest calculations</li>
              <li>Impairment reserve & provisioning accruals</li>
              <li>General Ledger equilibrium enforcement</li>
            </ul>
          </div>

          {/* Footer with PermissionGuard */}
          <div className="pt-4 border-t border-[var(--bdae-border)] flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-xl text-xs font-semibold border border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5"
            >
              Cancel
            </button>

            <PermissionGuard
              permissions={[PERMISSIONS.EOD_EXECUTE]}
              fallback={
                <span className="text-xs text-red-500 font-bold">
                  EOD Execution Authorization Required
                </span>
              }
            >
              <button
                type="button"
                disabled={isExecuting}
                onClick={onConfirm}
                className="px-5 py-2 rounded-xl text-xs font-bold bg-purple-600 hover:bg-purple-700 text-white flex items-center gap-1.5 shadow-lg shadow-purple-600/20 disabled:opacity-50 transition-all"
              >
                {isExecuting ? (
                  <Loader2 className="w-3.5 h-3.5 animate-spin" />
                ) : (
                  <Play className="w-3.5 h-3.5" />
                )}
                <span>Confirm & Execute EOD</span>
              </button>
            </PermissionGuard>
          </div>
        </div>
      </div>
    </div>
  );
};
