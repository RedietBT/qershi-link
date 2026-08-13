import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '../common/store/useAuthStore';
import { Lock, ShieldAlert } from 'lucide-react';

/**
 * Guard for routes requiring specific Roles or Permissions.
 * Renders a clean 403 Access Denied fallback when missing authorization.
 */
export const PermissionRoute = ({ role, permission, children }) => {
  const user = useAuthStore((state) => state.user);
  const hasRole = useAuthStore((state) => state.hasRole);
  const hasPermission = useAuthStore((state) => state.hasPermission);

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  const isRoleAuthorized = !role || hasRole(role);
  const isPermissionAuthorized = !permission || hasPermission(permission);

  if (!isRoleAuthorized || !isPermissionAuthorized) {
    return (
      <div className="min-h-screen flex items-center justify-center p-6 bg-[var(--bdae-bg)] text-[var(--bdae-text-primary)]">
        <div className="bdae-card p-8 max-w-md w-full text-center space-y-4 shadow-2xl border border-red-500/30">
          <div className="w-14 h-14 rounded-2xl bg-red-500/10 text-red-500 mx-auto flex items-center justify-center border border-red-500/20 shadow-md">
            <Lock className="w-7 h-7" />
          </div>
          <div>
            <h1 className="text-xl font-bold tracking-tight text-red-600 dark:text-red-400">
              403 Access Denied
            </h1>
            <p className="text-xs text-[var(--bdae-text-secondary)] mt-1">
              You do not possess the required authorization claims to view this page.
            </p>
          </div>
          <div className="p-3 rounded-xl bg-black/5 dark:bg-white/5 text-[11px] font-mono text-[var(--bdae-text-secondary)] text-left space-y-1">
            {role && <p><strong>Required Role:</strong> {role}</p>}
            {permission && <p><strong>Required Permission:</strong> {permission}</p>}
          </div>
          <a
            href="/dashboard"
            className="bdae-btn-primary block w-full py-2.5 text-xs font-bold rounded-xl"
          >
            Return to Dashboard
          </a>
        </div>
      </div>
    );
  }

  return children ? children : <Outlet />;
};
