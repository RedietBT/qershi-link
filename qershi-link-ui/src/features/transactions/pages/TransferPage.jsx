import React, { useState } from 'react';
import {
  ArrowLeftRight,
  Search,
  AlertTriangle,
  RefreshCw,
  ShieldCheck,
  Loader2,
  ArrowRight,
} from 'lucide-react';
import { transactionApi } from '../api/transactionApi';
import { accountLedgerApi } from '../../accounts/api/accountLedgerApi';
import { formatCurrency, generateIdempotencyKey } from '../../../common/utils/currency';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';
import { GLJournalModal } from '../components/GLJournalModal';
import { TransferAccountCard } from '../components/TransferAccountCard';
import { TransferReceiptVoucher } from '../components/TransferReceiptVoucher';
import { TransferConfirmModal } from '../components/TransferConfirmModal';

/**
 * Modular Member-to-Member Funds Transfer Page
 */
export const TransferPage = () => {
  // Account Inputs
  const [senderAccountNo, setSenderAccountNo] = useState('');
  const [receiverAccountNo, setReceiverAccountNo] = useState('');
  const [amount, setAmount] = useState('');
  const [narration, setNarration] = useState('Internal member savings transfer');
  const [idempotencyKey, setIdempotencyKey] = useState(generateIdempotencyKey());

  // Sender Lookup State
  const [senderDetails, setSenderDetails] = useState(null);
  const [senderLoading, setSenderLoading] = useState(false);
  const [senderError, setSenderError] = useState(null);

  // Receiver Lookup State
  const [receiverDetails, setReceiverDetails] = useState(null);
  const [receiverLoading, setReceiverLoading] = useState(false);
  const [receiverError, setReceiverError] = useState(null);

  // Processing & Confirmation State
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState(null);
  const [confirmModalOpen, setConfirmModalOpen] = useState(false);
  const [receipt, setReceipt] = useState(null);

  // GL Journal Audit Modal
  const [glModalOpen, setGlModalOpen] = useState(false);
  const [selectedTxRef, setSelectedTxRef] = useState(null);

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
      {/* Page Title */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-[var(--bdae-border)] pb-4">
        <div>
          <h1 className="text-xl font-black text-[var(--bdae-text-primary)] flex items-center gap-2.5">
            <ArrowLeftRight className="w-6 h-6 text-cyan-500" />
            Member Funds Transfer
          </h1>
          <p className="text-xs text-[var(--bdae-text-secondary)] mt-0.5">
            Process internal intra-SACCO account transfers with balanced double-entry General Ledger settlement.
          </p>
        </div>
      </div>

      {/* Error Alert */}
      {error && (
        <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 flex items-start space-x-3 text-xs">
          <AlertTriangle className="w-5 h-5 shrink-0 mt-0.5" />
          <div>
            <p className="font-semibold">Transfer Alert</p>
            <p className="mt-0.5">{error}</p>
          </div>
        </div>
      )}

      {/* Success Receipt Voucher Component */}
      {receipt && (
        <TransferReceiptVoucher
          receipt={receipt}
          onViewGL={(ref) => {
            setSelectedTxRef(ref);
            setGlModalOpen(true);
          }}
          onResetForNew={handleResetForNew}
        />
      )}

      {/* Main Transfer Flow */}
      {!receipt && (
        <div className="space-y-6">
          {/* Dual Account Verification Cards */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Sender Column */}
            <div className="bdae-card p-5 border border-[var(--bdae-border)] space-y-4">
              <h2 className="text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] flex items-center gap-2">
                <Search className="w-4 h-4 text-[var(--bdae-primary)]" />
                1. Sender Account (Source)
              </h2>

              <div className="flex gap-2">
                <input
                  type="text"
                  placeholder="e.g. ACC-AW-2026-0001"
                  value={senderAccountNo}
                  onChange={(e) => setSenderAccountNo(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleLookupSender()}
                  className="bdae-input flex-1 font-mono uppercase text-xs font-bold"
                />
                <button
                  type="button"
                  onClick={handleLookupSender}
                  disabled={senderLoading || !senderAccountNo.trim()}
                  className="px-4 py-2 bg-[var(--bdae-primary)] text-white rounded-xl text-xs font-bold flex items-center gap-1.5 hover:opacity-90 disabled:opacity-50 transition-all shadow-sm"
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

              <TransferAccountCard
                accountDetails={senderDetails}
                title="Debited Source Account"
              />
            </div>

            {/* Receiver Column */}
            <div className="bdae-card p-5 border border-[var(--bdae-border)] space-y-4">
              <h2 className="text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] flex items-center gap-2">
                <Search className="w-4 h-4 text-cyan-500" />
                2. Destination Account (Recipient)
              </h2>

              <div className="flex gap-2">
                <input
                  type="text"
                  placeholder="e.g. ACC-AW-2026-0002"
                  value={receiverAccountNo}
                  onChange={(e) => setReceiverAccountNo(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleLookupReceiver()}
                  className="bdae-input flex-1 font-mono uppercase text-xs font-bold"
                />
                <button
                  type="button"
                  onClick={handleLookupReceiver}
                  disabled={receiverLoading || !receiverAccountNo.trim()}
                  className="px-4 py-2 bg-cyan-600 text-white rounded-xl text-xs font-bold flex items-center gap-1.5 hover:opacity-90 disabled:opacity-50 transition-all shadow-sm"
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

              <TransferAccountCard
                accountDetails={receiverDetails}
                title="Credited Destination Account"
              />
            </div>
          </div>

          {/* Amount & Execution Form */}
          <form onSubmit={handleOpenConfirm} className="bdae-card p-6 border border-[var(--bdae-border)] space-y-5">
            <h2 className="text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] flex items-center gap-2">
              <ArrowRight className="w-4 h-4 text-cyan-500" />
              3. Transfer Specification & Execution
            </h2>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Amount */}
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-[var(--bdae-text-primary)] flex items-center justify-between">
                  <span>Transfer Amount (ETB) *</span>
                  {senderDetails && (
                    <span className="text-[11px] text-[var(--bdae-text-secondary)] font-normal">
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
                  <p className="text-[11px] text-red-500 font-semibold">
                    Amount exceeds sender available balance ({formatCurrency(senderAvailableBal)}).
                  </p>
                )}
              </div>

              {/* Narration */}
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-[var(--bdae-text-primary)]">
                  Transaction Purpose / Memo
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

            {/* Transfer Submission Button strictly gated with MEMBER_TRANSFER */}
            <PermissionGuard permissions={[PERMISSIONS.MEMBER_TRANSFER]}>
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
                className="w-full py-3.5 rounded-xl text-xs font-bold text-white bg-cyan-600 hover:bg-cyan-700 shadow-md flex items-center justify-center gap-2 transition-all disabled:opacity-50"
              >
                <ArrowLeftRight className="w-4 h-4" />
                Review & Confirm Transfer
              </button>
            </PermissionGuard>
          </form>
        </div>
      )}

      {/* Confirmation Modal Component */}
      <TransferConfirmModal
        isOpen={confirmModalOpen}
        senderDetails={senderDetails}
        receiverDetails={receiverDetails}
        numAmount={numAmount}
        senderAvailableBal={senderAvailableBal}
        processing={processing}
        onClose={() => setConfirmModalOpen(false)}
        onConfirm={handleExecuteTransfer}
      />

      {/* GL Journal Audit Modal */}
      <GLJournalModal
        isOpen={glModalOpen}
        transactionRef={selectedTxRef}
        onClose={() => setGlModalOpen(false)}
      />
    </div>
  );
};
