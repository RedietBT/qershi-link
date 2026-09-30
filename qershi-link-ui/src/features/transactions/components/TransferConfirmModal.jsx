import React from 'react';
import { ArrowLeftRight, Loader2 } from 'lucide-react';
import { formatCurrency } from '../../../common/utils/currency';
import { MaskedDataField } from '../../../common/components/MaskedDataField';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';

/**
 * Confirmation dialog before posting an internal fund transfer
 */
export const TransferConfirmModal = ({
  isOpen,
  senderDetails,
  receiverDetails,
  numAmount,
  senderAvailableBal,
  processing,
  onClose,
  onConfirm,
}) => {
  if (!isOpen || !senderDetails || !receiverDetails) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
      <div className="bdae-card w-full max-w-md p-6 space-y-5 rounded-2xl shadow-2xl border border-[var(--bdae-border)]">
        <div className="flex items-center space-x-3">
          <div className="w-10 h-10 rounded-xl bg-cyan-500/10 text-cyan-600 flex items-center justify-center">
            <ArrowLeftRight className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-base font-bold text-[var(--bdae-text-primary)]">
              Confirm Internal Transfer
            </h3>
            <p className="text-xs text-[var(--bdae-text-secondary)]">
              Double-check sender and destination before executing ledger settlement.
            </p>
          </div>
        </div>

        <div className="p-4 rounded-xl bg-black/5 dark:bg-white/5 space-y-3 text-xs">
          <div className="flex justify-between items-center">
            <span className="text-[var(--bdae-text-secondary)]">From (Debited):</span>
            <div className="text-right">
              <span className="font-mono font-bold text-[var(--bdae-text-primary)] block">
                <MaskedDataField value={senderDetails.accountNo} type="account" allowReveal={true} />
              </span>
              <span className="text-[10px] text-[var(--bdae-text-secondary)]">
                {senderDetails.holderName || senderDetails.userId || 'Verified Member'}
              </span>
            </div>
          </div>

          <div className="flex justify-between items-center">
            <span className="text-[var(--bdae-text-secondary)]">To (Credited):</span>
            <div className="text-right">
              <span className="font-mono font-bold text-[var(--bdae-text-primary)] block">
                <MaskedDataField value={receiverDetails.accountNo} type="account" allowReveal={true} />
              </span>
              <span className="text-[10px] text-[var(--bdae-text-secondary)]">
                {receiverDetails.holderName || receiverDetails.userId || 'Verified Member'}
              </span>
            </div>
          </div>

          <div className="flex justify-between pt-2 border-t border-[var(--bdae-border)]">
            <span className="font-bold text-[var(--bdae-text-primary)]">Transfer Amount:</span>
            <span className="font-mono text-sm font-black text-cyan-600">
              {formatCurrency(numAmount)}
            </span>
          </div>

          <div className="flex justify-between">
            <span className="text-[var(--bdae-text-secondary)]">Sender Post-Balance:</span>
            <span className="font-mono font-bold text-[var(--bdae-text-primary)]">
              {formatCurrency(senderAvailableBal - numAmount)}
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

          {/* Transfer button strictly guarded with MEMBER_TRANSFER */}
          <PermissionGuard permissions={[PERMISSIONS.MEMBER_TRANSFER]}>
            <button
              type="button"
              onClick={onConfirm}
              disabled={processing}
              className="px-5 py-2.5 rounded-xl text-xs font-bold text-white bg-cyan-600 hover:bg-cyan-700 shadow-md flex items-center gap-2 transition-all"
            >
              {processing && <Loader2 className="w-4 h-4 animate-spin" />}
              Execute Transfer & Post
            </button>
          </PermissionGuard>
        </div>
      </div>
    </div>
  );
};
