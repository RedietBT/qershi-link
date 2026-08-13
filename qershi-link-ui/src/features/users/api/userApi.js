import { authHttpClient } from '../../../common/api/httpClient';

/**
 * User Account Management API Service (/api/v1/users).
 */
export const userApi = {
  /**
   * Fetch user accounts (GET /api/v1/users?saccoId={saccoId}).
   */
  getUsers: async (saccoId = null) => {
    const params = saccoId ? { saccoId } : {};
    const response = await authHttpClient.get('/users', { params });
    return response.data;
  },

  /**
   * Register a new user (POST /api/v1/users?saccoId={saccoId}).
   * Payload: { msisdn: "+2519...", globalRole: "SACCO_USER" }
   */
  createUser: async (payload, saccoId = null) => {
    const params = saccoId ? { saccoId } : {};
    const response = await authHttpClient.post('/users', payload, { params });
    return response.data;
  },

  /**
   * Get user account details by ID (GET /api/v1/users/{id}).
   */
  getUserById: async (userId) => {
    const response = await authHttpClient.get(`/users/${userId}`);
    return response.data;
  },

  /**
   * Update user security parameters (PUT /api/v1/users/{id}).
   * Payload: { msisdn: "+2519...", status: "ACTIVE" }
   */
  updateUser: async (userId, payload) => {
    const response = await authHttpClient.put(`/users/${userId}`, payload);
    return response.data;
  },

  /**
   * Purge user identity (DELETE /api/v1/users/{id}).
   * Strictly gated to SUPER_ADMIN.
   */
  deleteUser: async (userId) => {
    const response = await authHttpClient.delete(`/users/${userId}`);
    return response.data;
  },

  /**
   * Assign role to user (POST /api/v1/users/{userId}/roles/{roleId}).
   * Tenant SACCO context is automatically extracted from caller JWT token or user profile.
   */
  assignRole: async (userId, roleId) => {
    const response = await authHttpClient.post(`/users/${userId}/roles/${roleId}`);
    return response.data;
  }
};
