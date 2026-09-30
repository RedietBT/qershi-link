import React, { useState } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  Banknote,
  Vault,
  ArrowLeftRight,
  History,
  FileSpreadsheet,
  ClipboardCheck,
  BadgePercent,
  Activity,
  CreditCard,
  PackagePlus,
  GitFork,
  FolderTree,
  Scale,
  Moon,
  Contact,
  ShieldCheck,
  Building2,
  PlusCircle,
  Users,
  Shield,
  ShieldAlert,
  Landmark,
  ChevronRight,
  ChevronDown,
  ArrowLeft,
  ArrowRight,
} from 'lucide-react';
import { useAuthStore } from '../store/useAuthStore';
import { PermissionGuard } from './PermissionGuard';
import { PERMISSIONS, ROLES } from '../constants/permissions';
import { getUserDisplayName, formatRole } from '../utils/masking';

/**
 * Visual Module Section Divider matching the reference design:
 * ──── SECTION NAME ────
 */
const SectionDivider = ({ title, isCollapsed }) => {
  if (isCollapsed) {
    return <div className="h-px bg-[#00CDDB]/20 my-3 mx-2" title={title} />;
  }

  return (
    <div className="flex items-center gap-2 px-3 pt-4 pb-1 text-[10px] font-extrabold uppercase tracking-widest text-[#00CDDB]">
      <span className="h-px flex-1 bg-[#00CDDB]/30" />
      <span className="shrink-0">{title}</span>
      <span className="h-px flex-1 bg-[#00CDDB]/30" />
    </div>
  );
};

/**
 * Top-level Flat NavLink Item
 * Active state features a thick vertical cyan accent bar and cyan highlighted text
 */
const NavItem = ({ path, label, icon: Icon, isCollapsed }) => (
  <NavLink
    to={path}
    title={isCollapsed ? label : undefined}
    className={({ isActive }) =>
      `relative flex items-center ${
        isCollapsed ? 'justify-center px-0 py-2.5' : 'justify-between px-3.5 py-2.5'
      } text-xs font-bold transition-all duration-150 group rounded-xl ${
        isActive
          ? 'text-[#00CDDB] bg-[#00CDDB]/10'
          : 'text-[var(--bdae-text-primary)] hover:bg-black/[0.03] dark:hover:bg-white/[0.03] hover:text-[#00CDDB]'
      }`
    }
  >
    {({ isActive }) => (
      <>
        {/* Thick Left Cyan Accent Bar for Active item */}
        {isActive && (
          <span className="absolute left-0 top-1.5 bottom-1.5 w-1 bg-[#00CDDB] rounded-r-md shadow-sm shadow-[#00CDDB]/50" />
        )}

        <div className={`flex items-center gap-3 ${isCollapsed ? 'justify-center' : ''}`}>
          <Icon
            className={`w-4 h-4 shrink-0 transition-colors ${
              isActive ? 'text-[#00CDDB]' : 'text-[var(--bdae-text-secondary)] group-hover:text-[#00CDDB]'
            }`}
          />
          {!isCollapsed && <span className="tracking-tight">{label}</span>}
        </div>
      </>
    )}
  </NavLink>
);

/**
 * Collapsible / Expandable Service Group (e.g. Loan Requests >)
 */
