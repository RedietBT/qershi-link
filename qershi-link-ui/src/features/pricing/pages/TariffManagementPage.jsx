import React, { useState, useEffect } from 'react';
import {
    Receipt,
    Plus,
    RefreshCw,
    Search,
    Filter,
    Edit2,
    ToggleLeft,
    ToggleRight,
    Calculator,
    ShieldCheck,
    Coins,
    Sliders,
    Layers,
    CheckCircle2,
    AlertCircle
} from 'lucide-react';
import { pricingApi, tariffApi } from '../api/pricingApi';
import { TariffFormModal } from '../components/TariffFormModal';
import { TariffSimulatorCard } from '../components/TariffSimulatorCard';
import { WithholdingTaxGovernanceCard } from '../components/WithholdingTaxGovernanceCard';

export const TariffManagementPage = () => {
    const [activeTab, setActiveTab] = useState('tariffs'); // 'tariffs' | 'simulator' | 'wht'
    const [tariffs, setTariffs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [typeFilter, setTypeFilter] = useState('ALL');
    const [statusFilter, setStatusFilter] = useState('ALL');

    // Modal state
    const [modalOpen, setModalOpen] = useState(false);
    const [editingTariff, setEditingTariff] = useState(null);
    const [submitting, setSubmitting] = useState(false);

    const loadTariffs = async () => {
        try {
            setLoading(true);
            setError(null);
            const res = await tariffApi.getAllTariffs();
            setTariffs(res.data || []);
        } catch (err) {
            console.error('Failed to load tariffs:', err);
            setError(err?.response?.data?.message || 'Failed to load tariff schedules.');
            // Fallback seed tariffs if backend connection is unavailable
            setTariffs([
                {
                    tariffId: '1',
                    tariffCode: 'TAR-WTH-01',
                    tariffName: 'Over-The-Counter Cash Withdrawal Fee',
                    transactionType: 'WITHDRAWAL',
                    feeType: 'FLAT',
                    feeValue: 10.00,
                    minFee: 10.00,
                    maxFee: 10.00,
                    feeGlCode: '4020',
                    active: true,
                    description: 'Standard flat service fee for teller cash withdrawals'
                },
                {
                    tariffId: '2',
                    tariffCode: 'TAR-TRF-INT',
                    tariffName: 'Internal Member-to-Member Transfer Fee',
                    transactionType: 'TRANSFER_INTERNAL',
                    feeType: 'PERCENTAGE',
                    feeValue: 0.25,
                    minFee: 2.00,
                    maxFee: 25.00,
                    feeGlCode: '4020',
                    active: true,
                    description: '0.25% fee on internal account transfers (min 2 ETB, max 25 ETB)'
                },
                {
                    tariffId: '3',
                    tariffCode: 'TAR-TRF-EXT',
                    tariffName: 'External Inter-Bank Transfer Fee',
                    transactionType: 'TRANSFER_EXTERNAL',
                    feeType: 'FLAT',
                    feeValue: 25.00,
                    minFee: 25.00,
                    maxFee: 25.00,
                    feeGlCode: '4020',
                    active: true,
                    description: 'RTGS / ACH external clearing commission'
                },
                {
                    tariffId: '4',
                    tariffCode: 'TAR-STMT-01',
                    tariffName: 'Physical Statement Print Fee',
                    transactionType: 'STATEMENT_PRINT',
                    feeType: 'FLAT',
                    feeValue: 15.00,
                    minFee: 15.00,
                    maxFee: 15.00,
                    feeGlCode: '4020',
                    active: true,
                    description: 'Paper statement printing surcharge'
                },
                {
                    tariffId: '5',
                    tariffCode: 'TAR-LOAN-APP',
                    tariffName: 'Loan Processing & Appraisal Fee',
                    transactionType: 'LOAN_PROCESSING',
                    feeType: 'PERCENTAGE',
                    feeValue: 1.00,
                    minFee: 100.00,
                    maxFee: 5000.00,
                    feeGlCode: '4021',
                    active: true,
                    description: '1.0% origination appraisal charge on approved loans'
                }
            ]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadTariffs();
    }, []);

    const handleCreateOrUpdate = async (formData) => {
        try {
            setSubmitting(true);
            if (editingTariff) {
                await tariffApi.updateTariff(editingTariff.tariffId, formData);
            } else {
                await tariffApi.createTariff(formData);
            }
            setModalOpen(false);
            setEditingTariff(null);
            loadTariffs();
        } catch (err) {
            alert(err?.response?.data?.message || 'Failed saving tariff configuration.');
        } finally {
            setSubmitting(false);
        }
    };

    const handleToggleStatus = async (tariff) => {
        try {
            const newActive = !tariff.active;
            await tariffApi.toggleTariff(tariff.tariffId, newActive);
            setTariffs(prev => prev.map(t => t.tariffId === tariff.tariffId ? { ...t, active: newActive } : t));
        } catch (err) {
            alert(err?.response?.data?.message || 'Failed updating tariff active status.');
        }
    };

    const filteredTariffs = tariffs.filter(t => {
        const matchesSearch = (t.tariffCode || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
            (t.tariffName || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
            (t.description || '').toLowerCase().includes(searchQuery.toLowerCase());

        const matchesType = typeFilter === 'ALL' || t.transactionType === typeFilter;
        const matchesStatus = statusFilter === 'ALL' ||
            (statusFilter === 'ACTIVE' && t.active) ||
            (statusFilter === 'INACTIVE' && !t.active);

        return matchesSearch && matchesType && matchesStatus;
    });

    const activeCount = tariffs.filter(t => t.active).length;

    return (
        <div className="p-8 space-y-6 max-w-7xl mx-auto">
            {/* Page Header */}
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                    <div className="flex items-center gap-2.5">
                        <div className="p-2 bg-emerald-50 dark:bg-emerald-950/50 rounded-xl text-emerald-600 dark:text-emerald-400 border border-emerald-200 dark:border-emerald-800">
                            <Receipt className="w-6 h-6" />
                        </div>
                        <div>
                            <h1 className="text-2xl font-black tracking-tight text-slate-900 dark:text-white">
                                Fee & Tariff Engine & 5% WHT
                            </h1>
                            <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                                Manage transaction fee schedules, real-time tariff calculation, and statutory 5% withholding tax governance.
                            </p>
                        </div>
                    </div>
                </div>

                <div className="flex items-center gap-3">
                    <button
                        onClick={loadTariffs}
                        className="p-2.5 text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl hover:bg-slate-50 transition"
                        title="Reload Tariffs"
                    >
                        <RefreshCw className="w-4 h-4" />
                    </button>
                    <button
                        onClick={() => {
                            setEditingTariff(null);
                            setModalOpen(true);
                        }}
                        className="px-4 py-2.5 bg-emerald-600 hover:bg-emerald-500 text-white text-sm font-semibold rounded-xl shadow-sm hover:shadow flex items-center gap-2 transition"
                    >
                        <Plus className="w-4 h-4" />
                        New Tariff Rule
                    </button>
                </div>
            </div>

            {/* Quick Metrics Deck */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-4 shadow-sm flex items-center gap-4">
                    <div className="p-3 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 rounded-xl text-emerald-600 dark:text-emerald-400">
                        <Layers className="w-5 h-5" />
                    </div>
                    <div>
                        <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Active Tariffs</span>
                        <div className="text-xl font-black text-slate-900 dark:text-white mt-0.5">
                            {activeCount} <span className="text-xs font-normal text-slate-400">/ {tariffs.length} total</span>
                        </div>
                    </div>
                </div>

                <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-4 shadow-sm flex items-center gap-4">
                    <div className="p-3 bg-blue-50 dark:bg-blue-950/40 border border-blue-200 dark:border-blue-800 rounded-xl text-blue-600 dark:text-blue-400">
                        <Coins className="w-5 h-5" />
                    </div>
                    <div>
                        <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Revenue GL Code</span>
                        <div className="text-xl font-black text-slate-900 dark:text-white mt-0.5 font-mono">
                            4020 <span className="text-xs font-normal text-slate-400">Fee Income</span>
                        </div>
                    </div>
                </div>

                <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-4 shadow-sm flex items-center gap-4">
                    <div className="p-3 bg-amber-50 dark:bg-amber-950/40 border border-amber-200 dark:border-amber-800 rounded-xl text-amber-600 dark:text-amber-400">
                        <ShieldCheck className="w-5 h-5" />
                    </div>
                    <div>
                        <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Statutory WHT Rate</span>
                        <div className="text-xl font-black text-amber-600 dark:text-amber-400 mt-0.5">
                            5.00% <span className="text-xs font-normal text-slate-400">(At Source)</span>
                        </div>
                    </div>
                </div>

                <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-4 shadow-sm flex items-center gap-4">
                    <div className="p-3 bg-purple-50 dark:bg-purple-950/40 border border-purple-200 dark:border-purple-800 rounded-xl text-purple-600 dark:text-purple-400">
                        <Sliders className="w-5 h-5" />
                    </div>
                    <div>
                        <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">WHT Tax Payable GL</span>
                        <div className="text-xl font-black text-slate-900 dark:text-white mt-0.5 font-mono">
                            2091 <span className="text-xs font-normal text-slate-400">Government WHT</span>
                        </div>
                    </div>
                </div>
            </div>

            {/* Navigation Tabs */}
            <div className="flex border-b border-slate-200 dark:border-slate-800 space-x-6">
                <button
                    onClick={() => setActiveTab('tariffs')}
                    className={`pb-3 text-sm font-bold flex items-center gap-2 transition relative ${
                        activeTab === 'tariffs'
                            ? 'text-emerald-600 dark:text-emerald-400'
                            : 'text-slate-500 hover:text-slate-800 dark:hover:text-slate-200'
                    }`}
                >
                    <Layers className="w-4 h-4" />
                    Configured Tariffs & Rules
                    {activeTab === 'tariffs' && (
                        <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-emerald-500 rounded-t" />
                    )}
                </button>

                <button
                    onClick={() => setActiveTab('simulator')}
                    className={`pb-3 text-sm font-bold flex items-center gap-2 transition relative ${
                        activeTab === 'simulator'
                            ? 'text-emerald-600 dark:text-emerald-400'
                            : 'text-slate-500 hover:text-slate-800 dark:hover:text-slate-200'
                    }`}
                >
                    <Calculator className="w-4 h-4" />
                    Live Tariff Simulator
                    {activeTab === 'simulator' && (
                        <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-emerald-500 rounded-t" />
                    )}
                </button>

                <button
                    onClick={() => setActiveTab('wht')}
                    className={`pb-3 text-sm font-bold flex items-center gap-2 transition relative ${
                        activeTab === 'wht'
                            ? 'text-emerald-600 dark:text-emerald-400'
                            : 'text-slate-500 hover:text-slate-800 dark:hover:text-slate-200'
                    }`}
                >
                    <ShieldCheck className="w-4 h-4" />
                    5% Withholding Tax (WHT) Governance
                    <span className="px-1.5 py-0.5 text-[10px] font-extrabold rounded-full bg-amber-100 dark:bg-amber-950 text-amber-700 dark:text-amber-300">
                        Statutory
                    </span>
                    {activeTab === 'wht' && (
                        <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-emerald-500 rounded-t" />
                    )}
                </button>
            </div>

            {/* Tab 1: Configured Tariffs */}
            {activeTab === 'tariffs' && (
                <div className="space-y-4">
                    {/* Search & Filter Header */}
                    <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-4 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
                        <div className="relative flex-1 max-w-md">
                            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                            <input
                                type="text"
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                                placeholder="Search tariff by code, name, or description..."
                                className="w-full pl-9 pr-4 py-2 text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                            />
                        </div>

                        <div className="flex flex-wrap items-center gap-3">
                            <select
                                value={typeFilter}
                                onChange={(e) => setTypeFilter(e.target.value)}
                                className="px-3 py-2 text-xs font-medium bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg focus:outline-none"
                            >
                                <option value="ALL">All Transaction Types</option>
                                <option value="WITHDRAWAL">Cash Withdrawal</option>
                                <option value="TRANSFER_INTERNAL">Internal Transfer</option>
                                <option value="TRANSFER_EXTERNAL">External Transfer</option>
                                <option value="STATEMENT_PRINT">Statement Print</option>
                                <option value="LOAN_PROCESSING">Loan Processing</option>
                            </select>

                            <select
                                value={statusFilter}
                                onChange={(e) => setStatusFilter(e.target.value)}
                                className="px-3 py-2 text-xs font-medium bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg focus:outline-none"
                            >
                                <option value="ALL">All Statuses</option>
                                <option value="ACTIVE">Active Only</option>
                                <option value="INACTIVE">Inactive Only</option>
                            </select>
                        </div>
                    </div>

                    {/* Tariffs Table */}
                    <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl shadow-sm overflow-hidden">
                        <div className="overflow-x-auto">
                            <table className="w-full text-left border-collapse">
                                <thead>
                                    <tr className="bg-slate-50 dark:bg-slate-800/50 border-b border-slate-200 dark:border-slate-800 text-[11px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400">
                                        <th className="py-3 px-4">Tariff Code</th>
                                        <th className="py-3 px-4">Name & Description</th>
                                        <th className="py-3 px-4">Transaction Type</th>
                                        <th className="py-3 px-4">Fee Structure</th>
                                        <th className="py-3 px-4">Min / Max Caps</th>
                                        <th className="py-3 px-4">Revenue GL</th>
                                        <th className="py-3 px-4 text-center">Active</th>
                                        <th className="py-3 px-4 text-right">Actions</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-slate-100 dark:divide-slate-800 text-xs">
                                    {filteredTariffs.length === 0 ? (
                                        <tr>
                                            <td colSpan="8" className="py-12 text-center text-slate-400">
                                                No tariffs found matching your search and filter criteria.
                                            </td>
                                        </tr>
                                    ) : (
                                        filteredTariffs.map((t) => (
                                            <tr key={t.tariffId || t.tariffCode} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/30 transition">
                                                <td className="py-3.5 px-4 font-mono font-bold text-slate-900 dark:text-white">
                                                    {t.tariffCode}
                                                </td>
                                                <td className="py-3.5 px-4">
                                                    <div className="font-semibold text-slate-900 dark:text-white">
                                                        {t.tariffName}
                                                    </div>
                                                    <div className="text-[11px] text-slate-500 dark:text-slate-400 truncate max-w-xs">
                                                        {t.description || '—'}
                                                    </div>
                                                </td>
                                                <td className="py-3.5 px-4">
                                                    <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300">
                                                        {t.transactionType}
                                                    </span>
                                                </td>
                                                <td className="py-3.5 px-4">
                                                    <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                                                        t.feeType === 'FLAT'
                                                            ? 'bg-blue-100 dark:bg-blue-950 text-blue-700 dark:text-blue-300'
                                                            : 'bg-emerald-100 dark:bg-emerald-950 text-emerald-700 dark:text-emerald-300'
                                                    }`}>
                                                        {t.feeType === 'FLAT' ? `${parseFloat(t.feeValue).toFixed(2)} ETB (Flat)` : `${t.feeValue}% (Percentage)`}
                                                    </span>
                                                </td>
                                                <td className="py-3.5 px-4 text-slate-600 dark:text-slate-400 font-mono">
                                                    {t.feeType === 'PERCENTAGE'
                                                        ? `${t.minFee != null ? t.minFee + ' ETB' : '—'} / ${t.maxFee != null ? t.maxFee + ' ETB' : '—'}`
                                                        : 'N/A (Flat)'}
                                                </td>
                                                <td className="py-3.5 px-4">
                                                    <span className="font-mono text-[11px] px-2 py-0.5 rounded bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300">
                                                        GL {t.feeGlCode || '4020'}
                                                    </span>
                                                </td>
                                                <td className="py-3.5 px-4 text-center">
                                                    <button
                                                        onClick={() => handleToggleStatus(t)}
                                                        className="inline-flex items-center transition"
                                                        title={t.active ? 'Click to disable' : 'Click to enable'}
                                                    >
                                                        {t.active ? (
                                                            <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-100 dark:bg-emerald-950 text-emerald-700 dark:text-emerald-300 flex items-center gap-1">
                                                                <CheckCircle2 className="w-3 h-3" />
                                                                Active
                                                            </span>
                                                        ) : (
                                                            <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-slate-100 dark:bg-slate-800 text-slate-500">
                                                                Inactive
                                                            </span>
                                                        )}
                                                    </button>
                                                </td>
                                                <td className="py-3.5 px-4 text-right">
                                                    <button
                                                        onClick={() => {
                                                            setEditingTariff(t);
                                                            setModalOpen(true);
                                                        }}
                                                        className="p-1.5 text-slate-400 hover:text-emerald-600 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 transition"
                                                        title="Edit Tariff"
                                                    >
                                                        <Edit2 className="w-4 h-4" />
                                                    </button>
                                                </td>
                                            </tr>
                                        ))
                                    )}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            )}

            {/* Tab 2: Live Simulator */}
            {activeTab === 'simulator' && (
                <TariffSimulatorCard tariffs={tariffs} />
            )}

            {/* Tab 3: Statutory 5% WHT */}
            {activeTab === 'wht' && (
                <WithholdingTaxGovernanceCard />
            )}

            {/* Add / Edit Modal */}
            <TariffFormModal
                isOpen={modalOpen}
                onClose={() => {
                    setModalOpen(false);
                    setEditingTariff(null);
                }}
                onSave={handleCreateOrUpdate}
                tariff={editingTariff}
                isSubmitting={submitting}
            />
        </div>
    );
};
export default TariffManagementPage;
