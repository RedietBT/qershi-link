import React from 'react';
import {
  Calendar,
  CreditCard,
  CheckCircle2,
  Clock,
  AlertTriangle,
  Loader2,
  FolderOpen,
  BadgePercent
} from 'lucide-react';
import { formatCurrency } from '../../../../common/utils/currency';
import { PermissionGuard } from '../../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../../common/constants/permissions';
import { MaskedDataField } from '../../../../common/components/MaskedDataField';

export const LoanAccountsTable = ({
  accounts = [],
  loading = false,
  searched = false,
  onOpenSchedule,
  onOpenRepay
}) => {
  const getStatusBadge = (status) => {
    switch (status) {
      case 'ACTIVE':
      case 'DISBURSED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-600 dark:text-emerald-400">
            <CheckCircle2 className="w-3 h-3" />
            Active / Disbursed
          </span>
        );
      case 'PENDING_DISBURSEMENT':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/10 text-amber-600 dark:text-amber-400">
            <Clock className="w-3 h-3" />
            Pending Checker Approval
          </span>
        );
      case 'CLOSED':
      case 'SETTLED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-blue-500/10 text-blue-600 dark:text-blue-400">
            <CheckCircle2 className="w-3 h-3" />
            Settled / Closed
          </span>
        );
      case 'DEFAULTED':
      case 'OVERDUE':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-red-500/10 text-red-600 dark:text-red-400">
            <AlertTriangle className="w-3 h-3" />
            Defaulted
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-gray-500/10 text-gray-600">
            {status || 'ACTIVE'}
          </span>
        );
    }
  };

  return (
    <div className="bdae-card border border-[var(--bdae-border)] overflow-hidden shadow-sm rounded-2xl">
      <div className="p-4 border-b border-[var(--bdae-border)] flex items-center justify-between">
        <h2 className="text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
          Active Loan Accounts {searched && `(${accounts.length} records)`}
        </h2>
      </div>

      {loading ? (
        <div className="flex flex-col items-center justify-center py-16 space-y-3">
          <Loader2 className="w-8 h-8 animate-spin text-emerald-600" />
          <p className="text-xs text-[var(--bdae-text-secondary)] font-medium">
            Retrieving loan portfolio from loan management service...
          </p>
        </div>
      ) : !searched && accounts.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-16 space-y-2 text-center">
          <FolderOpen className="w-10 h-10 text-[var(--bdae-text-secondary)]/50" />
          <p className="text-xs font-bold text-[var(--bdae-text-primary)]">
            No Member Portfolio Inquired
          </p>
          <p className="text-[11px] text-[var(--bdae-text-secondary)] max-w-sm">
            Enter a Borrower Member User ID or Account UUID above to inspect active loans, amortization schedules, and process repayments.
          </p>
        </div>
      ) : accounts.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-16 space-y-2 text-center">
          <BadgePercent className="w-10 h-10 text-[var(--bdae-text-secondary)]/50" />
          <p className="text-xs font-bold text-[var(--bdae-text-primary)]">
            No Loan Accounts Found
          </p>
          <p className="text-[11px] text-[var(--bdae-text-secondary)]">
            No active or historical loan accounts found for this borrower.
          </p>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="bg-black/5 dark:bg-white/5 border-b border-[var(--bdae-border)] text-[var(--bdae-text-secondary)]">
                <th className="py-3 px-4 font-semibold">Account No</th>
                <th className="py-3 px-4 font-semibold text-right">Principal</th>
                <th className="py-3 px-4 font-semibold text-right">Outstanding</th>
                <th className="py-3 px-4 font-semibold text-center">Rate</th>
                <th className="py-3 px-4 font-semibold text-center">Term</th>
                <th className="py-3 px-4 font-semibold">Status</th>
                <th className="py-3 px-4 font-semibold text-center">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[var(--bdae-border)]">
              {accounts.map((acc) => (
                <tr
                  key={acc.accountId}
                  className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
                >
                  <td className="py-3 px-4">
                    <MaskedDataField
                      value={acc.accountNo || acc.accountId}
                      maskType="accountNumber"
                      allowReveal={true}
                    />
                  </td>
                  <td className="py-3 px-4 text-right font-mono font-bold text-[var(--bdae-text-primary)]">
                    {formatCurrency(acc.principalAmount)}
                  </td>
                  <td className="py-3 px-4 text-right font-mono font-black text-red-600 dark:text-red-400">
                    {formatCurrency(acc.outstandingBalance ?? acc.principalAmount)}
                  </td>
                  <td className="py-3 px-4 text-center font-mono font-medium">
                    {acc.interestRatePct ?? '14.00'}%
                  </td>
                  <td className="py-3 px-4 text-center text-[var(--bdae-text-secondary)]">
                    {acc.termMonths ?? '12'} Mo.
                  </td>
                  <td className="py-3 px-4">{getStatusBadge(acc.status)}</td>
                  <td className="py-3 px-4 text-center">
                    <div className="flex items-center justify-center gap-2">
                      <PermissionGuard permissions={[PERMISSIONS.LOAN_ACCOUNT_VIEW]}>
                        <button
                          type="button"
                          onClick={() => onOpenSchedule(acc)}
                          className="px-2.5 py-1 rounded-lg bg-black/5 dark:bg-white/5 hover:bg-purple-500/10 hover:text-purple-600 border border-[var(--bdae-border)] font-bold text-[11px] flex items-center gap-1 transition-colors"
                          title="View Amortization Schedule"
                        >
                          <Calendar className="w-3.5 h-3.5" />
                          <span>Schedule</span>
                        </button>
                      </PermissionGuard>

                      <PermissionGuard permissions={[PERMISSIONS.LOAN_REPAYMENT_PROCESS]}>
                        <button
                          type="button"
                          onClick={() => onOpenRepay(acc)}
                          className="px-2.5 py-1 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-[11px] flex items-center gap-1 transition-colors shadow-sm"
                          title="Process Waterfall Repayment"
                        >
                          <CreditCard className="w-3.5 h-3.5" />
                          <span>Repay</span>
                        </button>
                      </PermissionGuard>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
