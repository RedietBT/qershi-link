import { authHttpClient } from '../../../common/api/httpClient';

/**
 * RBAC Role & Permission Management API Service (/api/v1/roles).
 */
export const roleApi = {
  /**
   * Fetch all roles (GET /api/v1/roles).
   * SUPER_ADMIN sees system platform roles; SACCO_ADMIN sees system roles and custom tenant roles.
   */
  getRoles: async () => {
    const response = await authHttpClient.get('/roles');
    return response.data;
  },

  /**
   * List all available permissions (GET /api/v1/roles/permissions).
   */
  getPermissions: async () => {
    const response = await authHttpClient.get('/roles/permissions');
    return response.data;
  },

  /**
   * Get role details by ID (GET /api/v1/roles/{roleId}).
   */
  getRoleById: async (roleId) => {
    const response = await authHttpClient.get(`/roles/${roleId}`);
    return response.data;
  },

  /**
   * Create a custom local role (POST /api/v1/roles).
   * Payload: { roleName: "CUSTOM_ROLE", permissionIds: ["uuid-1", "uuid-2"] }
   */
  createRole: async (payload) => {
    const response = await authHttpClient.post('/roles', payload);
    return response.data;
  },

  /**
   * Update custom role permissions (PUT /api/v1/roles/{roleId}).
   * Payload: { roleName: "CUSTOM_ROLE", permissionIds: ["uuid-1", "uuid-2"] }
   */
  updateRole: async (roleId, payload) => {
    const response = await authHttpClient.put(`/roles/${roleId}`, payload);
    return response.data;
  },

  /**
   * Delete custom role safely (DELETE /api/v1/roles/{roleId}).
   */
  deleteRole: async (roleId) => {
    const response = await authHttpClient.delete(`/roles/${roleId}`);
    return response.data;
  }
};
