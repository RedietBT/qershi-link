import React, { useState } from 'react';
import { X, PlusCircle, CheckCircle, AlertTriangle, ShieldCheck, Sparkles, Sliders } from 'lucide-react';
import { makerCheckerApi } from '../api/makerCheckerApi';

const PRESET_TEMPLATES = [
  {
    label: '🎓 Student / Youth',
    desc: 'Lower balance cap & tight single transaction limits for youth',
    productCode: 'STU-01',
    productName: 'Student High-Yield Savings',
    category: 'YOUTH_STUDENT',
    minOperatingBalance: 50,
    maxBalanceLimit: 100000,
    singleWithdrawalLimit: 5000,
    dailyWithdrawalLimit: 15000,
    enableMakerChecker: true
  },
  {
    label: '👩 Women Empowerment',
    desc: 'Special micro-enterprise & savings account for women groups',
    productCode: 'WMN-01',
    productName: "Women's Empowerment Savings",
    category: 'WOMEN_SPECIAL',
    minOperatingBalance: 100,
    maxBalanceLimit: 1500000,
    singleWithdrawalLimit: 40000,
    dailyWithdrawalLimit: 100000,
    enableMakerChecker: true
  },
  {
    label: '🏢 General Member Savings',
    desc: 'Standard commercial savings with default supervisor thresholds',
    productCode: 'GEN-01',
    productName: 'General Member Voluntary Savings',
    category: 'SAVINGS',
    minOperatingBalance: 100,
    maxBalanceLimit: 3000000,
    singleWithdrawalLimit: 50000,
    dailyWithdrawalLimit: 200000,
    enableMakerChecker: true
  },
  {
    label: '🌾 Farmer / Cooperative',
    desc: 'Seasonal agricultural group account with higher harvest ceilings',
    productCode: 'AGR-01',
    productName: 'Agricultural Cooperative Account',
    category: 'COOPERATIVE',
    minOperatingBalance: 200,
    maxBalanceLimit: 5000000,
    singleWithdrawalLimit: 100000,
    dailyWithdrawalLimit: 300000,
    enableMakerChecker: true
  },
  {
    label: '💼 Staff / Payroll',
    desc: 'Institutional employee current account with elevated operational limits',
    productCode: 'STF-01',
    productName: 'Staff Operating Current Account',
    category: 'CURRENT',
    minOperatingBalance: 500,
    maxBalanceLimit: 10000000,
    singleWithdrawalLimit: 150000,
    dailyWithdrawalLimit: 500000,
    enableMakerChecker: true
  }
];

