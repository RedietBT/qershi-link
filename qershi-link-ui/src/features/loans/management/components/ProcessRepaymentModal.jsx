import React, { useState } from 'react';
import {
  X,
  CreditCard,
  CheckCircle2,
  AlertTriangle,
  Loader2,
  ArrowDownToLine,
  Printer,
  BadgePercent,
  Layers
} from 'lucide-react';
import { loanManagementApi } from '../api/loanManagementApi';
import { formatCurrency, formatDateTime } from '../../../../common/utils/currency';

export const ProcessRepaymentModal = ({ account, isOpen, onClose, onSuccess }) => {
  const [amountPaid, setAmountPaid] = useState('');
  const [paymentChannel, setPaymentChannel] = useState('TELLER_COUNTER');
  const [sourceAccountNo, setSourceAccountNo] = useState('');
  const [memberPhone, setMemberPhone] = useState(account?.memberPhone || '');
  const [remarks, setRemarks] = useState('Monthly installment payment');

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [receipt, setReceipt] = useState(null);

  if (!isOpen || !account) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    const numAmount = Number(amountPaid);
    if (!numAmount || numAmount <= 0) {
      setError('Please provide a valid repayment amount.');
      return;
    }

    try {
      setSubmitting(true);
      setError(null);

      const payload = {
        accountId: account.accountId,
        amountPaid: numAmount,
        paymentChannel,
        sourceAccountNo: paymentChannel === 'MEMBER_SAVINGS_DEBIT' ? sourceAccountNo.trim() : undefined,
        memberPhone: memberPhone.trim() || undefined,
        remarks: remarks.trim() || undefined
      };

      const res = await loanManagementApi.processRepayment(payload);
      const data = res.data || res;
      setReceipt(data);
      onSuccess?.();
    } catch (err) {
      console.error('Repayment processing failed:', err);
      setError(err?.response?.data?.message || 'Failed to process repayment.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleResetForNew = () => {
    setReceipt(null);
    setAmountPaid('');
    setError(null);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
      <div className="bdae-card w-full max-w-xl max-h-[90vh] overflow-hidden flex flex-col shadow-2xl border border-[var(--bdae-border)]">
        {/* Header */}
        <div className="p-5 border-b border-[var(--bdae-border)] flex items-center justify-between bg-black/5 dark:bg-white/5">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-emerald-500/10 text-emerald-600 flex items-center justify-center">
              <CreditCard className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-[var(--bdae-text-primary)]">
                Process Loan Repayment
              </h2>
              <p className="text-xs text-[var(--bdae-text-secondary)] font-mono">
                Account: {account.accountNo || account.accountId?.slice(0, 13)}
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)] rounded-xl transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Modal Body */}
        <div className="p-6 overflow-y-auto space-y-5 flex-1 text-xs">
          {error && (
            <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 flex items-start space-x-3">
              <AlertTriangle className="w-5 h-5 shrink-0 mt-0.5" />
              <div>
                <p className="font-semibold">Repayment Processing Alert</p>
                <p className="mt-0.5 opacity-90">{error}</p>
              </div>
            </div>
          )}

          {/* Repayment Allocation Waterfall Receipt */}
          {receipt && (
            <div className="space-y-4 animate-fadeIn">
              <div className="p-4 rounded-2xl bg-emerald-500/10 border border-emerald-500/20 flex items-center space-x-3">
                <CheckCircle2 className="w-8 h-8 text-emerald-600 shrink-0" />
                <div>
                  <h3 className="text-sm font-bold text-[var(--bdae-text-primary)]">
                    Repayment Processed Successfully
                  </h3>
                  <p className="text-xs text-[var(--bdae-text-secondary)] font-mono">
                    Ref: {receipt.transactionRef || receipt.repaymentId}
                  </p>
                </div>
              </div>

              {/* Waterfall Priority Allocation Breakdown */}
              <div className="bdae-card p-4 border border-[var(--bdae-border)] space-y-3">
                <span className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)] flex items-center gap-1.5">
                  <Layers className="w-3.5 h-3.5 text-[var(--bdae-primary)]" />
                  Core Banking Waterfall Priority Allocation
                </span>

                <div className="grid grid-cols-3 gap-2 text-center text-xs">
                  <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/20">
                    <span className="text-[10px] text-red-600 dark:text-red-400 font-bold uppercase block">
                      1. Penalties
                    </span>
                    <span className="font-mono font-bold text-sm text-[var(--bdae-text-primary)]">
                      {formatCurrency(receipt.penaltyPortion || 0)}
                    </span>
                  </div>

                  <div className="p-3 rounded-xl bg-amber-500/10 border border-amber-500/20">
                    <span className="text-[10px] text-amber-600 dark:text-amber-400 font-bold uppercase block">
                      2. Interest
                    </span>
                    <span className="font-mono font-bold text-sm text-[var(--bdae-text-primary)]">
                      {formatCurrency(receipt.interestPortion || 0)}
                    </span>
                  </div>

                  <div className="p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/20">
                    <span className="text-[10px] text-emerald-600 dark:text-emerald-400 font-bold uppercase block">
                      3. Principal
                    </span>
                    <span className="font-mono font-bold text-sm text-[var(--bdae-text-primary)]">
                      {formatCurrency(receipt.principalPortion || 0)}
                    </span>
                  </div>
                </div>

                <div className="pt-2 border-t border-[var(--bdae-border)] flex justify-between font-bold">
                  <span>Total Amount Paid:</span>
                  <span className="font-mono text-emerald-600 dark:text-emerald-400">
                    {formatCurrency(receipt.amountPaid)}
                  </span>
                </div>
              </div>

              <div className="flex justify-between items-center pt-2">
                <button
                  type="button"
                  onClick={() => window.print()}
                  className="px-4 py-2 rounded-xl text-xs font-bold border border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5 flex items-center gap-1.5 transition-colors"
                >
                  <Printer className="w-3.5 h-3.5" />
                  Print Receipt
                </button>

                <button
                  type="button"
                  onClick={onClose}
                  className="px-5 py-2 rounded-xl text-xs font-bold bg-[var(--bdae-primary)] text-white hover:opacity-90 transition-opacity"
                >
                  Done
                </button>
              </div>
            </div>
          )}

          {!receipt && (
            <form id="repayment-form" onSubmit={handleSubmit} className="space-y-4">
              {/* Account Metrics */}
              <div className="p-3.5 rounded-xl bg-black/5 dark:bg-white/5 grid grid-cols-2 gap-3">
                <div>
                  <span className="text-[10px] text-[var(--bdae-text-secondary)] uppercase block font-bold">
                    Disbursed Principal
                  </span>
                  <span className="font-mono font-bold text-sm text-[var(--bdae-text-primary)]">
                    {formatCurrency(account.principalAmount)}
                  </span>
                </div>
                <div>
                  <span className="text-[10px] text-[var(--bdae-text-secondary)] uppercase block font-bold">
                    Outstanding Balance
                  </span>
                  <span className="font-mono font-black text-sm text-red-600 dark:text-red-400">
                    {formatCurrency(account.outstandingBalance ?? account.principalAmount)}
                  </span>
                </div>
              </div>

              {/* Repayment Amount */}
              <div className="space-y-1">
                <label className="font-bold text-[var(--bdae-text-primary)]">
                  Repayment Amount (ETB) *
                </label>
                <input
                  type="number"
                  step="0.01"
                  min="1"
                  required
                  placeholder="0.00"
                  value={amountPaid}
                  onChange={(e) => setAmountPaid(e.target.value)}
                  className="bdae-input font-mono font-black text-base"
                />
              </div>

              {/* Channel Selector */}
              <div className="space-y-1">
                <label className="font-bold text-[var(--bdae-text-primary)]">
                  Payment Channel
                </label>
                <select
                  value={paymentChannel}
                  onChange={(e) => setPaymentChannel(e.target.value)}
                  className="bdae-input text-xs font-semibold"
                >
                  <option value="TELLER_COUNTER">Over-the-Counter (Teller Desk)</option>
                  <option value="MEMBER_SAVINGS_DEBIT">Debit Member Savings Account</option>
                  <option value="MOBILE_TRANSFER">Telebirr / Mobile Money</option>
                </select>
              </div>

              {paymentChannel === 'MEMBER_SAVINGS_DEBIT' && (
                <div className="space-y-1 animate-fadeIn">
                  <label className="font-bold text-[var(--bdae-text-primary)]">
                    Source Savings Account Number *
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. ACC-AW-2026-0001"
                    value={sourceAccountNo}
                    onChange={(e) => setSourceAccountNo(e.target.value)}
                    className="bdae-input font-mono uppercase text-xs"
                  />
                </div>
              )}

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div className="space-y-1">
                  <label className="font-bold text-[var(--bdae-text-primary)]">
                    Member Notification Phone
                  </label>
                  <input
                    type="text"
                    placeholder="+251..."
                    value={memberPhone}
                    onChange={(e) => setMemberPhone(e.target.value)}
                    className="bdae-input text-xs font-mono"
                  />
                </div>

                <div className="space-y-1">
                  <label className="font-bold text-[var(--bdae-text-primary)]">
                    Payment Remarks
                  </label>
                  <input
                    type="text"
                    placeholder="Installment payment notes"
                    value={remarks}
                    onChange={(e) => setRemarks(e.target.value)}
                    className="bdae-input text-xs"
                  />
                </div>
              </div>
            </form>
          )}
        </div>

        {/* Footer */}
        {!receipt && (
          <div className="p-4 border-t border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              disabled={submitting}
              className="px-4 py-2 rounded-xl text-xs font-semibold hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              form="repayment-form"
              disabled={submitting || Number(amountPaid) <= 0}
              className="px-5 py-2.5 rounded-xl text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50 shadow-md flex items-center gap-2 transition-all"
            >
              {submitting && <Loader2 className="w-4 h-4 animate-spin" />}
              <ArrowDownToLine className="w-4 h-4" />
              Post Repayment
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
