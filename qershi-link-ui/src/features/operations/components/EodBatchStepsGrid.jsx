import React from 'react';
import {
  ShieldCheck,
  TrendingUp,
  AlertTriangle,
  UserX,
  Layers,
  Database,
  Calendar,
  FileText
} from 'lucide-react';

const EOD_STEPS = [
  {
    step: 1,
    title: 'Posting Window Cutoff',
    desc: 'Block new teller transactions and member-facing deposits/withdrawals.',
    icon: ShieldCheck
  },
  {
    step: 2,
    title: 'Savings Interest Accrual',
    desc: 'Calculate daily accrual on all active member savings accounts.',
    icon: TrendingUp
  },
  {
    step: 3,
    title: 'Loan Interest Accrual',
    desc: 'Post daily accrued interest on standard and emergency loans.',
    icon: TrendingUp
  },
  {
    step: 4,
    title: 'Delinquency & PAR Aging',
    desc: 'Increment DPD and update risk classification buckets (PAR 30/60/90).',
    icon: AlertTriangle
  },
  {
    step: 5,
    title: 'Statutory Impairment',
    desc: 'Compute required loan loss reserves and post GL provisioning.',
    icon: FileText
  },
  {
    step: 6,
    title: 'Dormancy Status Check',
    desc: 'Transition inactive accounts past statutory threshold to DORMANT.',
    icon: UserX
  },
  {
    step: 7,
    title: 'GL Balance Rollup',
    desc: 'Aggregate sub-ledger postings into control parent accounts.',
    icon: Layers
  },
  {
    step: 8,
    title: 'Trial Balance Equilibrium',
    desc: 'Enforce Debits == Credits and verify zero variance.',
    icon: Database
  },
  {
    step: 9,
    title: 'Advance Business Date',
    desc: 'Increment the system business calendar to next business day.',
    icon: Calendar
  }
];

export const EodBatchStepsGrid = () => {
  return (
    <div className="bdae-card p-6 rounded-2xl border border-[var(--bdae-border)] space-y-4 shadow-sm animate-fadeIn">
      <div className="flex items-center justify-between border-b border-[var(--bdae-border)] pb-3">
        <h3 className="text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
          Automated End-of-Day Execution Pipeline (9 Sequential Phases)
        </h3>
        <span className="text-[10px] font-mono text-[var(--bdae-text-secondary)]">
          Atomic Transaction Isolated
        </span>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
        {EOD_STEPS.map((s) => {
          const Icon = s.icon;
          return (
            <div
              key={s.step}
              className="p-3.5 rounded-xl border border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5 transition-all flex items-start gap-3"
            >
              <div className="w-7 h-7 rounded-lg bg-[var(--bdae-primary)]/10 text-[var(--bdae-primary)] flex items-center justify-center font-bold text-xs shrink-0 mt-0.5">
                {s.step}
              </div>
              <div className="min-w-0">
                <h4 className="text-xs font-bold text-[var(--bdae-text-primary)] flex items-center gap-1.5">
                  <Icon className="w-3.5 h-3.5 text-[var(--bdae-secondary)]" />
                  <span>{s.title}</span>
                </h4>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-0.5 leading-relaxed">
                  {s.desc}
                </p>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
