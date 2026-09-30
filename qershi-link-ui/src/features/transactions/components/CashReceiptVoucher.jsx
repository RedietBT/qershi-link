import React from 'react';
import { CheckCircle2, BookOpen, Printer, RefreshCw } from 'lucide-react';
import { formatCurrency, formatDateTime } from '../../../common/utils/currency';
import { MaskedDataField } from '../../../common/components/MaskedDataField';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';

/**
 * Committed Cash Desk Transaction Receipt Voucher
 */
export const CashReceiptVoucher = ({
  receipt,
  operationType,
  onViewGL,
  onResetForNew,
}) => {
  if (!receipt) return null;

  return (
    <div className="bdae-card p-6 border-2 border-emerald-500/30 bg-emerald-500/5 space-y-5 rounded-2xl shadow-lg animate-fadeIn">
      <div className="flex items-start justify-between">
        <div className="flex items-center space-x-3">
          <div className="w-12 h-12 rounded-2xl bg-emerald-500/20 text-emerald-600 flex items-center justify-center">
            <CheckCircle2 className="w-7 h-7" />
          </div>
          <div>
            <span className="px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-700 dark:text-emerald-300 text-[10px] font-extrabold uppercase">
              Transaction Committed
            </span>
            <h2 className="text-lg font-bold text-[var(--bdae-text-primary)] mt-1">
              {operationType === 'DEPOSIT' ? 'Deposit Completed' : 'Withdrawal Completed'}
            </h2>
            <p className="text-xs text-[var(--bdae-text-secondary)] font-mono">
              Ref: {receipt.transactionRef}
            </p>
          </div>
        </div>
        <div className="text-right">
          <span className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)] block">
            Amount Posted
          </span>
          <span className="text-2xl font-black text-emerald-600 dark:text-emerald-400 font-mono">
            {formatCurrency(receipt.amount, receipt.currency || 'ETB')}
          </span>
        </div>
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 p-4 rounded-xl bg-black/5 dark:bg-white/5 text-xs font-medium">
        <div>
          <span className="text-[var(--bdae-text-secondary)] block text-[10px] uppercase font-bold">
            Target Account
          </span>
          <span className="font-mono font-bold text-[var(--bdae-text-primary)]">
            <MaskedDataField value={receipt.accountNo} type="account" allowReveal={true} />
          </span>
        </div>
        <div>
          <span className="text-[var(--bdae-text-secondary)] block text-[10px] uppercase font-bold">
            Date & Time
          </span>
          <span className="text-[var(--bdae-text-primary)]">
            {formatDateTime(receipt.createdAt)}
          </span>
        </div>
        <div>
          <span className="text-[var(--bdae-text-secondary)] block text-[10px] uppercase font-bold">
            Status
          </span>
          <span className="text-emerald-600 dark:text-emerald-400 font-bold">
            {receipt.status || 'COMPLETED'}
          </span>
        </div>
        <div>
          <span className="text-[var(--bdae-text-secondary)] block text-[10px] uppercase font-bold">
            Narration
          </span>
          <span className="text-[var(--bdae-text-primary)] truncate block">
            {receipt.narration || 'OTC Cash Transaction'}
          </span>
        </div>
      </div>

      <div className="flex flex-wrap items-center justify-between gap-3 pt-2">
        <div className="flex items-center gap-2">
          {/* View GL Journal Button strictly gated by TRANSACTION_VIEW */}
          <PermissionGuard permissions={[PERMISSIONS.TRANSACTION_VIEW]}>
            <button
              type="button"
              onClick={() => onViewGL(receipt.transactionRef)}
              className="px-4 py-2 bg-[var(--bdae-surface)] border border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5 rounded-xl text-xs font-bold text-[var(--bdae-text-primary)] flex items-center gap-2 shadow-sm transition-all"
            >
              <BookOpen className="w-4 h-4 text-[var(--bdae-primary)]" />
              View GL Double-Entry Lines
            </button>
          </PermissionGuard>

          <button
            type="button"
            onClick={() => window.print()}
            className="px-4 py-2 bg-[var(--bdae-surface)] border border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5 rounded-xl text-xs font-bold text-[var(--bdae-text-primary)] flex items-center gap-2 shadow-sm transition-all"
          >
            <Printer className="w-4 h-4" />
            Print Voucher
          </button>
        </div>

        <button
          type="button"
          onClick={onResetForNew}
          className="px-5 py-2.5 bg-[var(--bdae-primary)] text-white hover:opacity-90 rounded-xl text-xs font-bold shadow-md transition-all flex items-center gap-2"
        >
          <RefreshCw className="w-4 h-4" />
          New Transaction
        </button>
      </div>
    </div>
  );
};
