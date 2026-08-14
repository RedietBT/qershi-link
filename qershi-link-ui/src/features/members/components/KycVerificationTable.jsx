import React from 'react';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { FileText, ShieldCheck, RefreshCw, AlertCircle, Calendar, Hash } from 'lucide-react';

export const KycVerificationTable = ({
    documents = [],
    isLoading,
    error,
    onVerifyAction,
    onRefresh
}) => {
    if (isLoading) {
        return (
            <div className="bdae-card p-12 text-center space-y-3 border border-[var(--bdae-border)]">
                <RefreshCw className="w-8 h-8 text-[var(--bdae-secondary)] animate-spin mx-auto" />
                <p className="text-xs font-semibold text-[var(--bdae-text-secondary)]">
                    Loading KYC Queue...
                </p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bdae-card p-8 border border-red-500/30 bg-red-500/5 text-center space-y-3">
                <AlertCircle className="w-8 h-8 text-red-500 mx-auto" />
                <p className="text-xs font-bold text-red-600 dark:text-red-400">{error}</p>
                <button
                    onClick={onRefresh}
                    className="bdae-btn-primary px-4 py-2 text-xs font-bold rounded-xl inline-flex items-center gap-1.5"
                >
                    <RefreshCw className="w-3.5 h-3.5" /> Retry Fetch
                </button>
            </div>
        );
    }

    if (documents.length === 0) {
        return (
            <div className="bdae-card p-12 text-center space-y-3 border border-[var(--bdae-border)]">
                <ShieldCheck className="w-10 h-10 text-[var(--bdae-text-secondary)] mx-auto opacity-50" />
                <p className="text-sm font-bold text-[var(--bdae-text-primary)]">KYC Queue Empty</p>
                <p className="text-xs text-[var(--bdae-text-secondary)]">No identification documents match your filter.</p>
            </div>
        );
    }

    return (
        <div className="bdae-card border border-[var(--bdae-border)] shadow-xl overflow-hidden rounded-2xl">
            <div className="overflow-x-auto">
                <table className="w-full text-left text-xs border-collapse">
                    <thead>
                        <tr className="border-b border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 text-[11px] font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider">
                            <th className="py-3.5 px-4">Member Profiler / Document Type</th>
                            <th className="py-3.5 px-4">ID Details</th>
                            <th className="py-3.5 px-4">Status</th>
                            <th className="py-3.5 px-4">Timestamps & Actor</th>
                            <th className="py-3.5 px-4 text-right">Verification</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-[var(--bdae-border)]">
                        {documents.map((doc) => {
                            return (
                                <tr
                                    key={doc.identificationId}
                                    className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors group"
                                >
                                    {/* Type */}
                                    <td className="py-3.5 px-4">
                                        <div className="flex items-center space-x-3">
                                            <div className="w-8 h-8 rounded-lg bg-[var(--bdae-primary)]/10 text-[var(--bdae-primary)] flex items-center justify-center text-xs shadow-sm shrink-0">
                                                <FileText className="w-4 h-4" />
                                            </div>
                                            <div>
                                                <p className="font-bold text-[var(--bdae-text-primary)] text-xs">
                                                    {doc.idType?.replace('_', ' ')}
                                                </p>
                                                <p className="text-[10px] text-[var(--bdae-text-secondary)] font-mono truncate max-w-[150px]">
                                                    User: {doc.userId}
                                                </p>
                                            </div>
                                        </div>
                                    </td>

                                    {/* Number */}
                                    <td className="py-3.5 px-4">
                                        <div className="text-[11px]">
                                            <span className="text-[var(--bdae-text-secondary)] mx-1">ID Number:</span>
                                            <strong className="font-mono text-[var(--bdae-text-primary)]">{doc.idNumber}</strong>
                                        </div>
                                        {doc.issuingAuthority && (
                                            <div className="text-[10px] text-[var(--bdae-text-secondary)] line-clamp-1 mt-0.5">
                                                Issuer: {doc.issuingAuthority}
                                            </div>
                                        )}
                                    </td>

                                    {/* Status Badge */}
                                    <td className="py-3.5 px-4">
                                        <span className={`inline-flex items-center space-x-1 px-2.5 py-1 rounded font-bold text-[10px] border tracking-wide uppercase ${doc.status === 'VERIFIED' ? 'bg-emerald-500/10 text-emerald-600 border-emerald-500/20' : doc.status === 'REJECTED' ? 'bg-red-500/10 text-red-600 border-red-500/20' : 'bg-amber-500/10 text-amber-600 border-amber-500/20'}`}>
                                            <span>{doc.status}</span>
                                        </span>
                                    </td>

                                    {/* Dates */}
                                    <td className="py-3.5 px-4 text-[10px] space-y-1">
                                        <div className="flex items-center space-x-1 text-[var(--bdae-text-secondary)]">
                                            <Calendar className="w-3 h-3 opacity-60" />
                                            <span>Sub: {new Date(doc.submittedAt).toLocaleDateString()}</span>
                                        </div>
                                        {doc.verifiedByUserId && (
                                            <div className="flex items-center space-x-1 text-[var(--bdae-text-secondary)]">
                                                <ShieldCheck className="w-3 h-3 text-cyan-500" />
                                                <span className="truncate font-mono max-w-[100px]" title={doc.verifiedByUserId}>By: {doc.verifiedByUserId.substring(0, 8)}...</span>
                                            </div>
                                        )}
                                    </td>

                                    {/* Action Buttons */}
                                    <td className="py-3.5 px-4 text-right">
                                        {doc.status === 'UNVERIFIED' ? (
                                            <PermissionGuard authorities={['KYC_VERIFY']} roles={['SUPER_ADMIN', 'SACCO_ADMIN']}>
                                                <button
                                                    onClick={() => onVerifyAction(doc)}
                                                    className="px-3 py-1.5 rounded-xl bdae-btn-primary hover:opacity-90 text-xs font-bold inline-flex items-center gap-1.5 transition-all shadow-sm"
                                                >
                                                    <ShieldCheck className="w-3.5 h-3.5" />
                                                    <span>Review</span>
                                                </button>
                                            </PermissionGuard>
                                        ) : (
                                            <button
                                                onClick={() => onVerifyAction(doc)}
                                                className="px-3 py-1.5 rounded-xl border border-[var(--bdae-border)] bg-black/5 hover:bg-black/10 dark:bg-white/5 dark:hover:bg-white/10 text-xs font-bold inline-flex items-center gap-1.5 transition-all"
                                            >
                                                <FileText className="w-3.5 h-3.5" />
                                                <span>View Remarks</span>
                                            </button>
                                        )}
                                    </td>
                                </tr>
                            );
                        })}
                    </tbody>
                </table>
            </div>
        </div>
    );
};
