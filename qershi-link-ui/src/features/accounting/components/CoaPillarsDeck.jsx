import React from 'react';
import { Scale, ArrowUpRight, ArrowDownRight, Layers, TrendingUp } from 'lucide-react';
import { formatCurrency } from '../../../common/utils/currency';

export const CoaPillarsDeck = ({ treeData = [] }) => {
  const getSubtotal = (type) => {
    const rootNode = treeData.find((n) => n.accountType === type);
    return rootNode ? parseFloat(rootNode.rollupBalance) || 0 : 0;
  };

  const pillars = [
    {
      type: 'ASSET',
      label: 'Assets (1000)',
      subtext: 'Cash, Vaults, Loans to Members',
      amount: getSubtotal('ASSET'),
      icon: Scale,
      color: 'text-emerald-500',
      bg: 'bg-emerald-500/10',
      border: 'border-emerald-500/20'
    },
    {
      type: 'LIABILITY',
      label: 'Liabilities (2000)',
      subtext: 'Member Savings, Term Deposits',
      amount: getSubtotal('LIABILITY'),
      icon: ArrowDownRight,
      color: 'text-amber-500',
      bg: 'bg-amber-500/10',
      border: 'border-amber-500/20'
    },
    {
      type: 'EQUITY',
      label: 'Equity (3000)',
      subtext: 'Share Capital, Legal Reserves',
      amount: getSubtotal('EQUITY'),
      icon: Layers,
      color: 'text-purple-500',
      bg: 'bg-purple-500/10',
      border: 'border-purple-500/20'
    },
    {
      type: 'REVENUE',
      label: 'Revenue (4000)',
      subtext: 'Interest & Fee Income',
      amount: getSubtotal('REVENUE'),
      icon: ArrowUpRight,
      color: 'text-blue-500',
      bg: 'bg-blue-500/10',
      border: 'border-blue-500/20'
    },
    {
      type: 'EXPENSE',
      label: 'Expenses (5000)',
      subtext: 'Interest Paid, Operations',
      amount: getSubtotal('EXPENSE'),
      icon: TrendingUp,
      color: 'text-rose-500',
      bg: 'bg-rose-500/10',
      border: 'border-rose-500/20'
    }
  ];

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3 animate-fadeIn">
      {pillars.map((p) => {
        const Icon = p.icon;
        return (
          <div
            key={p.type}
            className={`bdae-card p-4 rounded-2xl border ${p.border} space-y-2 hover:scale-[1.01] transition-all`}
          >
            <div className="flex items-center justify-between">
              <span className="text-[10px] font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
                {p.label}
              </span>
              <div className={`p-1.5 rounded-lg ${p.bg}`}>
                <Icon className={`w-3.5 h-3.5 ${p.color}`} />
              </div>
            </div>
            <div className="text-lg font-black font-mono text-[var(--bdae-text-primary)]">
              {formatCurrency(p.amount)}
            </div>
            <p className="text-[10px] text-[var(--bdae-text-secondary)] truncate">
              {p.subtext}
            </p>
          </div>
        );
      })}
    </div>
  );
};
