import React from 'react';
import { Lock, Calculator, AlertTriangle, CheckCircle2, RefreshCw, X } from 'lucide-react';
import { formatCurrency } from '../../../common/utils/currency';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';

/**
 * Modal dialog for closing teller shift and recording physical banknote reconciliation
 */
export const ReconcileTillModal = ({
  isOpen,
  electronicBalance,
  totalPhysicalCash,
  variance,
  notes200,
  notes100,
  notes50,
  notes10,
  notes5,
  recNotes,
  closeSubmitting,
  closeError,
  closeSuccess,
  onNotesChange,
  onRecNotesChange,
  onClose,
  onSubmit,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
      <div className="bdae-card w-full max-w-lg p-6 space-y-5 rounded-2xl shadow-2xl border border-[var(--bdae-border)] max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between border-b border-[var(--bdae-border)] pb-3">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-amber-500/10 text-amber-600 flex items-center justify-center font-bold">
              <Lock className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-[var(--bdae-text-primary)]">
                Close & Reconcile Cash Drawer
              </h3>
              <p className="text-xs text-[var(--bdae-text-secondary)]">
                Physical banknote count vs. General Ledger balance
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-1 rounded-lg text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {closeError && (
          <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 text-xs flex items-center gap-2">
            <AlertTriangle className="w-4 h-4 shrink-0" />
            <span>{closeError}</span>
          </div>
        )}

        {closeSuccess && (
          <div className="p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-600 text-xs flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 shrink-0" />
            <span>{closeSuccess}</span>
          </div>
        )}

        <form onSubmit={onSubmit} className="space-y-4 text-xs">
          {/* Comparison Banner */}
          <div className="grid grid-cols-3 gap-2 p-3 rounded-xl bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] text-center">
            <div>
              <span className="text-[10px] text-[var(--bdae-text-secondary)] block uppercase font-bold">
                GL Electronic
              </span>
              <span className="font-mono font-bold text-xs">
                {formatCurrency(electronicBalance)}
              </span>
            </div>
            <div>
              <span className="text-[10px] text-[var(--bdae-text-secondary)] block uppercase font-bold">
                Counted Physical
              </span>
              <span className="font-mono font-bold text-xs text-cyan-600">
                {formatCurrency(totalPhysicalCash)}
              </span>
            </div>
            <div>
              <span className="text-[10px] text-[var(--bdae-text-secondary)] block uppercase font-bold">
                Net Variance
              </span>
              <span
                className={`font-mono font-black text-xs ${
                  variance === 0
                    ? 'text-emerald-500'
                    : variance > 0
                    ? 'text-blue-500'
                    : 'text-rose-500'
                }`}
              >
                {variance === 0 ? 'BALANCED' : formatCurrency(variance)}
              </span>
            </div>
          </div>

          {/* Banknote Denominations Counter */}
          <div className="space-y-2">
            <h4 className="font-bold text-[var(--bdae-text-primary)] flex items-center gap-1.5">
              <Calculator className="w-3.5 h-3.5 text-amber-500" />
              <span>Physical Banknote Denomination Counts</span>
            </h4>

            <div className="grid grid-cols-2 sm:grid-cols-5 gap-2">
              {[
                { label: '200 ETB', count: notes200, key: 'notes200' },
                { label: '100 ETB', count: notes100, key: 'notes100' },
                { label: '50 ETB', count: notes50, key: 'notes50' },
                { label: '10 ETB', count: notes10, key: 'notes10' },
                { label: '5 ETB', count: notes5, key: 'notes5' },
              ].map((item) => (
                <div key={item.key} className="space-y-1">
                  <label className="text-[10px] text-[var(--bdae-text-secondary)] uppercase font-bold block">
                    {item.label}
                  </label>
                  <input
                    type="number"
                    min="0"
                    value={item.count}
                    onChange={(e) => onNotesChange(item.key, Math.max(0, parseInt(e.target.value) || 0))}
                    className="bdae-input font-mono text-center font-bold text-xs py-1.5"
                  />
                </div>
              ))}
            </div>
          </div>

          {/* Reconciliation Notes */}
          <div className="space-y-1">
            <label className="font-semibold block text-[var(--bdae-text-primary)]">
              Reconciliation Remarks
            </label>
            <input
              type="text"
              value={recNotes}
              onChange={(e) => onRecNotesChange(e.target.value)}
              placeholder="e.g. End of shift balancing verified"
              className="bdae-input text-xs"
            />
          </div>

          <div className="flex items-center justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              disabled={closeSubmitting}
              className="px-4 py-2 rounded-xl font-semibold hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
            >
              Cancel
            </button>

            {/* Close Till CTA strictly gated with TELLER_TILL_VIEW / CASH_DEPOSIT */}
            <PermissionGuard
              roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER', 'BRANCH_MANAGER']}
              permissions={[PERMISSIONS.TELLER_TILL_VIEW, PERMISSIONS.CASH_DEPOSIT]}
            >
              <button
                type="submit"
                disabled={closeSubmitting}
                className="px-5 py-2.5 rounded-xl font-bold text-white bg-amber-600 hover:bg-amber-700 shadow-md flex items-center gap-2 transition-all"
              >
                {closeSubmitting && <RefreshCw className="w-3.5 h-3.5 animate-spin" />}
                Commit Count & Close Shift
              </button>
            </PermissionGuard>
          </div>
        </form>
      </div>
    </div>
  );
};
