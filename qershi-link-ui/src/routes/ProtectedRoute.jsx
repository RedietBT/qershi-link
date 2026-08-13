import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '../common/store/useAuthStore';

/**
 * Guard for routes requiring active user authentication.
 * Redirects unauthenticated users to /login.
 */
export const ProtectedRoute = ({ children }) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return children ? children : <Outlet />;
};
