import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '../common/store/useAuthStore';
import { Lock } from 'lucide-react';

/**
 * Guard for routes requiring specific Roles or Permissions.
 * Supports arrays of roles (e.g. roles={['SUPER_ADMIN', 'SACCO_ADMIN']}).
 */
export const PermissionRoute = ({ role, roles, permission, permissions, children }) => {
  const user = useAuthStore((state) => state.user);
  const hasRole = useAuthStore((state) => state.hasRole);
  const hasPermission = useAuthStore((state) => state.hasPermission);

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // Super Admin global override
  if (
    user.globalRole === 'SUPER_ADMIN' ||
    user.globalRole === 'ROLE_SUPER_ADMIN' ||
    user.roles?.includes('ROLE_SUPER_ADMIN') ||
    user.roles?.includes('SUPER_ADMIN')
  ) {
    return children ? children : <Outlet />;
  }

  const roleList = roles || (Array.isArray(role) ? role : role ? [role] : []);
  const isRoleAuthorized = roleList.length === 0 || roleList.some((r) => hasRole(r));

  const permList = permissions || (Array.isArray(permission) ? permission : permission ? [permission] : []);
  const isPermissionAuthorized = permList.length === 0 || permList.some((p) => hasPermission(p));

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
