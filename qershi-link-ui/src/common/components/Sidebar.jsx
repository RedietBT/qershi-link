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
  Search
} from 'lucide-react';
import { PermissionGuard } from './PermissionGuard';

// ────────────────────────────────────────────────────────────
// Simple nav link item used for flat entries
// ────────────────────────────────────────────────────────────
const NavItem = ({ path, label, icon: Icon }) => (
  <NavLink
    to={path}
    className={({ isActive }) =>
      `w-full flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-semibold transition-all duration-200 ${isActive
        ? 'bg-[var(--bdae-primary)] text-white shadow-md'
        : 'text-[var(--bdae-text-primary)] hover:bg-black/5 dark:hover:bg-white/5'
      }`
    }
  >
    <div className="flex items-center space-x-3">
      <Icon className="w-4 h-4" />
      <span>{label}</span>
    </div>
    <ChevronRight className="w-3.5 h-3.5 opacity-60" />
  </NavLink>
);

// ────────────────────────────────────────────────────────────
// Expandable section item (e.g. Accounts group)
// ────────────────────────────────────────────────────────────
const NavGroup = ({ label, icon: Icon, children, defaultOpen }) => {
  const location = useLocation();
  // Auto-open if any child path is active
  const isChildActive = React.Children.toArray(children).some(child =>
    child?.props?.path && location.pathname.startsWith(child.props.path)
  );
  const [isOpen, setIsOpen] = useState(defaultOpen || isChildActive);

  return (
    <div>
      <button
        onClick={() => setIsOpen(o => !o)}
        className={`w-full flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-semibold transition-all duration-200 ${isChildActive
            ? 'text-[var(--bdae-primary)] font-bold'
            : 'text-[var(--bdae-text-primary)] hover:bg-black/5 dark:hover:bg-white/5'
          }`}
      >
        <div className="flex items-center space-x-3">
          <Icon className={`w-4 h-4 ${isChildActive ? 'text-[var(--bdae-primary)]' : ''}`} />
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
      `w-full flex items-center gap-2.5 px-3 py-2 rounded-xl text-[11px] font-semibold transition-all duration-200 ${isActive
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
// Sidebar
// ────────────────────────────────────────────────────────────
export const Sidebar = () => {
  return (
    <aside className="w-64 bdae-surface border-r border-[var(--bdae-border)] flex flex-col justify-between h-[calc(100vh-4rem)] sticky top-16 transition-colors duration-300">
      <div className="p-4 space-y-1.5 overflow-y-auto">
        <div className="text-[11px] font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] px-3 mb-2">
          Navigation Menu
        </div>

        {/* Dashboard — always visible */}
        <NavItem path="/dashboard" label="Dashboard" icon={LayoutDashboard} />

        {/* ── SUPER_ADMIN only ── */}
        <PermissionGuard role="SUPER_ADMIN">
          <NavItem path="/saccos" label="SACCO Registry" icon={Building2} />
          <NavItem path="/onboard" label="SACCO Onboarding" icon={PlusCircle} />
        </PermissionGuard>

        {/* ── Admin section label ── */}
        <PermissionGuard roles={['SUPER_ADMIN', 'SACCO_ADMIN']}>
          <div className="text-[9px] font-extrabold uppercase tracking-widest text-[var(--bdae-text-secondary)]/60 px-3 pt-3 pb-1">
            Administration
          </div>
        </PermissionGuard>

        <PermissionGuard roles={['SUPER_ADMIN', 'SACCO_ADMIN']}>
          <NavItem path="/users" label="User Management" icon={Users} />
        </PermissionGuard>

        <PermissionGuard roles={['SUPER_ADMIN', 'SACCO_ADMIN']} permissions={['MEMBER_VIEW_BASIC', 'MEMBER_VIEW_FULL']}>
          <NavItem path="/members" label="Member Profiles" icon={Contact} />
        </PermissionGuard>

        <PermissionGuard roles={['SUPER_ADMIN', 'SACCO_ADMIN']} permissions={['KYC_VIEW']}>
          <NavItem path="/kyc-verifications" label="KYC Validations" icon={FileText} />
        </PermissionGuard>

        {/* ── Accounts Group ── (shown if user has any account permission) */}
        <PermissionGuard roles={['SACCO_ADMIN', 'ADMIN']} permissions={['ACCOUNT_VIEW', 'PRODUCT_VIEW', 'ACCOUNT_APPROVE']}>
          <div className="text-[9px] font-extrabold uppercase tracking-widest text-[var(--bdae-text-secondary)]/60 px-3 pt-3 pb-1">
            Account Engine
          </div>

          <NavGroup label="Accounts" icon={CreditCard}>
            {/* Account Management (member roster + open accounts) */}
            <PermissionGuard roles={['SACCO_ADMIN', 'ADMIN']} permissions={['ACCOUNT_VIEW']}>
              <SubNavItem path="/accounts" label="Account Management" icon={Search} />
            </PermissionGuard>

            {/* Pending Authorizations (Four-Eye) */}
            <PermissionGuard roles={['SACCO_ADMIN', 'ADMIN']} permissions={['ACCOUNT_APPROVE']}>
              <SubNavItem path="/accounts/pending" label="Pending Approvals" icon={ClipboardCheck} />
            </PermissionGuard>

            {/* Deposit Product Factory */}
            <PermissionGuard roles={['SACCO_ADMIN', 'ADMIN']} permissions={['PRODUCT_VIEW']}>
              <SubNavItem path="/accounts/products" label="Deposit Products" icon={PackagePlus} />
            </PermissionGuard>

            {/* SACCO Configuration */}
            <PermissionGuard roles={['SACCO_ADMIN', 'ADMIN']} permissions={['ACCOUNT_VIEW']}>
              <SubNavItem path="/accounts/config" label="SACCO Configuration" icon={Landmark} />
            </PermissionGuard>
          </NavGroup>
        </PermissionGuard>

        {/* ── Security section ── */}
        <PermissionGuard roles={['SUPER_ADMIN', 'SACCO_ADMIN']}>
          <div className="text-[9px] font-extrabold uppercase tracking-widest text-[var(--bdae-text-secondary)]/60 px-3 pt-3 pb-1">
            Security
          </div>
          <NavItem path="/roles" label="Role & RBAC Management" icon={ShieldCheck} />
          <NavItem path="/audit-logs" label="Security Audit Logs" icon={ShieldAlert} />
        </PermissionGuard>
      </div>

      <div className="p-4 border-t border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 m-3 rounded-xl text-center">
        <p className="text-[11px] font-bold text-[var(--bdae-text-primary)]">Qershi-Link Core Banking</p>
        <p className="text-[10px] text-[var(--bdae-text-secondary)]">Multi-Tenant Platform v1.0</p>
      </div>
    </aside>
  );
};
