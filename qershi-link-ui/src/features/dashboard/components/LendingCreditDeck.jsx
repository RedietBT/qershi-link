import React from 'react';
import { useNavigate } from 'react-router-dom';
import { BadgePercent, FileSpreadsheet, CheckSquare, AlertTriangle } from 'lucide-react';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';

/**
 * Credit & Lending Operations (LOS & LMS) Dashboard Deck
 */
export const LendingCreditDeck = () => {
  const navigate = useNavigate();

  return (
    <PermissionGuard
      permissions={[
        PERMISSIONS.LOAN_APPLICATION_CREATE,
        PERMISSIONS.LOAN_APPLICATION_VIEW,
        PERMISSIONS.LOAN_APPLICATION_APPROVE,
        PERMISSIONS.LOAN_ACCOUNT_VIEW,
        PERMISSIONS.LOAN_DELINQUENCY_VIEW,
      ]}
    >
      <div className="bdae-card p-6 space-y-4 border border-[var(--bdae-border)] shadow-xl">
        <h2 className="text-sm font-bold border-b border-[var(--bdae-border)] pb-2 flex items-center gap-2">
          <BadgePercent className="w-4 h-4 text-emerald-500" />
          <span>Credit & Loan Management</span>
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 text-xs">
          {/* Card 1: Loan Applications Portfolio */}
          <PermissionGuard permissions={[PERMISSIONS.LOAN_APPLICATION_CREATE, PERMISSIONS.LOAN_APPLICATION_VIEW]}>
            <div
              onClick={() => navigate('/loans/applications')}
              className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-emerald-500 cursor-pointer space-y-2 transition-all shadow-sm group"
            >
              <div className="w-9 h-9 rounded-xl bg-emerald-500/10 text-emerald-600 flex items-center justify-center font-bold">
                <FileSpreadsheet className="w-5 h-5" />
              </div>
              <div>
                <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-emerald-500 transition-colors">
                  Loan Applications
                </p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                  Intake member loan applications with automatic affordability checks.
                </p>
              </div>
            </div>
          </PermissionGuard>

          {/* Card 2: Maker-Checker Underwriting Queue */}
          <PermissionGuard permissions={[PERMISSIONS.LOAN_APPLICATION_APPROVE]}>
            <div
              onClick={() => navigate('/loans/underwriting')}
              className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-blue-500 cursor-pointer space-y-2 transition-all shadow-sm group"
            >
              <div className="w-9 h-9 rounded-xl bg-blue-500/10 text-blue-600 flex items-center justify-center font-bold">
                <CheckSquare className="w-5 h-5" />
              </div>
              <div>
                <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-blue-500 transition-colors">
                  Loan Underwriting
                </p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                  Four-Eye credit committee appraisal, approval, and rejection workflow.
                </p>
              </div>
            </div>
          </PermissionGuard>

          {/* Card 3: Active Loan Accounts */}
          <PermissionGuard permissions={[PERMISSIONS.LOAN_ACCOUNT_VIEW, PERMISSIONS.LOAN_REPAYMENT_PROCESS]}>
            <div
              onClick={() => navigate('/loans/accounts')}
              className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-violet-500 cursor-pointer space-y-2 transition-all shadow-sm group"
            >
              <div className="w-9 h-9 rounded-xl bg-violet-500/10 text-violet-600 flex items-center justify-center font-bold">
                <BadgePercent className="w-5 h-5" />
              </div>
              <div>
                <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-violet-500 transition-colors">
                  Active Loan Portfolios
                </p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                  Disbursements, repayment schedules, amortization tables, and settlements.
                </p>
              </div>
            </div>
          </PermissionGuard>

          {/* Card 4: Portfolio at Risk (PAR) */}
          <PermissionGuard permissions={[PERMISSIONS.LOAN_DELINQUENCY_VIEW, PERMISSIONS.LOAN_ACCOUNT_VIEW]}>
            <div
              onClick={() => navigate('/loans/delinquency')}
              className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-red-500 cursor-pointer space-y-2 transition-all shadow-sm group"
            >
              <div className="w-9 h-9 rounded-xl bg-red-500/10 text-red-600 flex items-center justify-center font-bold">
                <AlertTriangle className="w-5 h-5" />
              </div>
              <div>
                <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-red-500 transition-colors">
                  PAR & Delinquency Aging
                </p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                  Track 30/60/90+ day aging buckets and IFRS-9 loss provisioning.
                </p>
              </div>
            </div>
          </PermissionGuard>
        </div>
      </div>
    </PermissionGuard>
  );
};
