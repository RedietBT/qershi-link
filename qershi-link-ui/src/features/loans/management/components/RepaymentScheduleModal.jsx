import React, { useState, useEffect } from 'react';
import {
  X,
  Calendar,
  CheckCircle2,
  Clock,
  AlertTriangle,
  Loader2,
  BadgePercent,
  FileSpreadsheet,
  Download
} from 'lucide-react';
import { loanManagementApi } from '../api/loanManagementApi';
import { formatCurrency, formatDate } from '../../../../common/utils/currency';

export const RepaymentScheduleModal = ({ accountId, accountNo, isOpen, onClose }) => {
  const [schedules, setSchedules] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isOpen && accountId) {
      loadSchedule();
    } else {
      setSchedules([]);
      setError(null);
    }
  }, [isOpen, accountId]);

  const loadSchedule = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await loanManagementApi.getAccountSchedule(accountId);
      const data = res.data || res || [];
      setSchedules(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Failed to load amortization schedule:', err);
      setError(err?.response?.data?.message || 'Could not retrieve repayment schedule.');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  // Calculate totals
  const totalPrincipal = schedules.reduce((acc, s) => acc + Number(s.principalDue || 0), 0);
  const totalInterest = schedules.reduce((acc, s) => acc + Number(s.interestDue || 0), 0);
  const totalDue = schedules.reduce((acc, s) => acc + Number(s.totalDue || 0), 0);
  const totalPaid = schedules.reduce((acc, s) => acc + Number(s.amountPaid || 0), 0);

  const getStatusBadge = (status) => {
    switch (status) {
      case 'PAID':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-600 dark:text-emerald-400">
            <CheckCircle2 className="w-3 h-3" />
            Paid
          </span>
        );
      case 'PARTIALLY_PAID':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/10 text-amber-600 dark:text-amber-400">
            <Clock className="w-3 h-3" />
            Partial
          </span>
        );
      case 'OVERDUE':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-red-500/10 text-red-600 dark:text-red-400">
            <AlertTriangle className="w-3 h-3" />
            Overdue
          </span>
        );
      case 'PENDING':
      default:
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-gray-500/10 text-gray-600">
            Pending
          </span>
        );
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
      <div className="bdae-card w-full max-w-4xl max-h-[90vh] overflow-hidden flex flex-col shadow-2xl border border-[var(--bdae-border)]">
        {/* Header */}
        <div className="p-5 border-b border-[var(--bdae-border)] flex items-center justify-between bg-black/5 dark:bg-white/5">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-purple-500/10 text-purple-600 flex items-center justify-center">
              <Calendar className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-[var(--bdae-text-primary)]">
                Amortization Repayment Schedule
              </h2>
              <p className="text-xs text-[var(--bdae-text-secondary)] font-mono">
                Loan Account: {accountNo || accountId?.slice(0, 13)}
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)] rounded-xl transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Summary Metric Ribbon */}
        <div className="p-4 bg-black/5 dark:bg-white/5 border-b border-[var(--bdae-border)] grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
          <div>
            <span className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)] block">
              Total Principal
            </span>
            <span className="font-mono font-bold text-[var(--bdae-text-primary)]">
              {formatCurrency(totalPrincipal)}
            </span>
          </div>
          <div>
            <span className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)] block">
              Total Interest
            </span>
            <span className="font-mono font-bold text-amber-600 dark:text-amber-400">
              {formatCurrency(totalInterest)}
            </span>
          </div>
          <div>
            <span className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)] block">
              Cumulative Paid
            </span>
            <span className="font-mono font-bold text-emerald-600 dark:text-emerald-400">
              {formatCurrency(totalPaid)}
            </span>
          </div>
          <div>
            <span className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)] block">
              Remaining Balance
            </span>
            <span className="font-mono font-black text-[var(--bdae-primary)]">
              {formatCurrency(Math.max(0, totalDue - totalPaid))}
            </span>
          </div>
        </div>

        {/* Content Body */}
        <div className="p-6 overflow-y-auto space-y-4 flex-1">
          {loading && (
            <div className="flex flex-col items-center justify-center py-16 space-y-3">
              <Loader2 className="w-8 h-8 animate-spin text-[var(--bdae-primary)]" />
              <p className="text-xs text-[var(--bdae-text-secondary)] font-medium">
                Generating amortization installment waterfall...
              </p>
            </div>
          )}

          {error && !loading && (
            <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 flex items-start space-x-3 text-xs">
              <AlertTriangle className="w-5 h-5 shrink-0 mt-0.5" />
              <div>
                <p className="font-semibold">Schedule Retrieval Alert</p>
                <p className="mt-0.5 opacity-90">{error}</p>
              </div>
            </div>
          )}

          {!loading && !error && schedules.length === 0 && (
            <div className="flex flex-col items-center justify-center py-16 space-y-2 text-center">
              <FileSpreadsheet className="w-10 h-10 text-[var(--bdae-text-secondary)]/50" />
              <p className="text-xs font-bold text-[var(--bdae-text-primary)]">
                No Schedule Installments Found
              </p>
              <p className="text-[11px] text-[var(--bdae-text-secondary)]">
                The repayment schedule will be generated once loan disbursement is approved.
              </p>
            </div>
          )}

          {!loading && schedules.length > 0 && (
            <div className="overflow-x-auto border border-[var(--bdae-border)] rounded-xl">
              <table className="w-full text-left border-collapse text-xs">
                <thead>
                  <tr className="bg-black/5 dark:bg-white/5 border-b border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] font-semibold">
                    <th className="py-2.5 px-3 text-center">#</th>
                    <th className="py-2.5 px-3">Due Date</th>
                    <th className="py-2.5 px-3 text-right">Principal Due</th>
                    <th className="py-2.5 px-3 text-right">Interest Due</th>
                    <th className="py-2.5 px-3 text-right">Total Installment</th>
                    <th className="py-2.5 px-3 text-right">Amount Paid</th>
                    <th className="py-2.5 px-3 text-center">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-[var(--bdae-border)]">
                  {schedules.map((item) => (
                    <tr key={item.scheduleId || item.installmentNo} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                      <td className="py-2.5 px-3 font-mono font-bold text-center text-[var(--bdae-text-secondary)]">
                        {item.installmentNo}
                      </td>
                      <td className="py-2.5 px-3 font-medium text-[var(--bdae-text-primary)]">
                        {formatDate(item.dueDate)}
                      </td>
                      <td className="py-2.5 px-3 text-right font-mono font-semibold text-[var(--bdae-text-primary)]">
                        {formatCurrency(item.principalDue)}
                      </td>
                      <td className="py-2.5 px-3 text-right font-mono text-amber-600 dark:text-amber-400">
                        {formatCurrency(item.interestDue)}
                      </td>
                      <td className="py-2.5 px-3 text-right font-mono font-black text-[var(--bdae-primary)]">
                        {formatCurrency(item.totalDue)}
                      </td>
                      <td className="py-2.5 px-3 text-right font-mono font-bold text-emerald-600 dark:text-emerald-400">
                        {formatCurrency(item.amountPaid || 0)}
                      </td>
                      <td className="py-2.5 px-3 text-center">
                        {getStatusBadge(item.status)}
                      </td>
                    </tr>
                  ))}
                </tbody>
                <tfoot>
                  <tr className="bg-black/5 dark:bg-white/5 font-bold border-t-2 border-[var(--bdae-border)] text-xs">
                    <td className="py-2.5 px-3 text-center" colSpan={2}>
                      Total Amortization
                    </td>
                    <td className="py-2.5 px-3 text-right font-mono text-[var(--bdae-text-primary)]">
                      {formatCurrency(totalPrincipal)}
                    </td>
                    <td className="py-2.5 px-3 text-right font-mono text-amber-600 dark:text-amber-400">
                      {formatCurrency(totalInterest)}
                    </td>
                    <td className="py-2.5 px-3 text-right font-mono text-[var(--bdae-primary)]">
                      {formatCurrency(totalDue)}
                    </td>
                    <td className="py-2.5 px-3 text-right font-mono text-emerald-600 dark:text-emerald-400">
                      {formatCurrency(totalPaid)}
                    </td>
                    <td></td>
                  </tr>
                </tfoot>
              </table>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 flex items-center justify-between">
          <button
            type="button"
            onClick={() => window.print()}
            className="px-4 py-2 rounded-xl text-xs font-bold border border-[var(--bdae-border)] bg-[var(--bdae-surface)] hover:bg-black/5 dark:hover:bg-white/5 flex items-center gap-2 transition-colors"
          >
            <Download className="w-3.5 h-3.5" />
            Print Schedule
          </button>
          <button
            type="button"
            onClick={onClose}
            className="px-5 py-2 rounded-xl text-xs font-bold bg-[var(--bdae-primary)] text-white hover:opacity-90 transition-opacity"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};
