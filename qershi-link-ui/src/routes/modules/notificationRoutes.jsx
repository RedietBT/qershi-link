import React from 'react';
import { Route } from 'react-router-dom';
import { PermissionRoute } from '../PermissionRoute';
import { Layout } from '../../common/components/Layout';
import { PERMISSIONS, ROLES } from '../../common/constants/permissions';
import { SmsGatewayConfigPage } from '../../features/notifications/pages/SmsGatewayConfigPage';
import { SmsTemplateEditorPage } from '../../features/notifications/pages/SmsTemplateEditorPage';
import { NotificationLogsPage } from '../../features/notifications/pages/NotificationLogsPage';

/**
 * Dedicated Enterprise SMS & Notifications Route Bundle.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
export const notificationRoutes = [
  <Route
    key="/notifications/gateway"
    path="/notifications/gateway"
    element={
      <PermissionRoute
        roles={[ROLES.SUPER_ADMIN, ROLES.SACCO_ADMIN, ROLES.ADMIN]}
        permissions={[PERMISSIONS.NOTIFICATION_CONFIG_MANAGE, PERMISSIONS.SACCO_CONFIG]}
      >
        <Layout>
          <SmsGatewayConfigPage />
        </Layout>
      </PermissionRoute>
    }
  />,
  <Route
    key="/notifications/templates"
    path="/notifications/templates"
    element={
      <PermissionRoute
        roles={[ROLES.SUPER_ADMIN, ROLES.SACCO_ADMIN, ROLES.ADMIN]}
        permissions={[PERMISSIONS.NOTIFICATION_TEMPLATE_MANAGE, PERMISSIONS.SACCO_CONFIG]}
      >
        <Layout>
          <SmsTemplateEditorPage />
        </Layout>
      </PermissionRoute>
    }
  />,
  <Route
    key="/notifications/logs"
    path="/notifications/logs"
    element={
      <PermissionRoute
        roles={[ROLES.SUPER_ADMIN, ROLES.SACCO_ADMIN, ROLES.ADMIN, ROLES.AUDITOR]}
        permissions={[PERMISSIONS.NOTIFICATION_LOG_VIEW, PERMISSIONS.AUDIT_LOG_VIEW]}
      >
        <Layout>
          <NotificationLogsPage />
        </Layout>
      </PermissionRoute>
    }
  />,
];
