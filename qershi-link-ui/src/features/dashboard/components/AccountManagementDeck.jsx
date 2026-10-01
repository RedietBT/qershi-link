import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Landmark, PackagePlus, ClipboardCheck, GitFork, CreditCard, CalendarClock } from 'lucide-react';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';

/**
 * Account Configuration, Branch Hierarchy & Product Engine Dashboard Deck
 */
export const AccountManagementDeck = () => {
  const navigate = useNavigate();

  return (
    <PermissionGuard
      permissions={[PERMISSIONS.ACCOUNT_VIEW, PERMISSIONS.PRODUCT_VIEW, PERMISSIONS.ACCOUNT_APPROVE, PERMISSIONS.BRANCH_VIEW]}
      roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'BRANCH_MANAGER']}
    >
      <div className="bdae-card p-6 space-y-4 border border-[var(--bdae-border)] shadow-xl">
        <h2 className="text-sm font-bold border-b border-[var(--bdae-border)] pb-2 flex items-center gap-2">
          <Landmark className="w-4 h-4 text-teal-500" />
          <span>Core Accounts & Product Factory</span>
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4 text-xs">
          {/* Card 1: Member Accounts Lookup */}
          <PermissionGuard permissions={[PERMISSIONS.ACCOUNT_VIEW]}>
            <div
              onClick={() => navigate('/accounts')}
              className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-teal-500 cursor-pointer space-y-2 transition-all shadow-sm group"
            >
              <div className="w-9 h-9 rounded-xl bg-teal-500/10 text-teal-600 flex items-center justify-center font-bold">
                <CreditCard className="w-5 h-5" />
              </div>
              <div>
                <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-teal-500 transition-colors">
                  Member Accounts
                </p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                  Lookup balances, place/release liens, and view statement histories.
                </p>
              </div>
            </div>
          </PermissionGuard>

          {/* Card 2: SACCO Entity Configuration */}
          <PermissionGuard roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN']} permissions={[PERMISSIONS.ACCOUNT_VIEW, PERMISSIONS.SACCO_CONFIG]}>
            <div
              onClick={() => navigate('/accounts/config')}
              className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-teal-500 cursor-pointer space-y-2 transition-all shadow-sm group"
            >
              <div className="w-9 h-9 rounded-xl bg-teal-500/10 text-teal-600 flex items-center justify-center font-bold">
                <Landmark className="w-5 h-5" />
              </div>
              <div>
                <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-teal-500 transition-colors">
                  SACCO Configuration
                </p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                  Configure SACCO tenant codes for ISO Luhn checksum account numbers.
                </p>
              </div>
            </div>
          </PermissionGuard>

          {/* Card 3: Deposit Product Factory */}
          <PermissionGuard roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN']} permissions={[PERMISSIONS.PRODUCT_VIEW, PERMISSIONS.PRODUCT_MANAGE]}>
            <div
              onClick={() => navigate('/accounts/products')}
              className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-violet-500 cursor-pointer space-y-2 transition-all shadow-sm group"
            >
              <div className="w-9 h-9 rounded-xl bg-violet-500/10 text-violet-600 flex items-center justify-center font-bold">
                <PackagePlus className="w-5 h-5" />
              </div>
              <div>
                <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-violet-500 transition-colors">
                  Deposit Product Factory
                </p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                  Define savings, mandatory shares, and term deposit account terms.
                </p>
              </div>
            </div>
          </PermissionGuard>

          {/* Card 4: Pending Authorizations */}
          <PermissionGuard roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN']} permissions={[PERMISSIONS.ACCOUNT_APPROVE]}>
            <div
              onClick={() => navigate('/accounts/pending')}
              className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-emerald-500 cursor-pointer space-y-2 transition-all shadow-sm group"
            >
              <div className="w-9 h-9 rounded-xl bg-emerald-500/10 text-emerald-600 flex items-center justify-center font-bold">
                <ClipboardCheck className="w-5 h-5" />
              </div>
              <div>
                <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-emerald-500 transition-colors">
                  Pending Authorizations
                </p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                  Four-Eye Maker/Checker authorization queue for pending accounts.
                </p>
              </div>
            </div>
          </PermissionGuard>

          {/* Card 5: Branch Hierarchy */}
          <PermissionGuard permissions={[PERMISSIONS.BRANCH_VIEW]}>
            <div
              onClick={() => navigate('/branches')}
              className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-cyan-500 cursor-pointer space-y-2 transition-all shadow-sm group"
            >
              <div className="w-9 h-9 rounded-xl bg-cyan-500/10 text-cyan-600 flex items-center justify-center font-bold">
                <GitFork className="w-5 h-5" />
              </div>
              <div>
                <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-cyan-500 transition-colors">
                  Branch Hierarchy
                </p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                  HQ, regional branches, and sub-branch physical locations.
                </p>
              </div>
            </div>
          </PermissionGuard>

          {/* Card 6: Term Deposits (FD) */}
          <PermissionGuard permissions={[PERMISSIONS.ACCOUNT_VIEW]}>
            <div
              onClick={() => navigate('/accounts/term-deposits')}
              className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-amber-500 cursor-pointer space-y-2 transition-all shadow-sm group"
            >
              <div className="w-9 h-9 rounded-xl bg-amber-500/10 text-amber-600 flex items-center justify-center font-bold">
                <CalendarClock className="w-5 h-5" />
              </div>
              <div>
                <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-amber-500 transition-colors">
                  Term Deposits (FD)
                </p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                  Fixed deposit contracts, early break penalties, and rollover.
                </p>
              </div>
            </div>
          </PermissionGuard>
        </div>
      </div>
    </PermissionGuard>
  );
};
