import React, { useState } from 'react';
import {
  X,
  Banknote,
  CheckCircle2,
  AlertTriangle,
  Loader2,
  ShieldCheck,
  RefreshCw
} from 'lucide-react';
import { loanManagementApi } from '../api/loanManagementApi';
import { formatCurrency, generateIdempotencyKey } from '../../../../common/utils/currency';

export const DisburseLoanModal = ({ isOpen, onClose, onSuccess, initialData }) => {
  const [applicationId, setApplicationId] = useState(initialData?.applicationId || '');
  const [userId, setUserId] = useState(initialData?.userId || '');
  const [productId, setProductId] = useState(initialData?.productId || '');
  const [amount, setAmount] = useState(initialData?.amountApproved || initialData?.amountRequested || '');
  const [interestRatePct, setInterestRatePct] = useState('14.00');
  const [termMonths, setTermMonths] = useState('12');
  const [repaymentFrequency, setRepaymentFrequency] = useState('MONTHLY');
  const [interestType, setInterestType] = useState('REDUCING_BALANCE');
  const [targetSavingsAccountId, setTargetSavingsAccountId] = useState('');
  const [memberPhone, setMemberPhone] = useState(initialData?.phone || '');
  const [guarantors, setGuarantors] = useState(initialData?.guarantors || []);
  const [idempotencyKey, setIdempotencyKey] = useState(generateIdempotencyKey());

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [result, setResult] = useState(null);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!applicationId.trim() || !userId.trim()) {
      setError('Application ID and Borrower User ID are required.');
      return;
    }
    const numAmount = Number(amount);
    if (!numAmount || numAmount <= 0) {
      setError('Please provide a valid loan amount to disburse.');
      return;
    }

    try {
      setSubmitting(true);
      setError(null);

      const payload = {
        applicationId: applicationId.trim(),
        userId: userId.trim(),
        productId: productId.trim() || undefined,
        amount: numAmount,
        interestRatePct: Number(interestRatePct) || 14.0,
        termMonths: Number(termMonths) || 12,
        repaymentFrequency,
        interestType,
        targetSavingsAccountId: targetSavingsAccountId.trim() || undefined,
        memberPhone: memberPhone.trim() || undefined,
        guarantors: guarantors && guarantors.length > 0 ? guarantors.map(g => ({
          guarantorUserId: g.guarantorUserId,
          guarantorName: g.guarantorName,
          guarantorPhone: g.guarantorPhone,
          savingsAccountNo: g.savingsAccountNo,
          guaranteedAmount: Number(g.guaranteedAmount)
        })) : undefined
      };

      const res = await loanManagementApi.disburseLoan(payload, idempotencyKey);
      setResult(res.data || res);
      onSuccess?.();
    } catch (err) {
      console.error('Loan disbursement initiation failed:', err);
      setError(err?.response?.data?.message || 'Failed to initiate loan disbursement.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleApproveDisbursement = async () => {
    if (!result?.accountId) return;
    try {
      setSubmitting(true);
      setError(null);
      const res = await loanManagementApi.approveDisbursement(result.accountId);
      setResult(res.data || res);
      onSuccess?.();
    } catch (err) {
      console.error('Disbursement approval failed:', err);
      setError(err?.response?.data?.message || 'Checker disbursement approval failed.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
      <div className="bdae-card w-full max-w-xl max-h-[90vh] overflow-hidden flex flex-col shadow-2xl border border-[var(--bdae-border)]">
        {/* Header */}
        <div className="p-5 border-b border-[var(--bdae-border)] flex items-center justify-between bg-black/5 dark:bg-white/5">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-purple-500/10 text-purple-600 flex items-center justify-center font-bold">
              <Banknote className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-[var(--bdae-text-primary)]">
                Initiate Loan Disbursement
              </h2>
              <p className="text-xs text-[var(--bdae-text-secondary)]">
                Activate loan account, configure amortization schedule, and credit member savings.
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
                <p className="font-semibold">Disbursement Processing Alert</p>
                <p className="mt-0.5 opacity-90">{error}</p>
              </div>
            </div>
          )}

          {/* Disbursement Result View */}
          {result && (
            <div className="p-5 rounded-2xl bg-emerald-500/10 border border-emerald-500/20 space-y-4 animate-fadeIn">
              <div className="flex items-start justify-between">
                <div className="flex items-center space-x-3">
                  <CheckCircle2 className="w-8 h-8 text-emerald-600 shrink-0" />
                  <div>
                    <h3 className="text-sm font-bold text-[var(--bdae-text-primary)]">
                      Loan Account Generated
                    </h3>
                    <p className="text-xs text-[var(--bdae-text-secondary)] font-mono">
                      Account No: {result.accountNo || result.accountId}
                    </p>
                  </div>
                </div>
                <span className="px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-700 dark:text-emerald-300 font-extrabold text-[10px]">
                  {result.status || 'PENDING_DISBURSEMENT'}
                </span>
              </div>

              <div className="p-3.5 rounded-xl bg-black/5 dark:bg-white/5 grid grid-cols-2 gap-3">
                <div>
                  <span className="text-[10px] text-[var(--bdae-text-secondary)] uppercase block">
                    Principal Amount
                  </span>
                  <span className="font-mono font-bold text-sm text-[var(--bdae-primary)]">
                    {formatCurrency(result.principalAmount || amount)}
                  </span>
                </div>
                <div>
                  <span className="text-[10px] text-[var(--bdae-text-secondary)] uppercase block">
                    Interest Rate
                  </span>
                  <span className="font-mono font-bold text-sm text-[var(--bdae-text-primary)]">
                    {result.interestRatePct || interestRatePct}% p.a.
                  </span>
                </div>
              </div>

              {result.status === 'PENDING_DISBURSEMENT' && (
                <div className="p-3.5 rounded-xl bg-amber-500/10 border border-amber-500/20 space-y-2">
                  <p className="font-bold text-amber-700 dark:text-amber-300">
                    Checker Authorization Required (Four-Eye Dual Control)
                  </p>
                  <p className="text-[11px] text-[var(--bdae-text-secondary)]">
                    Under core banking rules, a second authorized supervising admin must approve this disbursement before funds are released.
                  </p>
                  <button
                    type="button"
                    onClick={handleApproveDisbursement}
                    disabled={submitting}
                    className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold shadow-md flex items-center gap-1.5 transition-all"
                  >
                    {submitting && <Loader2 className="w-4 h-4 animate-spin" />}
                    Checker: Authorize & Release Funds
                  </button>
                </div>
              )}

              <div className="flex justify-end pt-1">
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

          {!result && (
            <form id="disburse-form" onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div className="space-y-1">
                  <label className="font-bold text-[var(--bdae-text-primary)]">
                    Approved Application ID *
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="Application UUID"
                    value={applicationId}
                    onChange={(e) => setApplicationId(e.target.value)}
                    className="bdae-input font-mono text-xs"
                  />
                </div>

                <div className="space-y-1">
                  <label className="font-bold text-[var(--bdae-text-primary)]">
                    Borrower User ID *
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="User UUID"
                    value={userId}
                    onChange={(e) => setUserId(e.target.value)}
                    className="bdae-input font-mono text-xs"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div className="space-y-1">
                  <label className="font-bold text-[var(--bdae-text-primary)]">
                    Disbursement Principal (ETB) *
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    min="1"
                    required
                    placeholder="50000.00"
                    value={amount}
                    onChange={(e) => setAmount(e.target.value)}
                    className="bdae-input font-mono font-bold text-sm"
                  />
                </div>

                <div className="space-y-1">
                  <label className="font-bold text-[var(--bdae-text-primary)]">
                    Interest Rate (% p.a.) *
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    required
                    value={interestRatePct}
                    onChange={(e) => setInterestRatePct(e.target.value)}
                    className="bdae-input font-mono text-xs"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div className="space-y-1">
                  <label className="font-bold text-[var(--bdae-text-primary)]">
                    Term (Months) *
                  </label>
                  <input
                    type="number"
                    min="1"
                    required
                    value={termMonths}
                    onChange={(e) => setTermMonths(e.target.value)}
                    className="bdae-input font-mono text-xs"
                  />
                </div>

                <div className="space-y-1">
                  <label className="font-bold text-[var(--bdae-text-primary)]">
                    Repayment Frequency
                  </label>
                  <select
                    value={repaymentFrequency}
                    onChange={(e) => setRepaymentFrequency(e.target.value)}
                    className="bdae-input text-xs"
                  >
                    <option value="MONTHLY">Monthly</option>
                    <option value="WEEKLY">Weekly</option>
                    <option value="QUARTERLY">Quarterly</option>
                    <option value="ANNUALLY">Annually</option>
                  </select>
                </div>

                <div className="space-y-1">
                  <label className="font-bold text-[var(--bdae-text-primary)]">
                    Interest Method
                  </label>
                  <select
                    value={interestType}
                    onChange={(e) => setInterestType(e.target.value)}
                    className="bdae-input text-xs"
                  >
                    <option value="REDUCING_BALANCE">Reducing Balance</option>
                    <option value="FLAT_RATE">Flat Rate</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div className="space-y-1">
                  <label className="font-bold text-[var(--bdae-text-primary)]">
                    Target Member Savings Account
                  </label>
                  <input
                    type="text"
                    placeholder="ACC-AW-2026-..."
                    value={targetSavingsAccountId}
                    onChange={(e) => setTargetSavingsAccountId(e.target.value)}
                    className="bdae-input font-mono uppercase text-xs"
                  />
                </div>

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
              </div>

              {guarantors.length > 0 && (
                <div className="p-3 bg-emerald-500/10 border border-emerald-500/20 rounded-xl space-y-1.5 text-xs">
                  <div className="flex items-center justify-between font-bold text-emerald-400">
                    <span className="flex items-center gap-1.5">
                      <ShieldCheck className="w-4 h-4" />
                      Pledged Peer Guarantor Liens ({guarantors.length})
                    </span>
                    <span className="font-mono">
                      Total: {formatCurrency(
                        guarantors.reduce((sum, g) => sum + (Number(g.guaranteedAmount) || 0), 0)
                      )}
                    </span>
                  </div>
                  <p className="text-[11px] text-[var(--bdae-text-secondary)]">
                    Monetary lien holds will be automatically locked on the {guarantors.length} guarantor accounts upon disbursement confirmation.
                  </p>
                </div>
              )}
            </form>
          )}
        </div>

        {/* Footer */}
        {!result && (
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
              form="disburse-form"
              disabled={submitting || Number(amount) <= 0}
              className="px-5 py-2.5 rounded-xl text-xs font-bold text-white bg-purple-600 hover:bg-purple-700 disabled:opacity-50 shadow-md flex items-center gap-2 transition-all"
            >
              {submitting && <Loader2 className="w-4 h-4 animate-spin" />}
              <Banknote className="w-4 h-4" />
              Disburse Loan
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
