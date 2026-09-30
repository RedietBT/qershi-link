import React from 'react';
import {
  Building2,
  X,
  Check,
  AlertCircle,
  CheckCircle2,
  Loader2,
  ShieldAlert
} from 'lucide-react';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';

export const BranchFormModal = ({
  isOpen,
  onClose,
  editingBranch,
  formData,
  setFormData,
  submitting,
  formError,
  formSuccess,
  onSubmit
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
      <div className="bdae-card w-full max-w-xl rounded-2xl border border-[var(--bdae-border)] shadow-2xl overflow-hidden">
        {/* Modal Header */}
        <div className="p-5 border-b border-[var(--bdae-border)] flex items-center justify-between bg-black/5 dark:bg-white/5">
          <div className="flex items-center gap-2.5">
            <Building2 className="w-5 h-5 text-[var(--bdae-primary)]" />
            <div>
              <h3 className="text-sm font-black text-[var(--bdae-text-primary)]">
                {editingBranch ? `Configure Branch [${editingBranch.branchCode}]` : 'Onboard New Physical / Digital Branch'}
              </h3>
              <p className="text-[11px] text-[var(--bdae-text-secondary)]">
                Define operational branch parameters, vault General Ledger code, and discretionary lending thresholds.
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
        <form onSubmit={onSubmit} className="p-6 space-y-4 max-h-[75vh] overflow-y-auto text-xs">
          {formError && (
            <div className="p-3.5 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 flex items-center gap-2">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{formError}</span>
            </div>
          )}

          {formSuccess && (
            <div className="p-3.5 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-600 dark:text-emerald-400 flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4 shrink-0" />
              <span>{formSuccess}</span>
            </div>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="text-[10px] font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] block mb-1.5">
                Branch Code *
              </label>
              <input
                type="text"
                required
                disabled={!!editingBranch}
                placeholder="e.g. 0002"
                value={formData.branchCode}
                onChange={(e) => setFormData({ ...formData, branchCode: e.target.value })}
                className="bdae-input font-mono font-bold"
              />
            </div>

            <div>
              <label className="text-[10px] font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] block mb-1.5">
                Branch Name *
              </label>
              <input
                type="text"
                required
                placeholder="e.g. Hawassa Central Branch"
                value={formData.branchName}
                onChange={(e) => setFormData({ ...formData, branchName: e.target.value })}
                className="bdae-input"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="text-[10px] font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] block mb-1.5">
                Region / State
              </label>
              <input
                type="text"
                placeholder="e.g. Sidama / Southern Region"
                value={formData.region}
                onChange={(e) => setFormData({ ...formData, region: e.target.value })}
                className="bdae-input"
              />
            </div>

            <div>
              <label className="text-[10px] font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] block mb-1.5">
                Contact Phone
              </label>
              <input
                type="text"
                placeholder="e.g. +251911234567"
                value={formData.contactPhone}
                onChange={(e) => setFormData({ ...formData, contactPhone: e.target.value })}
                className="bdae-input font-mono"
              />
            </div>
          </div>

          <div>
            <label className="text-[10px] font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] block mb-1.5">
              Physical Street Address
            </label>
            <input
              type="text"
              placeholder="e.g. Main Commercial Plaza, Suite 104"
              value={formData.address}
              onChange={(e) => setFormData({ ...formData, address: e.target.value })}
              className="bdae-input"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="text-[10px] font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] block mb-1.5">
                Vault GL Account Code *
              </label>
              <input
                type="text"
                required
                placeholder="e.g. 1011-0002"
                value={formData.vaultGlCode}
                onChange={(e) => setFormData({ ...formData, vaultGlCode: e.target.value })}
                className="bdae-input font-mono font-bold"
              />
            </div>

            <div>
              <label className="text-[10px] font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] block mb-1.5">
                Discretionary Lending Limit (ETB) *
              </label>
              <input
                type="number"
                required
                min="0"
                step="1000"
                placeholder="100000"
                value={formData.discretionaryLendingLimit}
                onChange={(e) =>
                  setFormData({ ...formData, discretionaryLendingLimit: e.target.value })
                }
                className="bdae-input font-mono font-bold"
              />
            </div>
          </div>

          <div>
            <label className="text-[10px] font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] block mb-1.5">
              Operational Status
            </label>
            <select
              value={formData.status}
              onChange={(e) => setFormData({ ...formData, status: e.target.value })}
              className="bdae-input font-bold"
            >
              <option value="ACTIVE">ACTIVE</option>
              <option value="INACTIVE">INACTIVE</option>
            </select>
          </div>

          {/* Modal Footer with Permission Guard */}
          <div className="pt-4 border-t border-[var(--bdae-border)] flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-xl text-xs font-semibold border border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5"
            >
              Cancel
            </button>

            <PermissionGuard
              permissions={[PERMISSIONS.BRANCH_MANAGE]}
              fallback={
                <span className="text-xs text-red-500 font-bold">
                  Branch Management Permission Required
                </span>
              }
            >
              <button
                type="submit"
                disabled={submitting}
                className="px-5 py-2 rounded-xl text-xs font-bold bg-[var(--bdae-primary)] hover:opacity-90 text-white flex items-center gap-1.5 shadow-lg shadow-[var(--bdae-primary)]/20 disabled:opacity-50"
              >
                {submitting ? (
                  <Loader2 className="w-3.5 h-3.5 animate-spin" />
                ) : (
                  <Check className="w-3.5 h-3.5" />
                )}
                <span>{editingBranch ? 'Update Configuration' : 'Onboard Branch'}</span>
              </button>
            </PermissionGuard>
          </div>
        </form>
      </div>
    </div>
  );
};
