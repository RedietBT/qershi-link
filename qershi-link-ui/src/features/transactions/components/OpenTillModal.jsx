import React from 'react';
import { Unlock, X, RefreshCw } from 'lucide-react';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';

/**
 * Modal dialog for opening a teller cash drawer with initial float
 */
export const OpenTillModal = ({
  isOpen,
  openingCashInput,
  openSubmitting,
  onCashChange,
  onClose,
  onSubmit,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
      <div className="bdae-card w-full max-w-md p-6 space-y-5 rounded-2xl shadow-2xl border border-[var(--bdae-border)]">
        <div className="flex items-center justify-between border-b border-[var(--bdae-border)] pb-3">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-emerald-500/10 text-emerald-600 flex items-center justify-center font-bold">
              <Unlock className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-[var(--bdae-text-primary)]">
                Open Cash Drawer
              </h3>
              <p className="text-xs text-[var(--bdae-text-secondary)]">
                Assign starting vault cash float for shift
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

        <form onSubmit={onSubmit} className="space-y-4 text-xs">
          <div>
            <label className="block font-semibold mb-1 text-[var(--bdae-text-primary)]">
              Opening Cash Float (ETB) *
            </label>
            <input
              type="number"
              min="0"
              step="0.01"
              required
              value={openingCashInput}
              onChange={(e) => onCashChange(e.target.value)}
              className="bdae-input font-mono text-base font-bold"
            />
            <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
              Standard initial teller float transferred from Head Office Vault.
            </p>
          </div>

          <div className="flex items-center justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              disabled={openSubmitting}
              className="px-4 py-2 rounded-xl font-semibold hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
            >
              Cancel
            </button>

            {/* Submit button strictly gated with TELLER_TILL_VIEW / CASH_DEPOSIT */}
            <PermissionGuard
              roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER', 'BRANCH_MANAGER']}
              permissions={[PERMISSIONS.TELLER_TILL_VIEW, PERMISSIONS.CASH_DEPOSIT]}
            >
              <button
                type="submit"
                disabled={openSubmitting}
                className="px-5 py-2.5 rounded-xl font-bold text-white bg-emerald-600 hover:bg-emerald-700 shadow-md flex items-center gap-2 transition-all"
              >
                {openSubmitting && <RefreshCw className="w-3.5 h-3.5 animate-spin" />}
                Confirm & Open Shift
              </button>
            </PermissionGuard>
          </div>
        </form>
      </div>
    </div>
  );
};
