import React from 'react';
import { Route } from 'react-router-dom';
import { PermissionRoute } from '../PermissionRoute';
import { Layout } from '../../common/components/Layout';
import { PERMISSIONS } from '../../common/constants/permissions';
import { ChartOfAccountsPage } from '../../features/accounting/pages/ChartOfAccountsPage';
import { FinancialReportsPage } from '../../features/accounting/pages/FinancialReportsPage';

/**
 * Accounting & General Ledger Routes
 */
export const accountingRoutes = [
  <Route
    key="/accounting/chart-of-accounts"
    path="/accounting/chart-of-accounts"
    element={
      <PermissionRoute
        roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'AUDITOR']}
        permissions={[PERMISSIONS.COA_VIEW, PERMISSIONS.COA_MANAGE]}
      >
        <Layout>
          <ChartOfAccountsPage />
        </Layout>
      </PermissionRoute>
    }
  />,
  <Route
    key="/accounting/reports"
    path="/accounting/reports"
    element={
      <PermissionRoute
        roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'AUDITOR']}
        permissions={[PERMISSIONS.FINANCIAL_REPORT_VIEW]}
      >
        <Layout>
          <FinancialReportsPage />
        </Layout>
      </PermissionRoute>
    }
  />,
];
