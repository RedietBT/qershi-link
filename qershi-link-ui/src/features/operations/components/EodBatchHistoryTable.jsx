import React from 'react';
import {
  History as HistoryIcon,
  CheckCircle2,
  XCircle,
  Clock,
  ChevronRight,
  Database,
  Loader2
} from 'lucide-react';
import { formatDateTime } from '../../../common/utils/currency';
import { MaskedDataField } from '../../../common/components/MaskedDataField';

export const EodBatchHistoryTable = ({
  batchHistory = [],
  isLoading = false,
  selectedBatch,
  onSelectBatch
}) => {
  const getStatusBadge = (status) => {
    switch (status) {
      case 'SUCCESS':
      case 'COMPLETED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-600 dark:text-emerald-400">
            <CheckCircle2 className="w-3 h-3" />
            Completed
          </span>
        );
      case 'FAILED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-rose-500/10 text-rose-600 dark:text-rose-400">
            <XCircle className="w-3 h-3" />
            Failed
          </span>
        );
      case 'IN_PROGRESS':
      default:
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/10 text-amber-600 dark:text-amber-400">
            <Clock className="w-3 h-3" />
            {status || 'Running'}
          </span>
        );
    }
  };

  return (
    <div className="bdae-card p-6 rounded-2xl border border-[var(--bdae-border)] space-y-4 shadow-sm animate-fadeIn">
      <div className="flex items-center justify-between">
        <h2 className="text-sm font-bold text-[var(--bdae-primary)] uppercase tracking-wider flex items-center gap-2">
          <HistoryIcon className="w-4 h-4 text-[var(--bdae-primary)]" />
          <span>Historical EOD Batch Executions</span>
        </h2>
        <span className="text-xs text-[var(--bdae-text-secondary)]">
          Total Runs: <span className="font-bold text-[var(--bdae-text-primary)]">{batchHistory.length}</span>
        </span>
      </div>

      {isLoading ? (
        <div className="py-12 text-center text-xs text-[var(--bdae-text-secondary)]">
          <Loader2 className="w-6 h-6 animate-spin mx-auto text-[var(--bdae-primary)] mb-2" />
          <span>Retrieving EOD operational logs...</span>
        </div>
      ) : batchHistory.length === 0 ? (
        <div className="text-center py-12 text-xs text-[var(--bdae-text-secondary)] space-y-2">
          <Database className="w-8 h-8 mx-auto opacity-40 text-[var(--bdae-text-secondary)]" />
          <p className="font-bold text-[var(--bdae-text-primary)]">
            No historical EOD batch runs recorded yet for this SACCO tenant.
          </p>
          <p className="text-[11px] opacity-75">
            Click "Run End-of-Day Batch" to execute the first operational cycle.
          </p>
        </div>
      ) : (
        <div className="overflow-x-auto -mx-6">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="border-y border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 text-[var(--bdae-text-secondary)] font-semibold">
                <th className="py-3 px-6">Business Date</th>
                <th className="py-3 px-4">Cycle ID</th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4">Operator Clearance</th>
                <th className="py-3 px-4">Execution Time</th>
                <th className="py-3 px-4 text-center">Duration</th>
                <th className="py-3 px-6 text-center">Audit Steps</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[var(--bdae-border)]">
              {batchHistory.map((batch) => {
                const isSelected = selectedBatch?.batchId === batch.batchId;
                return (
                  <tr
                    key={batch.batchId}
                    className={`hover:bg-black/5 dark:hover:bg-white/5 transition-colors ${
                      isSelected ? 'bg-[var(--bdae-primary)]/5 font-semibold' : ''
                    }`}
                  >
                    <td className="py-3.5 px-6 font-mono font-bold text-[var(--bdae-text-primary)]">
                      {batch.businessDate}
                    </td>
                    <td className="py-3.5 px-4 font-mono text-[11px] text-[var(--bdae-text-secondary)]">
                      {batch.batchId?.slice(0, 8)}...
                    </td>
                    <td className="py-3.5 px-4">{getStatusBadge(batch.status)}</td>
                    <td className="py-3.5 px-4">
                      <MaskedDataField
                        value={batch.executedByUserId || 'SYSTEM'}
                        maskType="memberId"
                        allowReveal={true}
                      />
                    </td>
                    <td className="py-3.5 px-4 text-[var(--bdae-text-secondary)]">
                      {formatDateTime(batch.startedAt)}
                    </td>
                    <td className="py-3.5 px-4 text-center font-mono text-xs">
                      {batch.executionDurationMs != null
                        ? `${(batch.executionDurationMs / 1000).toFixed(1)}s`
                        : '—'}
                    </td>
                    <td className="py-3.5 px-6 text-center">
                      <button
                        type="button"
                        onClick={() => onSelectBatch(isSelected ? null : batch)}
                        className="px-2.5 py-1 rounded-lg border border-[var(--bdae-border)] hover:bg-[var(--bdae-primary)] hover:text-white font-bold text-[10px] transition-all flex items-center gap-1 mx-auto"
                      >
                        <span>{isSelected ? 'Hide Steps' : 'View Steps'}</span>
                        <ChevronRight className="w-3 h-3" />
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {/* Step Logs Drilldown Panel */}
      {selectedBatch && selectedBatch.stepLogs && selectedBatch.stepLogs.length > 0 && (
        <div className="p-4 rounded-xl border border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 space-y-3 animate-fadeIn">
          <div className="flex items-center justify-between">
            <h4 className="text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-primary)]">
              Execution Log: Batch [{selectedBatch.businessDate}]
            </h4>
            <span className="text-[10px] text-[var(--bdae-text-secondary)] font-mono">
              {selectedBatch.stepLogs.length} Steps
            </span>
          </div>

          <div className="space-y-1.5 divide-y divide-[var(--bdae-border)]">
            {selectedBatch.stepLogs.map((log) => (
              <div key={log.stepNumber} className="pt-1.5 flex items-center justify-between text-xs">
                <div className="flex items-center gap-2">
                  <span className="font-mono text-[10px] font-bold text-[var(--bdae-text-secondary)]">
                    #{log.stepNumber}
                  </span>
                  <span className="font-semibold text-[var(--bdae-text-primary)]">
                    {log.stepName}
                  </span>
                </div>
                <div className="flex items-center gap-3">
                  <span className="text-[11px] text-[var(--bdae-text-secondary)]">
                    {log.details || 'Completed'}
                  </span>
                  <span
                    className={`font-mono text-[10px] font-bold ${
                      log.status === 'COMPLETED' ? 'text-emerald-500' : 'text-rose-500'
                    }`}
                  >
                    {log.status}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
