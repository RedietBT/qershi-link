import React, { useState, useEffect } from 'react';
import {
    PackagePlus, RefreshCw, AlertCircle, CheckCircle, X,
    TrendingUp, Clock, ChevronDown
} from 'lucide-react';
import { depositProductApi } from '../api/depositProductApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';

const CATEGORIES = ['SAVINGS', 'FIXED_DEPOSIT', 'CURRENT', 'SHARES', 'RECURRING'];
const FREQUENCIES = ['DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'ANNUALLY'];

const CATEGORY_COLORS = {
    SAVINGS: 'bg-blue-500/10 text-blue-600 border-blue-500/20',
    FIXED_DEPOSIT: 'bg-amber-500/10 text-amber-600 border-amber-500/20',
    CURRENT: 'bg-emerald-500/10 text-emerald-600 border-emerald-500/20',
    SHARES: 'bg-purple-500/10 text-purple-600 border-purple-500/20',
    RECURRING: 'bg-cyan-500/10 text-cyan-600 border-cyan-500/20',
};

/** Create Product Modal */
const CreateProductModal = ({ onClose, onCreated }) => {
    const [form, setForm] = useState({
        productName: '',
        category: 'SAVINGS',
        currency: 'ETB',
        interestRatePa: '0',
        postingFrequency: 'MONTHLY',
        minOperatingBalance: '0',
        minMonthlyContribution: '0',
        termPeriodMonths: '',
        earlyWithdrawalPenaltyPct: '0',
    });
    const [isSaving, setIsSaving] = useState(false);
    const [error, setError] = useState(null);

    const update = (field, val) => setForm(f => ({ ...f, [field]: val }));

    const handleSubmit = async () => {
        if (!form.productName.trim() || !form.category) {
            setError('Product name and category are required.');
            return;
        }
        setIsSaving(true);
        setError(null);
        try {
            const payload = {
                ...form,
                interestRatePa: parseFloat(form.interestRatePa) || 0,
                minOperatingBalance: parseFloat(form.minOperatingBalance) || 0,
                minMonthlyContribution: parseFloat(form.minMonthlyContribution) || 0,
                earlyWithdrawalPenaltyPct: parseFloat(form.earlyWithdrawalPenaltyPct) || 0,
                termPeriodMonths: form.termPeriodMonths ? parseInt(form.termPeriodMonths) : null,
            };
            await depositProductApi.createProduct(payload);
            onCreated();
            onClose();
        } catch (err) {
            setError(err?.response?.data?.message || 'Failed to create product.');
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
            <div className="bdae-card w-full max-w-2xl rounded-2xl border border-[var(--bdae-border)] shadow-2xl overflow-hidden animate-fadeIn">
                {/* Header */}
                <div
                    className="p-5 flex items-center justify-between text-white"
                    style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
                >
                    <div className="flex items-center gap-3">
                        <PackagePlus className="w-5 h-5" />
                        <div>
                            <h2 className="text-sm font-extrabold">New Deposit Product</h2>
                            <p className="text-[10px] opacity-80">Configure product rules & auto-assign a 3-digit product code</p>
                        </div>
                    </div>
                    <button onClick={onClose} className="text-white/70 hover:text-white transition-colors">
                        <X className="w-5 h-5" />
                    </button>
                </div>

                {/* Body */}
                <div className="p-6 grid grid-cols-1 md:grid-cols-2 gap-4 max-h-[70vh] overflow-y-auto">
                    {error && (
                        <div className="md:col-span-2 flex items-center gap-2 p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 text-xs font-bold">
                            <AlertCircle className="w-4 h-4 shrink-0" /> {error}
                        </div>
                    )}

                    {[
                        { label: 'Product Name *', field: 'productName', type: 'text', placeholder: 'e.g. General Member Savings', span: 2 },
                    ].map(({ label, field, type, placeholder, span }) => (
                        <div key={field} className={`space-y-1.5 ${span === 2 ? 'md:col-span-2' : ''}`}>
                            <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">{label}</label>
                            <input
                                type={type}
                                value={form[field]}
                                onChange={e => update(field, e.target.value)}
                                placeholder={placeholder}
                                className="w-full px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] focus:border-[var(--bdae-secondary)] bg-transparent text-xs text-[var(--bdae-text-primary)] outline-none transition-all"
                            />
                        </div>
                    ))}

                    {/* Category */}
                    <div className="space-y-1.5">
                        <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">Category *</label>
                        <select
                            value={form.category}
                            onChange={e => update('category', e.target.value)}
                            className="w-full px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-transparent text-xs text-[var(--bdae-text-primary)] outline-none transition-all"
                        >
                            {CATEGORIES.map(c => <option key={c} value={c}>{c.replace('_', ' ')}</option>)}
                        </select>
                    </div>

                    {/* Currency */}
                    <div className="space-y-1.5">
                        <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">Currency</label>
                        <input
                            type="text"
                            value={form.currency}
                            onChange={e => update('currency', e.target.value.toUpperCase())}
                            maxLength={3}
                            className="w-full px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-transparent text-xs font-mono text-[var(--bdae-text-primary)] outline-none transition-all tracking-widest"
                        />
                    </div>

                    {/* Interest Rate */}
                    <div className="space-y-1.5">
                        <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">Interest Rate p.a. (%)</label>
                        <input
                            type="number" min="0" step="0.01"
                            value={form.interestRatePa}
                            onChange={e => update('interestRatePa', e.target.value)}
                            className="w-full px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-transparent text-xs text-[var(--bdae-text-primary)] outline-none transition-all"
                        />
                    </div>

                    {/* Posting Frequency */}
                    <div className="space-y-1.5">
                        <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">Interest Posting Frequency</label>
                        <select
                            value={form.postingFrequency}
                            onChange={e => update('postingFrequency', e.target.value)}
                            className="w-full px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-transparent text-xs text-[var(--bdae-text-primary)] outline-none transition-all"
                        >
                            {FREQUENCIES.map(f => <option key={f} value={f}>{f}</option>)}
                        </select>
                    </div>

                    {/* Min Operating Balance */}
                    <div className="space-y-1.5">
                        <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">Min Operating Balance (ETB)</label>
                        <input
                            type="number" min="0" step="0.01"
                            value={form.minOperatingBalance}
                            onChange={e => update('minOperatingBalance', e.target.value)}
                            className="w-full px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-transparent text-xs text-[var(--bdae-text-primary)] outline-none transition-all"
                        />
                    </div>

                    {/* Min Monthly Contribution */}
                    <div className="space-y-1.5">
                        <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">Min Monthly Contribution (ETB)</label>
                        <input
                            type="number" min="0" step="0.01"
                            value={form.minMonthlyContribution}
                            onChange={e => update('minMonthlyContribution', e.target.value)}
                            className="w-full px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-transparent text-xs text-[var(--bdae-text-primary)] outline-none transition-all"
                        />
                    </div>

                    {/* Term Period */}
                    <div className="space-y-1.5">
                        <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">Term Period (Months) — optional</label>
                        <input
                            type="number" min="1"
                            value={form.termPeriodMonths}
                            onChange={e => update('termPeriodMonths', e.target.value)}
                            placeholder="Leave blank for open-ended"
                            className="w-full px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-transparent text-xs text-[var(--bdae-text-primary)] outline-none transition-all"
                        />
                    </div>

                    {/* Early Withdrawal Penalty */}
                    <div className="space-y-1.5">
                        <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">Early Withdrawal Penalty (%)</label>
                        <input
                            type="number" min="0" step="0.01"
                            value={form.earlyWithdrawalPenaltyPct}
                            onChange={e => update('earlyWithdrawalPenaltyPct', e.target.value)}
                            className="w-full px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-transparent text-xs text-[var(--bdae-text-primary)] outline-none transition-all"
                        />
                    </div>
                </div>

                {/* Footer */}
                <div className="px-6 py-4 border-t border-[var(--bdae-border)] flex items-center justify-end gap-3">
                    <button onClick={onClose} className="px-4 py-2 rounded-xl border border-[var(--bdae-border)] text-xs font-bold text-[var(--bdae-text-secondary)] hover:bg-black/5 transition-all">
                        Cancel
                    </button>
                    <button
                        onClick={handleSubmit}
                        disabled={isSaving}
                        className="px-5 py-2 rounded-xl text-xs font-bold text-white flex items-center gap-2 shadow-md disabled:opacity-50 transition-all"
                        style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
                    >
                        {isSaving ? <><RefreshCw className="w-3.5 h-3.5 animate-spin" /> Creating...</> : <><PackagePlus className="w-3.5 h-3.5" /> Create Product</>}
                    </button>
                </div>
            </div>
        </div>
    );
};

/** Deposit Products Dashboard Component */
export const DepositProductsDashboard = () => {
    const [products, setProducts] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showModal, setShowModal] = useState(false);

    const fetchProducts = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const res = await depositProductApi.getAllProducts();
            setProducts(res.data || res || []);
        } catch (err) {
            setError(err?.response?.data?.message || 'Failed to load deposit products.');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => { fetchProducts(); }, []);

    return (
        <div className="space-y-5">
            {/* Toolbar */}
            <div className="flex items-center justify-between gap-4 flex-wrap">
                <div className="flex items-center gap-2">
                    <TrendingUp className="w-4 h-4 text-[var(--bdae-secondary)]" />
                    <span className="text-sm font-bold text-[var(--bdae-text-primary)]">
                        {products.length} Active Product{products.length !== 1 ? 's' : ''}
                    </span>
                </div>
                <div className="flex items-center gap-2">
                    <button onClick={fetchProducts} disabled={isLoading} className="px-3 py-2 rounded-xl border border-[var(--bdae-border)] text-xs font-bold flex items-center gap-2 text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-primary)] transition-all">
                        <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} /> Refresh
                    </button>
                    {/* Only PRODUCT_CREATE or admins can see the create button */}
                    <PermissionGuard roles={['SACCO_ADMIN', 'ADMIN']} permissions={['PRODUCT_CREATE']}>
                        <button
                            onClick={() => setShowModal(true)}
                            className="px-4 py-2 rounded-xl text-xs font-bold text-white flex items-center gap-2 shadow-md transition-all"
                            style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
                        >
                            <PackagePlus className="w-3.5 h-3.5" /> New Product
                        </button>
                    </PermissionGuard>
                </div>
            </div>

            {error && (
                <div className="flex items-center gap-2 p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 text-xs font-bold">
                    <AlertCircle className="w-4 h-4 shrink-0" /> {error}
                </div>
            )}

            {/* Products Grid */}
            {isLoading ? (
                <div className="py-20 text-center"><RefreshCw className="w-6 h-6 animate-spin mx-auto text-[var(--bdae-secondary)]" /></div>
            ) : products.length === 0 ? (
                <div className="bdae-card border border-dashed border-[var(--bdae-border)] rounded-2xl py-16 text-center space-y-2 opacity-60">
                    <PackagePlus className="w-10 h-10 mx-auto" />
                    <p className="text-sm font-bold">No deposit products configured yet</p>
                    <p className="text-xs text-[var(--bdae-text-secondary)]">Create the first product to enable account opening for members.</p>
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
                    {products.map(product => (
                        <div key={product.productCode} className="bdae-card border border-[var(--bdae-border)] rounded-2xl p-5 space-y-3 hover:border-[var(--bdae-secondary)] transition-all shadow-sm group">
                            {/* Product Header */}
                            <div className="flex items-start justify-between gap-2">
                                <div>
                                    <p className="text-sm font-extrabold text-[var(--bdae-text-primary)] group-hover:text-[var(--bdae-secondary)] transition-colors leading-tight">
                                        {product.productName}
                                    </p>
                                    <p className="text-[10px] font-mono text-[var(--bdae-text-secondary)] mt-0.5">Code: <span className="font-bold text-[var(--bdae-primary)]">{product.productCode}</span></p>
                                </div>
                                <span className={`shrink-0 text-[9px] font-bold px-2 py-0.5 rounded-full border ${CATEGORY_COLORS[product.category] || 'bg-gray-500/10 text-gray-600 border-gray-500/20'}`}>
                                    {product.category?.replace('_', ' ')}
                                </span>
                            </div>

                            {/* Stats Row */}
                            <div className="grid grid-cols-2 gap-2">
                                <div className="p-2 rounded-xl bg-black/5 dark:bg-white/5 space-y-0.5">
                                    <p className="text-[9px] uppercase font-bold text-[var(--bdae-text-secondary)] tracking-wide">Interest p.a.</p>
                                    <p className="text-sm font-extrabold text-[var(--bdae-primary)]">{product.interestRatePa ?? 0}%</p>
                                </div>
                                <div className="p-2 rounded-xl bg-black/5 dark:bg-white/5 space-y-0.5">
                                    <p className="text-[9px] uppercase font-bold text-[var(--bdae-text-secondary)] tracking-wide">Currency</p>
                                    <p className="text-sm font-extrabold font-mono text-[var(--bdae-text-primary)]">{product.currency || 'ETB'}</p>
                                </div>
                            </div>

                            {/* Details */}
                            <div className="space-y-1 text-[10px] text-[var(--bdae-text-secondary)] border-t border-[var(--bdae-border)] pt-3">
                                <div className="flex justify-between">
                                    <span>Posting Frequency</span>
                                    <span className="font-bold text-[var(--bdae-text-primary)]">{product.postingFrequency || 'MONTHLY'}</span>
                                </div>
                                <div className="flex justify-between">
                                    <span>Min Balance</span>
                                    <span className="font-bold text-[var(--bdae-text-primary)]">{Number(product.minOperatingBalance || 0).toLocaleString()} ETB</span>
                                </div>
                                <div className="flex justify-between">
                                    <span>Min Monthly Contribution</span>
                                    <span className="font-bold text-[var(--bdae-text-primary)]">{Number(product.minMonthlyContribution || 0).toLocaleString()} ETB</span>
                                </div>
                                {product.termPeriodMonths && (
                                    <div className="flex justify-between">
                                        <span>Term</span>
                                        <span className="font-bold text-[var(--bdae-text-primary)] flex items-center gap-1"><Clock className="w-3 h-3" /> {product.termPeriodMonths} months</span>
                                    </div>
                                )}
                                {product.earlyWithdrawalPenaltyPct > 0 && (
                                    <div className="flex justify-between text-amber-600">
                                        <span>Early Withdrawal Penalty</span>
                                        <span className="font-bold">{product.earlyWithdrawalPenaltyPct}%</span>
                                    </div>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {showModal && (
                <CreateProductModal
                    onClose={() => setShowModal(false)}
                    onCreated={fetchProducts}
                />
            )}
        </div>
    );
};
