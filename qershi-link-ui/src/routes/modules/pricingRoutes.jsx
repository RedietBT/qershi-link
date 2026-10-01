import React from 'react';
import { Route } from 'react-router-dom';
import { PermissionRoute } from '../PermissionRoute';
import { Layout } from '../../common/components/Layout';
import { PERMISSIONS } from '../../common/constants/permissions';
import { TariffManagementPage } from '../../features/pricing/pages/TariffManagementPage';

/**
 * Dedicated Enterprise Pricing, Tariff Engine & Withholding Tax Routes
 */
export const pricingRoutes = [
  <Route
    key="/pricing"
    path="/pricing"
    element={
      <PermissionRoute
        roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'BRANCH_MANAGER', 'AUDITOR', 'TELLER']}
        permissions={[PERMISSIONS.ACCOUNT_VIEW]}
      >
        <Layout>
          <TariffManagementPage />
        </Layout>
      </PermissionRoute>
    }
  />,
  <Route
    key="/tariffs"
    path="/tariffs"
    element={
      <PermissionRoute
        roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'BRANCH_MANAGER', 'AUDITOR', 'TELLER']}
        permissions={[PERMISSIONS.ACCOUNT_VIEW]}
      >
        <Layout>
          <TariffManagementPage />
        </Layout>
      </PermissionRoute>
    }
  />,
];
