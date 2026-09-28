import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Banknote,
  ArrowDownToLine,
  ArrowUpFromLine,
  Search,
  CheckCircle2,
  AlertTriangle,
  RefreshCw,
  Printer,
  ShieldCheck,
  User,
  CreditCard,
  Loader2,
  BookOpen,
  Vault
} from 'lucide-react';
import { transactionApi } from '../api/transactionApi';
import { accountLedgerApi } from '../../accounts/api/accountLedgerApi';
import { formatCurrency, formatDateTime, generateIdempotencyKey } from '../../../common/utils/currency';
import { GLJournalModal } from '../components/GLJournalModal';

export const CashDeskPage = () => {
  const navigate = useNavigate();
  // Mode: 'DEPOSIT' | 'WITHDRAW'
  const [operationType, setOperationType] = useState('DEPOSIT');

  // Form State
  const [accountNo, setAccountNo] = useState('');
  const [amount, setAmount] = useState('');
  const [narration, setNarration] = useState('');
  const [idempotencyKey, setIdempotencyKey] = useState(generateIdempotencyKey());

  // Account Lookup State
  const [accountDetails, setAccountDetails] = useState(null);
  const [lookupLoading, setLookupLoading] = useState(false);
  const [lookupError, setLookupError] = useState(null);

  // Execution State
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState(null);
  const [confirmModalOpen, setConfirmModalOpen] = useState(false);
  const [receipt, setReceipt] = useState(null);

  // GL Journal Audit Modal
  const [glModalOpen, setGlModalOpen] = useState(false);
  const [selectedTxRef, setSelectedTxRef] = useState(null);

  // Reset default narration on mode toggle
  useEffect(() => {
    setNarration(
      operationType === 'DEPOSIT' ? 'Teller OTC Cash Deposit' : 'Teller OTC Cash Withdrawal'
    );
  }, [operationType]);

  const handleLookupAccount = async (acctNumberToLookup) => {
    const target = (acctNumberToLookup || accountNo).trim();
    if (!target) return;

    try {
      setLookupLoading(true);
      setLookupError(null);
      setAccountDetails(null);
      const res = await accountLedgerApi.getAccountByNo(target);
      const data = res.data || res;
      setAccountDetails(data);
    } catch (err) {
      console.error('Account lookup failed:', err);
      setLookupError(err?.response?.data?.message || 'Account not found. Please verify the account number.');
    } finally {
      setLookupLoading(false);
    }
  };

  const handleQuickAmount = (val) => {
    const current = Number(amount) || 0;
    setAmount(String(current + val));
  };

  const numAmount = Number(amount) || 0;
  const availableBal = Number(accountDetails?.availableBalance ?? accountDetails?.clearedBalance ?? 0);
  const isInsufficientFunds = operationType === 'WITHDRAW' && accountDetails && numAmount > availableBal;

  const handleOpenConfirm = (e) => {
    e.preventDefault();
    if (!accountDetails) {
      setError('Please verify the member account before proceeding.');
      return;
    }
    if (numAmount <= 0) {
      setError('Transaction amount must be greater than zero.');
      return;
    }
    if (isInsufficientFunds) {
      setError('Insufficient available funds for this cash withdrawal.');
      return;
    }
    setError(null);
    setConfirmModalOpen(true);
  };

  const handleExecuteTransaction = async () => {
    try {
      setProcessing(true);
      setError(null);

      const payload = {
        accountNo: accountDetails.accountNo,
        amount: numAmount,
        narration: narration.trim() || undefined,
      };

      let res;
      if (operationType === 'DEPOSIT') {
        res = await transactionApi.processDeposit(payload, idempotencyKey);
      } else {
        res = await transactionApi.processWithdrawal(payload, idempotencyKey);
      }

      setReceipt(res.data);
      setConfirmModalOpen(false);
    } catch (err) {
      console.error('Transaction execution failed:', err);
      setError(err?.response?.data?.message || 'Transaction failed. Please check network and permissions.');
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
    if (accountDetails) {
      // Re-fetch account details to refresh balance
      handleLookupAccount(accountDetails.accountNo);
    }
  };

  return (
    <div className="space-y-6 animate-fadeIn max-w-5xl mx-auto">
      {/* Page Title & Breadcrumb */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-[var(--bdae-border)] pb-4">
        <div>
          <h1 className="text-xl font-black text-[var(--bdae-text-primary)] flex items-center gap-2.5">
            <Banknote className="w-6 h-6 text-[var(--bdae-primary)]" />
            Teller Cash Desk (Over-the-Counter)
          </h1>
          <p className="text-xs text-[var(--bdae-text-secondary)] mt-0.5">
            Execute verified member cash deposits and withdrawals with real-time General Ledger balance checks.
          </p>
        </div>

        {/* Action Controls & Operation Switcher */}
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={() => navigate('/transactions/till')}
            className="flex items-center gap-2 px-3.5 py-2 rounded-xl border border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5 text-xs font-bold text-[var(--bdae-text-primary)] transition-all"
            title="Manage physical cash drawer and banknote reconciliation"
          >
            <Vault className="w-4 h-4 text-amber-500" />
            <span>Till Drawer</span>
          </button>

          {/* Operation Mode Switcher */}
          <div className="flex items-center bg-black/5 dark:bg-white/5 p-1 rounded-xl border border-[var(--bdae-border)]">
            <button
              type="button"
              onClick={() => {
                setOperationType('DEPOSIT');
                setReceipt(null);
                setError(null);
              }}
              className={`flex items-center gap-2 px-4 py-2 rounded-lg text-xs font-bold transition-all ${
                operationType === 'DEPOSIT'
                  ? 'bg-emerald-600 text-white shadow-sm'
                  : 'text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]'
              }`}
            >
              <ArrowDownToLine className="w-4 h-4" />
              Cash Deposit
            </button>
            <button
              type="button"
              onClick={() => {
                setOperationType('WITHDRAW');
                setReceipt(null);
                setError(null);
              }}
              className={`flex items-center gap-2 px-4 py-2 rounded-lg text-xs font-bold transition-all ${
                operationType === 'WITHDRAW'
                  ? 'bg-amber-600 text-white shadow-sm'
                  : 'text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]'
              }`}
            >
              <ArrowUpFromLine className="w-4 h-4" />
              Cash Withdrawal
            </button>
          </div>
        </div>
      </div>

      {/* Error Banner */}
      {error && (
        <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 flex items-start space-x-3 text-xs">
          <AlertTriangle className="w-5 h-5 shrink-0 mt-0.5" />
          <div>
            <p className="font-semibold">Transaction Processing Alert</p>
            <p className="mt-0.5">{error}</p>
          </div>
        </div>
      )}

      {/* Success Receipt Voucher */}
      {receipt && (
        <div className="bdae-card p-6 border-2 border-emerald-500/30 bg-emerald-500/5 space-y-5 rounded-2xl shadow-lg">
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
                {receipt.accountNo}
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
              New Transaction
            </button>
          </div>
        </div>
      )}

      {/* Main Two-Column Layout */}
      {!receipt && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          {/* Left Column: Account Verification & Balance Check (5 cols) */}
          <div className="lg:col-span-5 space-y-4">
            <div className="bdae-card p-5 border border-[var(--bdae-border)] space-y-4">
              <h2 className="text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] flex items-center gap-2">
                <Search className="w-4 h-4 text-[var(--bdae-primary)]" />
                1. Account Lookup & Verification
              </h2>

              <div className="flex gap-2">
                <input
                  type="text"
                  placeholder="e.g. ACC-AW-2026-0001"
                  value={accountNo}
                  onChange={(e) => setAccountNo(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleLookupAccount()}
                  className="bdae-input flex-1 font-mono uppercase text-xs font-bold"
                />
                <button
                  type="button"
                  onClick={() => handleLookupAccount()}
                  disabled={lookupLoading || !accountNo.trim()}
                  className="px-4 py-2 bg-[var(--bdae-primary)] text-white rounded-xl text-xs font-bold flex items-center gap-1.5 hover:opacity-90 disabled:opacity-50 transition-all shadow-sm"
                >
                  {lookupLoading ? <Loader2 className="w-4 h-4 animate-spin" /> : <Search className="w-4 h-4" />}
                  Verify
                </button>
              </div>

              {lookupError && (
                <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 text-xs">
                  {lookupError}
                </div>
              )}

              {/* Account Summary Card */}
              {accountDetails && (
                <div className="p-4 rounded-xl bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] space-y-3 animate-fadeIn">
                  <div className="flex items-start justify-between">
                    <div>
                      <span className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)]">
                        Account Holder
                      </span>
                      <p className="text-sm font-bold text-[var(--bdae-text-primary)] flex items-center gap-1.5">
                        <User className="w-3.5 h-3.5 text-[var(--bdae-primary)]" />
                        {accountDetails.holderName || accountDetails.userId || 'Verified Member'}
                      </p>
                      {accountDetails.phone && (
                        <p className="text-[11px] text-[var(--bdae-text-secondary)] font-mono">
                          {accountDetails.phone}
                        </p>
                      )}
                    </div>
                    <span
                      className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                        accountDetails.status === 'ACTIVE'
                          ? 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400'
                          : 'bg-amber-500/10 text-amber-600 dark:text-amber-400'
                      }`}
                    >
                      {accountDetails.status || 'ACTIVE'}
                    </span>
                  </div>

                  <div className="pt-2 border-t border-[var(--bdae-border)] grid grid-cols-2 gap-2 text-xs">
                    <div>
                      <span className="text-[10px] text-[var(--bdae-text-secondary)] uppercase block">
                        Account Type
                      </span>
                      <span className="font-semibold text-[var(--bdae-text-primary)]">
                        {accountDetails.productCode || accountDetails.accountType || 'Savings Account'}
                      </span>
                    </div>
                    <div>
                      <span className="text-[10px] text-[var(--bdae-text-secondary)] uppercase block">
                        Lien / Hold
                      </span>
                      <span className="font-semibold text-amber-600 dark:text-amber-400 font-mono">
                        {formatCurrency(accountDetails.lienBalance || 0)}
                      </span>
                    </div>
                  </div>

                  {/* Available Balance Box */}
                  <div className="p-3 rounded-lg bg-[var(--bdae-primary)]/10 border border-[var(--bdae-primary)]/20">
                    <span className="text-[10px] uppercase font-bold text-[var(--bdae-primary)] block">
                      Available Balance
                    </span>
                    <span className="text-xl font-black text-[var(--bdae-primary)] font-mono">
                      {formatCurrency(availableBal, accountDetails.currency || 'ETB')}
                    </span>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Right Column: Transaction Processing Form (7 cols) */}
          <div className="lg:col-span-7 space-y-4">
            <form onSubmit={handleOpenConfirm} className="bdae-card p-5 border border-[var(--bdae-border)] space-y-5">
              <h2 className="text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] flex items-center gap-2">
                <CreditCard className="w-4 h-4 text-[var(--bdae-secondary)]" />
                2. Transaction Details
              </h2>

              {/* Amount Input */}
              <div className="space-y-2">
                <label className="text-xs font-bold text-[var(--bdae-text-primary)] flex items-center justify-between">
                  <span>Transaction Amount (ETB) *</span>
                  {accountDetails && (
                    <span className="text-[11px] text-[var(--bdae-text-secondary)] font-normal">
                      Available: <strong className="font-mono">{formatCurrency(availableBal)}</strong>
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
                    Amount exceeds available account balance ({formatCurrency(availableBal)}).
                  </p>
                )}
              </div>

              {/* Quick Denominations */}
              <div className="space-y-1.5">
                <span className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)]">
                  Quick Denominations
                </span>
                <div className="flex flex-wrap gap-2">
                  {[100, 500, 1000, 2500, 5000, 10000].map((val) => (
                    <button
                      key={val}
                      type="button"
                      onClick={() => handleQuickAmount(val)}
                      className="px-2.5 py-1 rounded-lg text-xs font-semibold bg-black/5 dark:bg-white/5 hover:bg-[var(--bdae-primary)]/10 hover:text-[var(--bdae-primary)] border border-[var(--bdae-border)] transition-colors font-mono"
                    >
                      +{val.toLocaleString()}
                    </button>
                  ))}
                  <button
                    type="button"
                    onClick={() => setAmount('')}
                    className="px-2.5 py-1 rounded-lg text-xs font-semibold text-red-500 hover:bg-red-500/10 border border-transparent transition-colors"
                  >
                    Clear
                  </button>
                </div>
              </div>

              {/* Narration */}
              <div className="space-y-1">
                <label className="text-xs font-bold text-[var(--bdae-text-primary)]">
                  Transaction Narration / Note
                </label>
                <input
                  type="text"
                  value={narration}
                  onChange={(e) => setNarration(e.target.value)}
                  placeholder="Teller OTC Notes"
                  className="bdae-input text-xs"
                />
              </div>

              {/* Security & Idempotency Key */}
              <div className="p-3 rounded-xl bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] flex items-center justify-between text-xs">
                <div className="flex items-center space-x-2">
                  <ShieldCheck className="w-4 h-4 text-emerald-500" />
                  <div>
                    <span className="text-[10px] text-[var(--bdae-text-secondary)] font-bold uppercase block">
                      Idempotency Key (Double-Post Protection)
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

              {/* Submit CTA */}
              <button
                type="submit"
                disabled={!accountDetails || numAmount <= 0 || isInsufficientFunds || processing}
                className={`w-full py-3 rounded-xl text-xs font-bold text-white shadow-md transition-all flex items-center justify-center gap-2 ${
                  operationType === 'DEPOSIT'
                    ? 'bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50'
                    : 'bg-amber-600 hover:bg-amber-700 disabled:opacity-50'
                }`}
              >
                {operationType === 'DEPOSIT' ? (
                  <>
                    <ArrowDownToLine className="w-4 h-4" />
                    Review & Process Deposit
                  </>
                ) : (
                  <>
                    <ArrowUpFromLine className="w-4 h-4" />
                    Review & Process Withdrawal
                  </>
                )}
              </button>
            </form>
          </div>
        </div>
      )}

      {/* Confirmation Modal (Four-Eye / Safety Confirmation) */}
      {confirmModalOpen && accountDetails && (
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
              <div className="flex justify-between">
                <span className="text-[var(--bdae-text-secondary)]">Member Account:</span>
                <span className="font-mono font-bold text-[var(--bdae-text-primary)]">
                  {accountDetails.accountNo}
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
                onClick={() => setConfirmModalOpen(false)}
                disabled={processing}
                className="px-4 py-2 rounded-xl text-xs font-semibold hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleExecuteTransaction}
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
