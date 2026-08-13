import { authHttpClient } from '../../../common/api/httpClient';

/**
 * Super Admin SACCO Onboarding API Service (/api/v1/sacco).
 */
export const saccoApi = {
  /**
   * Onboards a new SACCO tenant (POST /api/v1/sacco/onboard).
   * 
   * Request Body Payload:
   * {
   *   "saccoName": "Awach SACCO",
   *   "isUnion": false,
   *   "minShareRequirement": 500,
   *   "adminMsisdn": "+251987654321",
   *   "adminName": "Arsema Degu",
   *   "region": "Addis Ababa"
   * }
   */
  onboardSacco: async (payload) => {
    const response = await authHttpClient.post('/sacco/onboard', payload);
    return response.data;
  }
};
