import { transactionHttpClient } from '../../../common/api/httpClient';

const TILL_BASE = '/transactions/tills';

export const tillApi = {
  /**
   * Get currently logged-in teller's cash drawer balance & status
   */
  getMyTill: async () => {
    const response = await transactionHttpClient.get(`${TILL_BASE}/my-till`);
    return response.data;
  },

  /**
   * Open teller drawer with initial cash from vault
   * @param {number|string} openingCash
   */
  openTill: async (openingCash) => {
    const response = await transactionHttpClient.post(`${TILL_BASE}/open`, {
      openingCash: Number(openingCash) || 0,
    });
    return response.data;
  },

  /**
   * Close drawer, submit physical banknote denomination counts, compute variance
   * @param {Object} data - { physicalCashCounted, notes200Count, notes100Count, notes50Count, notes10Count, notes5Count, reconciliationNotes }
   */
  closeTill: async (data) => {
    const response = await transactionHttpClient.post(`${TILL_BASE}/close`, data);
    return response.data;
  },

  /**
   * Assign or configure a till for a branch teller
   * @param {Object} data - { branchId, branchCode, tellerUserId, tillName, tillGlCode, maxCashLimit }
   */
  assignTill: async (data) => {
    const response = await transactionHttpClient.post(`${TILL_BASE}/assign`, data);
    return response.data;
  },

  /**
   * List all tills for a given branch
   * @param {string} branchId
   */
  getBranchTills: async (branchId) => {
    const response = await transactionHttpClient.get(`${TILL_BASE}/branch/${branchId}`);
    return response.data;
  },

  /**
   * Get past cash reconciliation audit records for current teller
   */
  getMyReconciliations: async () => {
    const response = await transactionHttpClient.get(`${TILL_BASE}/my-reconciliations`);
    return response.data;
  },

  /**
   * Get reconciliation audit records for a specific till ID
   * @param {string} tillId
   */
  getTillReconciliations: async (tillId) => {
    const response = await transactionHttpClient.get(`${TILL_BASE}/${tillId}/reconciliations`);
    return response.data;
  },
};
