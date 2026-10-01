import React, { useState, useEffect } from 'react';
import { X, Save, AlertCircle, HelpCircle } from 'lucide-react';

const TRANSACTION_TYPES = [
    { value: 'WITHDRAWAL', label: 'Cash Withdrawal (Over-the-Counter)' },
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
        description: ''
    });
    const [error, setError] = useState(null);

    useEffect(() => {
        if (tariff) {
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
                description: tariff.description || ''
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
                description: ''
            });
        }
        setError(null);
    }, [tariff, isOpen]);

    if (!isOpen) return null;

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
        if (isNaN(parseFloat(formData.feeValue)) || parseFloat(formData.feeValue) < 0) {
            setError('Fee value must be a valid positive number.');
            return;
        }

        onSave({
            ...formData,
            tariffCode: formData.tariffCode.trim().toUpperCase(),
            feeValue: parseFloat(formData.feeValue),
            minFee: formData.minFee ? parseFloat(formData.minFee) : null,
            maxFee: formData.maxFee ? parseFloat(formData.maxFee) : null
        });
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-in fade-in duration-200">
            <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl shadow-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
                <div className="flex items-center justify-between p-6 border-b border-slate-100 dark:border-slate-800">
                    <div>
                        <h2 className="text-xl font-bold text-slate-900 dark:text-white">
                            {tariff ? 'Edit Transaction Tariff' : 'Create New Transaction Tariff'}
                        </h2>
                        <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
                            Configure transaction fees, tariff caps, and Chart of Accounts revenue GL routing.
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
                                placeholder="e.g. TAR-WTH-01"
                                className="w-full px-3 py-2 text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:outline-none disabled:opacity-50"
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
                                Fee Assessment Type *
                            </label>
                            <div className="grid grid-cols-2 gap-2 mt-1">
                                <button
                                    type="button"
                                    onClick={() => setFormData({ ...formData, feeType: 'FLAT' })}
                                    className={`py-2 px-3 text-xs font-medium rounded-lg border transition ${
                                        formData.feeType === 'FLAT'
                                            ? 'bg-emerald-50 dark:bg-emerald-950/40 border-emerald-500 text-emerald-700 dark:text-emerald-400 font-bold'
                                            : 'border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400'
                                    }`}
                                >
                                    Fixed Flat Fee
                                </button>
                                <button
                                    type="button"
                                    onClick={() => setFormData({ ...formData, feeType: 'PERCENTAGE' })}
                                    className={`py-2 px-3 text-xs font-medium rounded-lg border transition ${
                                        formData.feeType === 'PERCENTAGE'
                                            ? 'bg-emerald-50 dark:bg-emerald-950/40 border-emerald-500 text-emerald-700 dark:text-emerald-400 font-bold'
                                            : 'border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400'
                                    }`}
                                >
                                    Percentage (%)
                                </button>
                            </div>
                        </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
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
                                className="w-full px-3 py-2 text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:outline-none"
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
                                className="w-full px-3 py-2 text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:outline-none disabled:opacity-40"
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
                                className="w-full px-3 py-2 text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:outline-none disabled:opacity-40"
                            />
                        </div>
                    </div>

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
                                className="w-full px-3 py-2 text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:outline-none"
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
