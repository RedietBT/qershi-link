import React from 'react';
import { Lock, AlertTriangle, CreditCard, Info } from 'lucide-react';

export const GlobalGovernanceTab = ({ rules, onToggle, onNumberChange }) => {
  return (
    <div className="space-y-6 animate-fadeIn">
      {/* Regulatory Banner: Anti-Self-Approval Principle */}
      <div className="p-5 rounded-2xl bg-gradient-to-r from-[#00CDDB]/10 via-[#004B87]/5 to-transparent border border-[#00CDDB]/30 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 shadow-sm">
        <div className="flex items-start space-x-3.5">
          <div className="w-9 h-9 rounded-xl bg-[#00CDDB]/20 border border-[#00CDDB]/40 flex items-center justify-center text-[#00CDDB] shrink-0 mt-0.5 sm:mt-0">
            <Lock className="w-5 h-5" />
          </div>
          <div className="space-y-1">
            <span className="text-xs font-extrabold text-[var(--bdae-text-primary)] block">
              Banking Regulatory Standard: Anti-Self-Approval Enforcement (Maker ≠ Checker)
            </span>
            <p className="text-[11px] text-[var(--bdae-text-secondary)] leading-relaxed">
              Under the Four-Eyes principle, any transaction or request initiated by a staff member (<code className="font-mono text-[var(--bdae-text-primary)]">maker_user_id</code>) can <strong>never</strong> be authorized or approved by that same user, even if they hold Senior Supervisor or Branch Manager credentials.
            </p>
          </div>
        </div>

        <div className="shrink-0 flex items-center space-x-2 px-3.5 py-1.5 rounded-full border border-emerald-500/30 bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 text-xs font-bold">
          <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
          <span>Enforcement Active</span>
        </div>
      </div>

      {/* Global Transaction Limits */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Single Transaction Supervisor Threshold */}
        <div className="bdae-card p-6 border border-[var(--bdae-border)] rounded-2xl space-y-4">
          <div className="flex items-start justify-between">
            <div>
              <h3 className="text-xs font-bold text-[var(--bdae-text-primary)]">
                Single-Transaction Supervisor Override Threshold
              </h3>
              <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                Cash deposits, withdrawals, or transfers exceeding this amount automatically intercept teller processing and require supervisor authorization.
              </p>
            </div>
            <div className="w-9 h-9 rounded-xl bg-[#00CDDB]/10 text-[#00CDDB] flex items-center justify-center shrink-0">
              <AlertTriangle className="w-4 h-4" />
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="text-[10px] font-extrabold uppercase text-[var(--bdae-text-secondary)] tracking-wider">
              Threshold Limit (ETB)
            </label>
            <div className="relative">
              <input
                type="number"
                min="0"
                step="500"
                value={rules.transactionCheckerThreshold}
                onChange={(e) => onNumberChange('transactionCheckerThreshold', e.target.value)}
                className="w-full pl-4 pr-16 py-2.5 rounded-xl border border-[var(--bdae-border)] focus:border-[#00CDDB] bg-black/5 dark:bg-white/5 text-sm font-mono font-bold text-[var(--bdae-text-primary)] outline-none transition-all"
              />
              <span className="absolute right-3.5 top-1/2 -translate-y-1/2 text-xs font-bold text-[var(--bdae-text-secondary)] font-mono">
                ETB
              </span>
            </div>
            <span className="text-[10px] text-[var(--bdae-text-secondary)] flex items-center gap-1">
              <Info className="w-3 h-3 text-[#00CDDB]" />
              Standard SACCO Default: 50,000.00 ETB
            </span>
          </div>
        </div>

        {/* Daily Account Limit Threshold */}
        <div className="bdae-card p-6 border border-[var(--bdae-border)] rounded-2xl space-y-4">
          <div className="flex items-start justify-between">
            <div>
              <h3 className="text-xs font-bold text-[var(--bdae-text-primary)]">
                Default Daily Cumulative Account Limit
              </h3>
              <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                Default maximum withdrawal volume per member account per calendar day across all branches before mandatory risk review.
              </p>
            </div>
            <div className="w-9 h-9 rounded-xl bg-purple-500/10 text-purple-600 flex items-center justify-center shrink-0">
              <CreditCard className="w-4 h-4" />
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="text-[10px] font-extrabold uppercase text-[var(--bdae-text-secondary)] tracking-wider">
              Daily Limit (ETB)
            </label>
            <div className="relative">
              <input
                type="number"
                min="0"
                step="5000"
                value={rules.dailyAccountLimitThreshold}
                onChange={(e) => onNumberChange('dailyAccountLimitThreshold', e.target.value)}
                className="w-full pl-4 pr-16 py-2.5 rounded-xl border border-[var(--bdae-border)] focus:border-[#00CDDB] bg-black/5 dark:bg-white/5 text-sm font-mono font-bold text-[var(--bdae-text-primary)] outline-none transition-all"
              />
              <span className="absolute right-3.5 top-1/2 -translate-y-1/2 text-xs font-bold text-[var(--bdae-text-secondary)] font-mono">
                ETB
              </span>
            </div>
            <span className="text-[10px] text-[var(--bdae-text-secondary)] flex items-center gap-1">
              <Info className="w-3 h-3 text-[#00CDDB]" />
              Standard SACCO Default: 200,000.00 ETB
            </span>
          </div>
        </div>
      </div>

      {/* Anti-Self-Approval Enforcement Setting */}
      <div className="bdae-card p-6 border border-[var(--bdae-border)] rounded-2xl flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="space-y-1">
          <h3 className="text-xs font-bold text-[var(--bdae-text-primary)] flex items-center gap-2">
            <Lock className="w-4 h-4 text-[#00CDDB]" />
            Strict Anti-Self-Approval Enforcement (<code className="font-mono text-[10px]">maker != checker</code>)
          </h3>
          <p className="text-[11px] text-[var(--bdae-text-secondary)]">
            When active, the approve button is disabled for the creating user across all approval screens. Recommended to remain active for all production SACCOs.
          </p>
        </div>

        <button
          type="button"
          onClick={() => onToggle('enforceAntiSelfApproval')}
          className={`w-14 h-7 flex items-center rounded-full p-1 transition-colors shrink-0 ${
            rules.enforceAntiSelfApproval ? 'bg-[#00CDDB]' : 'bg-gray-300 dark:bg-gray-700'
          }`}
        >
          <div
            className={`bg-white w-5 h-5 rounded-full shadow-md transform transition-transform ${
              rules.enforceAntiSelfApproval ? 'translate-x-7' : 'translate-x-0'
            }`}
          />
        </button>
      </div>
    </div>
  );
};
