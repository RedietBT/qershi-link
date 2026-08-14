import React, { useState, useEffect } from 'react';
import {
    CreditCard, Plus, RefreshCw, AlertCircle, CheckCircle,
    X, ShieldAlert, Snowflake, Lock, Unlock, Search
} from 'lucide-react';
import { accountLedgerApi } from '../api/accountLedgerApi';
import { depositProductApi } from '../api/depositProductApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';

const STATUS_STYLES = {
    ACTIVE: 'bg-emerald-500/10 text-emerald-600 border-emerald-500/20',
    PENDING_APPROVAL: 'bg-amber-500/10 text-amber-600 border-amber-500/20',
    DORMANT: 'bg-gray-500/10 text-gray-500 border-gray-500/20',
    CLOSED: 'bg-red-500/10 text-red-500 border-red-500/20',
    FROZEN: 'bg-blue-500/10 text-blue-600 border-blue-500/20',
};

const FREEZE_OPTIONS = ['NONE', 'DEBIT_FREEZE', 'CREDIT_FREEZE', 'FULL_FREEZE'];

/** Open Account Modal */
const OpenAccountModal = ({ userId, onClose, onOpened }) => {
    const [products, setProducts] = useState([]);
    const [productCode, setProductCode] = useState('');
    const [branchCode, setBranchCode] = useState('0001');
    const [isSaving, setIsSaving] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        depositProductApi.getAllProducts()
            .then(res => setProducts(res.data || res || []))
            .catch(() => { });
    }, []);

    const handleSubmit = async () => {
        if (!productCode) { setError('Please select a deposit product.'); return; }
        setIsSaving(true);
        setError(null);
        try {
            await accountLedgerApi.openAccount({ userId, branchCode, productCode });
            onOpened();
            onClose();
        } catch (err) {
            setError(err?.response?.data?.message || 'Failed to open account.');
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
            <div className="bdae-card w-full max-w-md rounded-2xl border border-[var(--bdae-border)] shadow-2xl overflow-hidden animate-fadeIn">
                <div className="p-5 flex items-center justify-between text-white"
                    style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}>
                    <div className="flex items-center gap-3">
                        <CreditCard className="w-5 h-5" />
                        <div>
                            <h2 className="text-sm font-extrabold">Open New Account</h2>
                            <p className="text-[10px] opacity-80">Account will enter PENDING_APPROVAL status</p>
                        </div>
                    </div>
                    <button onClick={onClose} className="text-white/70 hover:text-white"><X className="w-5 h-5" /></button>
                </div>
                <div className="p-6 space-y-4">
                    {error && (
                        <div className="flex items-center gap-2 p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 text-xs font-bold">
                            <AlertCircle className="w-4 h-4 shrink-0" /> {error}
                        </div>
                    )}
                    <div className="space-y-1.5">
                        <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">Deposit Product *</label>
                        <select
                            value={productCode}
                            onChange={e => setProductCode(e.target.value)}
                            className="w-full px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-transparent text-xs text-[var(--bdae-text-primary)] outline-none"
                        >
                            <option value="">— Select a product —</option>
                            {products.map(p => (
                                <option key={p.productCode} value={p.productCode}>
                                    [{p.productCode}] {p.productName}
                                </option>
                            ))}
                        </select>
                    </div>
                    <div className="space-y-1.5">
                        <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">Branch Code</label>
                        <input
                            type="text"
                            value={branchCode}
                            onChange={e => setBranchCode(e.target.value)}
                            className="w-full px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-transparent text-xs font-mono text-[var(--bdae-text-primary)] outline-none tracking-widest"
                            placeholder="0001"
                        />
                    </div>
                </div>
                <div className="px-6 py-4 border-t border-[var(--bdae-border)] flex justify-end gap-3">
                    <button onClick={onClose} className="px-4 py-2 rounded-xl border border-[var(--bdae-border)] text-xs font-bold text-[var(--bdae-text-secondary)] hover:bg-black/5">Cancel</button>
                    <button
                        onClick={handleSubmit} disabled={isSaving}
                        className="px-5 py-2 rounded-xl text-xs font-bold text-white flex items-center gap-2 shadow-md disabled:opacity-50"
                        style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
                    >
                        {isSaving ? <><RefreshCw className="w-3.5 h-3.5 animate-spin" /> Opening...</> : <><Plus className="w-3.5 h-3.5" /> Open Account</>}
                    </button>
                </div>
            </div>
        </div>
    );
};

