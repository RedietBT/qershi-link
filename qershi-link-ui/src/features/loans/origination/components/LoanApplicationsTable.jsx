import React from 'react';
import {
  FileText,
  CheckCircle2,
  XCircle,
  Clock,
  Loader2,
  FolderOpen,
  Eye
} from 'lucide-react';
import { formatCurrency, formatDateTime } from '../../../../common/utils/currency';
import { PermissionGuard } from '../../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../../common/constants/permissions';
import { MaskedDataField } from '../../../../common/components/MaskedDataField';

export const LoanApplicationsTable = ({
  applications = [],
  loading = false,
  searched = false,
  onSelectApplication
}) => {
  const getStatusBadge = (status) => {
    switch (status) {
      case 'APPROVED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-600 dark:text-emerald-400">
            <CheckCircle2 className="w-3 h-3" />
            Approved
          </span>
        );
      case 'DISBURSED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-purple-500/10 text-purple-600 dark:text-purple-400">
            <CheckCircle2 className="w-3 h-3" />
            Disbursed
          </span>
        );
      case 'REJECTED':
      case 'REJECTED_ELIGIBILITY':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-red-500/10 text-red-600 dark:text-red-400">
            <XCircle className="w-3 h-3" />
            Rejected
          </span>
        );
      case 'SUBMITTED':
      case 'UNDER_REVIEW':
      default:
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/10 text-amber-600 dark:text-amber-400">
            <Clock className="w-3 h-3" />
            {status || 'Pending Review'}
          </span>
        );
    }
  };

  return (
    <div className="bdae-card border border-[var(--bdae-border)] overflow-hidden shadow-sm rounded-2xl">
      <div className="p-4 border-b border-[var(--bdae-border)] flex items-center justify-between">
        <h2 className="text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
          Loan Applications {searched && `(${applications.length} records)`}
        </h2>
      </div>

      {loading ? (
        <div className="flex flex-col items-center justify-center py-16 space-y-3">
          <Loader2 className="w-8 h-8 animate-spin text-[var(--bdae-primary)]" />
          <p className="text-xs text-[var(--bdae-text-secondary)] font-medium">
            Retrieving loan records from origination service...
          </p>
        </div>
      ) : !searched && applications.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-16 space-y-2 text-center">
          <FolderOpen className="w-10 h-10 text-[var(--bdae-text-secondary)]/50" />
          <p className="text-xs font-bold text-[var(--bdae-text-primary)]">
            No Member Applications Queried
          </p>
          <p className="text-[11px] text-[var(--bdae-text-secondary)] max-w-sm">
            Search by Borrower User ID above or click "Originate Loan Application" to submit a new credit request.
          </p>
        </div>
      ) : applications.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-16 space-y-2 text-center">
          <FileText className="w-10 h-10 text-[var(--bdae-text-secondary)]/50" />
          <p className="text-xs font-bold text-[var(--bdae-text-primary)]">
            No Loan Applications Found
          </p>
          <p className="text-[11px] text-[var(--bdae-text-secondary)]">
            No loan applications found matching the selected criteria.
          </p>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="bg-black/5 dark:bg-white/5 border-b border-[var(--bdae-border)] text-[var(--bdae-text-secondary)]">
                <th className="py-3 px-4 font-semibold">Application Ref</th>
                <th className="py-3 px-4 font-semibold">Borrower Identity</th>
                <th className="py-3 px-4 font-semibold">Model</th>
                <th className="py-3 px-4 font-semibold text-right">Requested (ETB)</th>
                <th className="py-3 px-4 font-semibold text-center">Credit Score</th>
                <th className="py-3 px-4 font-semibold">Status</th>
                <th className="py-3 px-4 font-semibold">Date</th>
                <th className="py-3 px-4 font-semibold text-center">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[var(--bdae-border)]">
              {applications.map((app) => (
                <tr
                  key={app.applicationId}
                  className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
                >
                  <td className="py-3 px-4 font-mono font-bold text-[var(--bdae-text-primary)]">
                    {app.applicationNo || app.applicationId.slice(0, 13)}...
                  </td>
                  <td className="py-3 px-4">
                    <MaskedDataField
                      value={app.userId}
                      maskType="memberId"
                      allowReveal={true}
                    />
                  </td>
                  <td className="py-3 px-4 text-[var(--bdae-text-secondary)] font-medium">
                    {app.scoringType || 'INDIVIDUAL'}
                  </td>
                  <td className="py-3 px-4 text-right font-mono font-black text-[var(--bdae-primary)]">
                    {formatCurrency(app.amountRequested)}
                  </td>
                  <td className="py-3 px-4 text-center">
                    <span className="font-mono font-bold text-xs bg-purple-500/10 text-purple-600 dark:text-purple-400 px-2 py-0.5 rounded-md">
                      {app.creditScoring?.calculatedScore ?? '—'} / 100
                    </span>
                  </td>
                  <td className="py-3 px-4">{getStatusBadge(app.status)}</td>
                  <td className="py-3 px-4 text-[var(--bdae-text-secondary)]">
                    {formatDateTime(app.createdAt)}
                  </td>
                  <td className="py-3 px-4 text-center">
                    <PermissionGuard
                      permissions={[
                        PERMISSIONS.LOAN_APPLICATION_APPROVE,
                        PERMISSIONS.LOAN_APPLICATION_VIEW
                      ]}
                      fallback={
                        <span className="text-[10px] text-[var(--bdae-text-secondary)] opacity-50">
                          Restricted
                        </span>
                      }
                    >
                      <button
                        type="button"
                        onClick={() => onSelectApplication(app)}
                        className="px-3 py-1 rounded-lg bg-[var(--bdae-primary)]/10 text-[var(--bdae-primary)] hover:bg-[var(--bdae-primary)] hover:text-white font-bold text-[11px] transition-all flex items-center gap-1 mx-auto"
                      >
                        <Eye className="w-3 h-3" />
                        <span>Inspect / Review</span>
                      </button>
                    </PermissionGuard>
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
