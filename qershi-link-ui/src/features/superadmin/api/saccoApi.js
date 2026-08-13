import { authHttpClient } from '../../../common/api/httpClient';

/**
 * Super Admin SACCO Registry API Service (/api/v1/saccos).
 */
export const saccoApi = {
  /**
   * Onboards a new SACCO tenant (POST /api/v1/sacco/onboard).
   */
  onboardSacco: async (payload) => {
    const response = await authHttpClient.post('/sacco/onboard', payload);
    return response.data;
  },

  /**
   * Fetch all registered SACCO ecosystem workspaces (GET /api/v1/saccos).
   */
  getSaccos: async () => {
    const response = await authHttpClient.get('/saccos');
    return response.data;
  },

  /**
   * Fetch single SACCO registry profile metadata by UUID (GET /api/v1/saccos/{id}).
   */
  getSaccoById: async (saccoId) => {
    const response = await authHttpClient.get(`/saccos/${saccoId}`);
    return response.data;
  }
};
