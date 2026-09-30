import React from 'react';
import { Loader2, FolderTree } from 'lucide-react';
import { formatCurrency } from '../../../common/utils/currency';

const TYPE_CONFIG = {
  ASSET: { label: 'Asset', bg: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20' },
  LIABILITY: { label: 'Liability', bg: 'bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/20' },
  EQUITY: { label: 'Equity', bg: 'bg-purple-500/10 text-purple-600 dark:text-purple-400 border-purple-500/20' },
  REVENUE: { label: 'Revenue', bg: 'bg-blue-500/10 text-blue-600 dark:text-blue-400 border-blue-500/20' },
  EXPENSE: { label: 'Expense', bg: 'bg-rose-500/10 text-rose-600 dark:text-rose-400 border-rose-500/20' }
};

export const CoaFlatTable = ({ flatAccounts = [], loading = false, searchQuery = '' }) => {
  const filtered = flatAccounts.filter((a) => {
    if (!searchQuery) return true;
    const q = searchQuery.toLowerCase();
    return (
      a.glCode?.toLowerCase().includes(q) ||
      a.accountName?.toLowerCase().includes(q) ||
      a.accountType?.toLowerCase().includes(q)
    );
  });

  return (
    <div className="bdae-card rounded-2xl border border-[var(--bdae-border)] overflow-hidden shadow-sm">
      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs border-collapse">
          <thead>
            <tr className="border-b border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 text-[var(--bdae-text-secondary)]">
              <th className="py-3 px-4 font-semibold">GL Code</th>
              <th className="py-3 px-4 font-semibold">Account Title</th>
              <th className="py-3 px-3 font-semibold">Type</th>
              <th className="py-3 px-3 font-semibold">Parent GL</th>
              <th className="py-3 px-3 font-semibold text-center">Normal</th>
              <th className="py-3 px-4 font-semibold text-right">Balance (ETB)</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[var(--bdae-border)]">
            {loading ? (
              <tr>
                <td colSpan="6" className="py-16 text-center text-xs text-[var(--bdae-text-secondary)]">
                  <Loader2 className="w-6 h-6 animate-spin mx-auto text-[var(--bdae-primary)] mb-2" />
                  <span>Loading flat account list...</span>
                </td>
              </tr>
            ) : filtered.length === 0 ? (
              <tr>
                <td colSpan="6" className="py-16 text-center text-xs text-[var(--bdae-text-secondary)]">
                  <FolderTree className="w-8 h-8 mx-auto opacity-30 text-[var(--bdae-text-secondary)] mb-2" />
                  <p className="font-bold text-[var(--bdae-text-primary)]">No Accounts Match Query</p>
                </td>
              </tr>
            ) : (
              filtered.map((acc) => {
                const typeStyle = TYPE_CONFIG[acc.accountType] || TYPE_CONFIG.ASSET;
                return (
                  <tr key={acc.accountId} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                    <td className="py-3 px-4 font-mono font-bold text-[var(--bdae-primary)]">
                      {acc.glCode}
                    </td>
                    <td className="py-3 px-4 font-semibold text-[var(--bdae-text-primary)]">
                      {acc.accountName}
                    </td>
                    <td className="py-3 px-3">
                      <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold border ${typeStyle.bg}`}>
                        {typeStyle.label}
                      </span>
                    </td>
                    <td className="py-3 px-3 font-mono text-[var(--bdae-text-secondary)]">
                      {acc.parentGlCode || '— (Root)'}
                    </td>
                    <td className="py-3 px-3 text-center">
                      <span className="font-mono text-[10px] font-bold px-1.5 py-0.5 rounded bg-black/5 dark:bg-white/5 text-[var(--bdae-text-secondary)]">
                        {acc.normalBalance}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right font-mono font-bold text-[var(--bdae-text-primary)]">
                      {formatCurrency(acc.currentBalance ?? 0)}
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
