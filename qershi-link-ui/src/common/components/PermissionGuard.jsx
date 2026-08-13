import React from 'react';
import { useAuthStore } from '../store/useAuthStore';

/**
 * Component-Based Permission Guard consuming Zustand Auth Store.
 * Renders children only if the authenticated user possesses the required permission/role.
 */
export const PermissionGuard = ({ permission, role, children, fallback = null }) => {
  const user = useAuthStore((state) => state.user);
  const hasRole = useAuthStore((state) => state.hasRole);
  const hasPermission = useAuthStore((state) => state.hasPermission);

  if (!user) {
    return fallback;
  }

  if (role && !hasRole(role)) {
    return fallback;
  }

  if (permission && !hasPermission(permission)) {
    return fallback;
  }

  return <>{children}</>;
};
