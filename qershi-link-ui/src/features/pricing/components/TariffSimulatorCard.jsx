import React, { useState, useEffect } from 'react';
import { Calculator, ArrowRight, CheckCircle2, AlertTriangle, ShieldCheck, Sparkles } from 'lucide-react';
import { pricingApi } from '../api/pricingApi';

const TX_TYPES = [
    { value: 'WITHDRAWAL', label: 'Cash Withdrawal (OTC)' },
    { value: 'TRANSFER_INTERNAL', label: 'Internal Transfer (Member-to-Member)' },
    { value: 'TRANSFER_EXTERNAL', label: 'External Transfer (Inter-Bank)' },
    { value: 'STATEMENT_PRINT', label: 'Physical Statement Print' },
    { value: 'LOAN_PROCESSING', label: 'Loan Processing / Appraisal' }
];

export const TariffSimulatorCard = ({ tariffs = [] }) => {
    const [selectedType, setSelectedType] = useState('WITHDRAWAL');
    const [amount, setAmount] = useState('5000');
    const [calculation, setCalculation] = useState(null);
    const [loading, setLoading] = useState(false);

    const performCalculation = async (type, amt) => {
        const numAmt = parseFloat(amt);
        if (isNaN(numAmt) || numAmt <= 0) {
            setCalculation(null);
            return;
        }

        try {
            setLoading(true);
            const res = await pricingApi.calculateFee(type, numAmt);
            if (res.data) {
                setCalculation(res.data);
                return;
            }
        } catch (err) {
            // Client-side fallback based on active tariffs passed in
            const matchedTariff = tariffs.find(t => t.transactionType === type && t.active);
            if (!matchedTariff) {
                setCalculation({
                    feeApplicable: false,
                    tariffCode: 'NONE',
                    tariffName: 'No active tariff configured',
                    transactionType: type,
                    feeType: 'NONE',
                    rateOrFlatValue: 0,
                    calculatedFee: 0,
                    feeGlCode: '4020',
                    totalDebitRequired: numAmt
                });
                return;
            }

            let fee = 0;
            if (matchedTariff.feeType === 'FLAT') {
                fee = parseFloat(matchedTariff.feeValue);
            } else if (matchedTariff.feeType === 'PERCENTAGE') {
                fee = (numAmt * parseFloat(matchedTariff.feeValue)) / 100;
                if (matchedTariff.minFee != null && fee < parseFloat(matchedTariff.minFee)) {
                    fee = parseFloat(matchedTariff.minFee);
                }
                if (matchedTariff.maxFee != null && fee > parseFloat(matchedTariff.maxFee)) {
                    fee = parseFloat(matchedTariff.maxFee);
                }
            }

            setCalculation({
                feeApplicable: fee > 0,
                tariffCode: matchedTariff.tariffCode,
                tariffName: matchedTariff.tariffName,
                transactionType: type,
                feeType: matchedTariff.feeType,
                rateOrFlatValue: matchedTariff.feeValue,
                calculatedFee: fee,
                feeGlCode: matchedTariff.feeGlCode || '4020',
                totalDebitRequired: numAmt + fee
            });
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        performCalculation(selectedType, amount);
    }, [selectedType, amount, tariffs]);

    const presets = [500, 2000, 5000, 20000, 100000];

    return (
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-sm">
            <div className="flex items-center gap-3 mb-6">
                <div className="p-2.5 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 rounded-xl text-emerald-600 dark:text-emerald-400">
                    <Calculator className="w-5 h-5" />
                </div>
                <div>
                    <h3 className="text-lg font-bold text-slate-900 dark:text-white flex items-center gap-2">
                        Live Real-Time Tariff Simulator
                        <span className="text-xs px-2 py-0.5 rounded-full bg-emerald-100 dark:bg-emerald-900/50 text-emerald-700 dark:text-emerald-300 font-medium">
                            Double-Entry GL
                        </span>
                    </h3>
                    <p className="text-xs text-slate-500 dark:text-slate-400">
                        Simulate member fee deductions, min/max caps enforcement, and GL routing before posting transactions.
                    </p>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
                {/* Left: Input controls */}
                <div className="lg:col-span-6 space-y-4">
                    <div>
                        <label className="block text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider mb-1.5">
                            Transaction Type
                        </label>
                        <select
                            value={selectedType}
                            onChange={(e) => setSelectedType(e.target.value)}
                            className="w-full px-3.5 py-2.5 text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:outline-none font-medium"
                        >
                            {TX_TYPES.map((t) => (
                                <option key={t.value} value={t.value}>
                                    {t.label}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div>
                        <label className="block text-xs font-semibold text-slate-600 dark:text-slate-400 uppercase tracking-wider mb-1.5">
                            Principal Transaction Amount (ETB)
                        </label>
                        <div className="relative">
                            <input
                                type="number"
                                min="1"
                                step="any"
                                value={amount}
                                onChange={(e) => setAmount(e.target.value)}
                                placeholder="Enter amount..."
                                className="w-full pl-3.5 pr-16 py-2.5 text-base font-semibold text-slate-900 dark:text-white bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                            />
                            <span className="absolute right-3.5 top-1/2 -translate-y-1/2 text-xs font-bold text-slate-400">
                                ETB
                            </span>
                        </div>
                    </div>

                    <div>
                        <label className="block text-xs text-slate-500 mb-1.5">Quick Presets</label>
                        <div className="flex flex-wrap gap-2">
                            {presets.map((val) => (
                                <button
                                    key={val}
                                    type="button"
                                    onClick={() => setAmount(String(val))}
                                    className={`px-3 py-1 text-xs font-medium rounded-lg border transition ${
                                        amount === String(val)
                                            ? 'bg-emerald-600 text-white border-emerald-600'
                                            : 'bg-slate-50 dark:bg-slate-800 border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400 hover:border-slate-300'
                                    }`}
                                >
                                    {val.toLocaleString()} ETB
                                </button>
                            ))}
                        </div>
                    </div>
                </div>

                {/* Right: Calculation Result Breakdown */}
                <div className="lg:col-span-6">
                    {calculation ? (
                        <div className="h-full bg-slate-50/70 dark:bg-slate-800/40 border border-slate-200 dark:border-slate-700/60 rounded-xl p-5 flex flex-col justify-between">
                            <div>
                                <div className="flex items-center justify-between pb-3 border-b border-slate-200/80 dark:border-slate-700/80">
                                    <div className="flex items-center gap-2">
                                        <Sparkles className="w-4 h-4 text-emerald-500" />
                                        <span className="text-xs font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400">
                                            Simulation Breakdown
                                        </span>
                                    </div>
                                    <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-100 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-300">
                                        {calculation.tariffCode || 'STANDARD'}
                                    </span>
                                </div>

                                <div className="space-y-3 py-4 text-sm">
                                    <div className="flex justify-between items-center text-slate-600 dark:text-slate-400">
                                        <span>Tariff Rule:</span>
                                        <span className="font-semibold text-slate-900 dark:text-white text-right">
                                            {calculation.tariffName}
                                        </span>
                                    </div>

                                    <div className="flex justify-between items-center text-slate-600 dark:text-slate-400">
                                        <span>Principal Amount:</span>
                                        <span className="font-medium text-slate-900 dark:text-white">
                                            {parseFloat(amount || 0).toLocaleString(undefined, { minimumFractionDigits: 2 })} ETB
                                        </span>
                                    </div>

                                    <div className="flex justify-between items-center text-slate-600 dark:text-slate-400">
                                        <span>Fee Assessment:</span>
                                        <span className="font-semibold text-emerald-600 dark:text-emerald-400">
                                            + {parseFloat(calculation.calculatedFee || 0).toFixed(2)} ETB
                                            <span className="text-xs text-slate-400 ml-1">
                                                ({calculation.feeType === 'PERCENTAGE' ? `${calculation.rateOrFlatValue}%` : 'Flat'})
                                            </span>
                                        </span>
                                    </div>

                                    <div className="flex justify-between items-center text-slate-600 dark:text-slate-400">
                                        <span>Fee Income GL:</span>
                                        <span className="font-mono text-xs px-2 py-0.5 bg-slate-200 dark:bg-slate-700 rounded text-slate-700 dark:text-slate-300">
                                            GL {calculation.feeGlCode || '4020'} (Fee Income)
                                        </span>
                                    </div>
                                </div>
                            </div>

                            <div className="pt-3 border-t border-slate-200/80 dark:border-slate-700/80 bg-white dark:bg-slate-800/80 p-3.5 rounded-lg border border-slate-200 dark:border-slate-700">
                                <div className="flex items-center justify-between">
                                    <div>
                                        <span className="text-xs font-semibold uppercase text-slate-500 tracking-wider">
                                            Total Member Debit
                                        </span>
                                        <div className="text-xl font-black text-slate-900 dark:text-white">
                                            {parseFloat(calculation.totalDebitRequired || 0).toLocaleString(undefined, { minimumFractionDigits: 2 })} ETB
                                        </div>
                                    </div>
                                    <div className="flex items-center gap-1.5 text-xs font-medium text-emerald-600 dark:text-emerald-400">
                                        <CheckCircle2 className="w-4 h-4" />
                                        <span>Balanced GL Entry</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    ) : (
                        <div className="h-full border border-dashed border-slate-200 dark:border-slate-700 rounded-xl p-8 flex flex-col items-center justify-center text-center">
                            <Calculator className="w-8 h-8 text-slate-300 dark:text-slate-600 mb-2" />
                            <p className="text-sm text-slate-400">
                                Enter a transaction amount to preview the live fee and General Ledger double-entry posting.
                            </p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};
