import React from 'react';
import { FileText, CheckCircle2, Clock, XCircle } from 'lucide-react';
import { formatCurrency } from '../../../../common/utils/currency';

export const LoanApplicationsMetrics = ({ applications = [] }) => {
  const totalCount = applications.length;
  const approvedCount = applications.filter((a) => a.status === 'APPROVED' || a.status === 'DISBURSED').length;
  const pendingCount = applications.filter((a) => a.status === 'SUBMITTED' || a.status === 'UNDER_REVIEW').length;
  const rejectedCount = applications.filter((a) => a.status === 'REJECTED' || a.status === 'REJECTED_ELIGIBILITY').length;

  const totalRequested = applications.reduce((sum, a) => sum + (parseFloat(a.amountRequested) || 0), 0);

  const cards = [
    {
      label: 'Total Intake Requests',
      value: totalCount,
      subtext: `Total Exposure: ${formatCurrency(totalRequested)}`,
      icon: FileText,
      color: 'text-[var(--bdae-primary)]',
      bg: 'bg-[var(--bdae-primary)]/10',
      border: 'border-[var(--bdae-primary)]/20'
    },
    {
      label: 'Under Review / Pending',
      value: pendingCount,
      subtext: 'Awaiting Underwriting Review',
      icon: Clock,
      color: 'text-amber-500',
      bg: 'bg-amber-500/10',
      border: 'border-amber-500/20'
    },
    {
      label: 'Approved Applications',
      value: approvedCount,
      subtext: 'Eligible for Disbursement',
      icon: CheckCircle2,
      color: 'text-emerald-500',
      bg: 'bg-emerald-500/10',
      border: 'border-emerald-500/20'
    },
    {
      label: 'Rejected Requests',
      value: rejectedCount,
      subtext: 'Failed Policy or Eligibility',
      icon: XCircle,
      color: 'text-rose-500',
      bg: 'bg-rose-500/10',
      border: 'border-rose-500/20'
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
