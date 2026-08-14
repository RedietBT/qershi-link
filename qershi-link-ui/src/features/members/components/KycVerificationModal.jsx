import React, { useState } from 'react';
import { kycApi } from '../api/kycApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { X, ShieldCheck, CheckCircle, XCircle, RefreshCw, AlertCircle, Calendar } from 'lucide-react';

export const KycVerificationModal = ({ document, onClose, onSuccess }) => {
    const [notes, setNotes] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState(null);

    const isUnverified = document.status === 'UNVERIFIED';

    const handleVerify = async () => {
        if (!window.confirm("Approve this Official Identification Document?")) return;
        setIsSubmitting(true);
        setError(null);
        try {
            await kycApi.verifyKycIdentification(document.identificationId, notes);
            onSuccess();
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to verify KYC document');
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleReject = async () => {
        if (!notes) {
            setError("Rejection requires supervisor notes explaining the reason.");
            return;
        }
        if (!window.confirm("Reject and blacklist this Identification Document?")) return;
        setIsSubmitting(true);
        setError(null);
        try {
            await kycApi.rejectKycIdentification(document.identificationId, notes);
            onSuccess();
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to reject KYC document');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
            <div className="bdae-card w-full max-w-lg rounded-3xl shadow-2xl border border-[var(--bdae-border)] flex flex-col">
                {/* Header */}
                <div className="flex bg-[var(--bdae-surface)] items-center justify-between p-5 border-b border-[var(--bdae-border)] rounded-t-3xl border-t bg-black/5 dark:bg-white/5">
                    <div className="flex items-center space-x-3">
                        <div className="w-10 h-10 rounded-xl bg-indigo-500/10 text-indigo-500 flex items-center justify-center">
                            <ShieldCheck className="w-5 h-5" />
                        </div>
                        <div>
                            <h2 className="text-sm font-extrabold text-[var(--bdae-text-primary)] tracking-wide">
                                KYC Document Review
                            </h2>
                            <p className="text-[11px] font-mono text-[var(--bdae-text-secondary)]">ID: {document.identificationId}</p>
                        </div>
                    </div>
                    <button
                        onClick={onClose}
                        className="p-2 rounded-lg hover:bg-black/5 dark:hover:bg-white/5 text-[var(--bdae-text-secondary)] transition-colors"
                    >
                        <X className="w-5 h-5" />
                    </button>
                </div>

                {/* Body */}
                <div className="p-5 space-y-5">
                    {error && (
                        <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-500 text-xs flex gap-2">
                            <AlertCircle className="w-4 h-4 shrink-0" />
                            <span>{error}</span>
                        </div>
                    )}

                    <div className="grid grid-cols-2 gap-4 text-xs">
                        <div className="space-y-1">
                            <div className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)]">ID Type</div>
                            <div className="font-bold">{document.idType?.replace('_', ' ')}</div>
                        </div>
                        <div className="space-y-1">
                            <div className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)]">ID Number</div>
                            <div className="font-mono font-bold tracking-wide">{document.idNumber}</div>
                        </div>
                        <div className="space-y-1">
                            <div className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)]">Issue Date</div>
                            <div className="flex items-center gap-1 opacity-90"><Calendar className="w-3 h-3" /> {document.issueDate || 'N/A'}</div>
                        </div>
                        <div className="space-y-1">
                            <div className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)]">Expiry Date</div>
                            <div className="flex items-center gap-1 opacity-90"><Calendar className="w-3 h-3" /> {document.expiryDate || 'N/A'}</div>
                        </div>
                        <div className="col-span-2 space-y-1">
                            <div className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)]">Issuing Authority</div>
                            <div className="">{document.issuingAuthority || 'N/A'}</div>
                        </div>

                        {!isUnverified && (
                            <div className="col-span-2 space-y-1 mt-2 p-3 bg-black/5 dark:bg-white/5 rounded-xl border border-[var(--bdae-border)]">
                                <div className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)] flex justify-between">
                                    <span>Supervisor Audit Notes</span>
                                    <span className={document.status === 'VERIFIED' ? 'text-emerald-500' : 'text-red-500'}>
                                        {document.status} by {document.verifiedByUserId?.substring(0, 8)}...
                                    </span>
                                </div>
                                <div className="text-[var(--bdae-text-primary)] italic">
                                    {document.verificationNotes || 'No notes provided by supervisor.'}
                                </div>
                            </div>
                        )}
                    </div>

                    {isUnverified && (
                        <div className="space-y-2 pt-2 border-t border-[var(--bdae-border)]">
                            <label className="text-[11px] font-bold text-[var(--bdae-text-secondary)] uppercase">Supervisor Verification Notes</label>
                            <textarea
                                value={notes}
                                onChange={e => setNotes(e.target.value)}
                                placeholder="Enter reason for approval or rejection (required for rejection)"
                                className="w-full h-24 px-4 py-3 text-xs bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] rounded-xl outline-none focus:border-[var(--bdae-primary)]"
                            />
                        </div>
                    )}
                </div>

                {/* Footer Controls */}
                {isUnverified && (
                    <div className="p-5 border-t border-[var(--bdae-border)] flex justify-between gap-3 bg-black/5 dark:bg-white/5 items-center">
                        <PermissionGuard authorities={['KYC_VERIFY']} roles={['SUPER_ADMIN', 'SACCO_ADMIN']}>
                            <button
                                onClick={handleReject}
                                disabled={isSubmitting}
                                className="px-5 py-2.5 rounded-xl border border-red-500/30 hover:border-red-500 text-red-500 hover:bg-red-500/10 text-xs font-bold transition-all flex items-center justify-center gap-2"
                            >
                                {isSubmitting ? <RefreshCw className="w-4 h-4 animate-spin" /> : <XCircle className="w-4 h-4" />}
                                <span>Reject Document</span>
                            </button>

                            <button
                                onClick={handleVerify}
                                disabled={isSubmitting}
                                className="px-6 py-2.5 rounded-xl bdae-btn-primary shadow-lg text-xs font-bold transition-all flex items-center justify-center gap-2"
                            >
                                {isSubmitting ? <RefreshCw className="w-4 h-4 animate-spin" /> : <CheckCircle className="w-4 h-4" />}
                                <span>Verify Document</span>
                            </button>
                        </PermissionGuard>
                    </div>
                )}
            </div>
        </div>
    );
};
