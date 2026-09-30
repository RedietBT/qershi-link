import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Banknote, ArrowLeftRight, History, Vault } from 'lucide-react';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';

/**
 * Teller Operations, Cash Desk, Transfers & Drawer Deck
 */
export const BankingOperationsDeck = () => {
  const navigate = useNavigate();

  return (
    <PermissionGuard
      permissions={[
        PERMISSIONS.CASH_DEPOSIT,
        PERMISSIONS.SAVINGS_WITHDRAW,
        PERMISSIONS.MEMBER_TRANSFER,
        PERMISSIONS.TRANSACTION_VIEW,
        PERMISSIONS.TELLER_TILL_VIEW,
      ]}
    >
      <div className="bdae-card p-6 space-y-4 border border-[var(--bdae-border)] shadow-xl">
        <h2 className="text-sm font-bold border-b border-[var(--bdae-border)] pb-2 flex items-center gap-2">
          <Banknote className="w-4 h-4 text-emerald-500" />
          <span>Core Banking & Teller Operations</span>
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 text-xs">
          {/* Card 1: Cash Desk (OTC Deposits & Withdrawals) */}
          <PermissionGuard permissions={[PERMISSIONS.CASH_DEPOSIT, PERMISSIONS.SAVINGS_WITHDRAW]}>
            <div
              onClick={() => navigate('/transactions/cash')}
              className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-emerald-500 cursor-pointer space-y-2 transition-all shadow-sm group"
            >
              <div className="w-9 h-9 rounded-xl bg-emerald-500/10 text-emerald-600 flex items-center justify-center font-bold">
                <Banknote className="w-5 h-5" />
              </div>
              <div>
                <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-emerald-500 transition-colors">
                  Teller Cash Desk (OTC)
                </p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                  Execute cash deposits and withdrawals with real-time balance checks.
                </p>
              </div>
            </div>
          </PermissionGuard>

          {/* Card 2: Member-to-Member Transfers */}
          <PermissionGuard permissions={[PERMISSIONS.MEMBER_TRANSFER]}>
            <div
              onClick={() => navigate('/transactions/transfer')}
              className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-[var(--bdae-secondary)] cursor-pointer space-y-2 transition-all shadow-sm group"
            >
              <div className="w-9 h-9 rounded-xl bg-cyan-500/10 text-cyan-600 flex items-center justify-center font-bold">
                <ArrowLeftRight className="w-5 h-5" />
              </div>
              <div>
                <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-[var(--bdae-secondary)] transition-colors">
                  Member Funds Transfer
                </p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                  Process internal intra-SACCO account transfers with balanced settlement.
                </p>
              </div>
            </div>
          </PermissionGuard>

          {/* Card 3: General Ledger Statements */}
          <PermissionGuard permissions={[PERMISSIONS.TRANSACTION_VIEW, PERMISSIONS.ACCOUNT_VIEW]}>
            <div
              onClick={() => navigate('/transactions/history')}
              className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-[var(--bdae-primary)] cursor-pointer space-y-2 transition-all shadow-sm group"
            >
              <div className="w-9 h-9 rounded-xl bg-[var(--bdae-primary)]/10 text-[var(--bdae-primary)] flex items-center justify-center font-bold">
                <History className="w-5 h-5" />
              </div>
              <div>
                <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-[var(--bdae-primary)] transition-colors">
                  GL Statements & Postings
                </p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                  Inquire account histories and inspect balanced double-entry journal lines.
                </p>
              </div>
            </div>
          </PermissionGuard>

          {/* Card 4: Teller Drawer (Till) Reconciliation */}
          <PermissionGuard
            roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER', 'BRANCH_MANAGER']}
            permissions={[PERMISSIONS.CASH_DEPOSIT, PERMISSIONS.SAVINGS_WITHDRAW, PERMISSIONS.TELLER_TILL_VIEW]}
          >
            <div
              onClick={() => navigate('/transactions/till')}
              className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-amber-500 cursor-pointer space-y-2 transition-all shadow-sm group"
            >
              <div className="w-9 h-9 rounded-xl bg-amber-500/10 text-amber-600 flex items-center justify-center font-bold">
                <Vault className="w-5 h-5" />
              </div>
              <div>
                <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-amber-500 transition-colors">
                  Teller Cash Drawer (Till)
                </p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                  Manage till allocations, reconciliations, and vault transfers.
                </p>
              </div>
            </div>
          </PermissionGuard>
        </div>
      </div>
    </PermissionGuard>
  );
};
