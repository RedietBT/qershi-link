import React, { useState } from 'react';
import { X, Save, RefreshCw, AlertTriangle, ShieldCheck, DollarSign } from 'lucide-react';
import { makerCheckerApi } from '../api/makerCheckerApi';

export const EditProductRuleModal = ({ productRule, onClose, onSuccess }) => {
  const [formData, setFormData] = useState({
    minOperatingBalance: productRule.minOperatingBalance ?? 100,
    maxBalanceLimit: productRule.maxBalanceLimit ?? 1000000,
    singleWithdrawalLimit: productRule.singleWithdrawalLimit ?? 50000,
    dailyWithdrawalLimit: productRule.dailyWithdrawalLimit ?? 150000,
    enableMakerChecker: productRule.enableMakerChecker ?? true
  });

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError(null);
    try {
      await makerCheckerApi.updateProductRule(productRule.productCode, formData);
      onSuccess();
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update product rule');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
      <div className="bdae-card p-6 max-w-lg w-full rounded-3xl shadow-2xl border border-[var(--bdae-border)] space-y-5">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-[var(--bdae-border)] pb-3">
          <div className="flex items-center space-x-2.5">
            <div className="w-8 h-8 rounded-xl bg-[#00CDDB]/10 text-[#00CDDB] flex items-center justify-center">
              <DollarSign className="w-4 h-4" />
            </div>
            <div>
              <h2 className="text-sm font-bold text-[var(--bdae-text-primary)]">
                Product Risk Rules: {productRule.productName}
              </h2>
              <span className="text-[10px] text-[var(--bdae-text-secondary)] font-mono">
                Code: {productRule.productCode} • Category: {productRule.category}
              </span>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {error && (
          <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-600 dark:text-red-400 text-xs font-bold flex items-center gap-2">
            <AlertTriangle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
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
            </div>

            {/* Single Withdrawal Threshold */}
            <div className="space-y-1">
              <label className="text-[10px] font-bold text-[var(--bdae-text-secondary)] uppercase">
                Single Withdrawal Checker Threshold
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
            </div>

            {/* Daily Withdrawal Limit */}
            <div className="space-y-1">
              <label className="text-[10px] font-bold text-[var(--bdae-text-secondary)] uppercase">
                Daily Cumulative Withdrawal Limit
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
            </div>
          </div>

          {/* Maker-Checker Active Toggle */}
          <div className="p-3.5 rounded-xl border border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 flex items-center justify-between">
            <div className="space-y-0.5">
              <span className="text-xs font-bold text-[var(--bdae-text-primary)] flex items-center gap-1.5">
                <ShieldCheck className="w-3.5 h-3.5 text-[#00CDDB]" />
                Enforce Four-Eyes Verification on this Product
              </span>
              <p className="text-[10px] text-[var(--bdae-text-secondary)]">
                Accounts of this product will require supervisor authorization for high-value withdrawals.
              </p>
            </div>
            <button
              type="button"
              onClick={() =>
                setFormData({ ...formData, enableMakerChecker: !formData.enableMakerChecker })
              }
              className={`w-11 h-6 flex items-center rounded-full p-1 transition-colors ${
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

          <div className="flex justify-end space-x-2 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs font-bold rounded-xl border border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="bdae-btn-primary px-5 py-2 text-xs font-bold rounded-xl flex items-center gap-1.5 shadow-md"
            >
              {isSubmitting ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Save className="w-4 h-4" />}
              <span>Save Product Rules</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
