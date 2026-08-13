import React from 'react';
import { useAuthStore } from '../store/useAuthStore';

/**
 * Component-Based Permission Guard consuming Zustand Auth Store.
 * Supports single or array of roles (e.g. roles={['SUPER_ADMIN', 'SACCO_ADMIN']}) and permissions.
 */
export const PermissionGuard = ({ permission, permissions, role, roles, children, fallback = null }) => {
  const user = useAuthStore((state) => state.user);
  const hasRole = useAuthStore((state) => state.hasRole);
  const hasPermission = useAuthStore((state) => state.hasPermission);

  if (!user) {
    return fallback;
  }

  // Super Admin global override
  if (
    user.globalRole === 'SUPER_ADMIN' ||
    user.globalRole === 'ROLE_SUPER_ADMIN' ||
    user.roles?.includes('ROLE_SUPER_ADMIN') ||
    user.roles?.includes('SUPER_ADMIN')
  ) {
    return <>{children}</>;
  }

  const roleList = roles || (Array.isArray(role) ? role : role ? [role] : []);
  if (roleList.length > 0) {
    const isAuthorizedRole = roleList.some((r) => hasRole(r));
    if (!isAuthorizedRole) return fallback;
  }

  const permList = permissions || (Array.isArray(permission) ? permission : permission ? [permission] : []);
  if (permList.length > 0) {
    const isAuthorizedPerm = permList.some((p) => hasPermission(p));
    if (!isAuthorizedPerm) return fallback;
  }

  return <>{children}</>;
};
