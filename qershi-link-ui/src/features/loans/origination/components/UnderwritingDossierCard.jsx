import React from 'react';
import {
  ClipboardCheck,
  ShieldAlert,
  BadgePercent
} from 'lucide-react';
import { formatCurrency, formatDateTime } from '../../../../common/utils/currency';
import { MaskedDataField } from '../../../../common/components/MaskedDataField';
import { PermissionGuard } from '../../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../../common/constants/permissions';

export const UnderwritingDossierCard = ({
  application,
  isApplicant,
  onOpenDecision
}) => {
  if (!application) return null;

  return (
    <div className="bdae-card p-6 border border-[var(--bdae-border)] space-y-6 shadow-md rounded-2xl animate-fadeIn">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-[var(--bdae-border)] pb-4">
        <div>
          <span className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)]">
            Credit Application Dossier
          </span>
          <h2 className="text-lg font-black text-[var(--bdae-text-primary)] font-mono">
            {application.applicationNo || application.applicationId}
          </h2>
          <p className="text-xs text-[var(--bdae-text-secondary)] mt-0.5">
            Submitted on: {formatDateTime(application.createdAt)}
          </p>
        </div>

        <div className="flex items-center gap-2">
          <span className="px-3 py-1 rounded-full text-xs font-bold bg-amber-500/10 text-amber-600 dark:text-amber-400">
            {application.status}
          </span>
        </div>
      </div>

      {/* Self-Approval Warning */}
      {isApplicant && (
        <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 flex items-start space-x-3 text-xs">
          <ShieldAlert className="w-5 h-5 shrink-0 mt-0.5" />
          <div>
            <p className="font-bold">Maker-Checker Dual Control Guard Triggered</p>
            <p className="mt-0.5 opacity-90">
              You are the borrower for this application. Core banking rules strictly prevent self-approval. Another loan officer or supervisor must underwrite this loan.
            </p>
          </div>
        </div>
      )}

      {/* Financial Breakdown Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 p-4 rounded-xl bg-black/5 dark:bg-white/5 text-xs">
        <div>
          <span className="text-[10px] text-[var(--bdae-text-secondary)] uppercase font-bold block">
            Requested Principal
          </span>
          <span className="font-mono text-base font-black text-[var(--bdae-primary)]">
            {formatCurrency(application.amountRequested)}
          </span>
        </div>
        <div>
          <span className="text-[10px] text-[var(--bdae-text-secondary)] uppercase font-bold block">
            Approved Principal
          </span>
          <span className="font-mono text-base font-black text-emerald-600 dark:text-emerald-400">
            {application.amountApproved ? formatCurrency(application.amountApproved) : 'Pending Review'}
          </span>
        </div>
        <div>
          <span className="text-[10px] text-[var(--bdae-text-secondary)] uppercase font-bold block">
            Borrower Member ID
          </span>
          <div className="mt-0.5">
            <MaskedDataField
              value={application.userId}
              maskType="memberId"
              allowReveal={true}
            />
          </div>
        </div>
        <div>
          <span className="text-[10px] text-[var(--bdae-text-secondary)] uppercase font-bold block">
            Scoring Model
          </span>
          <span className="font-bold text-[var(--bdae-text-primary)]">
            {application.scoringType || 'INDIVIDUAL'}
          </span>
        </div>
      </div>

      {/* Credit Scoring Analysis */}
      {application.creditScoring && (
        <div className="p-4 rounded-xl border border-[var(--bdae-border)] space-y-3">
          <div className="flex items-center justify-between">
            <h3 className="text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-primary)] flex items-center gap-1.5">
              <BadgePercent className="w-4 h-4 text-purple-600" />
              Credit Scoring Analysis
            </h3>
            <span
              className={`px-2.5 py-0.5 rounded-full text-[10px] font-bold ${
                application.creditScoring.passedEligibility
                  ? 'bg-emerald-500/10 text-emerald-600'
                  : 'bg-red-500/10 text-red-600'
              }`}
            >
              {application.creditScoring.passedEligibility ? 'Eligibility Passed' : 'Eligibility Failed'}
            </span>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
            <div>
              <span className="text-[10px] text-[var(--bdae-text-secondary)] block">Risk Score</span>
              <span className="font-mono font-black text-sm text-purple-600">
                {application.creditScoring.calculatedScore ?? '—'} / 100
              </span>
            </div>
            <div>
              <span className="text-[10px] text-[var(--bdae-text-secondary)] block">Savings Consistency</span>
              <span className="font-mono font-bold text-[var(--bdae-text-primary)]">
                {application.creditScoring.savingsConsistency ?? '—'}%
              </span>
            </div>
            <div>
              <span className="text-[10px] text-[var(--bdae-text-secondary)] block">Historical Yield</span>
              <span className="font-mono font-bold text-[var(--bdae-text-primary)]">
                {formatCurrency(application.creditScoring.historicalYield || 0)}
              </span>
            </div>
            <div>
              <span className="text-[10px] text-[var(--bdae-text-secondary)] block">Land Size</span>
              <span className="font-mono font-bold text-[var(--bdae-text-primary)]">
                {application.creditScoring.landSizeHectares ?? '—'} Hectares
              </span>
            </div>
          </div>
        </div>
      )}

      {/* Underwriting Action CTA strictly guarded by LOAN_APPLICATION_APPROVE */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pt-4 border-t border-[var(--bdae-border)]">
        <span className="text-xs text-[var(--bdae-text-secondary)]">
          {isApplicant
            ? 'Self-approval disabled by Maker-Checker separation.'
            : 'Click below to review credit dossier and enter dual-control underwriting decision.'}
        </span>

        <PermissionGuard
          permissions={[PERMISSIONS.LOAN_APPLICATION_APPROVE]}
          fallback={
            <span className="text-xs font-bold text-[var(--bdae-text-secondary)] opacity-60">
              Underwriting Approval Permission Required
            </span>
          }
        >
          <button
            type="button"
            disabled={isApplicant || application.status === 'APPROVED' || application.status === 'DISBURSED'}
            onClick={() => onOpenDecision(application)}
            className="px-5 py-2.5 bg-purple-600 hover:bg-purple-700 disabled:opacity-50 text-white rounded-xl text-xs font-bold shadow-md transition-all flex items-center justify-center gap-2"
          >
            <ClipboardCheck className="w-4 h-4" />
            Perform Underwriting Decision
          </button>
        </PermissionGuard>
      </div>
    </div>
  );
};
