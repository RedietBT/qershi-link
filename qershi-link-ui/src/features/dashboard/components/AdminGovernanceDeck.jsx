import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldCheck, Building2, Users, Shield, ShieldAlert } from 'lucide-react';
import { PermissionGuard } from '../../../common/components/PermissionGuard';

/**
 * Super Admin & Platform Governance Dashboard Deck
 */
export const AdminGovernanceDeck = () => {
  const navigate = useNavigate();

  return (
    <PermissionGuard roles={['SUPER_ADMIN', 'SACCO_ADMIN']}>
      <div className="bdae-card p-6 space-y-4 border border-[var(--bdae-border)] shadow-xl">
        <h2 className="text-sm font-bold border-b border-[var(--bdae-border)] pb-2 flex items-center gap-2">
          <ShieldCheck className="w-4 h-4 text-[var(--bdae-secondary)]" />
          <span>Administrative Control Engines</span>
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 text-xs">
          {/* Card 1: SACCO Registry Management (SUPER_ADMIN) */}
          <PermissionGuard role="SUPER_ADMIN">
            <div
              onClick={() => navigate('/saccos')}
              className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-[var(--bdae-secondary)] cursor-pointer space-y-2 transition-all shadow-sm group"
            >
              <div className="w-9 h-9 rounded-xl bg-emerald-500/10 text-emerald-600 flex items-center justify-center font-bold">
                <Building2 className="w-5 h-5" />
              </div>
              <div>
                <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-[var(--bdae-secondary)] transition-colors">
                  SACCO Registry Management
                </p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                  Monitor and manage ecosystem tenant configurations.
                </p>
              </div>
            </div>
          </PermissionGuard>

          {/* Card 2: User Account Management (SUPER_ADMIN + SACCO_ADMIN) */}
          <PermissionGuard roles={['SUPER_ADMIN', 'SACCO_ADMIN']}>
            <div
              onClick={() => navigate('/users')}
              className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-[var(--bdae-secondary)] cursor-pointer space-y-2 transition-all shadow-sm group"
            >
              <div className="w-9 h-9 rounded-xl bg-blue-500/10 text-blue-600 flex items-center justify-center font-bold">
                <Users className="w-5 h-5" />
              </div>
              <div>
                <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-[var(--bdae-secondary)] transition-colors">
                  User Account Management
                </p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                  Track and perform CRUD options on identity records.
                </p>
              </div>
            </div>
          </PermissionGuard>

          {/* Card 3: Role & RBAC Management (SUPER_ADMIN + SACCO_ADMIN) */}
          <PermissionGuard roles={['SUPER_ADMIN', 'SACCO_ADMIN']}>
            <div
              onClick={() => navigate('/roles')}
              className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-[var(--bdae-secondary)] cursor-pointer space-y-2 transition-all shadow-sm group"
            >
              <div className="w-9 h-9 rounded-xl bg-amber-500/10 text-amber-600 flex items-center justify-center font-bold">
                <Shield className="w-5 h-5" />
              </div>
              <div>
                <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-[var(--bdae-secondary)] transition-colors">
                  Role & RBAC Management
                </p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                  Manage system roles, custom tenant roles, and permissions.
                </p>
              </div>
            </div>
          </PermissionGuard>

          {/* Card 4: Platform Security Audit Engine (SUPER_ADMIN + SACCO_ADMIN) */}
          <PermissionGuard roles={['SUPER_ADMIN', 'SACCO_ADMIN']}>
            <div
              onClick={() => navigate('/audit-logs')}
              className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-[var(--bdae-secondary)] cursor-pointer space-y-2 transition-all shadow-sm group"
            >
              <div className="w-9 h-9 rounded-xl bg-cyan-500/10 text-cyan-600 flex items-center justify-center font-bold">
                <ShieldAlert className="w-5 h-5" />
              </div>
              <div>
                <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-[var(--bdae-secondary)] transition-colors">
                  Security Audit Engine
                </p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                  Inspect system security, login events, and audit logs.
                </p>
              </div>
            </div>
          </PermissionGuard>
        </div>
      </div>
    </PermissionGuard>
  );
};
