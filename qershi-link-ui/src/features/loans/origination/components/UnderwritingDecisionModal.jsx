import React, { useState } from 'react';
import {
  X,
  ShieldAlert,
  CheckCircle2,
  XCircle,
  AlertTriangle,
  Loader2,
  FileText,
  User,
  BadgePercent,
  Layers,
  Calendar
} from 'lucide-react';
import { loanOriginationApi } from '../api/loanOriginationApi';
import { useAuthStore } from '../../../../common/store/useAuthStore';
import { formatCurrency, formatDateTime } from '../../../../common/utils/currency';

export const UnderwritingDecisionModal = ({ application, isOpen, onClose, onSuccess }) => {
  const currentUser = useAuthStore((state) => state.user);

  const [actionType, setActionType] = useState('APPROVE');
  const [amountApproved, setAmountApproved] = useState(
    application?.amountApproved || application?.amountRequested || ''
  );
  const [remarks, setRemarks] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  if (!isOpen || !application) return null;

  // Maker-Checker Separation Rule: Check if logged-in officer is the applicant
  const isApplicant =
    currentUser?.userId === application.userId ||
    currentUser?.id === application.userId;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (isApplicant) {
      setError('Maker-Checker Violation: You cannot review or approve your own loan application.');
      return;
    }
    if (!remarks.trim()) {
      setError('Underwriter remarks and justification are mandatory.');
      return;
    }
    if (actionType === 'APPROVE' && Number(amountApproved) <= 0) {
      setError('Approved amount must be greater than zero.');
      return;
    }

    try {
      setSubmitting(true);
      setError(null);

      const payload = {
        actionType,
        amountApproved: actionType === 'APPROVE' ? Number(amountApproved) : 0,
        remarks: remarks.trim()
      };

      await loanOriginationApi.processApproval(application.applicationId, payload);
      onSuccess?.();
      onClose();
    } catch (err) {
      console.error('Underwriting decision error:', err);
      setError(err?.response?.data?.message || 'Failed to submit underwriting decision.');
    } finally {
      setSubmitting(false);
    }
  };

  const score = application.creditScoring;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
      <div className="bdae-card w-full max-w-2xl max-h-[90vh] overflow-hidden flex flex-col shadow-2xl border border-[var(--bdae-border)]">
        {/* Header */}
        <div className="p-5 border-b border-[var(--bdae-border)] flex items-center justify-between bg-black/5 dark:bg-white/5">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-purple-500/10 text-purple-600 flex items-center justify-center font-bold">
              <ShieldAlert className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-[var(--bdae-text-primary)]">
                Maker-Checker Loan Underwriting Review
              </h2>
              <p className="text-xs text-[var(--bdae-text-secondary)] font-mono">
                App No: {application.applicationNo || application.applicationId?.slice(0, 13)}
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
        <div className="p-6 overflow-y-auto space-y-6 flex-1 text-xs">
          {/* Maker-Checker Alert Guard */}
          {isApplicant && (
            <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 flex items-start space-x-3">
              <AlertTriangle className="w-5 h-5 shrink-0 mt-0.5" />
              <div>
                <p className="font-bold">Dual-Control Maker-Checker Separation Required</p>
                <p className="mt-1 opacity-90">
                  You are the borrower/applicant for this loan request. Under Core Banking segregation of duties, another authorized underwriter or credit committee member must review and approve this application.
                </p>
              </div>
            </div>
          )}

          {error && (
            <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 flex items-start space-x-3">
              <AlertTriangle className="w-5 h-5 shrink-0 mt-0.5" />
              <div>
                <p className="font-semibold">Underwriting Submission Alert</p>
                <p className="mt-0.5 opacity-90">{error}</p>
              </div>
            </div>
          )}

          {/* Key Loan Metrics */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 p-4 rounded-xl bg-black/5 dark:bg-white/5">
            <div>
              <span className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)] block">
                Requested Amount
              </span>
              <span className="text-sm font-black font-mono text-[var(--bdae-primary)]">
                {formatCurrency(application.amountRequested)}
              </span>
            </div>
            <div>
              <span className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)] block">
                Current Status
              </span>
              <span className="font-bold text-amber-500">
                {application.status}
              </span>
            </div>
            <div>
              <span className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)] block">
                Scoring Model
              </span>
              <span className="font-medium text-[var(--bdae-text-primary)]">
                {application.scoringType || 'INDIVIDUAL'}
              </span>
            </div>
            <div>
              <span className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)] block">
                Submission Date
              </span>
              <span className="text-[var(--bdae-text-primary)]">
                {formatDateTime(application.createdAt)}
              </span>
            </div>
          </div>

          {/* Multi-Factor Credit Scoring Analysis */}
          {score && (
            <div className="p-4 rounded-xl border border-[var(--bdae-border)] space-y-3">
              <div className="flex items-center justify-between">
                <h3 className="font-bold text-[var(--bdae-text-primary)] flex items-center gap-1.5 uppercase text-[11px] tracking-wider">
                  <BadgePercent className="w-4 h-4 text-purple-500" />
                  Multi-Factor Credit Scoring Engine
                </h3>
                <span
                  className={`px-2.5 py-0.5 rounded-full text-[10px] font-extrabold flex items-center gap-1 ${
                    score.passedEligibility
                      ? 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400'
                      : 'bg-red-500/10 text-red-600 dark:text-red-400'
                  }`}
                >
                  {score.passedEligibility ? <CheckCircle2 className="w-3.5 h-3.5" /> : <XCircle className="w-3.5 h-3.5" />}
                  {score.passedEligibility ? 'Passed Pre-Eligibility' : 'Failed Pre-Eligibility'}
                </span>
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs pt-1">
                <div>
                  <span className="text-[10px] text-[var(--bdae-text-secondary)] block">Calculated Score</span>
                  <span className="font-mono font-bold text-sm text-[var(--bdae-text-primary)]">
                    {score.calculatedScore ?? '—'} / 100
                  </span>
                </div>
                <div>
                  <span className="text-[10px] text-[var(--bdae-text-secondary)] block">Savings Consistency</span>
                  <span className="font-mono font-bold text-[var(--bdae-text-primary)]">
                    {score.savingsConsistency ? `${score.savingsConsistency}%` : '—'}
                  </span>
                </div>
                <div>
                  <span className="text-[10px] text-[var(--bdae-text-secondary)] block">Historical Yield</span>
                  <span className="font-mono font-bold text-[var(--bdae-text-primary)]">
                    {score.historicalYield ? formatCurrency(score.historicalYield) : '—'}
                  </span>
                </div>
                <div>
                  <span className="text-[10px] text-[var(--bdae-text-secondary)] block">Land Size</span>
                  <span className="font-mono font-bold text-[var(--bdae-text-primary)]">
                    {score.landSizeHectares ? `${score.landSizeHectares} Ha` : '—'}
                  </span>
                </div>
              </div>
            </div>
          )}

          {/* Pledged Collaterals */}
          {application.collaterals?.length > 0 && (
            <div className="space-y-2">
              <h3 className="font-bold text-[var(--bdae-text-primary)] uppercase text-[11px] tracking-wider flex items-center gap-1.5">
                <Layers className="w-4 h-4 text-[var(--bdae-secondary)]" />
                Pledged Collateral Assets ({application.collaterals.length})
              </h3>
              <div className="border border-[var(--bdae-border)] rounded-xl overflow-hidden">
                <table className="w-full text-left border-collapse text-xs">
                  <thead className="bg-black/5 dark:bg-white/5 border-b border-[var(--bdae-border)] text-[var(--bdae-text-secondary)]">
                    <tr>
                      <th className="py-2 px-3 font-semibold">Type</th>
                      <th className="py-2 px-3 font-semibold text-right">Estimated Value (ETB)</th>
                      <th className="py-2 px-3 font-semibold">Document Ref</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-[var(--bdae-border)]">
                    {application.collaterals.map((col, idx) => (
                      <tr key={col.collateralId || idx}>
                        <td className="py-2 px-3 font-bold text-[var(--bdae-text-primary)]">{col.type}</td>
                        <td className="py-2 px-3 text-right font-mono font-bold">{formatCurrency(col.estimatedValue)}</td>
                        <td className="py-2 px-3 text-[var(--bdae-text-secondary)] truncate max-w-xs">{col.documentUrl || 'Verified in Registry'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* Decision Form */}
          <form id="underwriting-form" onSubmit={handleSubmit} className="space-y-4 pt-2 border-t border-[var(--bdae-border)]">
            <h3 className="font-bold text-[var(--bdae-text-primary)] uppercase text-[11px] tracking-wider">
              Underwriting Committee Decision
            </h3>

            {/* Action Buttons */}
            <div className="grid grid-cols-2 gap-3">
              <button
                type="button"
                disabled={isApplicant}
                onClick={() => setActionType('APPROVE')}
                className={`py-3 px-4 rounded-xl font-bold flex items-center justify-center gap-2 border transition-all ${
                  actionType === 'APPROVE'
                    ? 'bg-emerald-600 text-white border-emerald-600 shadow-md'
                    : 'bg-black/5 dark:bg-white/5 border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]'
                }`}
              >
                <CheckCircle2 className="w-4 h-4" />
                Approve Application
              </button>

              <button
                type="button"
                disabled={isApplicant}
                onClick={() => setActionType('REJECT')}
                className={`py-3 px-4 rounded-xl font-bold flex items-center justify-center gap-2 border transition-all ${
                  actionType === 'REJECT'
                    ? 'bg-red-600 text-white border-red-600 shadow-md'
                    : 'bg-black/5 dark:bg-white/5 border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]'
                }`}
              >
                <XCircle className="w-4 h-4" />
                Reject Application
              </button>
            </div>

            {/* Amount Approved (if approving) */}
            {actionType === 'APPROVE' && (
              <div className="space-y-1">
                <label className="font-bold text-[var(--bdae-text-primary)] flex justify-between">
                  <span>Approved Principal Amount (ETB) *</span>
                  <span className="text-[11px] font-normal text-[var(--bdae-text-secondary)]">
                    Requested: <strong>{formatCurrency(application.amountRequested)}</strong>
                  </span>
                </label>
                <input
                  type="number"
                  step="0.01"
                  min="1"
                  required
                  disabled={isApplicant}
                  value={amountApproved}
                  onChange={(e) => setAmountApproved(e.target.value)}
                  className="bdae-input font-mono font-bold"
                  placeholder="0.00"
                />
              </div>
            )}

            {/* Remarks / Justification */}
            <div className="space-y-1">
              <label className="font-bold text-[var(--bdae-text-primary)]">
                Underwriting Remarks & Audit Notes *
              </label>
              <textarea
                rows={3}
                required
                disabled={isApplicant}
                value={remarks}
                onChange={(e) => setRemarks(e.target.value)}
                placeholder="Document underwriting rationale, verification of savings, collateral assessment..."
                className="bdae-input text-xs"
              />
            </div>
          </form>
        </div>

        {/* Modal Footer */}
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
            form="underwriting-form"
            disabled={isApplicant || submitting}
            className={`px-5 py-2.5 rounded-xl text-xs font-bold text-white shadow-md flex items-center gap-2 transition-all ${
              actionType === 'APPROVE'
                ? 'bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50'
                : 'bg-red-600 hover:bg-red-700 disabled:opacity-50'
            }`}
          >
            {submitting && <Loader2 className="w-4 h-4 animate-spin" />}
            Submit {actionType === 'APPROVE' ? 'Approval' : 'Rejection'}
          </button>
        </div>
      </div>
    </div>
  );
};
