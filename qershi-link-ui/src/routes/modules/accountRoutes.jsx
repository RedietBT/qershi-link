import React from 'react';
import { Route } from 'react-router-dom';
import { PermissionRoute } from '../PermissionRoute';
import { Layout } from '../../common/components/Layout';
import { PERMISSIONS } from '../../common/constants/permissions';
import { AccountManagementPage } from '../../features/accounts/pages/AccountManagementPage';
import { SaccoConfigPage } from '../../features/accounts/pages/SaccoConfigPage';
import { DepositProductsPage } from '../../features/accounts/pages/DepositProductsPage';
import { PendingAuthorizationsPage } from '../../features/accounts/pages/PendingAuthorizationsPage';
import { BranchManagementPage } from '../../features/accounts/pages/BranchManagementPage';

/**
 * Account Management & Branch Hierarchy Routes
 */
export const accountRoutes = [
  <Route
    key="/accounts"
    path="/accounts"
    element={
      <PermissionRoute
        roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'BRANCH_MANAGER', 'AUDITOR']}
        permissions={[PERMISSIONS.ACCOUNT_VIEW]}
      >
        <Layout>
          <AccountManagementPage />
        </Layout>
      </PermissionRoute>
    }
  />,
  <Route
    key="/accounts/config"
    path="/accounts/config"
    element={
      <PermissionRoute
        roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN']}
        permissions={[PERMISSIONS.ACCOUNT_VIEW, PERMISSIONS.SACCO_CONFIG]}
      >
        <Layout>
          <SaccoConfigPage />
        </Layout>
      </PermissionRoute>
    }
  />,
  <Route
    key="/accounts/products"
    path="/accounts/products"
    element={
      <PermissionRoute
        roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN']}
        permissions={[PERMISSIONS.PRODUCT_VIEW, PERMISSIONS.PRODUCT_MANAGE]}
      >
        <Layout>
          <DepositProductsPage />
        </Layout>
      </PermissionRoute>
    }
  />,
  <Route
    key="/accounts/pending"
    path="/accounts/pending"
    element={
      <PermissionRoute
        roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN']}
        permissions={[PERMISSIONS.ACCOUNT_APPROVE]}
      >
        <Layout>
          <PendingAuthorizationsPage />
        </Layout>
      </PermissionRoute>
    }
  />,
  <Route
    key="/branches"
    path="/branches"
    element={
      <PermissionRoute
        roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'BRANCH_MANAGER', 'TELLER', 'AUDITOR']}
        permissions={[PERMISSIONS.BRANCH_VIEW, PERMISSIONS.ACCOUNT_VIEW]}
      >
        <Layout>
          <BranchManagementPage />
        </Layout>
      </PermissionRoute>
    }
  />,
];
