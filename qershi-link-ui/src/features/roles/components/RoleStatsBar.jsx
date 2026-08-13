import React from 'react';
import { ShieldCheck, Shield, Key, Layers } from 'lucide-react';

export const RoleStatsBar = ({ roles = [], permissions = [] }) => {
  const totalRoles = roles.length;
  const systemRoles = roles.filter(r => Boolean(r.isSystemDefined || r.systemDefined)).length;
  const customRoles = totalRoles - systemRoles;
  const totalPermissions = permissions.length;

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      {/* Total Roles */}
      <div className="bdae-card p-5 flex items-center space-x-4 border border-[var(--bdae-border)] shadow-md">
        <div 
          className="w-12 h-12 rounded-2xl flex items-center justify-center text-white shadow-md shrink-0"
          style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
        >
          <ShieldCheck className="w-6 h-6" />
        </div>
        <div>
          <p className="text-xs text-[var(--bdae-text-secondary)] font-semibold uppercase tracking-wider">
            Total RBAC Roles
          </p>
          <p className="text-2xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
            {totalRoles}
          </p>
        </div>
      </div>

      {/* System Platform Roles */}
      <div className="bdae-card p-5 flex items-center space-x-4 border border-[var(--bdae-border)] shadow-md">
        <div className="w-12 h-12 rounded-2xl bg-cyan-500/10 text-cyan-600 border border-cyan-500/30 flex items-center justify-center shrink-0">
          <Layers className="w-6 h-6" />
        </div>
        <div>
          <p className="text-xs text-[var(--bdae-text-secondary)] font-semibold uppercase tracking-wider">
            System Defined
          </p>
          <p className="text-2xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
            {systemRoles}
          </p>
        </div>
      </div>

      {/* Custom Tenant Roles */}
      <div className="bdae-card p-5 flex items-center space-x-4 border border-[var(--bdae-border)] shadow-md">
        <div className="w-12 h-12 rounded-2xl bg-emerald-500/10 text-emerald-600 border border-emerald-500/30 flex items-center justify-center shrink-0">
          <Shield className="w-6 h-6" />
        </div>
        <div>
          <p className="text-xs text-[var(--bdae-text-secondary)] font-semibold uppercase tracking-wider">
            Custom Local Roles
          </p>
          <p className="text-2xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
            {customRoles}
          </p>
        </div>
      </div>

      {/* Available Permissions */}
      <div className="bdae-card p-5 flex items-center space-x-4 border border-[var(--bdae-border)] shadow-md">
        <div className="w-12 h-12 rounded-2xl bg-amber-500/10 text-amber-600 border border-amber-500/30 flex items-center justify-center shrink-0">
          <Key className="w-6 h-6" />
        </div>
        <div>
          <p className="text-xs text-[var(--bdae-text-secondary)] font-semibold uppercase tracking-wider">
            System Permissions
          </p>
          <p className="text-2xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
            {totalPermissions}
          </p>
        </div>
      </div>
    </div>
  );
};
