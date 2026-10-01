import React from 'react';
import { History, FileText, CheckCircle2, AlertTriangle, ShieldCheck } from 'lucide-react';
import { formatCurrency, formatDateTime } from '../../../common/utils/currency';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS, ROLES } from '../../../common/constants/permissions';

/**
 * Historical audit log table of closed shifts, blind reconciliations,
 * automated GL variance adjustments, and supervisor authorizations.
 */
export const TillReconciliationTable = ({ reconciliations = [], onApproveVariance }) => {
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
                <th className="py-2.5 px-3">Physical Counted</th>
                <th className="py-2.5 px-3">GL Expected</th>
                <th className="py-2.5 px-3">Net Variance</th>
                <th className="py-2.5 px-3">GL Adjustment</th>
                <th className="py-2.5 px-3">Status</th>
                <th className="py-2.5 px-3">Notes</th>
                {onApproveVariance && <th className="py-2.5 px-3 text-right">Supervisor</th>}
              </tr>
            </thead>
            <tbody className="divide-y divide-[var(--bdae-border)]">
              {reconciliations.map((rec) => {
                const variance = Number(rec.cashVariance) || 0;
                const varianceType = rec.varianceType || (variance === 0 ? 'NONE' : variance < 0 ? 'SHORTAGE' : 'OVERAGE');
                const isPending = rec.status === 'PENDING_SUPERVISOR_APPROVAL';

                return (
                  <tr key={rec.reconciliationId || rec.id} className="hover:bg-black/[0.02] dark:hover:bg-white/[0.02] transition">
                    <td className="py-2 px-3 font-mono text-[11px]">
                      {formatDateTime(rec.reconciledAt || rec.createdAt)}
                    </td>
                    <td className="py-2 px-3 font-mono font-semibold">
                      {formatCurrency(rec.physicalCashCounted)}
                      {Number(rec.coinsAmount) > 0 && (
                        <span className="text-[10px] text-[var(--bdae-text-secondary)] block">
                          incl. {formatCurrency(rec.coinsAmount)} coins
                        </span>
                      )}
                    </td>
                    <td className="py-2 px-3 font-mono font-semibold text-[var(--bdae-text-secondary)]">
                      {formatCurrency(rec.electronicCashBalance)}
                    </td>
                    <td className="py-2 px-3">
                      <span
                        className={`font-mono font-black text-xs block ${
                          variance === 0
                            ? 'text-emerald-500'
                            : variance > 0
                            ? 'text-blue-500'
                            : 'text-rose-500'
                        }`}
                      >
                        {variance === 0 ? 'BALANCED' : formatCurrency(variance)}
                      </span>
                      {varianceType !== 'NONE' && (
                        <span className={`text-[9px] font-bold px-1.5 py-0.2 rounded uppercase ${
                          varianceType === 'SHORTAGE' ? 'bg-rose-500/10 text-rose-600' : 'bg-blue-500/10 text-blue-600'
                        }`}>
                          {varianceType}
                        </span>
                      )}
                    </td>
                    <td className="py-2 px-3">
                      {rec.varianceGlCode ? (
                        <span className="font-mono text-[10px] px-1.5 py-0.5 rounded bg-black/5 dark:bg-white/5">
                          GL {rec.varianceGlCode} ({varianceType === 'SHORTAGE' ? 'Expense' : 'Income'})
                        </span>
                      ) : (
                        <span className="text-[10px] text-[var(--bdae-text-secondary)]">—</span>
                      )}
                    </td>
                    <td className="py-2 px-3">
                      <span
                        className={`px-2 py-0.5 rounded-full text-[10px] font-bold inline-flex items-center gap-1 ${
                          rec.status === 'BALANCED' || variance === 0
                            ? 'bg-emerald-500/10 text-emerald-600'
                            : rec.status === 'SUPERVISOR_APPROVED'
                            ? 'bg-blue-500/10 text-blue-600'
                            : isPending
                            ? 'bg-amber-500/10 text-amber-600 font-extrabold'
                            : 'bg-slate-500/10 text-slate-500'
                        }`}
                      >
                        {rec.status === 'SUPERVISOR_APPROVED' && <ShieldCheck className="w-3 h-3" />}
                        {rec.status === 'BALANCED' && <CheckCircle2 className="w-3 h-3" />}
                        {isPending && <AlertTriangle className="w-3 h-3" />}
                        {rec.status === 'PENDING_SUPERVISOR_APPROVAL' ? 'Pending Approval' : rec.status || 'BALANCED'}
                      </span>
                    </td>
                    <td className="py-2 px-3 text-[var(--bdae-text-secondary)] truncate max-w-xs">
                      {rec.reconciliationNotes || 'Shift settlement'}
                    </td>
                    {onApproveVariance && (
                      <td className="py-2 px-3 text-right">
                        {isPending && (
                          <PermissionGuard
                            roles={[ROLES.SUPER_ADMIN, ROLES.ADMIN, ROLES.SACCO_ADMIN, ROLES.BRANCH_MANAGER]}
                            permissions={[PERMISSIONS.TELLER_TILL_MANAGE]}
                          >
                            <button
                              type="button"
                              onClick={() => onApproveVariance(rec.reconciliationId)}
                              className="px-2.5 py-1 text-[11px] font-bold rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white shadow-sm transition"
                            >
                              Sign Off
                            </button>
                          </PermissionGuard>
                        )}
                      </td>
                    )}
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
export default TillReconciliationTable;
