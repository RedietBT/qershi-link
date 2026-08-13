import { authHttpClient } from '../../../common/api/httpClient';

/**
 * Platform Security Audit Engine API Service (/api/v1/platform/audit-logs).
 */
export const auditApi = {
  /**
   * Fetch global platform security audit logs (GET /api/v1/platform/audit-logs?page=0&size=50).
   * Gated strictly to SUPER_ADMIN.
   */
  getGlobalAuditLogs: async (page = 0, size = 50) => {
    const response = await authHttpClient.get('/platform/audit-logs', {
      params: { page, size }
    });
    return response.data;
  },

  /**
   * Fetch authentication audit logs for tenant SACCO (GET /api/v1/platform/audit-logs/tenant).
   * Gated to SUPER_ADMIN or SACCO_ADMIN.
   */
  getTenantAuditLogs: async () => {
    const response = await authHttpClient.get('/platform/audit-logs/tenant');
    return response.data;
  },

  /**
   * Fetch security audit logs by SACCO ID (GET /api/v1/platform/audit-logs/sacco/{saccoId}).
   */
  getAuditLogsBySacco: async (saccoId) => {
    const response = await authHttpClient.get(`/platform/audit-logs/sacco/${saccoId}`);
    return response.data;
  },

  /**
   * Fetch security audit logs by User ID (GET /api/v1/platform/audit-logs/user/{userId}).
   */
  getAuditLogsByUser: async (userId) => {
    const response = await authHttpClient.get(`/platform/audit-logs/user/${userId}`);
    return response.data;
  }
};
