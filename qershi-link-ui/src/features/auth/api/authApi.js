import { authHttpClient } from '../../../common/api/httpClient';

/**
 * Auth API Service (/api/v1/auth).
 */
export const authApi = {
  /**
   * Login user with msisdn and 6-digit PIN (POST /api/v1/auth/login).
   */
  login: async ({ msisdn, pin }) => {
    const response = await authHttpClient.post('/auth/login', { msisdn, pin });
    return response.data;
  },

  /**
   * Logout user and revoke token (POST /api/v1/auth/logout).
   */
  logout: async () => {
    const response = await authHttpClient.post('/auth/logout');
    return response.data;
  },

  /**
   * First-time PIN rotation or PIN change (POST /api/v1/auth/change-password).
   * Payload: { msisdn: "+2519...", oldPin: "123456", newPin: "654321" }
   */
  changePassword: async ({ msisdn, oldPin, newPin }) => {
    const response = await authHttpClient.post('/auth/change-password', {
      msisdn,
      oldPin,
      newPin
    });
    return response.data;
  }
};
