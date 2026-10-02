import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from '../common/store/useAuthStore';
import { ProtectedRoute } from './ProtectedRoute';
import { NotFoundPage } from '../common/pages/NotFoundPage';
import { LoginPage } from '../features/auth/pages/LoginPage';
import { DashboardPage } from '../features/dashboard/pages/DashboardPage';

// Modular Route Configurations
import { adminRoutes } from './modules/adminRoutes';
import { memberRoutes } from './modules/memberRoutes';
import { accountRoutes } from './modules/accountRoutes';
import { transactionRoutes } from './modules/transactionRoutes';
import { loanRoutes } from './modules/loanRoutes';
import { operationRoutes } from './modules/operationRoutes';
import { accountingRoutes } from './modules/accountingRoutes';
import { pricingRoutes } from './modules/pricingRoutes';
import { notificationRoutes } from './modules/notificationRoutes';

/**
 * Master Application Routing Engine
 */
export const AppRoutes = () => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  return (
    <Routes>
      {/* Public Authentication Route */}
      <Route
        path="/login"
        element={isAuthenticated ? <Navigate to="/dashboard" replace /> : <LoginPage />}
      />

      {/* Authenticated Core Banking Routes */}
      <Route element={<ProtectedRoute />}>
        <Route path="/dashboard" element={<DashboardPage />} />

        {/* Modular Route Bundles */}
        {adminRoutes}
        {memberRoutes}
        {accountRoutes}
        {transactionRoutes}
        {loanRoutes}
        {accountingRoutes}
        {operationRoutes}
        {pricingRoutes}
        {notificationRoutes}
      </Route>

      {/* Fallbacks */}
      <Route
        path="/"
        element={<Navigate to={isAuthenticated ? '/dashboard' : '/login'} replace />}
      />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
};
