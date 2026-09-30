import React from 'react';
import { Route } from 'react-router-dom';
import { PermissionRoute } from '../PermissionRoute';
import { Layout } from '../../common/components/Layout';
import { PERMISSIONS } from '../../common/constants/permissions';
import { TellerDrawerPage } from '../../features/transactions/pages/TellerDrawerPage';
import { CashDeskPage } from '../../features/transactions/pages/CashDeskPage';
import { TransferPage } from '../../features/transactions/pages/TransferPage';
import { TransactionHistoryPage } from '../../features/transactions/pages/TransactionHistoryPage';

/**
 * Banking Operations & Cash Desk Routes
 */
export const transactionRoutes = [
  <Route
    key="/transactions/till"
    path="/transactions/till"
    element={
      <PermissionRoute
        roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER', 'BRANCH_MANAGER']}
        permissions={[PERMISSIONS.CASH_DEPOSIT, PERMISSIONS.SAVINGS_WITHDRAW, PERMISSIONS.TELLER_TILL_VIEW]}
      >
        <Layout>
          <TellerDrawerPage />
        </Layout>
      </PermissionRoute>
    }
  />,
  <Route
    key="/transactions/cash"
    path="/transactions/cash"
    element={
      <PermissionRoute permissions={[PERMISSIONS.CASH_DEPOSIT, PERMISSIONS.SAVINGS_WITHDRAW]}>
        <Layout>
          <CashDeskPage />
        </Layout>
      </PermissionRoute>
    }
  />,
  <Route
    key="/transactions/transfer"
    path="/transactions/transfer"
    element={
      <PermissionRoute permissions={[PERMISSIONS.MEMBER_TRANSFER]}>
        <Layout>
          <TransferPage />
        </Layout>
      </PermissionRoute>
    }
  />,
  <Route
    key="/transactions/history"
    path="/transactions/history"
    element={
      <PermissionRoute permissions={[PERMISSIONS.TRANSACTION_VIEW, PERMISSIONS.ACCOUNT_VIEW]}>
        <Layout>
          <TransactionHistoryPage />
        </Layout>
      </PermissionRoute>
    }
  />,
];
