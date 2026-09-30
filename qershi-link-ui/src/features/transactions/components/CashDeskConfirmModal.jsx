import React from 'react';
import { ArrowDownToLine, ArrowUpFromLine, Loader2 } from 'lucide-react';
import { formatCurrency } from '../../../common/utils/currency';
import { MaskedDataField } from '../../../common/components/MaskedDataField';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';

/**
 * Confirmation dialog for reviewing details before submitting cash transactions
 */
export const CashDeskConfirmModal = ({
  isOpen,
  operationType,
  accountDetails,
  availableBal,
  numAmount,
  processing,
  onClose,
  onConfirm,
}) => {
  if (!isOpen || !accountDetails) return null;

  const requiredPermission =
    operationType === 'DEPOSIT' ? PERMISSIONS.CASH_DEPOSIT : PERMISSIONS.SAVINGS_WITHDRAW;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
      <div className="bdae-card w-full max-w-md p-6 space-y-5 rounded-2xl shadow-2xl border border-[var(--bdae-border)]">
        <div className="flex items-center space-x-3">
          <div
            className={`w-10 h-10 rounded-xl flex items-center justify-center ${
              operationType === 'DEPOSIT'
                ? 'bg-emerald-500/10 text-emerald-600'
                : 'bg-amber-500/10 text-amber-600'
            }`}
          >
            {operationType === 'DEPOSIT' ? (
              <ArrowDownToLine className="w-5 h-5" />
            ) : (
              <ArrowUpFromLine className="w-5 h-5" />
            )}
          </div>
          <div>
            <h3 className="text-base font-bold text-[var(--bdae-text-primary)]">
              Confirm {operationType === 'DEPOSIT' ? 'Cash Deposit' : 'Cash Withdrawal'}
            </h3>
            <p className="text-xs text-[var(--bdae-text-secondary)]">
              Review transaction details before posting to General Ledger.
            </p>
          </div>
        </div>

        <div className="p-4 rounded-xl bg-black/5 dark:bg-white/5 space-y-2.5 text-xs">
          <div className="flex justify-between items-center">
            <span className="text-[var(--bdae-text-secondary)]">Member Account:</span>
            <span className="font-mono font-bold text-[var(--bdae-text-primary)]">
              <MaskedDataField value={accountDetails.accountNo} type="account" allowReveal={true} />
            </span>
          </div>
          <div className="flex justify-between">
            <span className="text-[var(--bdae-text-secondary)]">Account Holder:</span>
            <span className="font-semibold text-[var(--bdae-text-primary)]">
              {accountDetails.holderName || accountDetails.userId || 'Verified Member'}
            </span>
          </div>
          <div className="flex justify-between">
            <span className="text-[var(--bdae-text-secondary)]">Current Available:</span>
            <span className="font-mono font-medium">{formatCurrency(availableBal)}</span>
          </div>
          <div className="flex justify-between pt-2 border-t border-[var(--bdae-border)]">
            <span className="font-bold text-[var(--bdae-text-primary)]">Transaction Amount:</span>
            <span
              className={`font-mono text-sm font-black ${
                operationType === 'DEPOSIT' ? 'text-emerald-600' : 'text-amber-600'
              }`}
            >
              {formatCurrency(numAmount)}
            </span>
          </div>
          <div className="flex justify-between">
            <span className="text-[var(--bdae-text-secondary)]">Projected Balance:</span>
            <span className="font-mono font-bold text-[var(--bdae-text-primary)]">
              {formatCurrency(
                operationType === 'DEPOSIT' ? availableBal + numAmount : availableBal - numAmount
              )}
            </span>
          </div>
        </div>

        <div className="flex items-center justify-end gap-3 pt-2">
          <button
            type="button"
            onClick={onClose}
            disabled={processing}
            className="px-4 py-2 rounded-xl text-xs font-semibold hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
          >
            Cancel
          </button>

          {/* Execute button strictly guarded by required deposit/withdrawal permission */}
          <PermissionGuard permissions={[requiredPermission]}>
            <button
              type="button"
              onClick={onConfirm}
              disabled={processing}
              className={`px-5 py-2.5 rounded-xl text-xs font-bold text-white shadow-md flex items-center gap-2 transition-all ${
                operationType === 'DEPOSIT'
                  ? 'bg-emerald-600 hover:bg-emerald-700'
                  : 'bg-amber-600 hover:bg-amber-700'
              }`}
            >
              {processing && <Loader2 className="w-4 h-4 animate-spin" />}
              Confirm & Execute Posting
            </button>
          </PermissionGuard>
        </div>
      </div>
    </div>
  );
};
