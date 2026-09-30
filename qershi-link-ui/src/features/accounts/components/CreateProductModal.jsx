import React, { useState } from 'react';
import { PackagePlus, X, AlertCircle, Loader2, Check } from 'lucide-react';
import { depositProductApi } from '../api/depositProductApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';

const CATEGORIES = ['SAVINGS', 'FIXED_DEPOSIT', 'CURRENT', 'SHARES', 'RECURRING'];
const FREQUENCIES = ['DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'ANNUALLY'];

export const CreateProductModal = ({ isOpen, onClose, onCreated }) => {
  const [form, setForm] = useState({
    productName: '',
    category: 'SAVINGS',
    currency: 'ETB',
    interestRatePa: '0',
    postingFrequency: 'MONTHLY',
    minOperatingBalance: '0',
    minMonthlyContribution: '0',
    termPeriodMonths: '',
    earlyWithdrawalPenaltyPct: '0'
  });
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState(null);

  if (!isOpen) return null;

  const update = (field, val) => setForm((f) => ({ ...f, [field]: val }));

  const handleSubmit = async (e) => {
    if (e) e.preventDefault();
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
        termPeriodMonths: form.termPeriodMonths ? parseInt(form.termPeriodMonths) : null
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
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 animate-fadeIn">
      <div className="bdae-card w-full max-w-2xl rounded-2xl border border-[var(--bdae-border)] shadow-2xl overflow-hidden">
        {/* Header */}
        <div
          className="p-5 flex items-center justify-between text-white"
          style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
        >
          <div className="flex items-center gap-3">
            <PackagePlus className="w-5 h-5" />
            <div>
              <h2 className="text-sm font-extrabold">New Deposit Product</h2>
              <p className="text-[10px] opacity-80">
                Configure product rules & auto-assign a 3-digit product code
              </p>
            </div>
          </div>
          <button onClick={onClose} className="text-white/70 hover:text-white transition-colors">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Body */}
        <form onSubmit={handleSubmit} className="p-6 grid grid-cols-1 md:grid-cols-2 gap-4 max-h-[70vh] overflow-y-auto text-xs">
          {error && (
            <div className="md:col-span-2 flex items-center gap-2 p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 font-bold">
              <AlertCircle className="w-4 h-4 shrink-0" /> {error}
            </div>
          )}

          <div className="space-y-1.5 md:col-span-2">
            <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">
              Product Name *
            </label>
            <input
              type="text"
              required
              value={form.productName}
              onChange={(e) => update('productName', e.target.value)}
              placeholder="e.g. General Member Savings"
              className="bdae-input font-bold"
            />
          </div>

          <div className="space-y-1.5">
            <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">
              Category *
            </label>
            <select
              value={form.category}
              onChange={(e) => update('category', e.target.value)}
              className="bdae-input font-bold"
            >
              {CATEGORIES.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </div>

          <div className="space-y-1.5">
            <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">
              Currency
            </label>
            <input
              type="text"
              disabled
              value={form.currency}
              className="bdae-input font-mono opacity-60"
            />
          </div>

          <div className="space-y-1.5">
            <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">
              Interest Rate (% p.a.)
            </label>
            <input
              type="number"
              step="0.05"
              min="0"
              value={form.interestRatePa}
              onChange={(e) => update('interestRatePa', e.target.value)}
              className="bdae-input font-mono font-bold"
            />
          </div>

          <div className="space-y-1.5">
            <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">
              Interest Posting Frequency
            </label>
            <select
              value={form.postingFrequency}
              onChange={(e) => update('postingFrequency', e.target.value)}
              className="bdae-input font-bold"
            >
              {FREQUENCIES.map((f) => (
                <option key={f} value={f}>
                  {f}
                </option>
              ))}
            </select>
          </div>

          <div className="space-y-1.5">
            <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">
              Min Operating Balance (ETB)
            </label>
            <input
              type="number"
              min="0"
              value={form.minOperatingBalance}
              onChange={(e) => update('minOperatingBalance', e.target.value)}
              className="bdae-input font-mono"
            />
          </div>

          <div className="space-y-1.5">
            <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">
              Min Monthly Contribution (ETB)
            </label>
            <input
              type="number"
              min="0"
              value={form.minMonthlyContribution}
              onChange={(e) => update('minMonthlyContribution', e.target.value)}
              className="bdae-input font-mono"
            />
          </div>

          {form.category === 'FIXED_DEPOSIT' && (
            <div className="space-y-1.5">
              <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">
                Term Period (Months)
              </label>
              <input
                type="number"
                min="1"
                value={form.termPeriodMonths}
                onChange={(e) => update('termPeriodMonths', e.target.value)}
                placeholder="e.g. 12"
                className="bdae-input font-mono"
              />
            </div>
          )}

          <div className="space-y-1.5">
            <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">
              Early Withdrawal Penalty (%)
            </label>
            <input
              type="number"
              step="0.1"
              min="0"
              value={form.earlyWithdrawalPenaltyPct}
              onChange={(e) => update('earlyWithdrawalPenaltyPct', e.target.value)}
              className="bdae-input font-mono"
            />
          </div>

          {/* Footer with Permission Guard */}
          <div className="md:col-span-2 pt-4 border-t border-[var(--bdae-border)] flex justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-xl border border-[var(--bdae-border)] font-bold text-[var(--bdae-text-secondary)] hover:bg-black/5 dark:hover:bg-white/5"
            >
              Cancel
            </button>

            <PermissionGuard
              permissions={[PERMISSIONS.PRODUCT_CREATE]}
              fallback={
                <span className="text-xs text-red-500 font-bold self-center">
                  Product Creation Restricted
                </span>
              }
            >
              <button
                type="submit"
                disabled={isSaving}
                className="px-5 py-2 rounded-xl font-bold text-white flex items-center gap-2 shadow-md transition-all hover:opacity-90 disabled:opacity-50"
                style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
              >
                {isSaving ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <Check className="w-3.5 h-3.5" />}
                <span>Create Product</span>
              </button>
            </PermissionGuard>
          </div>
        </form>
      </div>
    </div>
  );
};
