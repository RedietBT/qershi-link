import React, { useState, useEffect } from 'react';
import { X, Save, AlertCircle, Plus, Trash2, Layers, Sparkles, HelpCircle } from 'lucide-react';

const TRANSACTION_TYPES = [
    { value: 'WITHDRAWAL', label: 'Cash Withdrawal (Over-the-Counter)' },
    { value: 'WITHDRAWAL_TIERED', label: 'Cash Withdrawal (Tiered Brackets)' },
    { value: 'TRANSFER_INTERNAL', label: 'Internal Transfer (Member-to-Member)' },
    { value: 'TRANSFER_EXTERNAL', label: 'External Transfer (Inter-Bank / RTGS)' },
    { value: 'STATEMENT_PRINT', label: 'Statement Printing Fee' },
    { value: 'LOAN_PROCESSING', label: 'Loan Processing / Appraisal' },
    { value: 'ACCOUNT_MAINTENANCE', label: 'Monthly Account Maintenance' }
];

export const TariffFormModal = ({ isOpen, onClose, onSave, tariff, isSubmitting }) => {
    const [formData, setFormData] = useState({
        tariffCode: '',
        tariffName: '',
        transactionType: 'WITHDRAWAL',
        feeType: 'FLAT',
        feeValue: '10.00',
        minFee: '',
        maxFee: '',
        feeGlCode: '4020',
        currency: 'ETB',
        isActive: true,
        description: '',
        slabs: []
    });
    const [error, setError] = useState(null);

    useEffect(() => {
        if (tariff) {
            const mappedSlabs = (tariff.slabs || []).map((s, idx) => ({
                slabOrder: s.slabOrder != null ? s.slabOrder : idx + 1,
                fromAmount: s.fromAmount != null ? String(s.fromAmount) : '0',
                toAmount: s.toAmount != null ? String(s.toAmount) : '',
                feeType: s.feeType || 'FLAT',
                feeValue: s.feeValue != null ? String(s.feeValue) : '0',
                minFee: s.minFee != null ? String(s.minFee) : '',
                maxFee: s.maxFee != null ? String(s.maxFee) : ''
            }));

            setFormData({
                tariffCode: tariff.tariffCode || '',
                tariffName: tariff.tariffName || '',
                transactionType: tariff.transactionType || 'WITHDRAWAL',
                feeType: tariff.feeType || 'FLAT',
                feeValue: tariff.feeValue != null ? String(tariff.feeValue) : '0',
                minFee: tariff.minFee != null ? String(tariff.minFee) : '',
                maxFee: tariff.maxFee != null ? String(tariff.maxFee) : '',
                feeGlCode: tariff.feeGlCode || '4020',
                currency: tariff.currency || 'ETB',
                isActive: tariff.active !== false,
                description: tariff.description || '',
                slabs: mappedSlabs
            });
        } else {
            setFormData({
                tariffCode: '',
                tariffName: '',
                transactionType: 'WITHDRAWAL',
                feeType: 'FLAT',
                feeValue: '10.00',
                minFee: '',
                maxFee: '',
                feeGlCode: '4020',
                currency: 'ETB',
                isActive: true,
                description: '',
                slabs: []
            });
        }
        setError(null);
    }, [tariff, isOpen]);

    if (!isOpen) return null;

    const handleAddSlab = () => {
        const lastSlab = formData.slabs[formData.slabs.length - 1];
        let nextFrom = '0.00';
        if (lastSlab && lastSlab.toAmount) {
            nextFrom = String(parseFloat(lastSlab.toAmount) + 0.01);
        }
        setFormData(prev => ({
            ...prev,
            slabs: [
                ...prev.slabs,
                {
                    slabOrder: prev.slabs.length + 1,
                    fromAmount: nextFrom,
                    toAmount: '',
                    feeType: 'FLAT',
                    feeValue: '10.00',
                    minFee: '',
                    maxFee: ''
                }
            ]
        }));
    };

    const handleRemoveSlab = (index) => {
        setFormData(prev => ({
            ...prev,
            slabs: prev.slabs.filter((_, i) => i !== index)
        }));
    };

    const handleSlabChange = (index, field, value) => {
        setFormData(prev => {
            const updated = [...prev.slabs];
            updated[index] = { ...updated[index], [field]: value };
            return { ...prev, slabs: updated };
        });
    };

    const handleLoadStandardLadder = () => {
        setFormData(prev => ({
            ...prev,
            slabs: [
                { slabOrder: 1, fromAmount: '0.00', toAmount: '1000.00', feeType: 'FLAT', feeValue: '5.00', minFee: '', maxFee: '' },
                { slabOrder: 2, fromAmount: '1000.01', toAmount: '10000.00', feeType: 'FLAT', feeValue: '15.00', minFee: '', maxFee: '' },
                { slabOrder: 3, fromAmount: '10000.01', toAmount: '50000.00', feeType: 'FLAT', feeValue: '25.00', minFee: '', maxFee: '' },
                { slabOrder: 4, fromAmount: '50000.01', toAmount: '', feeType: 'PERCENTAGE', feeValue: '0.25', minFee: '30.00', maxFee: '100.00' }
            ]
        }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        setError(null);

        if (!formData.tariffCode.trim()) {
            setError('Tariff Code is required (e.g. TAR-WTH-01).');
            return;
        }
        if (!formData.tariffName.trim()) {
            setError('Tariff Name is required.');
            return;
        }

        if (formData.feeType === 'TIERED') {
            if (!formData.slabs || formData.slabs.length === 0) {
                setError('At least one slab bracket must be defined for a Tiered tariff schedule.');
                return;
            }
            for (let i = 0; i < formData.slabs.length; i++) {
                const s = formData.slabs[i];
                if (isNaN(parseFloat(s.fromAmount)) || parseFloat(s.fromAmount) < 0) {
                    setError(`Slab #${i + 1}: From Amount must be a valid positive number.`);
                    return;
                }
                if (s.toAmount && parseFloat(s.toAmount) < parseFloat(s.fromAmount)) {
                    setError(`Slab #${i + 1}: To Amount must be greater than From Amount.`);
                    return;
                }
                if (isNaN(parseFloat(s.feeValue)) || parseFloat(s.feeValue) < 0) {
                    setError(`Slab #${i + 1}: Fee value must be a valid positive number.`);
                    return;
                }
            }
        } else {
            if (isNaN(parseFloat(formData.feeValue)) || parseFloat(formData.feeValue) < 0) {
                setError('Fee value must be a valid positive number.');
                return;
            }
        }

        const formattedSlabs = formData.feeType === 'TIERED' ? formData.slabs.map((s, idx) => ({
            slabOrder: idx + 1,
            fromAmount: parseFloat(s.fromAmount) || 0,
            toAmount: s.toAmount !== '' && s.toAmount != null ? parseFloat(s.toAmount) : null,
            feeType: s.feeType || 'FLAT',
            feeValue: parseFloat(s.feeValue) || 0,
            minFee: s.minFee ? parseFloat(s.minFee) : null,
            maxFee: s.maxFee ? parseFloat(s.maxFee) : null
        })) : [];

        onSave({
            ...formData,
            tariffCode: formData.tariffCode.trim().toUpperCase(),
            feeValue: parseFloat(formData.feeValue || 0),
            minFee: formData.minFee ? parseFloat(formData.minFee) : null,
            maxFee: formData.maxFee ? parseFloat(formData.maxFee) : null,
            slabs: formattedSlabs
        });
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-in fade-in duration-200">
            <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl shadow-2xl max-w-3xl w-full max-h-[92vh] overflow-y-auto">
                <div className="flex items-center justify-between p-6 border-b border-slate-100 dark:border-slate-800 sticky top-0 bg-white/95 dark:bg-slate-900/95 backdrop-blur z-10">
                    <div>
                        <h2 className="text-xl font-bold text-slate-900 dark:text-white">
                            {tariff ? 'Edit Transaction Tariff' : 'Create New Transaction Tariff'}
                        </h2>
                        <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
                            Configure transaction fees, tiered amount-bracket slabs, caps, and Chart of Accounts revenue GL routing.
                        </p>
                    </div>
                    <button
                        onClick={onClose}
                        className="p-2 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 transition"
                    >
                        <X className="w-5 h-5" />
                    </button>
                </div>

                {error && (
                    <div className="mx-6 mt-4 p-3 rounded-lg bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-800 text-rose-700 dark:text-rose-300 text-sm flex items-center gap-2">
                        <AlertCircle className="w-4 h-4 flex-shrink-0" />
                        <span>{error}</span>
                    </div>
                )}

                <form onSubmit={handleSubmit} className="p-6 space-y-4">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <label className="block text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider mb-1">
                                Tariff Code *
                            </label>
                            <input
                                type="text"
                                disabled={!!tariff}
                                value={formData.tariffCode}
                                onChange={(e) => setFormData({ ...formData, tariffCode: e.target.value.toUpperCase() })}
                                placeholder="e.g. TAR-WTH-01 or TAR-WTH-TIER"
                                className="w-full px-3 py-2 text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:outline-none disabled:opacity-50 font-mono font-semibold"
                            />
                        </div>

                        <div>
                            <label className="block text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider mb-1">
                                Tariff Display Name *
                            </label>
                            <input
                                type="text"
                                value={formData.tariffName}
                                onChange={(e) => setFormData({ ...formData, tariffName: e.target.value })}
                                placeholder="e.g. Teller Cash Withdrawal Surcharge"
                                className="w-full px-3 py-2 text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                            />
                        </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <label className="block text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider mb-1">
                                Transaction Type *
                            </label>
                            <select
                                value={formData.transactionType}
                                onChange={(e) => setFormData({ ...formData, transactionType: e.target.value })}
                                className="w-full px-3 py-2 text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                            >
                                {TRANSACTION_TYPES.map((t) => (
                                    <option key={t.value} value={t.value}>
                                        {t.label}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div>
                            <label className="block text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider mb-1">
                                Fee Calculation Mode *
                            </label>
                            <div className="grid grid-cols-3 gap-2 mt-1">
                                <button
                                    type="button"
                                    onClick={() => setFormData({ ...formData, feeType: 'FLAT' })}
                                    className={`py-2 px-2 text-xs font-medium rounded-lg border transition text-center ${
                                        formData.feeType === 'FLAT'
                                            ? 'bg-emerald-50 dark:bg-emerald-950/40 border-emerald-500 text-emerald-700 dark:text-emerald-400 font-bold'
                                            : 'border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400'
                                    }`}
                                >
                                    Fixed Flat
                                </button>
                                <button
                                    type="button"
                                    onClick={() => setFormData({ ...formData, feeType: 'PERCENTAGE' })}
                                    className={`py-2 px-2 text-xs font-medium rounded-lg border transition text-center ${
                                        formData.feeType === 'PERCENTAGE'
                                            ? 'bg-emerald-50 dark:bg-emerald-950/40 border-emerald-500 text-emerald-700 dark:text-emerald-400 font-bold'
                                            : 'border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400'
                                    }`}
                                >
                                    Percentage (%)
                                </button>
                                <button
                                    type="button"
                                    onClick={() => {
                                        setFormData(prev => ({
                                            ...prev,
                                            feeType: 'TIERED',
                                            slabs: prev.slabs.length === 0 ? [
                                                { slabOrder: 1, fromAmount: '0.00', toAmount: '1000.00', feeType: 'FLAT', feeValue: '5.00', minFee: '', maxFee: '' },
                                                { slabOrder: 2, fromAmount: '1000.01', toAmount: '', feeType: 'FLAT', feeValue: '15.00', minFee: '', maxFee: '' }
                                            ] : prev.slabs
                                        }));
                                    }}
                                    className={`py-2 px-2 text-xs font-medium rounded-lg border transition text-center flex items-center justify-center gap-1 ${
                                        formData.feeType === 'TIERED'
                                            ? 'bg-purple-50 dark:bg-purple-950/40 border-purple-500 text-purple-700 dark:text-purple-400 font-bold shadow-sm'
                                            : 'border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400'
                                    }`}
                                >
                                    <Layers className="w-3.5 h-3.5" />
                                    Tiered Slabs
                                </button>
                            </div>
                        </div>
                    </div>

                    {/* Non-Tiered Flat/Percentage Controls */}
                    {formData.feeType !== 'TIERED' ? (
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 p-4 rounded-xl bg-slate-50/70 dark:bg-slate-800/40 border border-slate-200 dark:border-slate-700">
                            <div>
                                <label className="block text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider mb-1">
                                    {formData.feeType === 'PERCENTAGE' ? 'Fee Rate (%) *' : 'Flat Amount (ETB) *'}
                                </label>
                                <input
                                    type="number"
                                    step="0.0001"
                                    value={formData.feeValue}
                                    onChange={(e) => setFormData({ ...formData, feeValue: e.target.value })}
                                    placeholder={formData.feeType === 'PERCENTAGE' ? 'e.g. 0.25' : 'e.g. 10.00'}
                                    className="w-full px-3 py-2 text-sm bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                                />
                            </div>

                            <div>
                                <label className="block text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider mb-1">
                                    Min Fee Cap (ETB)
                                </label>
                                <input
                                    type="number"
                                    step="0.01"
                                    disabled={formData.feeType === 'FLAT'}
                                    value={formData.minFee}
                                    onChange={(e) => setFormData({ ...formData, minFee: e.target.value })}
                                    placeholder="e.g. 2.00"
                                    className="w-full px-3 py-2 text-sm bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:outline-none disabled:opacity-40"
                                />
                            </div>

                            <div>
                                <label className="block text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider mb-1">
                                    Max Fee Cap (ETB)
                                </label>
                                <input
                                    type="number"
                                    step="0.01"
                                    disabled={formData.feeType === 'FLAT'}
                                    value={formData.maxFee}
                                    onChange={(e) => setFormData({ ...formData, maxFee: e.target.value })}
                                    placeholder="e.g. 25.00"
                                    className="w-full px-3 py-2 text-sm bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:outline-none disabled:opacity-40"
                                />
                            </div>
                        </div>
                    ) : (
                        /* Tiered Slabs / Bracket Ladder Builder */
                        <div className="space-y-3 p-4 rounded-xl bg-purple-50/40 dark:bg-purple-950/20 border border-purple-200 dark:border-purple-800/60">
                            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-2 border-b border-purple-100 dark:border-purple-900/50">
                                <div>
                                    <div className="flex items-center gap-1.5 font-bold text-sm text-purple-900 dark:text-purple-200">
                                        <Layers className="w-4 h-4 text-purple-600 dark:text-purple-400" />
                                        Tiered Amount-Bracket Slabs (Volume Ladders)
                                    </div>
                                    <p className="text-xs text-purple-700/80 dark:text-purple-300/80 mt-0.5">
                                        Transactions match the bracket where amount falls between From and To. Leave 'To' empty for uncapped ceiling.
                                    </p>
                                </div>
                                <div className="flex items-center gap-2">
                                    <button
                                        type="button"
                                        onClick={handleLoadStandardLadder}
                                        className="px-2.5 py-1 text-xs font-semibold rounded-lg bg-purple-100 dark:bg-purple-900/60 text-purple-700 dark:text-purple-300 hover:bg-purple-200 transition flex items-center gap-1"
                                    >
                                        <Sparkles className="w-3 h-3" />
                                        Load 4-Tier Preset
                                    </button>
                                    <button
                                        type="button"
                                        onClick={handleAddSlab}
                                        className="px-2.5 py-1 text-xs font-semibold rounded-lg bg-emerald-600 text-white hover:bg-emerald-500 transition flex items-center gap-1 shadow-sm"
                                    >
                                        <Plus className="w-3 h-3" />
                                        Add Bracket
                                    </button>
                                </div>
                            </div>

                            {formData.slabs.length === 0 ? (
                                <div className="py-6 text-center text-xs text-slate-500 border border-dashed border-purple-200 dark:border-purple-800 rounded-lg bg-white/50 dark:bg-slate-900/50">
                                    No bracket slabs defined yet. Click "Load 4-Tier Preset" or "+ Add Bracket".
                                </div>
                            ) : (
                                <div className="space-y-2">
                                    {formData.slabs.map((slab, index) => (
                                        <div
                                            key={index}
                                            className="grid grid-cols-12 gap-2 items-center bg-white dark:bg-slate-800/90 border border-purple-200/70 dark:border-purple-800/40 p-2.5 rounded-lg text-xs"
                                        >
                                            <div className="col-span-1 text-center font-bold text-slate-400">
                                                #{index + 1}
                                            </div>
                                            <div className="col-span-3">
                                                <label className="text-[10px] uppercase font-bold text-slate-500 block mb-0.5">From (ETB)</label>
                                                <input
                                                    type="number"
                                                    step="0.01"
                                                    value={slab.fromAmount}
                                                    onChange={(e) => handleSlabChange(index, 'fromAmount', e.target.value)}
                                                    className="w-full px-2 py-1 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded focus:ring-1 focus:ring-purple-500 focus:outline-none"
                                                    placeholder="0.00"
                                                />
                                            </div>
                                            <div className="col-span-3">
                                                <label className="text-[10px] uppercase font-bold text-slate-500 block mb-0.5">To (ETB / +)</label>
                                                <input
                                                    type="number"
                                                    step="0.01"
                                                    value={slab.toAmount}
                                                    onChange={(e) => handleSlabChange(index, 'toAmount', e.target.value)}
                                                    className="w-full px-2 py-1 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded focus:ring-1 focus:ring-purple-500 focus:outline-none"
                                                    placeholder="Uncapped (+)"
                                                />
                                            </div>
                                            <div className="col-span-2">
                                                <label className="text-[10px] uppercase font-bold text-slate-500 block mb-0.5">Type</label>
                                                <select
                                                    value={slab.feeType}
                                                    onChange={(e) => handleSlabChange(index, 'feeType', e.target.value)}
                                                    className="w-full px-1.5 py-1 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded focus:ring-1 focus:ring-purple-500 focus:outline-none font-medium"
                                                >
                                                    <option value="FLAT">Flat</option>
                                                    <option value="PERCENTAGE">%</option>
                                                </select>
                                            </div>
                                            <div className="col-span-2">
                                                <label className="text-[10px] uppercase font-bold text-slate-500 block mb-0.5">
                                                    {slab.feeType === 'PERCENTAGE' ? 'Rate %' : 'Fee ETB'}
                                                </label>
                                                <input
                                                    type="number"
                                                    step="0.0001"
                                                    value={slab.feeValue}
                                                    onChange={(e) => handleSlabChange(index, 'feeValue', e.target.value)}
                                                    className="w-full px-2 py-1 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded focus:ring-1 focus:ring-purple-500 focus:outline-none font-bold text-purple-700 dark:text-purple-300"
                                                    placeholder="Fee"
                                                />
                                            </div>
                                            <div className="col-span-1 flex justify-center">
                                                <button
                                                    type="button"
                                                    onClick={() => handleRemoveSlab(index)}
                                                    className="p-1.5 text-slate-400 hover:text-rose-600 rounded transition"
                                                    title="Remove bracket"
                                                >
                                                    <Trash2 className="w-3.5 h-3.5" />
                                                </button>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    )}

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <label className="block text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider mb-1">
                                Revenue GL Code (Credit) *
                            </label>
                            <input
                                type="text"
                                value={formData.feeGlCode}
                                onChange={(e) => setFormData({ ...formData, feeGlCode: e.target.value })}
                                placeholder="4020 (Fee and Commission Income)"
                                className="w-full px-3 py-2 text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:outline-none font-mono"
                            />
                        </div>

                        <div>
                            <label className="block text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider mb-1">
                                Tariff Status
                            </label>
                            <label className="flex items-center gap-2 mt-2 cursor-pointer">
                                <input
                                    type="checkbox"
                                    checked={formData.isActive}
                                    onChange={(e) => setFormData({ ...formData, isActive: e.target.checked })}
                                    className="w-4 h-4 text-emerald-600 rounded focus:ring-emerald-500 border-slate-300"
                                />
                                <span className="text-sm font-medium text-slate-700 dark:text-slate-300">
                                    Active for live transactions
                                </span>
                            </label>
                        </div>
                    </div>

                    <div>
                        <label className="block text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider mb-1">
                            Policy Description & Notes
                        </label>
                        <textarea
                            rows={3}
                            value={formData.description}
                            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                            placeholder="Explain tariff purpose, board authorization ref, or tier guidelines..."
                            className="w-full px-3 py-2 text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                        />
                    </div>

                    <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-100 dark:border-slate-800">
                        <button
                            type="button"
                            onClick={onClose}
                            className="px-4 py-2 text-sm font-medium text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-lg transition"
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            disabled={isSubmitting}
                            className="px-5 py-2 text-sm font-medium text-white bg-emerald-600 hover:bg-emerald-500 rounded-lg shadow-sm hover:shadow transition flex items-center gap-2 disabled:opacity-50"
                        >
                            <Save className="w-4 h-4" />
                            {isSubmitting ? 'Saving...' : (tariff ? 'Save Changes' : 'Create Tariff')}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};
export default TariffFormModal;
