import React from 'react';
import { useKycQueue } from '../hooks/useKyc';
import { kycApi } from '../api/kycApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { ShieldCheck, RefreshCw, Lock, Filter, FileText, CheckCircle, XCircle } from 'lucide-react';
import { KycVerificationTable } from '../components/KycVerificationTable';
import { KycVerificationModal } from '../components/KycVerificationModal';

export const KycVerificationPage = () => {
    const {
        identifications,
        isLoading,
        error,
        statusFilter,
        setStatusFilter,
        refreshQueue,
        verifyingDoc,
        setVerifyingDoc
    } = useKycQueue();

    return (
        <PermissionGuard roles={['SUPER_ADMIN', 'SACCO_ADMIN']} authorities={['KYC_VIEW']} fallback={
            <div className="p-8 text-center max-w-lg mx-auto space-y-4">
                <div className="w-12 h-12 rounded-full bg-amber-500/10 border border-amber-500/30 text-amber-600 mx-auto flex items-center justify-center">
                    <Lock className="w-6 h-6" />
                </div>
                <h2 className="text-lg font-bold">Access Restricted</h2>
                <p className="text-xs text-[var(--bdae-text-secondary)]">
                    KYC Verification Queue requires Supervisor permissions.
                </p>
            </div>
        }>
            <div className="space-y-6 animate-fadeIn max-w-7xl mx-auto">
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-[var(--bdae-border)] pb-4">
                    <div className="flex items-center space-x-3">
                        <div
                            className="w-10 h-10 rounded-xl flex items-center justify-center text-white shadow-md shrink-0"
                            style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
                        >
                            <ShieldCheck className="w-5 h-5" />
                        </div>
                        <div>
                            <h1 className="text-xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
                                KYC Verification Queue
                            </h1>
                            <p className="text-xs text-[var(--bdae-text-secondary)]">
                                Review and approve official government identification documents.
                            </p>
                        </div>
                    </div>

                    <div className="flex items-center space-x-3">
                        <button
                            onClick={refreshQueue}
                            disabled={isLoading}
                            className="px-3.5 py-2 rounded-xl border border-[var(--bdae-border)] hover:border-[var(--bdae-secondary)] text-xs font-bold flex items-center gap-2 transition-all shadow-sm"
                        >
                            <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
                            <span>Refresh Queue</span>
                        </button>
                    </div>
                </div>

                <div className="bdae-card p-4 border border-[var(--bdae-border)] shadow-sm rounded-xl flex flex-col md:flex-row items-center gap-4">
                    <div className="flex items-center space-x-2 text-[var(--bdae-text-secondary)] text-xs font-bold shrink-0">
                        <Filter className="w-4 h-4" />
                        <span>Filter Queue:</span>
                    </div>

                    <select
                        value={statusFilter}
                        onChange={(e) => setStatusFilter(e.target.value)}
                        className="px-4 py-2 flex-grow max-w-sm text-xs bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] rounded-lg text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)] font-bold"
                    >
                        <option value="UNVERIFIED">Pending Review (UNVERIFIED)</option>
                        <option value="VERIFIED">Approved (VERIFIED)</option>
                        <option value="REJECTED">Declined (REJECTED)</option>
                        <option value="">All Documents</option>
                    </select>

                    {/* Stats */}
                    <div className="ml-auto flex gap-4">
                        <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-amber-500/10 text-amber-600 border border-amber-500/20 text-xs font-bold">
                            <FileText className="w-3.5 h-3.5" />
                            <span>{identifications.filter(d => d.status === 'UNVERIFIED').length} Pending</span>
                        </div>
                    </div>
                </div>

                <KycVerificationTable
                    documents={identifications}
                    isLoading={isLoading}
                    error={error}
                    onVerifyAction={(doc) => setVerifyingDoc(doc)}
                    onRefresh={refreshQueue}
                />

                {verifyingDoc && (
                    <KycVerificationModal
                        document={verifyingDoc}
                        onClose={() => setVerifyingDoc(null)}
                        onSuccess={() => {
                            setVerifyingDoc(null);
                            refreshQueue();
                        }}
                    />
                )}
            </div>
        </PermissionGuard>
    );
};
