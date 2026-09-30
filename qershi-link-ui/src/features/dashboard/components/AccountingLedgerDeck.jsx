import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Scale, FolderTree, FileText } from 'lucide-react';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';

/**
 * Chart of Accounts & Financial Reports Dashboard Deck
 */
export const AccountingLedgerDeck = () => {
  const navigate = useNavigate();

  return (
    <PermissionGuard
      permissions={[PERMISSIONS.COA_VIEW, PERMISSIONS.COA_MANAGE, PERMISSIONS.FINANCIAL_REPORT_VIEW]}
      roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'AUDITOR']}
    >
      <div className="bdae-card p-6 space-y-4 border border-[var(--bdae-border)] shadow-xl">
        <h2 className="text-sm font-bold border-b border-[var(--bdae-border)] pb-2 flex items-center gap-2">
          <Scale className="w-4 h-4 text-purple-500" />
          <span>General Ledger & Financial Accounting</span>
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
          {/* Card 1: Chart of Accounts Tree */}
          <PermissionGuard permissions={[PERMISSIONS.COA_VIEW, PERMISSIONS.COA_MANAGE]}>
            <div
              onClick={() => navigate('/accounting/chart-of-accounts')}
              className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-purple-500 cursor-pointer space-y-2 transition-all shadow-sm group"
            >
              <div className="w-9 h-9 rounded-xl bg-purple-500/10 text-purple-600 flex items-center justify-center font-bold">
                <FolderTree className="w-5 h-5" />
              </div>
              <div>
                <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-purple-500 transition-colors">
                  Chart of Accounts (COA)
                </p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                  Multi-tiered GL tree structure with real-time balance rollups across Assets, Liabilities, and Equity.
                </p>
              </div>
            </div>
          </PermissionGuard>

          {/* Card 2: Financial Statements */}
          <PermissionGuard permissions={[PERMISSIONS.FINANCIAL_REPORT_VIEW]}>
            <div
              onClick={() => navigate('/accounting/reports')}
              className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-[var(--bdae-primary)] cursor-pointer space-y-2 transition-all shadow-sm group"
            >
              <div className="w-9 h-9 rounded-xl bg-blue-500/10 text-blue-600 flex items-center justify-center font-bold">
                <FileText className="w-5 h-5" />
              </div>
              <div>
                <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-[var(--bdae-primary)] transition-colors">
                  Financial Statements & Reports
                </p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                  Institutional Trial Balance, Balance Sheet equilibrium check, and period Profit & Loss statements.
                </p>
              </div>
            </div>
          </PermissionGuard>
        </div>
      </div>
    </PermissionGuard>
  );
};
