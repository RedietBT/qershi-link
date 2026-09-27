import { loanOrigHttpClient } from '../../../../common/api/httpClient';

const BASE = '/loan-org';

export const loanOriginationApi = {
  /**
   * Submit an individual or group loan application
   * @param {Object} data - { userId, groupId, productId, scoringType, amountRequested, savingsConsistency, historicalYield, projectedYield, landSizeHectares, collaterals }
   */
  submitApplication: async (data) => {
    const response = await loanOrigHttpClient.post(`${BASE}/apply`, data);
    return response.data;
  },

  /**
   * Retrieve loan application details, credit score, and approval audit trail
   * @param {string} applicationId
   */
  getApplicationById: async (applicationId) => {
    const response = await loanOrigHttpClient.get(`${BASE}/applications/${applicationId}`);
    return response.data;
  },

  /**
   * List all loan applications submitted for a borrower
   * @param {string} userId
   */
  listApplicationsForUser: async (userId) => {
    const response = await loanOrigHttpClient.get(`${BASE}/applications/user/${userId}`);
    return response.data;
  },

  /**
   * Maker-Checker dual control decision (APPROVE or REJECT)
   * @param {string} applicationId
   * @param {Object} data - { actionType: 'APPROVE' | 'REJECT', amountApproved, remarks }
   */
  processApproval: async (applicationId, data) => {
    const response = await loanOrigHttpClient.patch(`${BASE}/approve/${applicationId}`, data);
    return response.data;
  },

  /**
   * List registered SACCO borrowing groups
   */
  listGroups: async () => {
    const response = await loanOrigHttpClient.get(`${BASE}/groups`);
    return response.data;
  },

  /**
   * Register a new SACCO borrowing group
   * @param {Object} data - { groupName, isFormal, licenseNo, memberUserIds, leaderUserId }
   */
  createGroup: async (data) => {
    const response = await loanOrigHttpClient.post(`${BASE}/groups`, data);
    return response.data;
  },

  /**
   * Get borrowing group details by ID
   * @param {string} groupId
   */
  getGroupById: async (groupId) => {
    const response = await loanOrigHttpClient.get(`${BASE}/groups/${groupId}`);
    return response.data;
  }
};