const NavGroup = ({ label, icon: Icon, isCollapsed, children, defaultOpen = false }) => {
  const location = useLocation();
  const isChildActive = React.Children.toArray(children).some(
    (child) => child?.props?.path && location.pathname.startsWith(child.props.path)
  );
  const [isOpen, setIsOpen] = useState(defaultOpen || isChildActive);

  if (isCollapsed) {
    return (
      <div className="py-1">
        <div
          title={label}
          className="flex items-center justify-center py-2 text-[var(--bdae-text-secondary)] hover:text-[#00CDDB] cursor-pointer"
          onClick={() => setIsOpen(!isOpen)}
        >
          <Icon className="w-4 h-4" />
        </div>
        {isOpen && <div className="space-y-1">{children}</div>}
      </div>
    );
  }

  return (
    <div className="space-y-0.5">
      <button
        type="button"
        onClick={() => setIsOpen((prev) => !prev)}
        className={`w-full flex items-center justify-between px-3.5 py-2.5 text-xs font-bold rounded-xl transition-all ${
          isChildActive
            ? 'text-[#00CDDB] font-extrabold'
            : 'text-[var(--bdae-text-primary)] hover:bg-black/[0.03] dark:hover:bg-white/[0.03] hover:text-[#00CDDB]'
        }`}
      >
        <div className="flex items-center gap-3">
          <Icon
            className={`w-4 h-4 shrink-0 ${
              isChildActive ? 'text-[#00CDDB]' : 'text-[var(--bdae-text-secondary)]'
            }`}
          />
          <span className="tracking-tight">{label}</span>
        </div>
        {isOpen ? (
          <ChevronDown className="w-3.5 h-3.5 opacity-60 text-[#00CDDB]" />
        ) : (
          <ChevronRight className="w-3.5 h-3.5 opacity-50" />
        )}
      </button>

      {isOpen && (
        <div className="ml-3 pl-3 border-l border-[#00CDDB]/20 space-y-0.5 pt-0.5 pb-1 animate-fadeIn">
          {children}
        </div>
      )}
    </div>
  );
};

/**
 * Sub-item nested within a NavGroup
 */
const SubNavItem = ({ path, label, icon: Icon, isCollapsed }) => (
  <NavLink
    to={path}
    title={isCollapsed ? label : undefined}
    className={({ isActive }) =>
      `relative w-full flex items-center gap-2.5 px-3 py-1.5 rounded-lg text-[11px] font-semibold transition-all ${
        isActive
          ? 'text-[#00CDDB] bg-[#00CDDB]/10 font-bold'
          : 'text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)] hover:bg-black/[0.02] dark:hover:bg-white/[0.02]'
      }`
    }
  >
    {({ isActive }) => (
      <>
        {isActive && (
          <span className="absolute left-0 top-1 bottom-1 w-0.5 bg-[#00CDDB] rounded-r" />
        )}
        <Icon className={`w-3.5 h-3.5 shrink-0 ${isActive ? 'text-[#00CDDB]' : 'opacity-70'}`} />
        <span>{label}</span>
      </>
    )}
  </NavLink>
);

/**
 * Core Banking Departmental Sidebar
 */
