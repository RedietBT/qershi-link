import React from 'react';
import { Plus, X, Layers, AlertCircle, Loader2, Check } from 'lucide-react';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';

export const CreateGlAccountModal = ({
  isOpen,
  onClose,
  parentAccount,
  formData,
  setFormData,
  flatAccounts = [],
  isSubmitting,
  error,
  onSubmit
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
      <div className="bdae-card w-full max-w-lg rounded-2xl border border-[var(--bdae-border)] shadow-2xl overflow-hidden">
        {/* Modal Header */}
        <div className="p-5 border-b border-[var(--bdae-border)] flex items-center justify-between bg-black/5 dark:bg-white/5">
          <div className="flex items-center gap-2.5">
            <Layers className="w-5 h-5 text-[var(--bdae-primary)]" />
            <div>
              <h3 className="text-sm font-black text-[var(--bdae-text-primary)]">
                {parentAccount ? `New Sub-Account under [${parentAccount.glCode}]` : 'Create Root / Control GL Account'}
              </h3>
              <p className="text-[11px] text-[var(--bdae-text-secondary)]">
                Define the GL account code, descriptive title, category, and normal balance rules.
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

        {/* Modal Body */}
        <form onSubmit={onSubmit} className="p-6 space-y-4 text-xs">
          {error && (
            <div className="p-3.5 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 flex items-center gap-2">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-[10px] font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] block mb-1.5">
                GL Code *
              </label>
              <input
                type="text"
                required
                placeholder="e.g. 1014"
                value={formData.glCode}
                onChange={(e) => setFormData({ ...formData, glCode: e.target.value })}
                className="bdae-input font-mono font-bold"
              />
            </div>

            <div>
              <label className="text-[10px] font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] block mb-1.5">
                Account Type *
              </label>
              <select
                disabled={!!parentAccount}
                value={formData.accountType}
                onChange={(e) => setFormData({ ...formData, accountType: e.target.value })}
                className="bdae-input font-bold"
              >
                <option value="ASSET">ASSET</option>
                <option value="LIABILITY">LIABILITY</option>
                <option value="EQUITY">EQUITY</option>
                <option value="REVENUE">REVENUE</option>
                <option value="EXPENSE">EXPENSE</option>
              </select>
            </div>
          </div>

          <div>
            <label className="text-[10px] font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] block mb-1.5">
              Account Name *
            </label>
            <input
              type="text"
              required
              placeholder="e.g. Mobile Money Clearing Account"
              value={formData.accountName}
              onChange={(e) => setFormData({ ...formData, accountName: e.target.value })}
              className="bdae-input font-bold"
            />
          </div>

          <div>
            <label className="text-[10px] font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] block mb-1.5">
              Parent GL Account
            </label>
            <select
              value={formData.parentGlCode || ''}
              disabled={!!parentAccount}
              onChange={(e) => setFormData({ ...formData, parentGlCode: e.target.value || null })}
              className="bdae-input font-mono"
            >
              <option value="">— None (Root Level Account) —</option>
              {flatAccounts.map((a) => (
                <option key={a.accountId} value={a.glCode}>
                  [{a.glCode}] {a.accountName} ({a.accountType})
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-[10px] font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] block mb-1.5">
                Normal Balance
              </label>
              <select
                value={formData.normalBalance}
                onChange={(e) => setFormData({ ...formData, normalBalance: e.target.value })}
                className="bdae-input font-mono font-bold"
              >
                <option value="DEBIT">DEBIT</option>
                <option value="CREDIT">CREDIT</option>
              </select>
            </div>

            <div className="flex items-center gap-2 pt-6">
              <input
                type="checkbox"
                id="isReconciliationAccount"
                checked={formData.isReconciliationAccount}
                onChange={(e) =>
                  setFormData({ ...formData, isReconciliationAccount: e.target.checked })
                }
                className="w-4 h-4 rounded text-[var(--bdae-primary)] focus:ring-[var(--bdae-primary)]"
              />
              <label
                htmlFor="isReconciliationAccount"
                className="text-[11px] font-semibold text-[var(--bdae-text-primary)] cursor-pointer"
              >
                Reconciliation Account
              </label>
            </div>
          </div>

          {/* Modal Footer */}
          <div className="pt-4 border-t border-[var(--bdae-border)] flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-xl text-xs font-semibold border border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5"
            >
              Cancel
            </button>

            <PermissionGuard
              permissions={[PERMISSIONS.COA_MANAGE]}
              fallback={
                <span className="text-xs text-red-500 font-bold">
                  COA Management Permission Required
                </span>
              }
            >
              <button
                type="submit"
                disabled={isSubmitting}
                className="px-5 py-2 rounded-xl text-xs font-bold bg-[var(--bdae-primary)] hover:opacity-90 text-white flex items-center gap-1.5 shadow-lg shadow-[var(--bdae-primary)]/20 disabled:opacity-50"
              >
                {isSubmitting ? (
                  <Loader2 className="w-3.5 h-3.5 animate-spin" />
                ) : (
                  <Check className="w-3.5 h-3.5" />
                )}
                <span>Save Account</span>
              </button>
            </PermissionGuard>
          </div>
        </form>
      </div>
    </div>
  );
};
