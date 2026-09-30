import React, { useState } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  Building2,
  Users,
  ShieldCheck,
  PlusCircle,
  ShieldAlert,
  ChevronRight,
  ChevronDown,
  Contact,
  FileText,
  CreditCard,
  Landmark,
  PackagePlus,
  ClipboardCheck,
  Search,
  Banknote,
  ArrowLeftRight,
  History,
  Briefcase,
  Shield,
  BadgePercent,
  Vault,
  Moon,
  AlertTriangle,
  FolderTree,
  Scale
} from 'lucide-react';
import { PermissionGuard } from './PermissionGuard';
import { PERMISSIONS, ROLES } from '../constants/permissions';

// ────────────────────────────────────────────────────────────
// Simple nav link item used for flat entries
// ────────────────────────────────────────────────────────────
const NavItem = ({ path, label, icon: Icon }) => (
  <NavLink
    to={path}
    className={({ isActive }) =>
      `w-full flex items-center justify-between px-3 py-2 rounded-xl text-xs font-semibold transition-all duration-200 ${isActive
        ? 'bg-[var(--bdae-primary)] text-white shadow-md'
        : 'text-[var(--bdae-text-primary)] hover:bg-black/5 dark:hover:bg-white/5'
      }`
    }
  >
    <div className="flex items-center space-x-2.5">
      <Icon className="w-4 h-4 shrink-0" />
      <span>{label}</span>
    </div>
    <ChevronRight className="w-3.5 h-3.5 opacity-60" />
  </NavLink>
);

// ────────────────────────────────────────────────────────────
// Expandable section item (e.g. Accounts, Loans groups)
// ────────────────────────────────────────────────────────────
const NavGroup = ({ label, icon: Icon, children, defaultOpen }) => {
  const location = useLocation();
  const isChildActive = React.Children.toArray(children).some(child =>
    child?.props?.path && location.pathname.startsWith(child.props.path)
  );
  const [isOpen, setIsOpen] = useState(defaultOpen || isChildActive);

  return (
    <div>
      <button
        onClick={() => setIsOpen(o => !o)}
        className={`w-full flex items-center justify-between px-3 py-2 rounded-xl text-xs font-semibold transition-all duration-200 ${isChildActive
            ? 'text-[var(--bdae-primary)] font-bold'
            : 'text-[var(--bdae-text-primary)] hover:bg-black/5 dark:hover:bg-white/5'
          }`}
      >
        <div className="flex items-center space-x-2.5">
          <Icon className={`w-4 h-4 shrink-0 ${isChildActive ? 'text-[var(--bdae-primary)]' : ''}`} />
          <span>{label}</span>
        </div>
        {isOpen
          ? <ChevronDown className="w-3.5 h-3.5 opacity-70" />
          : <ChevronRight className="w-3.5 h-3.5 opacity-60" />
        }
      </button>

      {isOpen && (
        <div className="ml-3 mt-1 pl-3 border-l-2 border-[var(--bdae-primary)]/20 space-y-1">
          {children}
        </div>
      )}
    </div>
  );
};

// Sub-item inside a NavGroup
const SubNavItem = ({ path, label, icon: Icon }) => (
  <NavLink
    to={path}
    className={({ isActive }) =>
      `w-full flex items-center gap-2.5 px-3 py-1.5 rounded-xl text-[11px] font-semibold transition-all duration-200 ${isActive
        ? 'bg-[var(--bdae-primary)]/10 text-[var(--bdae-primary)] font-bold'
        : 'text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)] hover:bg-black/5 dark:hover:bg-white/5'
      }`
    }
  >
    <Icon className="w-3.5 h-3.5 shrink-0" />
    <span>{label}</span>
  </NavLink>
);

