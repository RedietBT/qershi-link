import React from 'react';
import { NavLink } from 'react-router-dom';
import { 
  LayoutDashboard, 
  Building2, 
  Users, 
  Wallet, 
  ArrowLeftRight, 
  FileText, 
  PlusCircle,
  ShieldAlert,
  UserCheck,
  ChevronRight
} from 'lucide-react';
import { PermissionGuard } from './PermissionGuard';

const NAV_ITEMS = [
  { path: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { path: '/saccos', label: 'SACCO Registry', icon: Building2, role: 'SUPER_ADMIN' },
  { path: '/onboard', label: 'SACCO Tenant Onboarding', icon: PlusCircle, role: 'SUPER_ADMIN' },
  { path: '/users', label: 'User Management', icon: Users, role: 'SUPER_ADMIN' },
  { path: '/audit-logs', label: 'Security Audit Logs', icon: ShieldAlert, role: 'SUPER_ADMIN' },
  { path: '/members', label: 'Member Profiles', icon: UserCheck },
  { path: '/accounts', label: 'Core Accounts', icon: Wallet },
  { path: '/transactions', label: 'Transactions', icon: ArrowLeftRight },
  { path: '/loans', label: 'Loan Origination', icon: FileText },
];

export const Sidebar = () => {
  return (
    <aside className="w-64 bdae-surface border-r border-[var(--bdae-border)] flex flex-col justify-between h-[calc(100vh-4rem)] sticky top-16 transition-colors duration-300">
      <div className="p-4 space-y-1.5 overflow-y-auto">
        <div className="text-[11px] font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] px-3 mb-2">
          Navigation Menu
        </div>

        {NAV_ITEMS.map((item) => {
          const Icon = item.icon;
          const navButton = (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `w-full flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-semibold transition-all duration-200 ${
                  isActive
                    ? 'bg-[var(--bdae-primary)] text-white shadow-md'
                    : 'text-[var(--bdae-text-primary)] hover:bg-black/5 dark:hover:bg-white/5'
                }`
              }
            >
              <div className="flex items-center space-x-3">
                <Icon className="w-4 h-4" />
                <span>{item.label}</span>
              </div>
              <ChevronRight className="w-3.5 h-3.5 opacity-60" />
            </NavLink>
          );

          if (item.role || item.permission) {
            return (
              <PermissionGuard key={item.path} role={item.role} permission={item.permission}>
                {navButton}
              </PermissionGuard>
            );
          }

          return navButton;
        })}
      </div>

      <div className="p-4 border-t border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 m-3 rounded-xl text-center">
        <p className="text-[11px] font-bold text-[var(--bdae-text-primary)]">Qershi-Link Core Banking</p>
        <p className="text-[10px] text-[var(--bdae-text-secondary)]">Multi-Tenant Platform v1.0</p>
      </div>
    </aside>
  );
};
