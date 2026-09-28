import { accountHttpClient } from '../../../common/api/httpClient';

const BRANCH_BASE = '/accounts/branches';

export const branchApi = {
  /**
   * Fetch all branches, optionally filtered by status ('ACTIVE' or 'INACTIVE')
   */
  getAllBranches: async (status) => {
    const params = status ? { status } : {};
    const response = await accountHttpClient.get(BRANCH_BASE, { params });
    return response.data;
  },

  /**
   * Fetch branch by UUID
   */
  getBranchById: async (branchId) => {
    const response = await accountHttpClient.get(`${BRANCH_BASE}/${branchId}`);
    return response.data;
  },

  /**
   * Fetch branch by code (e.g. 001)
   */
  getBranchByCode: async (branchCode) => {
    const response = await accountHttpClient.get(`${BRANCH_BASE}/code/${branchCode}`);
    return response.data;
  },

  /**
   * Create a new branch
   */
  createBranch: async (branchData) => {
    const response = await accountHttpClient.post(BRANCH_BASE, branchData);
    return response.data;
  },

  /**
   * Update existing branch details
   */
  updateBranch: async (branchId, branchData) => {
    const response = await accountHttpClient.put(`${BRANCH_BASE}/${branchId}`, branchData);
    return response.data;
  },

  /**
   * Activate or deactivate branch
   */
  updateBranchStatus: async (branchId, status) => {
    const response = await accountHttpClient.patch(`${BRANCH_BASE}/${branchId}/status`, { status });
    return response.data;
  },
};