// ────────────────────────────────────────────────────────────
// Core Banking Departmental Sidebar
// ────────────────────────────────────────────────────────────
export const Sidebar = () => {
  return (
    <aside className="w-64 bdae-surface border-r border-[var(--bdae-border)] flex flex-col justify-between h-[calc(100vh-4rem)] sticky top-16 transition-colors duration-300">
      <div className="p-3.5 space-y-1 overflow-y-auto">
        {/* Core Workspace */}
        <NavItem path="/dashboard" label="Executive Dashboard" icon={LayoutDashboard} />

        {/* ── 1. BANKING OPERATIONS DEPARTMENT ── */}
        <PermissionGuard permissions={[PERMISSIONS.CASH_DEPOSIT, PERMISSIONS.SAVINGS_WITHDRAW, PERMISSIONS.MEMBER_TRANSFER, PERMISSIONS.TRANSACTION_VIEW]}>
          <div className="text-[9px] font-extrabold uppercase tracking-widest text-[var(--bdae-text-secondary)]/70 px-3 pt-3 pb-1">
            Banking Operations
          </div>
          <NavGroup label="Cash Desk & Transfers" icon={Banknote}>
            <PermissionGuard permissions={[PERMISSIONS.CASH_DEPOSIT, PERMISSIONS.SAVINGS_WITHDRAW]}>
              <SubNavItem path="/transactions/cash" label="Cash Desk (Over-the-Counter)" icon={Banknote} />
            </PermissionGuard>
            <PermissionGuard permissions={[PERMISSIONS.CASH_DEPOSIT, PERMISSIONS.SAVINGS_WITHDRAW, PERMISSIONS.TELLER_TILL_VIEW]}>
              <SubNavItem path="/transactions/till" label="Teller Cash Drawer (Till)" icon={Vault} />
            </PermissionGuard>
            <PermissionGuard permissions={[PERMISSIONS.MEMBER_TRANSFER]}>
              <SubNavItem path="/transactions/transfer" label="Member Funds Transfer" icon={ArrowLeftRight} />
            </PermissionGuard>
            <PermissionGuard permissions={[PERMISSIONS.TRANSACTION_VIEW, PERMISSIONS.ACCOUNT_VIEW]}>
              <SubNavItem path="/transactions/history" label="General Ledger Journal" icon={History} />
            </PermissionGuard>
            <PermissionGuard permissions={[PERMISSIONS.EOD_VIEW, PERMISSIONS.EOD_EXECUTE]}>
              <SubNavItem path="/operations/eod" label="End-of-Day (EOD) Batch" icon={Moon} />
            </PermissionGuard>
          </NavGroup>
        </PermissionGuard>

        {/* ── 2. CREDIT & LENDING DEPARTMENT ── */}
        <PermissionGuard permissions={[PERMISSIONS.LOAN_APPLICATION_VIEW, PERMISSIONS.LOAN_APPLICATION_CREATE, PERMISSIONS.LOAN_ACCOUNT_VIEW]}>
          <div className="text-[9px] font-extrabold uppercase tracking-widest text-[var(--bdae-text-secondary)]/70 px-3 pt-3 pb-1">
            Credit & Lending
          </div>
          <NavGroup label="Loan Lifecycle" icon={Briefcase}>
            <PermissionGuard permissions={[PERMISSIONS.LOAN_APPLICATION_CREATE, PERMISSIONS.LOAN_APPLICATION_VIEW]}>
              <SubNavItem path="/loans/applications" label="Loan Applications" icon={FileText} />
            </PermissionGuard>
            <PermissionGuard permissions={[PERMISSIONS.LOAN_APPLICATION_APPROVE]}>
              <SubNavItem path="/loans/underwriting" label="Underwriting Queue" icon={ClipboardCheck} />
            </PermissionGuard>
            <PermissionGuard permissions={[PERMISSIONS.LOAN_ACCOUNT_VIEW, PERMISSIONS.LOAN_REPAYMENT_PROCESS]}>
              <SubNavItem path="/loans/accounts" label="Active Portfolios & Repay" icon={BadgePercent} />
            </PermissionGuard>
            <PermissionGuard permissions={[PERMISSIONS.LOAN_DELINQUENCY_VIEW, PERMISSIONS.LOAN_ACCOUNT_VIEW]}>
              <SubNavItem path="/loans/delinquency" label="PAR & Delinquency Aging" icon={AlertTriangle} />
            </PermissionGuard>
          </NavGroup>
        </PermissionGuard>

        {/* ── 3. MEMBER ACCOUNTS DEPARTMENT ── */}
        <PermissionGuard permissions={[PERMISSIONS.MEMBER_VIEW_BASIC, PERMISSIONS.ACCOUNT_VIEW]}>
          <div className="text-[9px] font-extrabold uppercase tracking-widest text-[var(--bdae-text-secondary)]/70 px-3 pt-3 pb-1">
            Member Accounts
          </div>
          <NavItem path="/members" label="Member Registry" icon={Contact} />
          <PermissionGuard permissions={[PERMISSIONS.KYC_VERIFY]}>
            <NavItem path="/kyc-verifications" label="KYC Verifications" icon={FileText} />
          </PermissionGuard>

          <NavGroup label="Savings & Deposits" icon={CreditCard}>
            <PermissionGuard permissions={[PERMISSIONS.ACCOUNT_VIEW]}>
              <SubNavItem path="/accounts" label="Account Roster" icon={Search} />
            </PermissionGuard>
            <PermissionGuard permissions={[PERMISSIONS.ACCOUNT_APPROVE]}>
              <SubNavItem path="/accounts/pending" label="Pending Authorizations" icon={ClipboardCheck} />
            </PermissionGuard>
            <PermissionGuard permissions={[PERMISSIONS.PRODUCT_VIEW]}>
              <SubNavItem path="/accounts/products" label="Deposit Products" icon={PackagePlus} />
            </PermissionGuard>
          </NavGroup>
        </PermissionGuard>

        {/* ── 4. ACCOUNTING & GENERAL LEDGER ── */}
        <PermissionGuard
          permissions={[PERMISSIONS.COA_VIEW, PERMISSIONS.COA_MANAGE, PERMISSIONS.FINANCIAL_REPORT_VIEW]}
          roles={[ROLES.SUPER_ADMIN, ROLES.SACCO_ADMIN, ROLES.ADMIN, ROLES.AUDITOR]}
        >
          <div className="text-[9px] font-extrabold uppercase tracking-widest text-[var(--bdae-text-secondary)]/70 px-3 pt-3 pb-1">
            Accounting & General Ledger
          </div>
          <NavGroup label="General Ledger & Reports" icon={Scale}>
            <PermissionGuard permissions={[PERMISSIONS.COA_VIEW, PERMISSIONS.COA_MANAGE]}>
              <SubNavItem path="/accounting/chart-of-accounts" label="Chart of Accounts" icon={FolderTree} />
            </PermissionGuard>
            <PermissionGuard permissions={[PERMISSIONS.FINANCIAL_REPORT_VIEW]}>
              <SubNavItem path="/accounting/reports" label="Financial Statements" icon={FileText} />
            </PermissionGuard>
          </NavGroup>
        </PermissionGuard>

        {/* ── 5. GOVERNANCE & SECURITY ── */}
        <PermissionGuard roles={[ROLES.SUPER_ADMIN, ROLES.SACCO_ADMIN, ROLES.ADMIN]}>
          <div className="text-[9px] font-extrabold uppercase tracking-widest text-[var(--bdae-text-secondary)]/70 px-3 pt-3 pb-1">
            Governance & Security
          </div>

          {/* Super Admin tenant registry */}
          <PermissionGuard role={ROLES.SUPER_ADMIN}>
            <NavItem path="/saccos" label="SACCO Registry" icon={Building2} />
            <NavItem path="/onboard" label="SACCO Onboarding" icon={PlusCircle} />
          </PermissionGuard>

          <PermissionGuard roles={[ROLES.SUPER_ADMIN, ROLES.SACCO_ADMIN]}>
            <NavItem path="/users" label="User Management" icon={Users} />
            <NavItem path="/roles" label="Role & RBAC Matrix" icon={Shield} />
            <NavItem path="/audit-logs" label="Security Audit Trail" icon={ShieldAlert} />
          </PermissionGuard>

          <PermissionGuard permissions={[PERMISSIONS.SACCO_CONFIG, PERMISSIONS.ACCOUNT_VIEW]}>
            <NavItem path="/accounts/config" label="SACCO Configuration" icon={Landmark} />
          </PermissionGuard>
          <PermissionGuard permissions={[PERMISSIONS.BRANCH_VIEW, PERMISSIONS.ACCOUNT_VIEW]}>
            <NavItem path="/branches" label="Branch Management" icon={Building2} />
          </PermissionGuard>
        </PermissionGuard>
      </div>

      <div className="p-3.5 border-t border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 m-2.5 rounded-xl text-center">
        <p className="text-[11px] font-bold text-[var(--bdae-text-primary)]">Qershi-Link Core Banking</p>
        <p className="text-[10px] text-[var(--bdae-text-secondary)]">Enterprise Edition v2.0</p>
      </div>
    </aside>
  );
};
