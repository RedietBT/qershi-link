import { loanMgmtHttpClient } from '../../../../common/api/httpClient';

const BASE = '/loan-mgmt';

export const loanManagementApi = {
  /**
   * Initiate loan disbursement for an approved application
   * @param {Object} data - { applicationId, userId, productId, amount, interestRatePct, termMonths, repaymentFrequency, interestType, targetSavingsAccountId, memberPhone }
   * @param {string} idempotencyKey - Optional UUID idempotency key
   */
  disburseLoan: async (data, idempotencyKey) => {
    const config = idempotencyKey ? { headers: { 'X-Idempotency-Key': idempotencyKey } } : {};
    const response = await loanMgmtHttpClient.post(`${BASE}/disburse`, data, config);
    return response.data;
  },

  /**
   * Maker-Checker dual control approval for loan disbursement
   * @param {string} accountId - Loan account UUID
   */
  approveDisbursement: async (accountId) => {
    const response = await loanMgmtHttpClient.patch(`${BASE}/disburse/${accountId}/approve`);
    return response.data;
  },

  /**
   * Get loan account details by ID
   * @param {string} accountId
   */
  getAccountById: async (accountId) => {
    const response = await loanMgmtHttpClient.get(`${BASE}/accounts/${accountId}`);
    return response.data;
  },

  /**
   * Get month-by-month amortization repayment schedule for a loan account
   * @param {string} accountId
   */
  getAccountSchedule: async (accountId) => {
    const response = await loanMgmtHttpClient.get(`${BASE}/accounts/${accountId}/schedule`);
    return response.data;
  },

  /**
   * List all active and past loan accounts for a member
   * @param {string} userId
   */
  getUserAccounts: async (userId) => {
    const response = await loanMgmtHttpClient.get(`${BASE}/accounts/user/${userId}`);
    return response.data;
  },

  /**
   * Process loan repayment using Core Banking waterfall rules (Penalty -> Interest -> Principal)
   * @param {Object} data - { accountId, amountPaid, paymentChannel, remarks, memberPhone, sourceAccountNo }
   */
  processRepayment: async (data) => {
    const response = await loanMgmtHttpClient.post(`${BASE}/repayments`, data);
    return response.data;
  }
};
