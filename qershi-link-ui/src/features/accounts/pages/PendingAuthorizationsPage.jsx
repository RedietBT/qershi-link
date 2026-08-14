import React, { useState, useEffect } from 'react';
import {
    ClipboardCheck, RefreshCw, AlertCircle, CheckCircle, Search, Lock
} from 'lucide-react';
import { accountLedgerApi } from '../api/accountLedgerApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';

/**
 * Pending Authorizations Page — Four-Eye Maker/Checker Approval Queue.
 * Gated: ACCOUNT_APPROVE permission or SACCO_ADMIN / ADMIN role.
 */
const PendingAuthorizationsContent = () => {
    const [accounts, setAccounts] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [approvingNo, setApprovingNo] = useState(null);

    const fetchPending = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const res = await accountLedgerApi.getAllAccounts();
            const all = res.data || res || [];
            setAccounts(all.filter(a => a.accountStatus === 'PENDING_APPROVAL'));
        } catch (err) {
            setError(err?.response?.data?.message || 'Failed to load pending accounts.');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => { fetchPending(); }, []);

    const handleApprove = async (accountNo) => {
        setApprovingNo(accountNo);
        setSuccess(null);
        setError(null);
        try {
            await accountLedgerApi.approveAccount(accountNo);
            setSuccess(`Account ${accountNo} approved and activated!`);
            setTimeout(() => setSuccess(null), 4000);
            fetchPending();
        } catch (err) {
            setError(err?.response?.data?.message || 'Approval failed. The same user cannot approve their own account opening (Four-Eye rule).');
        } finally {
            setApprovingNo(null);
        }
    };

    const filtered = accounts.filter(a => {
        if (!searchTerm) return true;
        const t = searchTerm.toLowerCase();
        return (
            a.accountNo?.toLowerCase().includes(t) ||
            a.productCode?.toLowerCase().includes(t) ||
            a.userId?.toLowerCase().includes(t)
        );
    });

    return (
        <div className="space-y-6 animate-fadeIn">
            {/* Page Header */}
            <div className="flex items-center gap-3 border-b border-[var(--bdae-border)] pb-4">
                <div
                    className="w-10 h-10 rounded-xl flex items-center justify-center text-white shadow-md shrink-0"
                    style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
                >
                    <ClipboardCheck className="w-5 h-5" />
                </div>
                <div>
                    <h1 className="text-xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
                        Pending Authorizations
                    </h1>
                    <p className="text-xs text-[var(--bdae-text-secondary)]">
                        Four-Eye Maker/Checker queue — approve accounts opened by other operators.
                    </p>
                </div>
            </div>

            {/* Toolbar */}
            <div className="flex items-center gap-3 flex-wrap">
                <div className="relative flex-1 min-w-[200px]">
                    <input
                        type="text"
                        value={searchTerm}
                        onChange={e => setSearchTerm(e.target.value)}
                        placeholder="Search by account number, product, or user ID..."
                        className="w-full pl-9 pr-4 py-2 rounded-xl border border-[var(--bdae-border)] focus:border-[var(--bdae-secondary)] text-xs bg-transparent outline-none text-[var(--bdae-text-primary)]"
                    />
                    <Search className="w-3.5 h-3.5 text-[var(--bdae-text-secondary)] absolute left-3 top-2.5" />
                </div>
                <button onClick={fetchPending} disabled={isLoading} className="px-3 py-2 rounded-xl border border-[var(--bdae-border)] text-xs font-bold flex items-center gap-2 text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-primary)] transition-all">
                    <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} /> Refresh
                </button>
            </div>

            {error && <div className="flex items-center gap-2 p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 text-xs font-bold"><AlertCircle className="w-4 h-4" /> {error}</div>}
            {success && <div className="flex items-center gap-2 p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-600 text-xs font-bold"><CheckCircle className="w-4 h-4" /> {success}</div>}

            {/* Pending Table */}
            <div className="bdae-card border border-[var(--bdae-border)] rounded-2xl overflow-hidden">
                {isLoading ? (
                    <div className="py-20 text-center"><RefreshCw className="w-6 h-6 animate-spin mx-auto text-[var(--bdae-secondary)]" /></div>
                ) : filtered.length === 0 ? (
                    <div className="py-20 text-center flex flex-col items-center gap-3 opacity-50">
                        <ClipboardCheck className="w-10 h-10" />
                        <p className="text-sm font-bold">No accounts pending approval</p>
                        <p className="text-xs text-[var(--bdae-text-secondary)]">All accounts have been processed.</p>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse">
                            <thead>
                                <tr className="bg-black/5 dark:bg-white/5 border-b border-[var(--bdae-border)] text-[10px] uppercase font-extrabold text-[var(--bdae-text-secondary)] tracking-wider">
                                    <th className="p-4">Account Number</th>
                                    <th className="p-4">Product</th>
                                    <th className="p-4">Branch</th>
                                    <th className="p-4">Opened At</th>
                                    <th className="p-4 text-right">Action</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-[var(--bdae-border)] text-xs">
                                {filtered.map(acc => (
                                    <tr key={acc.accountNo} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                                        <td className="p-4">
                                            <span className="font-mono font-extrabold text-[var(--bdae-primary)] tracking-widest text-xs">{acc.accountNo}</span>
                                        </td>
                                        <td className="p-4">
                                            <span className="px-2 py-0.5 rounded-full text-[9px] font-bold bg-[var(--bdae-primary)]/10 text-[var(--bdae-primary)] border border-[var(--bdae-primary)]/20">{acc.productCode}</span>
                                        </td>
                                        <td className="p-4 font-mono text-[var(--bdae-text-secondary)]">{acc.branchCode || '—'}</td>
                                        <td className="p-4 text-[var(--bdae-text-secondary)]">
                                            {acc.openedAt ? new Date(acc.openedAt).toLocaleString() : '—'}
                                        </td>
                                        <td className="p-4 text-right">
                                            <button
                                                onClick={() => handleApprove(acc.accountNo)}
                                                disabled={approvingNo === acc.accountNo}
                                                className="px-4 py-1.5 rounded-xl text-[10px] font-bold text-white bg-emerald-500 hover:bg-emerald-600 flex items-center gap-1.5 ml-auto disabled:opacity-50 transition-all"
                                            >
                                                {approvingNo === acc.accountNo
                                                    ? <><RefreshCw className="w-3 h-3 animate-spin" /> Approving...</>
                                                    : <><CheckCircle className="w-3 h-3" /> Approve &amp; Activate</>}
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

            {/* Four-Eye Warning Notice */}
            <div className="text-[11px] text-[var(--bdae-text-secondary)] border border-amber-500/30 rounded-xl p-4 bg-amber-500/5 leading-relaxed">
                <span className="font-bold text-amber-600">⚠ Four-Eye Principle:</span> The same operator who opened the account cannot approve it.
                Approval must be performed by a different authorized officer with the <span className="font-mono font-bold">ACCOUNT_APPROVE</span> permission.
                Violations will be rejected by the backend.
            </div>
        </div>
    );
};

export const PendingAuthorizationsPage = () => (
    <PermissionGuard
        roles={['SACCO_ADMIN', 'ADMIN']}
        permissions={['ACCOUNT_APPROVE']}
        fallback={
            <div className="p-8 text-center max-w-lg mx-auto space-y-4 mt-10">
                <div className="w-14 h-14 rounded-2xl bg-red-500/10 border border-red-500/20 text-red-500 mx-auto flex items-center justify-center">
                    <Lock className="w-7 h-7" />
                </div>
                <h2 className="text-lg font-bold">Access Restricted</h2>
                <p className="text-xs text-[var(--bdae-text-secondary)]">
                    Account approvals require the <span className="font-mono font-bold">ACCOUNT_APPROVE</span> permission.
                </p>
            </div>
        }
    >
        <PendingAuthorizationsContent />
    </PermissionGuard>
);
