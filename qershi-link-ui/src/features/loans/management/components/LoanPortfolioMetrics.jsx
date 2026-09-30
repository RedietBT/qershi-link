import React from 'react';
import { BadgePercent, DollarSign, Clock, ShieldAlert } from 'lucide-react';
import { formatCurrency } from '../../../../common/utils/currency';

export const LoanPortfolioMetrics = ({ accounts = [] }) => {
  const totalPrincipal = accounts.reduce((sum, a) => sum + (parseFloat(a.principalAmount) || 0), 0);
  const totalOutstanding = accounts.reduce((sum, a) => sum + (parseFloat(a.outstandingBalance ?? a.principalAmount) || 0), 0);
  const activeCount = accounts.filter((a) => a.status === 'ACTIVE' || a.status === 'DISBURSED').length;
  const defaultedCount = accounts.filter((a) => a.status === 'DEFAULTED' || a.status === 'OVERDUE').length;

  const cards = [
    {
      label: 'Portfolio Count',
      value: accounts.length,
      subtext: `${activeCount} Performing Accounts`,
      icon: BadgePercent,
      color: 'text-emerald-500',
      bg: 'bg-emerald-500/10',
      border: 'border-emerald-500/20'
    },
    {
      label: 'Total Disbursed Principal',
      value: formatCurrency(totalPrincipal),
      subtext: 'Cumulative Original Principal',
      icon: DollarSign,
      color: 'text-[var(--bdae-primary)]',
      bg: 'bg-[var(--bdae-primary)]/10',
      border: 'border-[var(--bdae-primary)]/20'
    },
    {
      label: 'Total Outstanding Balance',
      value: formatCurrency(totalOutstanding),
      subtext: 'Current Portfolio Principal + Interest',
      icon: Clock,
      color: 'text-purple-500',
      bg: 'bg-purple-500/10',
      border: 'border-purple-500/20'
    },
    {
      label: 'Impaired / Defaulted',
      value: defaultedCount,
      subtext: defaultedCount > 0 ? 'Action Required: Collection Aging' : 'Zero Defaulted Accounts',
      icon: ShieldAlert,
      color: defaultedCount > 0 ? 'text-rose-500' : 'text-emerald-500',
      bg: defaultedCount > 0 ? 'bg-rose-500/10' : 'bg-emerald-500/10',
      border: defaultedCount > 0 ? 'border-rose-500/20' : 'border-emerald-500/20'
    }
  ];

  return (
    <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 animate-fadeIn">
      {cards.map((c, idx) => {
        const Icon = c.icon;
        return (
          <div
            key={idx}
            className={`bdae-card p-4 rounded-2xl border ${c.border} space-y-2 transition-all hover:scale-[1.01]`}
          >
            <div className="flex items-center justify-between">
              <span className="text-[10px] font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider">
                {c.label}
              </span>
              <div className={`p-2 rounded-xl ${c.bg}`}>
                <Icon className={`w-4 h-4 ${c.color}`} />
              </div>
            </div>
            <div className="text-xl font-black text-[var(--bdae-text-primary)] font-mono">
              {c.value}
            </div>
            <p className="text-[10px] text-[var(--bdae-text-secondary)] truncate">
              {c.subtext}
            </p>
          </div>
        );
      })}
    </div>
  );
};
