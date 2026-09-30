import React from 'react';
import { History, FileText } from 'lucide-react';
import { formatCurrency, formatDateTime } from '../../../common/utils/currency';

/**
 * Historical audit log table of closed shifts and till reconciliations
 */
export const TillReconciliationTable = ({ reconciliations }) => {
  return (
    <div className="bdae-card p-5 border border-[var(--bdae-border)] space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] flex items-center gap-2">
          <History className="w-4 h-4 text-[var(--bdae-primary)]" />
          <span>Shift Reconciliations & Audit Log</span>
        </h3>
        <span className="text-[10px] text-[var(--bdae-text-secondary)] font-mono">
          {reconciliations.length} record{reconciliations.length !== 1 ? 's' : ''}
        </span>
      </div>

      {reconciliations.length === 0 ? (
        <div className="p-8 text-center text-xs text-[var(--bdae-text-secondary)] space-y-1">
          <FileText className="w-8 h-8 opacity-40 mx-auto" />
          <p>No historical drawer reconciliations found for your active profile.</p>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="border-b border-[var(--bdae-border)] text-[10px] text-[var(--bdae-text-secondary)] uppercase font-bold">
                <th className="py-2.5 px-3">Closed At</th>
                <th className="py-2.5 px-3">Physical Cash</th>
                <th className="py-2.5 px-3">Electronic Cash</th>
                <th className="py-2.5 px-3">Variance</th>
                <th className="py-2.5 px-3">Status</th>
                <th className="py-2.5 px-3">Notes</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[var(--bdae-border)]">
              {reconciliations.map((rec) => {
                const variance = Number(rec.cashVariance) || 0;
                return (
                  <tr key={rec.reconciliationId || rec.id} className="hover:bg-black/[0.02] dark:hover:bg-white/[0.02]">
                    <td className="py-2 px-3 font-mono text-[11px]">
                      {formatDateTime(rec.reconciledAt || rec.createdAt)}
                    </td>
                    <td className="py-2 px-3 font-mono font-semibold">
                      {formatCurrency(rec.physicalCashCounted)}
                    </td>
                    <td className="py-2 px-3 font-mono font-semibold text-[var(--bdae-text-secondary)]">
                      {formatCurrency(rec.electronicCashBalance)}
                    </td>
                    <td
                      className={`py-2 px-3 font-mono font-black ${
                        variance === 0
                          ? 'text-emerald-500'
                          : variance > 0
                          ? 'text-blue-500'
                          : 'text-rose-500'
                      }`}
                    >
                      {variance === 0 ? 'BALANCED' : formatCurrency(variance)}
                    </td>
                    <td className="py-2 px-3">
                      <span
                        className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                          rec.status === 'BALANCED' || variance === 0
                            ? 'bg-emerald-500/10 text-emerald-600'
                            : 'bg-amber-500/10 text-amber-600'
                        }`}
                      >
                        {rec.status || (variance === 0 ? 'BALANCED' : 'VARIANCE')}
                      </span>
                    </td>
                    <td className="py-2 px-3 text-[var(--bdae-text-secondary)] truncate max-w-xs">
                      {rec.reconciliationNotes || 'Shift settlement'}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
