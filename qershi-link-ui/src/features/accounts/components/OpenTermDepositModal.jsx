import React, { useState } from 'react';
import { X, Landmark, Info, ToggleLeft, ToggleRight } from 'lucide-react';
import { accountHttpClient } from '../../../common/api/httpClient';
import { useAuthStore } from '../../../common/store/useAuthStore';
import { formatCurrency } from '../../../common/utils/currency';

const TENOR_OPTIONS = [
  { months: 1,  label: '1 Month',  rate: '5.50' },
  { months: 3,  label: '3 Months', rate: '6.50' },
  { months: 6,  label: '6 Months', rate: '8.00' },
  { months: 12, label: '12 Months', rate: '9.50' },
  { months: 24, label: '24 Months', rate: '10.50' },
  { months: 36, label: '36 Months', rate: '11.00' },
];

export const OpenTermDepositModal = ({ onClose, onSuccess }) => {
  const user = useAuthStore((s) => s.user);

  const [form, setForm] = useState({
    accountNo:           '',
    principalAmount:     '',
    tenorMonths:         12,
    agreedInterestRatePa: '9.50',
    earlyBreakPenaltyPct: '2.00',
    autoRollover:        false,
    rolloverTenorMonths: '',
    makerNotes:          '',
  });

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const handleTenorSelect = (opt) => {
    setForm(f => ({
      ...f,
      tenorMonths: opt.months,
      agreedInterestRatePa: opt.rate,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError(null);
    try {
      const payload = {
        accountNo:            form.accountNo,
        principalAmount:      parseFloat(form.principalAmount),
        tenorMonths:          parseInt(form.tenorMonths),
        agreedInterestRatePa: parseFloat(form.agreedInterestRatePa),
        earlyBreakPenaltyPct: parseFloat(form.earlyBreakPenaltyPct || '2'),
        autoRollover:         form.autoRollover,
        rolloverTenorMonths:  form.rolloverTenorMonths ? parseInt(form.rolloverTenorMonths) : null,
        makerNotes:           form.makerNotes,
      };
      const res = await accountHttpClient.post('/term-deposits/open', payload);
      const data = res.data;
      onSuccess(
        `FD contract ${data.contractNo} opened. Matures: ${data.maturityDate}. GL Ref: ${data.openingGlRef}.`
      );
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to open term deposit. Check inputs and try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  // Estimated maturity interest
  const estimatedInterest = () => {
    const p = parseFloat(form.principalAmount) || 0;
    const r = parseFloat(form.agreedInterestRatePa) || 0;
    const t = parseInt(form.tenorMonths) || 0;
    return (p * r / 100 * (t / 12)).toFixed(2);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
      <div className="bg-[var(--bdae-bg-primary)] rounded-2xl border border-[var(--bdae-border)] w-full max-w-lg shadow-2xl animate-fadeIn overflow-y-auto max-h-[90vh]">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-5 border-b border-[var(--bdae-border)]">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-violet-500/10 border border-violet-500/20">
              <Landmark className="w-5 h-5 text-violet-500" />
            </div>
            <div>
              <h2 className="text-sm font-bold text-[var(--bdae-text-primary)]">Open Fixed Term Deposit</h2>
              <p className="text-[11px] text-[var(--bdae-text-secondary)]">Lock funds at preferential FD rate</p>
            </div>
          </div>
          <button onClick={onClose} className="p-1.5 rounded-lg hover:bg-black/10 dark:hover:bg-white/10 text-[var(--bdae-text-secondary)] transition-all">
            <X className="w-4 h-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-5">
          {/* Error */}
          {error && (
            <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 text-xs">
              {error}
            </div>
          )}

          {/* Source Account */}
          <div>
            <label className="block text-xs font-semibold text-[var(--bdae-text-secondary)] mb-1.5">
              Source Savings Account No. *
            </label>
            <input
              type="text"
              required
              value={form.accountNo}
              onChange={e => setForm(f => ({ ...f, accountNo: e.target.value }))}
              placeholder="e.g. SAV-00012345"
              className="w-full px-3 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-bg-secondary)] text-sm text-[var(--bdae-text-primary)] focus:outline-none focus:ring-2 focus:ring-violet-500/30"
            />
          </div>

          {/* Principal */}
          <div>
            <label className="block text-xs font-semibold text-[var(--bdae-text-secondary)] mb-1.5">
              Principal Amount (ETB) *
            </label>
            <input
              type="number"
              required
              min="100"
              step="0.01"
              value={form.principalAmount}
              onChange={e => setForm(f => ({ ...f, principalAmount: e.target.value }))}
              placeholder="Minimum 100.00 ETB"
              className="w-full px-3 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-bg-secondary)] text-sm text-[var(--bdae-text-primary)] focus:outline-none focus:ring-2 focus:ring-violet-500/30"
            />
          </div>

          {/* Tenor Selection */}
          <div>
            <label className="block text-xs font-semibold text-[var(--bdae-text-secondary)] mb-2">
              Tenor &amp; Rate *
            </label>
            <div className="grid grid-cols-3 gap-2">
              {TENOR_OPTIONS.map(opt => (
                <button
                  key={opt.months}
                  type="button"
                  onClick={() => handleTenorSelect(opt)}
                  className={`rounded-xl border p-2.5 text-center transition-all ${
                    form.tenorMonths === opt.months
                      ? 'bg-violet-600 border-violet-600 text-white shadow-lg'
                      : 'border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] hover:border-violet-400'
                  }`}
                >
                  <div className="text-xs font-bold">{opt.label}</div>
                  <div className={`text-[11px] mt-0.5 ${form.tenorMonths === opt.months ? 'text-violet-200' : 'text-emerald-500'}`}>
                    {opt.rate}% p.a.
                  </div>
                </button>
              ))}
            </div>
          </div>

          {/* Interest Rate (manual override) */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-[var(--bdae-text-secondary)] mb-1.5">
                Agreed Rate (% p.a.)
              </label>
              <input
                type="number"
                step="0.01"
                min="0.01"
                max="50"
                value={form.agreedInterestRatePa}
                onChange={e => setForm(f => ({ ...f, agreedInterestRatePa: e.target.value }))}
                className="w-full px-3 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-bg-secondary)] text-sm text-[var(--bdae-text-primary)] focus:outline-none focus:ring-2 focus:ring-violet-500/30"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-[var(--bdae-text-secondary)] mb-1.5">
                Early Break Penalty (%)
              </label>
              <input
                type="number"
                step="0.01"
                min="0"
                max="20"
                value={form.earlyBreakPenaltyPct}
                onChange={e => setForm(f => ({ ...f, earlyBreakPenaltyPct: e.target.value }))}
                className="w-full px-3 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-bg-secondary)] text-sm text-[var(--bdae-text-primary)] focus:outline-none focus:ring-2 focus:ring-violet-500/30"
              />
            </div>
          </div>

          {/* Auto Rollover toggle */}
          <div className="flex items-center justify-between rounded-xl border border-[var(--bdae-border)] px-4 py-3">
            <div>
              <p className="text-xs font-semibold text-[var(--bdae-text-primary)]">Auto-Rollover at Maturity</p>
              <p className="text-[11px] text-[var(--bdae-text-secondary)]">Principal re-invested automatically</p>
            </div>
            <button
              type="button"
              onClick={() => setForm(f => ({ ...f, autoRollover: !f.autoRollover }))}
              className="transition-all"
            >
              {form.autoRollover
                ? <ToggleRight className="w-8 h-8 text-violet-500" />
                : <ToggleLeft className="w-8 h-8 text-[var(--bdae-text-secondary)]" />}
            </button>
          </div>

          {/* Notes */}
          <div>
            <label className="block text-xs font-semibold text-[var(--bdae-text-secondary)] mb-1.5">
              Maker Notes (Optional)
            </label>
            <textarea
              rows={2}
              value={form.makerNotes}
              onChange={e => setForm(f => ({ ...f, makerNotes: e.target.value }))}
              placeholder="Reason for FD contract..."
              className="w-full px-3 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-bg-secondary)] text-sm text-[var(--bdae-text-primary)] focus:outline-none focus:ring-2 focus:ring-violet-500/30 resize-none"
            />
          </div>

          {/* Projected Returns */}
          {form.principalAmount && (
            <div className="rounded-xl bg-violet-500/5 border border-violet-500/15 px-4 py-3 text-xs space-y-1.5">
              <div className="flex items-center gap-1.5 text-violet-600 dark:text-violet-400 font-semibold mb-1">
                <Info className="w-3.5 h-3.5" />
                <span>Projected Returns at Maturity</span>
              </div>
              <div className="flex justify-between text-[var(--bdae-text-secondary)]">
                <span>Principal:</span>
                <span className="font-mono font-bold text-[var(--bdae-text-primary)]">{formatCurrency(form.principalAmount)} ETB</span>
              </div>
              <div className="flex justify-between text-[var(--bdae-text-secondary)]">
                <span>Estimated Interest ({form.tenorMonths}M @ {form.agreedInterestRatePa}%):</span>
                <span className="font-mono font-bold text-emerald-500">+{formatCurrency(estimatedInterest())} ETB</span>
              </div>
              <div className="flex justify-between font-bold border-t border-violet-500/20 pt-1.5">
                <span>Total Maturity Value:</span>
                <span className="font-mono text-violet-600 dark:text-violet-400">
                  {formatCurrency(parseFloat(form.principalAmount || 0) + parseFloat(estimatedInterest()))} ETB
                </span>
              </div>
            </div>
          )}

          {/* GL Entry Info */}
          <div className="rounded-xl bg-[var(--bdae-bg-secondary)] border border-[var(--bdae-border)] px-4 py-3 text-xs text-[var(--bdae-text-secondary)]">
            <strong className="text-[var(--bdae-text-primary)]">GL Entry on Open:</strong>{' '}
            <span className="font-mono text-red-500">DEBIT GL 1010</span> (Savings) →{' '}
            <span className="font-mono text-emerald-500">CREDIT GL 2060</span> (FD Liability)
          </div>

          {/* Submit */}
          <div className="flex gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] text-xs font-semibold text-[var(--bdae-text-secondary)] hover:bg-black/5 dark:hover:bg-white/5 transition-all"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="flex-1 px-4 py-2.5 rounded-xl bg-violet-600 hover:bg-violet-700 text-white text-xs font-bold transition-all disabled:opacity-50 shadow-lg shadow-violet-600/20"
            >
              {isSubmitting ? 'Opening FD...' : 'Open Term Deposit'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
