import React from 'react';
import { Route } from 'react-router-dom';
import { PermissionRoute } from '../PermissionRoute';
import { Layout } from '../../common/components/Layout';
import { MemberProfilePage } from '../../features/members/pages/MemberProfilePage';
import { KycVerificationPage } from '../../features/members/pages/KycVerificationPage';

/**
 * Member Operations & KYC Verification Routes
 */
export const memberRoutes = [
  <Route
    key="/members"
    path="/members"
    element={
      <PermissionRoute roles={['SUPER_ADMIN', 'SACCO_ADMIN']} authorities={['MEMBER_VIEW_BASIC', 'MEMBER_VIEW_FULL']}>
        <Layout>
          <MemberProfilePage />
        </Layout>
      </PermissionRoute>
    }
  />,
  <Route
    key="/kyc-verifications"
    path="/kyc-verifications"
    element={
      <PermissionRoute roles={['SUPER_ADMIN', 'SACCO_ADMIN']} authorities={['KYC_VIEW']}>
        <Layout>
          <KycVerificationPage />
        </Layout>
      </PermissionRoute>
    }
  />,
];
