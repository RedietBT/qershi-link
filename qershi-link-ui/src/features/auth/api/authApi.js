import { authHttpClient } from '../../../common/api/httpClient';

/**
 * Authentication API Service interfacing with identity-auth-service backend (Port 8080).
 */
export const authApi = {
  /**
   * User login endpoint (POST /api/v1/auth/login).
   * Request Body: { "msisdn": "+2519...", "pin": "1234" }
   */
  login: async ({ msisdn, pin }) => {
    const response = await authHttpClient.post('/auth/login', { msisdn, pin });
    return response.data;
  },

  changePin: async (data) => {
    const response = await authHttpClient.post('/auth/pin/change', data);
    return response.data;
  }
};
