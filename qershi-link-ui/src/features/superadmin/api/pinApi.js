import { authHttpClient } from '../../../common/api/httpClient';

/**
 * PIN & Credential Operations API Service (/api/v1/pin).
 */
export const pinApi = {
  /**
   * Resend Initial Login PIN via SMS by MSISDN (POST /api/v1/pin/resend).
   */
  resendPinByMsisdn: async (msisdn) => {
    const response = await authHttpClient.post('/pin/resend', { msisdn });
    return response.data;
  },

  /**
   * Resend Initial Login PIN via SMS by User ID UUID (POST /api/v1/pin/resend/{userId}).
   */
  resendPinByUserId: async (userId) => {
    const response = await authHttpClient.post(`/pin/resend/${userId}`);
    return response.data;
  }
};
