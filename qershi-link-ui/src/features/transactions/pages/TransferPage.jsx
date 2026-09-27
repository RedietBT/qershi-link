import React, { useState } from 'react';
import {
  ArrowLeftRight,
  Search,
  CheckCircle2,
  AlertTriangle,
  RefreshCw,
  Printer,
  ShieldCheck,
  User,
  Loader2,
  BookOpen,
  ArrowRight
} from 'lucide-react';
import { transactionApi } from '../api/transactionApi';
import { accountLedgerApi } from '../../accounts/api/accountLedgerApi';
import { formatCurrency, formatDateTime, generateIdempotencyKey } from '../../../common/utils/currency';
import { GLJournalModal } from '../components/GLJournalModal';

export const TransferPage = () => {
  // Account Inputs
  const [senderAccountNo, setSenderAccountNo] = useState('');
  const [receiverAccountNo, setReceiverAccountNo] = useState('');
  const [amount, setAmount] = useState('');
  const [narration, setNarration] = useState('Internal member savings transfer');
  const [idempotencyKey, setIdempotencyKey] = useState(generateIdempotencyKey());

  // Sender Lookup
  const [senderDetails, setSenderDetails] = useState(null);
  const [senderLoading, setSenderLoading] = useState(false);
  const [senderError, setSenderError] = useState(null);

  // Receiver Lookup
  const [receiverDetails, setReceiverDetails] = useState(null);
  const [receiverLoading, setReceiverLoading] = useState(false);
  const [receiverError, setReceiverError] = useState(null);

  // Processing & State
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState(null);
  const [confirmModalOpen, setConfirmModalOpen] = useState(false);
  const [receipt, setReceipt] = useState(null);

  // GL Journal Audit Modal
  const [glModalOpen, setGlModalOpen] = useState(false);
  const [selectedTxRef, setSelectedTxRef] = useState(null);

  // Sender Lookup Action
  const handleLookupSender = async () => {
    const target = senderAccountNo.trim();
    if (!target) return;
    try {
      setSenderLoading(true);
      setSenderError(null);
      setSenderDetails(null);
      const res = await accountLedgerApi.getAccountByNo(target);
      const data = res.data || res;
      setSenderDetails(data);
    } catch (err) {
      console.error('Sender lookup error:', err);
      setSenderError(err?.response?.data?.message || 'Sender account not found.');
    } finally {
      setSenderLoading(false);
    }
  };

  // Receiver Lookup Action
  const handleLookupReceiver = async () => {
    const target = receiverAccountNo.trim();
    if (!target) return;
    try {
      setReceiverLoading(true);
      setReceiverError(null);
      setReceiverDetails(null);
      const res = await accountLedgerApi.getAccountByNo(target);
      const data = res.data || res;
      setReceiverDetails(data);
    } catch (err) {
      console.error('Receiver lookup error:', err);
      setReceiverError(err?.response?.data?.message || 'Destination account not found.');
    } finally {
      setReceiverLoading(false);
    }
  };

  const numAmount = Number(amount) || 0;
  const senderAvailableBal = Number(senderDetails?.availableBalance ?? senderDetails?.clearedBalance ?? 0);
  const isSameAccount = senderDetails && receiverDetails && senderDetails.accountNo === receiverDetails.accountNo;
  const isInsufficientFunds = senderDetails && numAmount > senderAvailableBal;

  const handleOpenConfirm = (e) => {
    e.preventDefault();
    if (!senderDetails) {
      setError('Please verify the source (sender) account before proceeding.');
      return;
    }
    if (!receiverDetails) {
      setError('Please verify the destination (receiver) account before proceeding.');
      return;
    }
    if (isSameAccount) {
      setError('Source and destination accounts cannot be identical.');
      return;
    }
    if (numAmount <= 0) {
      setError('Transfer amount must be greater than zero.');
      return;
    }
    if (isInsufficientFunds) {
      setError('Insufficient funds in sender account.');
      return;
    }
    setError(null);
    setConfirmModalOpen(true);
  };

  const handleExecuteTransfer = async () => {
    try {
      setProcessing(true);
      setError(null);

      const payload = {
        senderAccountNo: senderDetails.accountNo,
        receiverAccountNo: receiverDetails.accountNo,
        amount: numAmount,
        narration: narration.trim() || undefined,
      };

      const res = await transactionApi.processTransfer(payload, idempotencyKey);
      setReceipt(res.data);
      setConfirmModalOpen(false);
    } catch (err) {
      console.error('Transfer failed:', err);
      setError(err?.response?.data?.message || 'Transfer failed. Check network or permissions.');
      setConfirmModalOpen(false);
    } finally {
      setProcessing(false);
    }
  };

  const handleResetForNew = () => {
    setReceipt(null);
    setAmount('');
    setError(null);
    setIdempotencyKey(generateIdempotencyKey());
    if (senderDetails) handleLookupSender();
    if (receiverDetails) handleLookupReceiver();
  };

  return (
    <div className="space-y-6 animate-fadeIn max-w-5xl mx-auto">
      {/* Title Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-[var(--bdae-border)] pb-4">
        <div>
          <h1 className="text-xl font-black text-[var(--bdae-text-primary)] flex items-center gap-2.5">
            <ArrowLeftRight className="w-6 h-6 text-[var(--bdae-secondary)]" />
            Member-to-Member Funds Transfer
          </h1>
          <p className="text-xs text-[var(--bdae-text-secondary)] mt-0.5">
            Execute internal intra-SACCO account transfers with atomic debit/credit settlement and GL journal synchronization.
          </p>
        </div>
      </div>

      {/* Global Error Banner */}
      {error && (
        <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 flex items-start space-x-3 text-xs">
          <AlertTriangle className="w-5 h-5 shrink-0 mt-0.5" />
          <div>
            <p className="font-semibold">Transfer Processing Alert</p>
            <p className="mt-0.5">{error}</p>
          </div>
        </div>
      )}

      {/* Success Receipt Slip */}
      {receipt && (
        <div className="bdae-card p-6 border-2 border-emerald-500/30 bg-emerald-500/5 space-y-5 rounded-2xl shadow-lg">
          <div className="flex items-start justify-between">
            <div className="flex items-center space-x-3">
              <div className="w-12 h-12 rounded-2xl bg-emerald-500/20 text-emerald-600 flex items-center justify-center">
                <CheckCircle2 className="w-7 h-7" />
              </div>
              <div>
                <span className="px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-700 dark:text-emerald-300 text-[10px] font-extrabold uppercase">
                  Settlement Committed
                </span>
                <h2 className="text-lg font-bold text-[var(--bdae-text-primary)] mt-1">
                  Transfer Executed Successfully
                </h2>
                <p className="text-xs text-[var(--bdae-text-secondary)] font-mono">
                  Ref: {receipt.transactionRef}
                </p>
              </div>
            </div>
            <div className="text-right">
              <span className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)] block">
                Total Transferred
              </span>
              <span className="text-2xl font-black text-emerald-600 dark:text-emerald-400 font-mono">
                {formatCurrency(receipt.amount, receipt.currency || 'ETB')}
              </span>
            </div>
          </div>

          {/* Transfer Flow Route Card */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3 p-4 rounded-xl bg-black/5 dark:bg-white/5 items-center text-xs">
            <div className="p-3 bg-[var(--bdae-surface)] rounded-xl border border-[var(--bdae-border)]">
              <span className="text-[10px] uppercase font-bold text-red-500 block">
                Source (Debited)
              </span>
              <p className="font-mono font-bold text-[var(--bdae-text-primary)] mt-0.5">
                {senderDetails?.accountNo}
              </p>
              <p className="text-[11px] text-[var(--bdae-text-secondary)]">
                {senderDetails?.holderName || 'Sender Account'}
              </p>
            </div>

            <div className="flex flex-col items-center justify-center text-[var(--bdae-text-secondary)]">
              <ArrowRight className="w-6 h-6 text-[var(--bdae-secondary)]" />
              <span className="text-[10px] font-mono font-bold text-[var(--bdae-secondary)]">
                {formatCurrency(receipt.amount)}
              </span>
            </div>

            <div className="p-3 bg-[var(--bdae-surface)] rounded-xl border border-[var(--bdae-border)]">
              <span className="text-[10px] uppercase font-bold text-emerald-500 block">
                Destination (Credited)
              </span>
              <p className="font-mono font-bold text-[var(--bdae-text-primary)] mt-0.5">
                {receiverDetails?.accountNo}
              </p>
              <p className="text-[11px] text-[var(--bdae-text-secondary)]">
                {receiverDetails?.holderName || 'Receiver Account'}
              </p>
            </div>
          </div>

          <div className="flex flex-wrap items-center justify-between gap-3 pt-2">
            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => {
                  setSelectedTxRef(receipt.transactionRef);
                  setGlModalOpen(true);
                }}
                className="px-4 py-2 bg-[var(--bdae-surface)] border border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5 rounded-xl text-xs font-bold text-[var(--bdae-text-primary)] flex items-center gap-2 shadow-sm transition-all"
              >
                <BookOpen className="w-4 h-4 text-[var(--bdae-primary)]" />
                View GL Double-Entry Lines
              </button>
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
              onClick={handleResetForNew}
              className="px-5 py-2.5 bg-[var(--bdae-primary)] text-white hover:opacity-90 rounded-xl text-xs font-bold shadow-md transition-all flex items-center gap-2"
            >
              <RefreshCw className="w-4 h-4" />
              New Transfer
            </button>
          </div>
        </div>
      )}

      {/* Main Transfer Workflow Form */}
      {!receipt && (
        <form onSubmit={handleOpenConfirm} className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Sender / Source Account Card */}
            <div className="bdae-card p-5 border border-[var(--bdae-border)] space-y-4">
              <div className="flex items-center justify-between">
                <h2 className="text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] flex items-center gap-2">
                  <User className="w-4 h-4 text-red-500" />
                  1. Source Account (Debit)
                </h2>
                <span className="text-[10px] font-bold text-red-500 bg-red-500/10 px-2 py-0.5 rounded-full">
                  FUNDS SENDER
                </span>
              </div>

              <div className="flex gap-2">
                <input
                  type="text"
                  placeholder="e.g. ACC-AW-2026-0001"
                  value={senderAccountNo}
                  onChange={(e) => setSenderAccountNo(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), handleLookupSender())}
                  className="bdae-input flex-1 font-mono uppercase text-xs font-bold"
                />
                <button
                  type="button"
                  onClick={handleLookupSender}
                  disabled={senderLoading || !senderAccountNo.trim()}
                  className="px-3.5 py-2 bg-black/5 dark:bg-white/5 hover:bg-black/10 rounded-xl text-xs font-bold flex items-center gap-1.5 transition-colors border border-[var(--bdae-border)]"
                >
                  {senderLoading ? <Loader2 className="w-4 h-4 animate-spin" /> : <Search className="w-4 h-4" />}
                  Verify
                </button>
              </div>

              {senderError && (
                <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 text-xs">
                  {senderError}
                </div>
              )}

              {senderDetails && (
                <div className="p-3.5 rounded-xl bg-black/5 dark:bg-white/5 space-y-2 text-xs animate-fadeIn">
                  <div className="flex justify-between">
                    <span className="text-[var(--bdae-text-secondary)]">Member Name:</span>
                    <span className="font-bold text-[var(--bdae-text-primary)]">
                      {senderDetails.holderName || senderDetails.userId || 'Verified Member'}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-[var(--bdae-text-secondary)]">Product:</span>
                    <span className="font-medium text-[var(--bdae-text-primary)]">
                      {senderDetails.productCode || 'Savings'}
                    </span>
                  </div>
                  <div className="flex justify-between pt-1 border-t border-[var(--bdae-border)]">
                    <span className="font-bold text-[var(--bdae-primary)]">Available Balance:</span>
                    <span className="font-mono font-black text-sm text-[var(--bdae-primary)]">
                      {formatCurrency(senderAvailableBal)}
                    </span>
                  </div>
                </div>
              )}
            </div>

            {/* Receiver / Destination Account Card */}
            <div className="bdae-card p-5 border border-[var(--bdae-border)] space-y-4">
              <div className="flex items-center justify-between">
                <h2 className="text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] flex items-center gap-2">
                  <User className="w-4 h-4 text-emerald-500" />
                  2. Destination Account (Credit)
                </h2>
                <span className="text-[10px] font-bold text-emerald-500 bg-emerald-500/10 px-2 py-0.5 rounded-full">
                  BENEFICIARY
                </span>
              </div>

              <div className="flex gap-2">
                <input
                  type="text"
                  placeholder="e.g. ACC-AW-2026-0002"
                  value={receiverAccountNo}
                  onChange={(e) => setReceiverAccountNo(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), handleLookupReceiver())}
                  className="bdae-input flex-1 font-mono uppercase text-xs font-bold"
                />
                <button
                  type="button"
                  onClick={handleLookupReceiver}
                  disabled={receiverLoading || !receiverAccountNo.trim()}
                  className="px-3.5 py-2 bg-black/5 dark:bg-white/5 hover:bg-black/10 rounded-xl text-xs font-bold flex items-center gap-1.5 transition-colors border border-[var(--bdae-border)]"
                >
                  {receiverLoading ? <Loader2 className="w-4 h-4 animate-spin" /> : <Search className="w-4 h-4" />}
                  Verify
                </button>
              </div>

              {receiverError && (
                <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 text-xs">
                  {receiverError}
                </div>
              )}

              {isSameAccount && (
                <div className="p-3 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-600 dark:text-amber-400 text-xs">
                  Source and Destination cannot be the same account.
                </div>
              )}

              {receiverDetails && (
                <div className="p-3.5 rounded-xl bg-black/5 dark:bg-white/5 space-y-2 text-xs animate-fadeIn">
                  <div className="flex justify-between">
                    <span className="text-[var(--bdae-text-secondary)]">Member Name:</span>
                    <span className="font-bold text-[var(--bdae-text-primary)]">
                      {receiverDetails.holderName || receiverDetails.userId || 'Verified Member'}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-[var(--bdae-text-secondary)]">Product:</span>
                    <span className="font-medium text-[var(--bdae-text-primary)]">
                      {receiverDetails.productCode || 'Savings'}
                    </span>
                  </div>
                  <div className="flex justify-between pt-1 border-t border-[var(--bdae-border)]">
                    <span className="text-[var(--bdae-text-secondary)]">Status:</span>
                    <span className="font-bold text-emerald-500">
                      {receiverDetails.status || 'ACTIVE'}
                    </span>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Transfer Amount & Execution Card */}
          <div className="bdae-card p-6 border border-[var(--bdae-border)] space-y-5">
            <h2 className="text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] flex items-center gap-2">
              <ArrowLeftRight className="w-4 h-4 text-[var(--bdae-primary)]" />
              3. Transfer Amount & Remittance Info
            </h2>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label className="text-xs font-bold text-[var(--bdae-text-primary)] flex justify-between">
                  <span>Amount to Transfer (ETB) *</span>
                  {senderDetails && (
                    <span className="text-[11px] font-normal text-[var(--bdae-text-secondary)]">
                      Max: <strong className="font-mono">{formatCurrency(senderAvailableBal)}</strong>
                    </span>
                  )}
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-xs font-bold text-[var(--bdae-text-secondary)]">
                    ETB
                  </div>
                  <input
                    type="number"
                    step="0.01"
                    min="1"
                    required
                    placeholder="0.00"
                    value={amount}
                    onChange={(e) => setAmount(e.target.value)}
                    className="bdae-input pl-12 font-mono text-lg font-bold"
                  />
                </div>
                {isInsufficientFunds && (
                  <p className="text-[11px] text-red-500 font-semibold flex items-center gap-1">
                    <AlertTriangle className="w-3.5 h-3.5" />
                    Amount exceeds available balance in sender account.
                  </p>
                )}
              </div>

              <div className="space-y-2">
                <label className="text-xs font-bold text-[var(--bdae-text-primary)]">
                  Remittance / Narration
                </label>
                <input
                  type="text"
                  value={narration}
                  onChange={(e) => setNarration(e.target.value)}
                  placeholder="e.g. Monthly contribution transfer"
                  className="bdae-input text-xs"
                />
              </div>
            </div>

            {/* Idempotency Protection */}
            <div className="p-3 rounded-xl bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] flex items-center justify-between text-xs">
              <div className="flex items-center space-x-2">
                <ShieldCheck className="w-4 h-4 text-emerald-500" />
                <div>
                  <span className="text-[10px] text-[var(--bdae-text-secondary)] font-bold uppercase block">
                    Idempotency Settlement Key
                  </span>
                  <span className="font-mono text-[11px] text-[var(--bdae-text-primary)]">
                    {idempotencyKey.slice(0, 18)}...
                  </span>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setIdempotencyKey(generateIdempotencyKey())}
                className="p-1.5 hover:bg-black/10 dark:hover:bg-white/10 rounded-lg text-[var(--bdae-text-secondary)] transition-colors"
                title="Regenerate Key"
              >
                <RefreshCw className="w-3.5 h-3.5" />
              </button>
            </div>

            <button
              type="submit"
              disabled={
                !senderDetails ||
                !receiverDetails ||
                isSameAccount ||
                numAmount <= 0 ||
                isInsufficientFunds ||
                processing
              }
              className="w-full py-3.5 rounded-xl text-xs font-bold text-white bg-[var(--bdae-primary)] hover:opacity-90 disabled:opacity-50 shadow-md transition-all flex items-center justify-center gap-2"
            >
              <ArrowLeftRight className="w-4 h-4" />
              Review & Execute Funds Transfer
            </button>
          </div>
        </form>
      )}

      {/* Confirmation Modal */}
      {confirmModalOpen && senderDetails && receiverDetails && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
          <div className="bdae-card w-full max-w-md p-6 space-y-5 rounded-2xl shadow-2xl border border-[var(--bdae-border)]">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 rounded-xl bg-[var(--bdae-primary)]/10 text-[var(--bdae-primary)] flex items-center justify-center">
                <ArrowLeftRight className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-base font-bold text-[var(--bdae-text-primary)]">
                  Confirm Member Transfer
                </h3>
                <p className="text-xs text-[var(--bdae-text-secondary)]">
                  Verify internal settlement details before posting to General Ledger.
                </p>
              </div>
            </div>

            <div className="p-4 rounded-xl bg-black/5 dark:bg-white/5 space-y-2.5 text-xs">
              <div className="flex justify-between">
                <span className="text-[var(--bdae-text-secondary)]">Debit Account (From):</span>
                <span className="font-mono font-bold text-[var(--bdae-text-primary)]">
                  {senderDetails.accountNo}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-[var(--bdae-text-secondary)]">Credit Account (To):</span>
                <span className="font-mono font-bold text-[var(--bdae-text-primary)]">
                  {receiverDetails.accountNo}
                </span>
              </div>
              <div className="flex justify-between pt-2 border-t border-[var(--bdae-border)]">
                <span className="font-bold text-[var(--bdae-text-primary)]">Transfer Amount:</span>
                <span className="font-mono text-base font-black text-emerald-600">
                  {formatCurrency(numAmount)}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-[var(--bdae-text-secondary)]">Sender Balance After:</span>
                <span className="font-mono font-bold text-[var(--bdae-text-primary)]">
                  {formatCurrency(senderAvailableBal - numAmount)}
                </span>
              </div>
            </div>

            <div className="flex items-center justify-end gap-3 pt-2">
              <button
                type="button"
                onClick={() => setConfirmModalOpen(false)}
                disabled={processing}
                className="px-4 py-2 rounded-xl text-xs font-semibold hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleExecuteTransfer}
                disabled={processing}
                className="px-5 py-2.5 rounded-xl text-xs font-bold text-white bg-[var(--bdae-primary)] hover:opacity-90 shadow-md flex items-center gap-2 transition-all"
              >
                {processing && <Loader2 className="w-4 h-4 animate-spin" />}
                Confirm & Transfer
              </button>
            </div>
          </div>
        </div>
      )}

      {/* GL Journal Audit Modal */}
      <GLJournalModal
        isOpen={glModalOpen}
        transactionRef={selectedTxRef}
        onClose={() => setGlModalOpen(false)}
      />
    </div>
  );
};
