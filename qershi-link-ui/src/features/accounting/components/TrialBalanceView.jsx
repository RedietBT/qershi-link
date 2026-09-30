import React from 'react';
import { Scale, CheckCircle2, AlertTriangle } from 'lucide-react';
import { formatCurrency } from '../../../common/utils/currency';
import { BalanceStatusBanner } from './BalanceStatusBanner';

export const TrialBalanceView = ({ report }) => {
  if (!report) return null;

  const lines = report.lines || [];
  const totalDebits = report.totalDebits || 0;
  const totalCredits = report.totalCredits || 0;
  const isBalanced = report.isBalanced;
  const variance = report.variance || Math.abs(totalDebits - totalCredits);

  return (
    <div className="space-y-4 animate-fadeIn">
      {/* Equilibrium Status Banner */}
      <BalanceStatusBanner
        isBalanced={isBalanced}
        title="Double-Entry Trial Balance Verification"
        successMessage={`General Ledger Debits (${formatCurrency(totalDebits)}) precisely match Credits (${formatCurrency(totalCredits)}).`}
        difference={variance}
        leftLabel="Total Debits"
        leftValue={totalDebits}
        rightLabel="Total Credits"
        rightValue={totalCredits}
      />

      {/* Trial Balance Table */}
      <div className="bdae-card rounded-2xl border border-[var(--bdae-border)] overflow-hidden shadow-sm">
        <div className="p-4 border-b border-[var(--bdae-border)] flex items-center justify-between bg-black/5 dark:bg-white/5">
          <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
            <Scale className="w-4 h-4 text-[var(--bdae-primary)]" />
            <span>Trial Balance Ledger Posts (As of {report.asOfDate})</span>
          </div>
          <span className="text-[10px] font-mono text-[var(--bdae-text-secondary)] font-bold">
            {lines.length} Active GL Accounts
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="border-b border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 text-[var(--bdae-text-secondary)] font-semibold">
                <th className="py-3 px-4">GL Code</th>
                <th className="py-3 px-4">Account Title</th>
                <th className="py-3 px-3">Type</th>
                <th className="py-3 px-4 text-right">Debit (ETB)</th>
                <th className="py-3 px-4 text-right">Credit (ETB)</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[var(--bdae-border)]">
              {lines.map((line) => (
                <tr
                  key={line.glCode}
                  className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
                >
                  <td className="py-2.5 px-4 font-mono font-bold text-[var(--bdae-primary)]">
                    {line.glCode}
                  </td>
                  <td className="py-2.5 px-4 font-semibold text-[var(--bdae-text-primary)]">
                    {line.accountName}
                  </td>
                  <td className="py-2.5 px-3">
                    <span className="text-[10px] font-bold text-[var(--bdae-text-secondary)] uppercase">
                      {line.accountType}
                    </span>
                  </td>
                  <td className="py-2.5 px-4 text-right font-mono font-bold text-[var(--bdae-text-primary)]">
                    {line.debitAmount > 0 ? formatCurrency(line.debitAmount) : '—'}
                  </td>
                  <td className="py-2.5 px-4 text-right font-mono font-bold text-[var(--bdae-text-primary)]">
                    {line.creditAmount > 0 ? formatCurrency(line.creditAmount) : '—'}
                  </td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr className="border-t-2 border-[var(--bdae-border)] bg-black/10 dark:bg-white/10 font-bold text-xs">
                <td colSpan="3" className="py-3.5 px-4 uppercase tracking-wider text-[var(--bdae-text-primary)]">
                  Total Trial Balance Equilibrium
                </td>
                <td className="py-3.5 px-4 text-right font-mono text-sm text-[var(--bdae-primary)]">
                  {formatCurrency(totalDebits)}
                </td>
                <td className="py-3.5 px-4 text-right font-mono text-sm text-[var(--bdae-primary)]">
                  {formatCurrency(totalCredits)}
                </td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>
    </div>
  );
};
