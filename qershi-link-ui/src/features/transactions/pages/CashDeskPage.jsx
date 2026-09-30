import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Banknote,
  ArrowDownToLine,
  ArrowUpFromLine,
  Search,
  AlertTriangle,
  RefreshCw,
  CreditCard,
  Loader2,
  Vault,
  ShieldCheck,
} from 'lucide-react';
import { transactionApi } from '../api/transactionApi';
import { accountLedgerApi } from '../../accounts/api/accountLedgerApi';
import { formatCurrency, generateIdempotencyKey } from '../../../common/utils/currency';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';
import { GLJournalModal } from '../components/GLJournalModal';
import { CashDeskAccountCard } from '../components/CashDeskAccountCard';
import { CashReceiptVoucher } from '../components/CashReceiptVoucher';
import { CashDeskConfirmModal } from '../components/CashDeskConfirmModal';

/**
 * Modular Teller Cash Desk (Over-the-Counter) Page
 */
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
      handleLookupAccount(accountDetails.accountNo);
    }
  };

  return (
    <div className="space-y-6 animate-fadeIn max-w-5xl mx-auto">
      {/* Header & Department Action Bar */}
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

        <div className="flex items-center gap-3">
          {/* Till Drawer link strictly guarded */}
          <PermissionGuard permissions={[PERMISSIONS.TELLER_TILL_VIEW, PERMISSIONS.CASH_DEPOSIT]}>
            <button
              type="button"
              onClick={() => navigate('/transactions/till')}
              className="flex items-center gap-2 px-3.5 py-2 rounded-xl border border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5 text-xs font-bold text-[var(--bdae-text-primary)] transition-all"
              title="Manage physical cash drawer and banknote reconciliation"
            >
              <Vault className="w-4 h-4 text-amber-500" />
              <span>Till Drawer</span>
            </button>
          </PermissionGuard>

          {/* Operation Mode Switcher with Permission Gates */}
          <div className="flex items-center bg-black/5 dark:bg-white/5 p-1 rounded-xl border border-[var(--bdae-border)]">
            <PermissionGuard permissions={[PERMISSIONS.CASH_DEPOSIT]}>
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
            </PermissionGuard>

            <PermissionGuard permissions={[PERMISSIONS.SAVINGS_WITHDRAW]}>
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
            </PermissionGuard>
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

      {/* Success Receipt Voucher Component */}
      {receipt && (
        <CashReceiptVoucher
          receipt={receipt}
          operationType={operationType}
          onViewGL={(ref) => {
            setSelectedTxRef(ref);
            setGlModalOpen(true);
          }}
          onResetForNew={handleResetForNew}
        />
      )}

      {/* Main Two-Column Working Area */}
      {!receipt && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          {/* Left Column: Account Verification */}
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

              {/* Verified Account Card */}
              <CashDeskAccountCard
                accountDetails={accountDetails}
                availableBalance={availableBal}
              />
            </div>
          </div>

          {/* Right Column: Transaction Processing Form */}
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

              {/* Idempotency Key */}
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

              {/* Submit CTA strictly guarded by permission */}
              <PermissionGuard
                permissions={[
                  operationType === 'DEPOSIT'
                    ? PERMISSIONS.CASH_DEPOSIT
                    : PERMISSIONS.SAVINGS_WITHDRAW,
                ]}
              >
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
              </PermissionGuard>
            </form>
          </div>
        </div>
      )}

      {/* Confirmation Modal Component */}
      <CashDeskConfirmModal
        isOpen={confirmModalOpen}
        operationType={operationType}
        accountDetails={accountDetails}
        availableBal={availableBal}
        numAmount={numAmount}
        processing={processing}
        onClose={() => setConfirmModalOpen(false)}
        onConfirm={handleExecuteTransaction}
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
