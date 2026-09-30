import React from 'react';
import { Route } from 'react-router-dom';
import { PermissionRoute } from '../PermissionRoute';
import { Layout } from '../../common/components/Layout';
import { PERMISSIONS } from '../../common/constants/permissions';
import { EodControlPage } from '../../features/operations/pages/EodControlPage';

/**
 * Operations & Batch Processing Routes
 */
export const operationRoutes = [
  <Route
    key="/operations/eod"
    path="/operations/eod"
    element={
      <PermissionRoute permissions={[PERMISSIONS.EOD_VIEW, PERMISSIONS.EOD_EXECUTE]}>
        <Layout>
          <EodControlPage />
        </Layout>
      </PermissionRoute>
    }
  />,
];