export const CreateProductRuleModal = ({ onClose, onSuccess, existingCodes = [] }) => {
  const [formData, setFormData] = useState({
    productCode: '',
    productName: '',
    category: 'SAVINGS',
    minOperatingBalance: 100,
    maxBalanceLimit: 1000000,
    singleWithdrawalLimit: 50000,
    dailyWithdrawalLimit: 150000,
    enableMakerChecker: true
  });

  const [selectedTemplate, setSelectedTemplate] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const applyTemplate = (tpl) => {
    setSelectedTemplate(tpl.label);
    setFormData({
      productCode: tpl.productCode,
      productName: tpl.productName,
      category: tpl.category,
      minOperatingBalance: tpl.minOperatingBalance,
      maxBalanceLimit: tpl.maxBalanceLimit,
      singleWithdrawalLimit: tpl.singleWithdrawalLimit,
      dailyWithdrawalLimit: tpl.dailyWithdrawalLimit,
      enableMakerChecker: tpl.enableMakerChecker
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.productCode.trim()) {
      setError('Please provide a unique product code (e.g. STU-01, 105).');
      return;
    }
    if (!formData.productName.trim()) {
      setError('Please provide a descriptive account product name.');
      return;
    }

    setIsSubmitting(true);
    setError(null);
    try {
      await makerCheckerApi.createProductRule(formData);
      onSuccess();
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to create product rule');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
      <div className="bdae-card p-6 max-w-2xl w-full rounded-3xl shadow-2xl border border-[var(--bdae-border)] space-y-5 max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-[var(--bdae-border)] pb-3">
          <div className="flex items-center space-x-2.5">
            <div className="w-9 h-9 rounded-xl bg-[#00CDDB]/10 text-[#00CDDB] flex items-center justify-center">
              <Sliders className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-sm font-bold text-[var(--bdae-text-primary)]">
                Create Account Type Risk & Transaction Rule
              </h2>
              <p className="text-[10px] text-[var(--bdae-text-secondary)]">
                Define distinct daily transaction limits, balance caps, and Four-Eyes overrides per account type.
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)] transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Quick Template Picker */}
        <div className="space-y-2">
          <label className="text-[10px] font-bold text-[var(--bdae-text-secondary)] uppercase flex items-center gap-1.5">
            <Sparkles className="w-3.5 h-3.5 text-[#00CDDB]" />
            Quick Presets / Templates (Click to Auto-fill)
          </label>
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
            {PRESET_TEMPLATES.map((tpl) => (
              <button
                key={tpl.productCode}
                type="button"
                onClick={() => applyTemplate(tpl)}
                className={`p-2.5 rounded-xl border text-left transition-all ${
                  selectedTemplate === tpl.label
                    ? 'border-[#00CDDB] bg-[#00CDDB]/10 text-[#00CDDB] shadow-sm'
                    : 'border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 hover:border-[var(--bdae-border-hover)] text-[var(--bdae-text-primary)]'
                }`}
              >
                <div className="text-[11px] font-bold">{tpl.label}</div>
                <div className="text-[9px] text-[var(--bdae-text-secondary)] truncate mt-0.5 font-mono">
                  Daily: {tpl.dailyWithdrawalLimit.toLocaleString()} ETB
                </div>
              </button>
            ))}
          </div>
        </div>

        {error && (
          <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-600 dark:text-red-400 text-xs font-bold flex items-center gap-2">
            <AlertTriangle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {/* Product Code */}
            <div className="space-y-1">
              <label className="text-[10px] font-bold text-[var(--bdae-text-secondary)] uppercase">
                Account Type / Product Code *
              </label>
              <input
                type="text"
                placeholder="e.g. STU-01 or 105"
                value={formData.productCode}
                onChange={(e) => setFormData({ ...formData, productCode: e.target.value.toUpperCase() })}
                className="w-full px-3 py-2 text-xs rounded-xl border border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 font-mono font-bold text-[var(--bdae-text-primary)] focus:border-[#00CDDB] outline-none"
                required
              />
            </div>

            {/* Category */}
            <div className="space-y-1">
              <label className="text-[10px] font-bold text-[var(--bdae-text-secondary)] uppercase">
                Category
              </label>
              <select
                value={formData.category}
                onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                className="w-full px-3 py-2 text-xs rounded-xl border border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 font-bold text-[var(--bdae-text-primary)] focus:border-[#00CDDB] outline-none"
              >
                <option value="SAVINGS">Savings Account</option>
                <option value="YOUTH_STUDENT">Youth & Student Account</option>
                <option value="WOMEN_SPECIAL">Women Empowerment Account</option>
                <option value="CURRENT">Current / Operating Account</option>
                <option value="TERM_DEPOSIT">Fixed Term Deposit</option>
                <option value="COOPERATIVE">Cooperative / Group Account</option>
              </select>
            </div>
          </div>

          {/* Product Name */}
          <div className="space-y-1">
            <label className="text-[10px] font-bold text-[var(--bdae-text-secondary)] uppercase">
              Account / Product Name *
            </label>
            <input
              type="text"
              placeholder="e.g. Student High-Yield Savings Account"
              value={formData.productName}
              onChange={(e) => setFormData({ ...formData, productName: e.target.value })}
              className="w-full px-3 py-2 text-xs rounded-xl border border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 font-bold text-[var(--bdae-text-primary)] focus:border-[#00CDDB] outline-none"
              required
            />
          </div>

          {/* Risk Limit Matrix */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
            {/* Min Operating Balance */}
            <div className="space-y-1">
              <label className="text-[10px] font-bold text-[var(--bdae-text-secondary)] uppercase">
                Min Operating Balance (ETB)
              </label>
              <input
                type="number"
                min="0"
                step="50"
                value={formData.minOperatingBalance}
                onChange={(e) =>
                  setFormData({ ...formData, minOperatingBalance: parseFloat(e.target.value) || 0 })
                }
                className="w-full px-3 py-2 text-xs rounded-xl border border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 font-mono font-bold text-[var(--bdae-text-primary)] focus:border-[#00CDDB] outline-none"
                required
              />
              <span className="text-[9px] text-[var(--bdae-text-secondary)]">Floor balance required to keep account active</span>
            </div>

            {/* Max Storing Limit */}
            <div className="space-y-1">
              <label className="text-[10px] font-bold text-[var(--bdae-text-secondary)] uppercase">
                Max Balance Storing Limit (ETB)
              </label>
              <input
                type="number"
                min="0"
                step="10000"
                value={formData.maxBalanceLimit}
                onChange={(e) =>
                  setFormData({ ...formData, maxBalanceLimit: parseFloat(e.target.value) || 0 })
                }
                className="w-full px-3 py-2 text-xs rounded-xl border border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 font-mono font-bold text-[var(--bdae-text-primary)] focus:border-[#00CDDB] outline-none"
                required
              />
              <span className="text-[9px] text-[var(--bdae-text-secondary)]">Ceiling balance allowed for this product</span>
            </div>

            {/* Single Withdrawal Supervisor Threshold */}
            <div className="space-y-1">
              <label className="text-[10px] font-bold text-[var(--bdae-text-secondary)] uppercase">
                Single Withdrawal Threshold (ETB)
              </label>
              <input
                type="number"
                min="0"
                step="1000"
                value={formData.singleWithdrawalLimit}
                onChange={(e) =>
                  setFormData({ ...formData, singleWithdrawalLimit: parseFloat(e.target.value) || 0 })
                }
                className="w-full px-3 py-2 text-xs rounded-xl border border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 font-mono font-bold text-[var(--bdae-text-primary)] focus:border-[#00CDDB] outline-none"
                required
              />
              <span className="text-[9px] text-[var(--bdae-text-secondary)]">Transactions exceeding this require Maker-Checker</span>
            </div>

            {/* Daily Cumulative Withdrawal Limit */}
            <div className="space-y-1">
              <label className="text-[10px] font-bold text-[var(--bdae-text-secondary)] uppercase">
                Daily Withdrawal Limit (ETB)
              </label>
              <input
                type="number"
                min="0"
                step="5000"
                value={formData.dailyWithdrawalLimit}
                onChange={(e) =>
                  setFormData({ ...formData, dailyWithdrawalLimit: parseFloat(e.target.value) || 0 })
                }
                className="w-full px-3 py-2 text-xs rounded-xl border border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 font-mono font-bold text-[var(--bdae-text-primary)] focus:border-[#00CDDB] outline-none"
                required
              />
              <span className="text-[9px] text-[var(--bdae-text-secondary)]">Max total withdrawals allowed within 24 hours</span>
            </div>
          </div>

          {/* Four-Eyes Toggle */}
          <div className="p-3 rounded-2xl border border-[var(--bdae-border)] bg-black/[0.02] dark:bg-white/[0.02] flex items-center justify-between">
            <div className="flex items-center space-x-2.5">
              <div className="w-8 h-8 rounded-xl bg-emerald-500/10 text-emerald-600 flex items-center justify-center">
                <ShieldCheck className="w-4 h-4" />
              </div>
              <div>
                <span className="text-xs font-bold text-[var(--bdae-text-primary)] block">
                  Enforce Four-Eyes / Maker-Checker For This Product
                </span>
                <span className="text-[10px] text-[var(--bdae-text-secondary)]">
                  When enabled, all openings and threshold-exceeding transactions mandate dual sign-off.
                </span>
              </div>
            </div>

            <button
              type="button"
              onClick={() => setFormData({ ...formData, enableMakerChecker: !formData.enableMakerChecker })}
              className={`w-11 h-6 flex items-center rounded-full p-1 transition-colors shrink-0 ${
                formData.enableMakerChecker ? 'bg-[#00CDDB]' : 'bg-gray-300 dark:bg-gray-700'
              }`}
            >
              <div
                className={`bg-white w-4 h-4 rounded-full shadow-md transform transition-transform ${
                  formData.enableMakerChecker ? 'translate-x-5' : 'translate-x-0'
                }`}
              />
            </button>
          </div>

          {/* Actions */}
          <div className="flex items-center justify-end space-x-2.5 pt-3 border-t border-[var(--bdae-border)]">
            <button
              type="button"
              onClick={onClose}
              disabled={isSubmitting}
              className="px-4 py-2 rounded-xl text-xs font-bold text-[var(--bdae-text-secondary)] hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="px-5 py-2 rounded-xl text-xs font-bold text-white bg-[#00CDDB] hover:bg-[#00b4c0] shadow-md hover:shadow-cyan-500/20 flex items-center gap-1.5 transition-all disabled:opacity-50"
            >
              <PlusCircle className="w-4 h-4" />
              <span>{isSubmitting ? 'Creating Rule...' : 'Save Product Rule'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