export const Sidebar = () => {
  const [isCollapsed, setIsCollapsed] = useState(false);
  const user = useAuthStore((state) => state.user);

  const displayName = getUserDisplayName(user);
  const roleTitle = formatRole(user?.globalRole || user?.roles?.[0]);

  return (
    <aside
      className={`${
        isCollapsed ? 'w-18' : 'w-64'
      } bdae-surface border-r border-[var(--bdae-border)] flex flex-col justify-between h-[calc(100vh-4rem)] sticky top-16 transition-all duration-300 z-20`}
    >
      <div className="p-3 space-y-1 overflow-y-auto custom-scrollbar flex-1">
        {/* ── TOP OPERATOR & ROLE CONTEXT (Matches reference header) ── */}
        {!isCollapsed ? (
          <div className="px-3 py-2.5 mb-2 rounded-xl bg-black/[0.02] dark:bg-white/[0.02] border border-[var(--bdae-border)] grid grid-cols-2 text-center divide-x divide-[var(--bdae-border)]">
            <div className="pr-2">
              <span className="text-[10px] font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider block">
                Operator
              </span>
              <span
                className="text-xs font-extrabold text-[var(--bdae-text-primary)] block truncate"
                title={displayName}
              >
                {displayName}
              </span>
            </div>
            <div className="pl-2">
              <span className="text-[10px] font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider block">
                Clearance
              </span>
              <span
                className="text-xs font-extrabold text-[#00CDDB] block truncate"
                title={roleTitle}
              >
                {roleTitle}
              </span>
            </div>
          </div>
        ) : (
          <div className="py-2 text-center border-b border-[var(--bdae-border)] mb-2">
            <div className="w-2 h-2 rounded-full bg-emerald-400 mx-auto animate-pulse" />
          </div>
        )}

        {/* ── 0. DASHBOARD WORKSPACE ── */}
        <NavItem
          path="/dashboard"
          label="Dashboard"
          icon={LayoutDashboard}
          isCollapsed={isCollapsed}
        />

        {/* ── 1. BANKING OPERATIONS ── */}
        <PermissionGuard
          permissions={[
            PERMISSIONS.CASH_DEPOSIT,
            PERMISSIONS.SAVINGS_WITHDRAW,
            PERMISSIONS.MEMBER_TRANSFER,
            PERMISSIONS.TRANSACTION_VIEW,
            PERMISSIONS.TELLER_TILL_VIEW,
          ]}
        >
          <SectionDivider title="Banking Operations" isCollapsed={isCollapsed} />
          <PermissionGuard permissions={[PERMISSIONS.CASH_DEPOSIT, PERMISSIONS.SAVINGS_WITHDRAW]}>
            <NavItem
              path="/transactions/cash"
              label="Cash Desk (OTC)"
              icon={Banknote}
              isCollapsed={isCollapsed}
            />
          </PermissionGuard>
          <PermissionGuard
            roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER', 'BRANCH_MANAGER']}
            permissions={[PERMISSIONS.CASH_DEPOSIT, PERMISSIONS.SAVINGS_WITHDRAW, PERMISSIONS.TELLER_TILL_VIEW]}
          >
            <NavItem
              path="/transactions/till"
              label="Teller Cash Drawer"
              icon={Vault}
              isCollapsed={isCollapsed}
            />
          </PermissionGuard>
          <PermissionGuard permissions={[PERMISSIONS.MEMBER_TRANSFER]}>
            <NavItem
              path="/transactions/transfer"
              label="Fund Transfers"
              icon={ArrowLeftRight}
              isCollapsed={isCollapsed}
            />
          </PermissionGuard>
          <PermissionGuard permissions={[PERMISSIONS.TRANSACTION_VIEW, PERMISSIONS.ACCOUNT_VIEW]}>
            <NavItem
              path="/transactions/history"
              label="GL Statements"
              icon={History}
              isCollapsed={isCollapsed}
            />
          </PermissionGuard>
        </PermissionGuard>

        {/* ── 2. CREDIT & LENDING ── */}
        <PermissionGuard
          permissions={[
            PERMISSIONS.LOAN_APPLICATION_CREATE,
            PERMISSIONS.LOAN_APPLICATION_VIEW,
            PERMISSIONS.LOAN_APPLICATION_APPROVE,
            PERMISSIONS.LOAN_ACCOUNT_VIEW,
            PERMISSIONS.LOAN_DELINQUENCY_VIEW,
          ]}
        >
          <SectionDivider title="Credit & Lending" isCollapsed={isCollapsed} />
          <NavGroup label="Loan Services" icon={FileSpreadsheet} isCollapsed={isCollapsed}>
            <PermissionGuard permissions={[PERMISSIONS.LOAN_APPLICATION_CREATE, PERMISSIONS.LOAN_APPLICATION_VIEW]}>
              <SubNavItem
                path="/loans/applications"
                label="Loan Requests"
                icon={FileSpreadsheet}
                isCollapsed={isCollapsed}
              />
            </PermissionGuard>
            <PermissionGuard permissions={[PERMISSIONS.LOAN_APPLICATION_APPROVE]}>
              <SubNavItem
                path="/loans/underwriting"
                label="Underwriting Queue"
                icon={ClipboardCheck}
                isCollapsed={isCollapsed}
              />
            </PermissionGuard>
            <PermissionGuard permissions={[PERMISSIONS.LOAN_ACCOUNT_VIEW, PERMISSIONS.LOAN_REPAYMENT_PROCESS]}>
              <SubNavItem
                path="/loans/accounts"
                label="Active Portfolios"
                icon={BadgePercent}
                isCollapsed={isCollapsed}
              />
            </PermissionGuard>
            <PermissionGuard permissions={[PERMISSIONS.LOAN_DELINQUENCY_VIEW, PERMISSIONS.LOAN_ACCOUNT_VIEW]}>
              <SubNavItem
                path="/loans/delinquency"
                label="PAR & Delinquency"
                icon={Activity}
                isCollapsed={isCollapsed}
              />
            </PermissionGuard>
          </NavGroup>
        </PermissionGuard>

        {/* ── 3. ACCOUNTS & PRODUCTS ── */}
        <PermissionGuard
          permissions={[
            PERMISSIONS.ACCOUNT_VIEW,
            PERMISSIONS.PRODUCT_VIEW,
            PERMISSIONS.ACCOUNT_APPROVE,
            PERMISSIONS.BRANCH_VIEW,
          ]}
        >
          <SectionDivider title="Accounts & Products" isCollapsed={isCollapsed} />
          <NavGroup label="Deposit Accounts" icon={CreditCard} isCollapsed={isCollapsed}>
            <PermissionGuard permissions={[PERMISSIONS.ACCOUNT_VIEW]}>
              <SubNavItem
                path="/accounts"
                label="Account Roster"
                icon={CreditCard}
                isCollapsed={isCollapsed}
              />
            </PermissionGuard>
            <PermissionGuard permissions={[PERMISSIONS.ACCOUNT_APPROVE]}>
              <SubNavItem
                path="/accounts/pending"
                label="Pending Approvals"
                icon={ClipboardCheck}
                isCollapsed={isCollapsed}
              />
            </PermissionGuard>
            <PermissionGuard permissions={[PERMISSIONS.PRODUCT_VIEW, PERMISSIONS.PRODUCT_MANAGE]}>
              <SubNavItem
                path="/accounts/products"
                label="Deposit Products"
                icon={PackagePlus}
                isCollapsed={isCollapsed}
              />
            </PermissionGuard>
            <PermissionGuard permissions={[PERMISSIONS.BRANCH_VIEW]}>
              <SubNavItem
                path="/branches"
                label="Branch Hierarchy"
                icon={GitFork}
                isCollapsed={isCollapsed}
              />
            </PermissionGuard>
          </NavGroup>
        </PermissionGuard>

        {/* ── 4. ACCOUNTING & GENERAL LEDGER ── */}
        <PermissionGuard
          permissions={[PERMISSIONS.COA_VIEW, PERMISSIONS.COA_MANAGE, PERMISSIONS.FINANCIAL_REPORT_VIEW]}
          roles={[ROLES.SUPER_ADMIN, ROLES.SACCO_ADMIN, ROLES.ADMIN, ROLES.AUDITOR]}
        >
          <SectionDivider title="Accounting & Ledger" isCollapsed={isCollapsed} />
          <PermissionGuard permissions={[PERMISSIONS.COA_VIEW, PERMISSIONS.COA_MANAGE]}>
            <NavItem
              path="/accounting/chart-of-accounts"
              label="Chart of Accounts"
              icon={FolderTree}
              isCollapsed={isCollapsed}
            />
          </PermissionGuard>
          <PermissionGuard permissions={[PERMISSIONS.FINANCIAL_REPORT_VIEW]}>
            <NavItem
              path="/accounting/reports"
              label="Financial Reports"
              icon={Scale}
              isCollapsed={isCollapsed}
            />
          </PermissionGuard>
        </PermissionGuard>

        {/* ── 5. CORE BATCH & OPERATIONS ── */}
        <PermissionGuard permissions={[PERMISSIONS.EOD_VIEW, PERMISSIONS.EOD_EXECUTE]}>
          <SectionDivider title="Batch Operations" isCollapsed={isCollapsed} />
          <NavItem
            path="/operations/eod"
            label="End-of-Day (EOD)"
            icon={Moon}
            isCollapsed={isCollapsed}
          />
        </PermissionGuard>

        {/* ── 6. MEMBERS & IDENTITY ── */}
        <PermissionGuard
          authorities={['MEMBER_VIEW_BASIC', 'MEMBER_VIEW_FULL', 'KYC_VIEW']}
          roles={['SUPER_ADMIN', 'SACCO_ADMIN']}
        >
          <SectionDivider title="Members & Identity" isCollapsed={isCollapsed} />
          <PermissionGuard authorities={['MEMBER_VIEW_BASIC', 'MEMBER_VIEW_FULL']}>
            <NavItem
              path="/members"
              label="Member Registry"
              icon={Contact}
              isCollapsed={isCollapsed}
            />
          </PermissionGuard>
          <PermissionGuard authorities={['KYC_VIEW']}>
            <NavItem
              path="/kyc-verifications"
              label="KYC Verifications"
              icon={ShieldCheck}
              isCollapsed={isCollapsed}
            />
          </PermissionGuard>
        </PermissionGuard>

        {/* ── 7. PLATFORM GOVERNANCE ── */}
        <PermissionGuard roles={[ROLES.SUPER_ADMIN, ROLES.SACCO_ADMIN, ROLES.ADMIN, ROLES.AUDITOR]}>
          <SectionDivider title="Governance & Security" isCollapsed={isCollapsed} />

          <PermissionGuard role={ROLES.SUPER_ADMIN}>
            <NavItem
              path="/saccos"
              label="SACCO Registry"
              icon={Building2}
              isCollapsed={isCollapsed}
            />
            <NavItem
              path="/onboard"
              label="SACCO Onboarding"
              icon={PlusCircle}
              isCollapsed={isCollapsed}
            />
          </PermissionGuard>

          <PermissionGuard roles={[ROLES.SUPER_ADMIN, ROLES.SACCO_ADMIN]}>
            <NavItem
              path="/users"
              label="User Management"
              icon={Users}
              isCollapsed={isCollapsed}
            />
            <NavItem
              path="/roles"
              label="Role & RBAC Matrix"
              icon={Shield}
              isCollapsed={isCollapsed}
            />
          </PermissionGuard>

          <PermissionGuard
            roles={[ROLES.SUPER_ADMIN, ROLES.SACCO_ADMIN, ROLES.AUDITOR]}
            permissions={[PERMISSIONS.AUDIT_LOG_VIEW]}
          >
            <NavItem
              path="/audit-logs"
              label="Security Audit Trail"
              icon={ShieldAlert}
              isCollapsed={isCollapsed}
            />
          </PermissionGuard>

          <PermissionGuard permissions={[PERMISSIONS.SACCO_CONFIG, PERMISSIONS.ACCOUNT_VIEW]}>
            <NavItem
              path="/accounts/config"
              label="SACCO Config"
              icon={Landmark}
              isCollapsed={isCollapsed}
            />
          </PermissionGuard>
        </PermissionGuard>
      </div>

      {/* ── BOTTOM TOGGLE BAR (Matches arrow in reference screenshot) ── */}
      <div className="p-2 border-t border-[var(--bdae-border)] flex items-center justify-between">
        <button
          type="button"
          onClick={() => setIsCollapsed(!isCollapsed)}
          className="p-2 rounded-xl text-[var(--bdae-text-secondary)] hover:text-[#00CDDB] hover:bg-black/5 dark:hover:bg-white/5 transition-colors focus:outline-none"
          title={isCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
        >
          {isCollapsed ? <ArrowRight className="w-4 h-4" /> : <ArrowLeft className="w-4 h-4" />}
        </button>

        {!isCollapsed && (
          <span className="text-[10px] font-mono text-[var(--bdae-text-secondary)] pr-2">
            Qershi-Link v2.0
          </span>
        )}
      </div>
    </aside>
  );
};
