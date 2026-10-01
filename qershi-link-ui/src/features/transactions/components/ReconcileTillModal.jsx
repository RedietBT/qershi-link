import React, { useState } from 'react';
import { Lock, Calculator, AlertTriangle, CheckCircle2, RefreshCw, X, Coins, ShieldCheck, Eye, EyeOff, Layers } from 'lucide-react';
import { formatCurrency } from '../../../common/utils/currency';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';

/**
 * Enterprise Blind Till Balancing & Banknote Denominations Counter Modal.
 * Compliant with Temenos Transact / Finacle Core Banking Standard:
 * - Tellers perform blind physical counts without seeing system ledger balances.
 * - System detects cash shortages or overages automatically.
 * - Posts balanced General Ledger entries to 5090 (Shortage) or 4090 (Overage).
 * - Enforces supervisor authorization for variances exceeding tolerance limit.
 */
export const ReconcileTillModal = ({
  isOpen,
  electronicBalance,
  totalPhysicalCash,
  variance,
  notes200 = 0,
  notes100 = 0,
  notes50 = 0,
  notes10 = 0,
  notes5 = 0,
  coins = 0,
  recNotes,
  closeSubmitting,
  closeError,
  closeSuccess,
  reconciliationResult,
  onNotesChange,
  onRecNotesChange,
  onClose,
  onSubmit,
}) => {
  const [showElectronicBalance, setShowElectronicBalance] = useState(false);

  if (!isOpen) return null;

  const denominations = [
    { label: '200 ETB', multiplier: 200, count: notes200, key: 'notes200' },
    { label: '100 ETB', multiplier: 100, count: notes100, key: 'notes100' },
    { label: '50 ETB', multiplier: 50, count: notes50, key: 'notes50' },
    { label: '10 ETB', multiplier: 10, count: notes10, key: 'notes10' },
    { label: '5 ETB', multiplier: 5, count: notes5, key: 'notes5' },
  ];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fadeIn">
      <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 w-full max-w-xl p-6 space-y-5 rounded-2xl shadow-2xl max-h-[92vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-slate-100 dark:border-slate-800 pb-3">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-amber-500/10 text-amber-600 dark:text-amber-400 flex items-center justify-center font-bold">
              <Lock className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-base font-bold text-slate-900 dark:text-white">
                  Blind Till Balancing & Denomination Counter
                </h3>
                <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 dark:bg-amber-950 text-amber-700 dark:text-amber-300">
                  CBS Blind Count
                </span>
              </div>
              <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                Physical banknote count & automated General Ledger variance reconciliation
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {closeError && (
          <div className="p-3.5 rounded-xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-800 text-rose-700 dark:text-rose-300 text-xs flex items-center gap-2">
            <AlertTriangle className="w-4 h-4 shrink-0" />
            <span>{closeError}</span>
          </div>
        )}

        {closeSuccess && (
          <div className="p-3.5 rounded-xl bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 text-emerald-700 dark:text-emerald-300 text-xs flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 shrink-0" />
            <span>{closeSuccess}</span>
          </div>
        )}

        {/* Post-Reconciliation Result Banner */}
        {reconciliationResult && (
          <div className={`p-4 rounded-xl border space-y-2 text-xs ${
            reconciliationResult.cashVariance === 0 || reconciliationResult.varianceType === 'NONE'
              ? 'bg-emerald-50 dark:bg-emerald-950/30 border-emerald-200 dark:border-emerald-800 text-emerald-800 dark:text-emerald-200'
              : reconciliationResult.varianceType === 'SHORTAGE'
              ? 'bg-rose-50 dark:bg-rose-950/30 border-rose-200 dark:border-rose-800 text-rose-800 dark:text-rose-200'
              : 'bg-amber-50 dark:bg-amber-950/30 border-amber-200 dark:border-amber-800 text-amber-800 dark:text-amber-200'
          }`}>
            <div className="flex items-center justify-between font-bold">
              <span className="flex items-center gap-1.5">
                <ShieldCheck className="w-4 h-4" />
                Variance Result: {reconciliationResult.varianceType || 'BALANCED'}
              </span>
              <span className="font-mono text-sm">
                Variance: {formatCurrency(reconciliationResult.cashVariance)}
              </span>
            </div>
            {reconciliationResult.varianceGlCode && (
              <p className="text-[11px] font-mono">
                Automated GL Posting: Balanced adjustment logged to GL {reconciliationResult.varianceGlCode} ({
                  reconciliationResult.varianceType === 'SHORTAGE' ? 'Cash Shortage Expense' : 'Cash Overage Income'
                })
              </p>
            )}
            {reconciliationResult.status === 'PENDING_SUPERVISOR_APPROVAL' && (
              <div className="mt-1 font-semibold text-amber-700 dark:text-amber-300">
                ⚠️ Variance exceeds ETB 100.00 tolerance. Awaiting Branch Supervisor Sign-Off.
              </div>
            )}
          </div>
        )}

        <form onSubmit={onSubmit} className="space-y-4 text-xs">
          {/* Blind Count Notice & Physical Total Banner */}
          <div className="p-3.5 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700 flex items-center justify-between">
            <div>
              <span className="text-[10px] text-slate-500 uppercase tracking-wider font-bold block">
                Total Physical Cash Counted
              </span>
              <span className="font-mono font-black text-lg text-emerald-600 dark:text-emerald-400">
                {formatCurrency(totalPhysicalCash)}
              </span>
            </div>

            <div className="text-right">
              <button
                type="button"
                onClick={() => setShowElectronicBalance(prev => !prev)}
                className="text-[11px] font-semibold text-slate-500 hover:text-slate-800 dark:hover:text-slate-200 flex items-center gap-1 ml-auto"
                title="Supervisor audit toggle"
              >
                {showElectronicBalance ? <EyeOff className="w-3.5 h-3.5" /> : <Eye className="w-3.5 h-3.5" />}
                {showElectronicBalance ? 'Hide GL Target' : 'Supervisor Peek'}
              </button>
              {showElectronicBalance && (
                <span className="font-mono font-bold text-xs text-slate-700 dark:text-slate-300 block mt-0.5">
                  GL Expected: {formatCurrency(electronicBalance)}
                </span>
              )}
            </div>
          </div>

          {/* Banknote Denominations Counter Grid */}
          <div className="space-y-2">
            <h4 className="font-bold text-slate-800 dark:text-slate-200 flex items-center justify-between">
              <span className="flex items-center gap-1.5">
                <Calculator className="w-4 h-4 text-amber-500" />
                Physical Banknote Breakdown (ETB)
              </span>
              <span className="text-[10px] font-normal text-slate-400">
                Quantity x Face Value
              </span>
            </h4>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
              {denominations.map((item) => {
                const subtotal = (Number(item.count) || 0) * item.multiplier;
                return (
                  <div
                    key={item.key}
                    className="p-2.5 rounded-xl bg-slate-50/70 dark:bg-slate-800/40 border border-slate-200/80 dark:border-slate-700 flex items-center justify-between gap-3"
                  >
                    <div className="w-20">
                      <span className="text-[11px] font-black text-slate-900 dark:text-white block font-mono">
                        {item.label}
                      </span>
                      <span className="text-[10px] text-slate-400 font-mono">
                        = {formatCurrency(subtotal)}
                      </span>
                    </div>
                    <div className="flex-1 max-w-[120px]">
                      <input
                        type="number"
                        min="0"
                        value={item.count}
                        onChange={(e) => onNotesChange(item.key, Math.max(0, parseInt(e.target.value) || 0))}
                        className="w-full px-2.5 py-1.5 text-center font-mono font-bold text-sm bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-lg focus:ring-2 focus:ring-amber-500 focus:outline-none"
                        placeholder="0"
                      />
                    </div>
                  </div>
                );
              })}

              {/* Coins & Small Change Field */}
              <div className="p-2.5 rounded-xl bg-slate-50/70 dark:bg-slate-800/40 border border-slate-200/80 dark:border-slate-700 flex items-center justify-between gap-3">
                <div className="w-20">
                  <span className="text-[11px] font-black text-slate-900 dark:text-white block font-mono flex items-center gap-1">
                    <Coins className="w-3 h-3 text-amber-500" />
                    Coins
                  </span>
                  <span className="text-[10px] text-slate-400 font-mono">
                    = {formatCurrency(coins)}
                  </span>
                </div>
                <div className="flex-1 max-w-[120px]">
                  <input
                    type="number"
                    min="0"
                    step="0.01"
                    value={coins}
                    onChange={(e) => onNotesChange('coins', Math.max(0, parseFloat(e.target.value) || 0))}
                    className="w-full px-2.5 py-1.5 text-center font-mono font-bold text-sm bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-lg focus:ring-2 focus:ring-amber-500 focus:outline-none"
                    placeholder="0.00"
                  />
                </div>
              </div>
            </div>
          </div>

          {/* Reconciliation Remarks */}
          <div className="space-y-1">
            <label className="font-semibold block text-slate-700 dark:text-slate-300">
              Shift Settlement & Handover Remarks
            </label>
            <input
              type="text"
              value={recNotes}
              onChange={(e) => onRecNotesChange(e.target.value)}
              placeholder="e.g. End of shift balancing verified. Cash transferred to main vault."
              className="w-full px-3 py-2 text-xs bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg focus:ring-2 focus:ring-amber-500 focus:outline-none"
            />
          </div>

          <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-100 dark:border-slate-800">
            <button
              type="button"
              onClick={onClose}
              disabled={closeSubmitting}
              className="px-4 py-2 text-xs font-semibold text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl transition"
            >
              Cancel
            </button>

            <PermissionGuard
              roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER', 'BRANCH_MANAGER']}
              permissions={[PERMISSIONS.TELLER_TILL_VIEW, PERMISSIONS.CASH_DEPOSIT]}
            >
              <button
                type="submit"
                disabled={closeSubmitting || totalPhysicalCash <= 0}
                className="px-5 py-2.5 rounded-xl font-bold text-xs text-white bg-amber-600 hover:bg-amber-500 shadow-md flex items-center gap-2 transition disabled:opacity-50"
              >
                {closeSubmitting && <RefreshCw className="w-3.5 h-3.5 animate-spin" />}
                Commit Blind Count & Close Drawer
              </button>
            </PermissionGuard>
          </div>
        </form>
      </div>
    </div>
  );
};
export default ReconcileTillModal;
