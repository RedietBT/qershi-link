import React from 'react';
import { Route } from 'react-router-dom';
import { PermissionRoute } from '../PermissionRoute';
import { Layout } from '../../common/components/Layout';
import { PERMISSIONS } from '../../common/constants/permissions';
import { LoanApplicationsPage } from '../../features/loans/origination/pages/LoanApplicationsPage';
import { LoanUnderwritingPage } from '../../features/loans/origination/pages/LoanUnderwritingPage';
import { LoanAccountsPage } from '../../features/loans/management/pages/LoanAccountsPage';
import { LoanDelinquencyDashboard } from '../../features/loans/management/pages/LoanDelinquencyDashboard';

/**
 * Credit & Lending (LOS & LMS) Routes
 */
export const loanRoutes = [
  <Route
    key="/loans/applications"
    path="/loans/applications"
    element={
      <PermissionRoute permissions={[PERMISSIONS.LOAN_APPLICATION_CREATE, PERMISSIONS.LOAN_APPLICATION_VIEW]}>
        <Layout>
          <LoanApplicationsPage />
        </Layout>
      </PermissionRoute>
    }
  />,
  <Route
    key="/loans/underwriting"
    path="/loans/underwriting"
    element={
      <PermissionRoute permissions={[PERMISSIONS.LOAN_APPLICATION_APPROVE]}>
        <Layout>
          <LoanUnderwritingPage />
        </Layout>
      </PermissionRoute>
    }
  />,
  <Route
    key="/loans/accounts"
    path="/loans/accounts"
    element={
      <PermissionRoute permissions={[PERMISSIONS.LOAN_ACCOUNT_VIEW, PERMISSIONS.LOAN_REPAYMENT_PROCESS]}>
        <Layout>
          <LoanAccountsPage />
        </Layout>
      </PermissionRoute>
    }
  />,
  <Route
    key="/loans/delinquency"
    path="/loans/delinquency"
    element={
      <PermissionRoute permissions={[PERMISSIONS.LOAN_DELINQUENCY_VIEW, PERMISSIONS.LOAN_ACCOUNT_VIEW]}>
        <Layout>
          <LoanDelinquencyDashboard />
        </Layout>
      </PermissionRoute>
    }
  />,
];
