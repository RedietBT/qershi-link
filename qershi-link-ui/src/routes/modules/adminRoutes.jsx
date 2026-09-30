import React from 'react';
import { Route } from 'react-router-dom';
import { PermissionRoute } from '../PermissionRoute';
import { Layout } from '../../common/components/Layout';
import { SaccoRegistryPage } from '../../features/superadmin/pages/SaccoRegistryPage';
import { SaccoOnboardingPage } from '../../features/superadmin/pages/SaccoOnboardingPage';
import { UserManagementPage } from '../../features/users/pages/UserManagementPage';
import { RoleManagementPage } from '../../features/roles/pages/RoleManagementPage';
import { AuditLogsPage } from '../../features/audit/pages/AuditLogsPage';

/**
 * Super Admin & Platform Governance Routes
 */
export const adminRoutes = [
  <Route
    key="/saccos"
    path="/saccos"
    element={
      <PermissionRoute role="SUPER_ADMIN">
        <Layout>
          <SaccoRegistryPage />
        </Layout>
      </PermissionRoute>
    }
  />,
  <Route
    key="/onboard"
    path="/onboard"
    element={
      <PermissionRoute role="SUPER_ADMIN">
        <Layout>
          <SaccoOnboardingPage />
        </Layout>
      </PermissionRoute>
    }
  />,
  <Route
    key="/users"
    path="/users"
    element={
      <PermissionRoute roles={['SUPER_ADMIN', 'SACCO_ADMIN']}>
        <Layout>
          <UserManagementPage />
        </Layout>
      </PermissionRoute>
    }
  />,
  <Route
    key="/roles"
    path="/roles"
    element={
      <PermissionRoute roles={['SUPER_ADMIN', 'SACCO_ADMIN']}>
        <Layout>
          <RoleManagementPage />
        </Layout>
      </PermissionRoute>
    }
  />,
  <Route
    key="/audit-logs"
    path="/audit-logs"
    element={
      <PermissionRoute roles={['SUPER_ADMIN', 'SACCO_ADMIN']}>
        <Layout>
          <AuditLogsPage />
        </Layout>
      </PermissionRoute>
    }
  />,
];
