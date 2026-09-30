import React from 'react';
import { Calendar, Clock, Moon, Sun, ShieldCheck, ShieldAlert, CheckCircle2 } from 'lucide-react';

export const EodStatusCard = ({ eodStatus }) => {
  const isOperating = eodStatus?.operationalStatus === 'OPEN';
  const currentDate = eodStatus?.businessDate || new Date().toISOString().split('T')[0];
  const nextDate = eodStatus?.nextBusinessDate || '—';

  return (
    <div className="bdae-card p-6 rounded-2xl border border-[var(--bdae-border)] space-y-4 shadow-sm animate-fadeIn">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-[var(--bdae-border)] pb-4">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-[10px] font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
              Operational Cycle State
            </span>
            <span
              className={`px-2.5 py-0.5 rounded-full text-[10px] font-black uppercase border ${
                isOperating
                  ? 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20'
                  : 'bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/20'
              }`}
            >
              {eodStatus?.operationalStatus || 'OPEN'}
            </span>
          </div>
          <h2 className="text-xl font-black text-[var(--bdae-text-primary)] mt-1 flex items-center gap-2">
            {isOperating ? <Sun className="w-5 h-5 text-amber-500" /> : <Moon className="w-5 h-5 text-purple-500" />}
            <span>Business Date: {currentDate}</span>
          </h2>
        </div>

        <div className="flex items-center gap-3 text-right">
          <div className="border-r border-[var(--bdae-border)] pr-4">
            <span className="text-[10px] block uppercase font-bold text-[var(--bdae-text-secondary)]">
              Current Business Day
            </span>
            <span className="font-mono text-sm font-black text-[var(--bdae-primary)]">
              {currentDate}
            </span>
          </div>
          <div>
            <span className="text-[10px] block uppercase font-bold text-[var(--bdae-text-secondary)]">
              Next Business Day
            </span>
            <span className="font-mono text-sm font-black text-[var(--bdae-text-primary)]">
              {nextDate}
            </span>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs pt-2">
        <div className="p-3 rounded-xl bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)]">
          <span className="text-[10px] text-[var(--bdae-text-secondary)] uppercase font-bold block">
            Posting Cutoff Mode
          </span>
          <span className="font-semibold text-[var(--bdae-text-primary)] mt-0.5 block">
            {isOperating ? 'Real-Time Transactions Active' : 'Posting Cutoff Locked'}
          </span>
        </div>

        <div className="p-3 rounded-xl bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)]">
          <span className="text-[10px] text-[var(--bdae-text-secondary)] uppercase font-bold block">
            Automated Accruals
          </span>
          <span className="font-semibold text-[var(--bdae-text-primary)] mt-0.5 block">
            Daily Compound Interest & Fees
          </span>
        </div>

        <div className="p-3 rounded-xl bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)]">
          <span className="text-[10px] text-[var(--bdae-text-secondary)] uppercase font-bold block">
            Accounting Integrity
          </span>
          <span className="font-semibold text-emerald-600 dark:text-emerald-400 mt-0.5 block flex items-center gap-1">
            <CheckCircle2 className="w-3.5 h-3.5" />
            General Ledger Balanced
          </span>
        </div>
      </div>
    </div>
  );
};
