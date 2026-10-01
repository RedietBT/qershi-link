import React, { useState } from 'react';
import { ShieldCheck, AlertCircle, Loader2, X, Check, UserCheck, ShieldAlert } from 'lucide-react';
import { accountLedgerApi } from '../api/accountLedgerApi';
import { useAuthStore } from '../../../common/store/useAuthStore';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';

export const ReactivateAccountModal = ({ isOpen, account, onClose, onUpdated }) => {
  const currentUser = useAuthStore((state) => state.user);
  const [reason, setReason] = useState('');
  const [kycNotes, setKycNotes] = useState('');
  const [checkerNotes, setCheckerNotes] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);

  if (!isOpen || !account) return null;

  const isPendingChecker = account.reactivationStatus === 'PENDING_CHECKER_APPROVAL';
  const isMaker = currentUser?.userId && account.reactivationMakerUserId && (currentUser.userId === account.reactivationMakerUserId);

  const handleInitiate = async (e) => {
    if (e) e.preventDefault();
    if (!reason.trim() || !kycNotes.trim()) {
      setError('Please provide both a reactivation reason and in-person KYC verification details.');
      return;
    }
    setIsSubmitting(true);
    setError(null);
    try {
      await accountLedgerApi.requestReactivation(account.accountNo || account.accountNumber, {
        reason: reason.trim(),
        kycVerificationNotes: kycNotes.trim()
      });
      onUpdated();
      onClose();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to submit KYC reactivation request.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleApprove = async () => {
    if (!checkerNotes.trim()) {
      setError('Supervisor audit notes are required for approval.');
      return;
    }
    setIsSubmitting(true);
    setError(null);
    try {
      await accountLedgerApi.approveReactivation(account.accountNo || account.accountNumber, {
        notes: checkerNotes.trim()
      });
      onUpdated();
      onClose();
    } catch (err) {
      setError(err?.response?.data?.message || 'Approval failed. Anti-Self-Approval violation or permission error.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleReject = async () => {
    if (!checkerNotes.trim()) {
      setError('Rejection reason/notes are required.');
      return;
    }
    setIsSubmitting(true);
    setError(null);
    try {
      await accountLedgerApi.rejectReactivation(account.accountNo || account.accountNumber, {
        notes: checkerNotes.trim()
      });
      onUpdated();
      onClose();
    } catch (err) {
      setError(err?.response?.data?.message || 'Rejection failed.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 animate-fadeIn">
      <div className="bdae-card w-full max-w-lg rounded-2xl border border-[var(--bdae-border)] shadow-2xl overflow-hidden">
        {/* Modal Header */}
        <div className="p-5 flex items-center justify-between bg-amber-600 text-white">
          <div className="flex items-center gap-3">
            <ShieldCheck className="w-5 h-5" />
            <div>
              <h2 className="text-sm font-extrabold">KYC Dormancy Reactivation</h2>
              <p className="text-[10px] opacity-85 font-mono">
                Account: {account.accountNo || account.accountNumber}
              </p>
            </div>
          </div>
          <button onClick={onClose} className="text-white/70 hover:text-white transition-colors">
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-6 space-y-4 text-xs">
          {error && (
            <div className="flex items-center gap-2 p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 font-bold">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {/* Regulatory Information Banner */}
          <div className="p-3.5 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-700 dark:text-amber-300 space-y-1">
            <div className="flex items-center gap-1.5 font-bold text-[11px]">
              <ShieldAlert className="w-3.5 h-3.5" />
              <span>Central Bank & WOCCU Dormancy Compliance</span>
            </div>
            <p className="text-[10px] leading-relaxed opacity-90">
              Accounts inactive for &gt;180 days are locked against automated debits to prevent insider fraud.
              Reactivation mandates in-person biometric/KYC re-verification under Four-Eye dual control.
            </p>
          </div>

          {/* Mode A: Maker Initiates KYC Reactivation */}
          {!isPendingChecker ? (
            <form onSubmit={handleInitiate} className="space-y-4">
              <div>
                <label className="block text-[11px] font-bold text-[var(--bdae-text-primary)] mb-1">
                  Reactivation Reason / Member Justification <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  required
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  placeholder="e.g. Member visited branch in-person to resume active savings"
                  className="w-full px-3 py-2 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] focus:outline-none focus:ring-2 focus:ring-amber-500"
                />
              </div>

              <div>
                <label className="block text-[11px] font-bold text-[var(--bdae-text-primary)] mb-1">
                  In-Person KYC Verification Details <span className="text-red-500">*</span>
                </label>
                <textarea
                  required
                  rows={3}
                  value={kycNotes}
                  onChange={(e) => setKycNotes(e.target.value)}
                  placeholder="Document Kebele ID / National ID #, biometric fingerprint match status, and signature specimen confirmation..."
                  className="w-full px-3 py-2 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] focus:outline-none focus:ring-2 focus:ring-amber-500"
                />
              </div>

              <div className="pt-2 flex justify-end gap-2 border-t border-[var(--bdae-border)]">
                <button
                  type="button"
                  onClick={onClose}
                  className="px-4 py-2 rounded-xl border border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] hover:bg-black/5"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="px-4 py-2 rounded-xl bg-amber-600 hover:bg-amber-700 text-white font-bold flex items-center gap-1.5 shadow-sm transition-all"
                >
                  {isSubmitting ? (
                    <>
                      <Loader2 className="w-3.5 h-3.5 animate-spin" />
                      <span>Submitting...</span>
                    </>
                  ) : (
                    <>
                      <UserCheck className="w-3.5 h-3.5" />
                      <span>Submit for Supervisor Approval</span>
                    </>
                  )}
                </button>
              </div>
            </form>
          ) : (
            /* Mode B: Checker Supervisor Review & Decision */
            <div className="space-y-4">
              <div className="p-3 rounded-xl bg-[var(--bdae-surface-elevated)] border border-[var(--bdae-border)] space-y-2">
                <div className="text-[11px] font-bold text-[var(--bdae-text-primary)]">
                  Pending Maker Verification Details
                </div>
                <div className="text-[10px] text-[var(--bdae-text-secondary)] font-mono">
                  Maker Operator: {account.reactivationMakerUserId || 'Authorized Officer'}
                </div>
                <p className="text-[11px] bg-black/5 dark:bg-white/5 p-2 rounded-lg text-[var(--bdae-text-primary)]">
                  {account.reactivationMakerNotes || 'In-person KYC documents verified.'}
                </p>
              </div>

              {isMaker && (
                <div className="p-2.5 rounded-lg bg-red-500/10 border border-red-500/20 text-red-600 text-[10px] font-bold">
                  Anti-Self-Approval Active: You are the maker who submitted this request. A distinct supervisor must approve it.
                </div>
              )}

              <div>
                <label className="block text-[11px] font-bold text-[var(--bdae-text-primary)] mb-1">
                  Supervisor Audit Notes <span className="text-red-500">*</span>
                </label>
                <textarea
                  rows={2}
                  value={checkerNotes}
                  onChange={(e) => setCheckerNotes(e.target.value)}
                  placeholder="Enter audit confirmation or rejection reason..."
                  className="w-full px-3 py-2 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] focus:outline-none focus:ring-2 focus:ring-amber-500"
                />
              </div>

              <div className="pt-2 flex justify-end gap-2 border-t border-[var(--bdae-border)]">
                <button
                  type="button"
                  onClick={onClose}
                  className="px-3.5 py-2 rounded-xl border border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] hover:bg-black/5"
                >
                  Close
                </button>
                <button
                  type="button"
                  disabled={isSubmitting || isMaker}
                  onClick={handleReject}
                  className="px-3.5 py-2 rounded-xl border border-red-500/30 text-red-600 hover:bg-red-500/10 font-bold disabled:opacity-50"
                >
                  Reject
                </button>
                <button
                  type="button"
                  disabled={isSubmitting || isMaker}
                  onClick={handleApprove}
                  className="px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold flex items-center gap-1.5 shadow-sm disabled:opacity-50"
                >
                  {isSubmitting ? (
                    <>
                      <Loader2 className="w-3.5 h-3.5 animate-spin" />
                      <span>Processing...</span>
                    </>
                  ) : (
                    <>
                      <Check className="w-3.5 h-3.5" />
                      <span>Approve & Restore Active</span>
                    </>
                  )}
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