/** Lien Modal */
const LienModal = ({ accountNo, onClose, onDone }) => {
    const [amount, setAmount] = useState('');
    const [reason, setReason] = useState('');
    const [referenceNo, setReferenceNo] = useState('');
    const [isSaving, setIsSaving] = useState(false);
    const [error, setError] = useState(null);

    const handleSubmit = async () => {
        if (!amount || !reason) { setError('Amount and reason are required.'); return; }
        setIsSaving(true);
        setError(null);
        try {
            await accountLedgerApi.placeLien(accountNo, { amount: parseFloat(amount), reason, referenceNo });
            onDone();
            onClose();
        } catch (err) {
            setError(err?.response?.data?.message || 'Failed to place lien.');
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
            <div className="bdae-card w-full max-w-md rounded-2xl border border-amber-500/30 shadow-2xl overflow-hidden animate-fadeIn">
                <div className="p-5 flex items-center justify-between bg-amber-500 text-white">
                    <div className="flex items-center gap-3"><ShieldAlert className="w-5 h-5" /><h2 className="text-sm font-extrabold">Place Lien Hold — {accountNo}</h2></div>
                    <button onClick={onClose}><X className="w-5 h-5" /></button>
                </div>
                <div className="p-6 space-y-4">
                    {error && <div className="flex items-center gap-2 p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 text-xs font-bold"><AlertCircle className="w-4 h-4" /> {error}</div>}
                    {[
                        { label: 'Amount (ETB) *', val: amount, set: setAmount, type: 'number', placeholder: '0.00' },
                        { label: 'Reason *', val: reason, set: setReason, type: 'text', placeholder: 'e.g. Loan collateral hold' },
                        { label: 'Reference No', val: referenceNo, set: setReferenceNo, type: 'text', placeholder: 'Optional' },
                    ].map(({ label, val, set, type, placeholder }) => (
                        <div key={label} className="space-y-1.5">
                            <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">{label}</label>
                            <input type={type} value={val} onChange={e => set(e.target.value)} placeholder={placeholder}
                                className="w-full px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-transparent text-xs text-[var(--bdae-text-primary)] outline-none" />
                        </div>
                    ))}
                </div>
                <div className="px-6 py-4 border-t border-[var(--bdae-border)] flex justify-end gap-3">
                    <button onClick={onClose} className="px-4 py-2 rounded-xl border border-[var(--bdae-border)] text-xs font-bold text-[var(--bdae-text-secondary)]">Cancel</button>
                    <button onClick={handleSubmit} disabled={isSaving}
                        className="px-5 py-2 rounded-xl text-xs font-bold text-white bg-amber-500 flex items-center gap-2 disabled:opacity-50">
                        {isSaving ? <><RefreshCw className="w-3.5 h-3.5 animate-spin" /> Placing...</> : <><ShieldAlert className="w-3.5 h-3.5" /> Place Lien</>}
                    </button>
                </div>
            </div>
        </div>
    );
};

/** Freeze Modal */
const FreezeModal = ({ accountNo, currentStatus, onClose, onDone }) => {
    const [freezeStatus, setFreezeStatus] = useState(currentStatus || 'NONE');
    const [reason, setReason] = useState('');
    const [isSaving, setIsSaving] = useState(false);
    const [error, setError] = useState(null);

    const handleSubmit = async () => {
        setIsSaving(true); setError(null);
        try {
            await accountLedgerApi.freezeAccount(accountNo, { freezeStatus, reason });
            onDone(); onClose();
        } catch (err) {
            setError(err?.response?.data?.message || 'Failed to update freeze status.');
        } finally { setIsSaving(false); }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
            <div className="bdae-card w-full max-w-md rounded-2xl border border-blue-500/30 shadow-2xl overflow-hidden animate-fadeIn">
                <div className="p-5 flex items-center justify-between bg-blue-600 text-white">
                    <div className="flex items-center gap-3"><Snowflake className="w-5 h-5" /><h2 className="text-sm font-extrabold">Freeze Control — {accountNo}</h2></div>
                    <button onClick={onClose}><X className="w-5 h-5" /></button>
                </div>
                <div className="p-6 space-y-4">
                    {error && <div className="flex items-center gap-2 p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 text-xs font-bold"><AlertCircle className="w-4 h-4" /> {error}</div>}
                    <div className="space-y-1.5">
                        <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">Freeze Status *</label>
                        <div className="grid grid-cols-2 gap-2">
                            {FREEZE_OPTIONS.map(opt => (
                                <button key={opt} onClick={() => setFreezeStatus(opt)}
                                    className={`px-3 py-2 rounded-xl border text-[10px] font-bold transition-all ${freezeStatus === opt ? 'border-blue-500 bg-blue-500/10 text-blue-600' : 'border-[var(--bdae-border)] text-[var(--bdae-text-secondary)]'}`}>
                                    {opt.replace('_', ' ')}
                                </button>
                            ))}
                        </div>
                    </div>
                    <div className="space-y-1.5">
                        <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">Reason</label>
                        <input type="text" value={reason} onChange={e => setReason(e.target.value)} placeholder="Optional reason for audit trail"
                            className="w-full px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-transparent text-xs text-[var(--bdae-text-primary)] outline-none" />
                    </div>
                </div>
                <div className="px-6 py-4 border-t border-[var(--bdae-border)] flex justify-end gap-3">
                    <button onClick={onClose} className="px-4 py-2 rounded-xl border border-[var(--bdae-border)] text-xs font-bold text-[var(--bdae-text-secondary)]">Cancel</button>
                    <button onClick={handleSubmit} disabled={isSaving}
                        className="px-5 py-2 rounded-xl text-xs font-bold text-white bg-blue-600 flex items-center gap-2 disabled:opacity-50">
                        {isSaving ? <><RefreshCw className="w-3.5 h-3.5 animate-spin" /> Saving...</> : <><Snowflake className="w-3.5 h-3.5" /> Apply Freeze</>}
                    </button>
                </div>
            </div>
        </div>
    );
};

/** Member Account Tab — embeds into MemberProfileDetailModal */
export const MemberAccountsTab = ({ userId }) => {
    const [accounts, setAccounts] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);
    const [showOpenModal, setShowOpenModal] = useState(false);
    const [lienTarget, setLienTarget] = useState(null);
    const [freezeTarget, setFreezeTarget] = useState(null);
    const [approvingNo, setApprovingNo] = useState(null);

    const fetchAccounts = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const res = await accountLedgerApi.getAccountsByUserId(userId);
            setAccounts(res.data || res || []);
        } catch (err) {
            setError(err?.response?.data?.message || 'Failed to load accounts.');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => { if (userId) fetchAccounts(); }, [userId]);

    const handleApprove = async (accountNo) => {
        setApprovingNo(accountNo);
        setSuccess(null);
        try {
            await accountLedgerApi.approveAccount(accountNo);
            setSuccess(`Account ${accountNo} approved and activated!`);
            setTimeout(() => setSuccess(null), 4000);
            fetchAccounts();
        } catch (err) {
            setError(err?.response?.data?.message || 'Approval failed.');
        } finally {
            setApprovingNo(null);
        }
    };

    return (
        <div className="space-y-4">
            {/* Toolbar */}
            <div className="flex items-center justify-between gap-3 flex-wrap">
                <span className="text-xs font-bold text-[var(--bdae-text-secondary)]">
                    {accounts.length} Account{accounts.length !== 1 ? 's' : ''} on record
                </span>
                <div className="flex gap-2">
                    <button onClick={fetchAccounts} disabled={isLoading} className="px-3 py-1.5 rounded-xl border border-[var(--bdae-border)] text-[10px] font-bold flex items-center gap-1.5 text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-primary)] transition-all">
                        <RefreshCw className={`w-3 h-3 ${isLoading ? 'animate-spin' : ''}`} /> Refresh
                    </button>
                    <PermissionGuard roles={['SACCO_ADMIN', 'ADMIN']} permissions={['ACCOUNT_OPEN']}>
                        <button
                            onClick={() => setShowOpenModal(true)}
                            className="px-3 py-1.5 rounded-xl text-[10px] font-bold text-white flex items-center gap-1.5 shadow-sm"
                            style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
                        >
                            <Plus className="w-3 h-3" /> Open Account
                        </button>
                    </PermissionGuard>
                </div>
            </div>

            {error && <div className="flex items-center gap-2 p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 text-xs font-bold"><AlertCircle className="w-4 h-4" /> {error}</div>}
            {success && <div className="flex items-center gap-2 p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-600 text-xs font-bold"><CheckCircle className="w-4 h-4" /> {success}</div>}

            {/* Account Cards */}
            {isLoading ? (
                <div className="py-12 text-center"><RefreshCw className="w-5 h-5 animate-spin mx-auto text-[var(--bdae-secondary)]" /></div>
            ) : accounts.length === 0 ? (
                <div className="py-12 text-center opacity-50">
                    <CreditCard className="w-8 h-8 mx-auto mb-2" />
                    <p className="text-xs font-bold">No accounts found for this member</p>
                </div>
            ) : (
                <div className="space-y-3">
                    {accounts.map(acc => (
                        <div key={acc.accountNo} className="bdae-card border border-[var(--bdae-border)] rounded-2xl p-4 space-y-3">
                            {/* Account Header */}
                            <div className="flex items-start justify-between gap-2">
                                <div>
                                    <p className="font-mono font-extrabold text-sm text-[var(--bdae-primary)] tracking-widest">{acc.accountNo}</p>
                                    <p className="text-[10px] text-[var(--bdae-text-secondary)] mt-0.5">
                                        Product: <span className="font-bold">{acc.productCode}</span>
                                        {acc.branchCode && <> · Branch: <span className="font-bold">{acc.branchCode}</span></>}
                                    </p>
                                </div>
                                <div className="flex items-center gap-1.5 flex-wrap justify-end">
                                    <span className={`text-[9px] font-extrabold px-2 py-0.5 rounded-full border ${STATUS_STYLES[acc.accountStatus] || STATUS_STYLES.DORMANT}`}>
                                        {acc.accountStatus}
                                    </span>
                                    {acc.freezeStatus && acc.freezeStatus !== 'NONE' && (
                                        <span className="text-[9px] font-extrabold px-2 py-0.5 rounded-full border bg-blue-500/10 text-blue-600 border-blue-500/20 flex items-center gap-1">
                                            <Snowflake className="w-2.5 h-2.5" /> {acc.freezeStatus}
                                        </span>
                                    )}
                                </div>
                            </div>

                            {/* Balance Row */}
                            <div className="grid grid-cols-3 gap-2">
                                {[
                                    { label: 'Ledger Balance', val: acc.ledgerBalance, color: 'var(--bdae-primary)' },
                                    { label: 'Cleared Balance', val: acc.clearedBalance, color: 'var(--bdae-secondary)' },
                                    { label: 'Lien Amount', val: acc.lienAmount, color: '#f59e0b' },
                                ].map(({ label, val, color }) => (
                                    <div key={label} className="p-2 rounded-xl bg-black/5 dark:bg-white/5 space-y-0.5">
                                        <p className="text-[8px] uppercase font-bold tracking-wide text-[var(--bdae-text-secondary)]">{label}</p>
                                        <p className="text-xs font-extrabold" style={{ color }}>{Number(val || 0).toLocaleString()} ETB</p>
                                    </div>
                                ))}
                            </div>

                            {/* Action Buttons */}
                            <div className="flex items-center gap-2 flex-wrap border-t border-[var(--bdae-border)] pt-3">
                                {/* Four-Eye Approval */}
                                {acc.accountStatus === 'PENDING_APPROVAL' && (
                                    <PermissionGuard roles={['SACCO_ADMIN', 'ADMIN']} permissions={['ACCOUNT_APPROVE']}>
                                        <button
                                            onClick={() => handleApprove(acc.accountNo)}
                                            disabled={approvingNo === acc.accountNo}
                                            className="px-3 py-1.5 rounded-xl text-[10px] font-bold text-white bg-emerald-500 hover:bg-emerald-600 flex items-center gap-1.5 disabled:opacity-50 transition-all"
                                        >
                                            {approvingNo === acc.accountNo ? <><RefreshCw className="w-3 h-3 animate-spin" /> Approving...</> : <><CheckCircle className="w-3 h-3" /> Approve &amp; Activate</>}
                                        </button>
                                    </PermissionGuard>
                                )}

                                {/* Place Lien */}
                                <PermissionGuard roles={['SACCO_ADMIN', 'ADMIN']} permissions={['LIEN_CREATE']}>
                                    <button
                                        onClick={() => setLienTarget(acc.accountNo)}
                                        className="px-3 py-1.5 rounded-xl border border-amber-500/30 text-amber-600 text-[10px] font-bold hover:bg-amber-500/10 flex items-center gap-1.5 transition-all"
                                    >
                                        <ShieldAlert className="w-3 h-3" /> Place Lien
                                    </button>
                                </PermissionGuard>

                                {/* Freeze Control */}
                                <PermissionGuard roles={['SACCO_ADMIN', 'ADMIN']} permissions={['ACCOUNT_FREEZE']}>
                                    <button
                                        onClick={() => setFreezeTarget({ accountNo: acc.accountNo, freezeStatus: acc.freezeStatus })}
                                        className="px-3 py-1.5 rounded-xl border border-blue-500/30 text-blue-600 text-[10px] font-bold hover:bg-blue-500/10 flex items-center gap-1.5 transition-all"
                                    >
                                        <Snowflake className="w-3 h-3" /> Freeze Control
                                    </button>
                                </PermissionGuard>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {showOpenModal && (
                <OpenAccountModal userId={userId} onClose={() => setShowOpenModal(false)} onOpened={fetchAccounts} />
            )}
            {lienTarget && (
                <LienModal accountNo={lienTarget} onClose={() => setLienTarget(null)} onDone={fetchAccounts} />
            )}
            {freezeTarget && (
                <FreezeModal accountNo={freezeTarget.accountNo} currentStatus={freezeTarget.freezeStatus} onClose={() => setFreezeTarget(null)} onDone={fetchAccounts} />
            )}
        </div>
    );
};
