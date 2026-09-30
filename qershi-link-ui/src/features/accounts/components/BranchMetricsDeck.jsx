import React from 'react';
import { Building2, CheckCircle2, Vault, DollarSign } from 'lucide-react';
import { formatCurrency } from '../../../common/utils/currency';

export const BranchMetricsDeck = ({ branches = [] }) => {
  const activeCount = branches.filter((b) => b.status === 'ACTIVE').length;
  const centralCount = branches.filter((b) => b.isCentralVault).length;
  const totalLendingLimit = branches.reduce(
    (sum, b) => sum + (parseFloat(b.discretionaryLendingLimit) || 0),
    0
  );

  const cards = [
    {
      label: 'Total SACCO Branches',
      value: branches.length,
      subtext: 'Operational branch network',
      icon: Building2,
      color: 'text-[var(--bdae-primary)]',
      bg: 'bg-[var(--bdae-primary)]/10',
      border: 'border-[var(--bdae-primary)]/20'
    },
    {
      label: 'Active Branches',
      value: activeCount,
      subtext: `${branches.length - activeCount} Suspended / Inactive`,
      icon: CheckCircle2,
      color: 'text-emerald-500',
      bg: 'bg-emerald-500/10',
      border: 'border-emerald-500/20'
    },
    {
      label: 'Central Vaults',
      value: centralCount,
      subtext: 'Headquarter treasury hubs',
      icon: Vault,
      color: 'text-amber-500',
      bg: 'bg-amber-500/10',
      border: 'border-amber-500/20'
    },
    {
      label: 'Total Discretionary Limit',
      value: formatCurrency(totalLendingLimit),
      subtext: 'Aggregated Branch Discretionary Cap',
      icon: DollarSign,
      color: 'text-purple-500',
      bg: 'bg-purple-500/10',
      border: 'border-purple-500/20'
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
