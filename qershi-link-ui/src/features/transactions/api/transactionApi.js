import { transactionHttpClient } from '../../../common/api/httpClient';

const BASE = '/transactions';

export const transactionApi = {
  /**
   * Process over-the-counter cash deposit into a member savings account
   * @param {Object} data - { accountNo, amount, narration }
   * @param {string} idempotencyKey - UUID idempotency key
   */
  processDeposit: async (data, idempotencyKey) => {
    const config = idempotencyKey ? { headers: { 'X-Idempotency-Key': idempotencyKey } } : {};
    const response = await transactionHttpClient.post(`${BASE}/deposit`, data, config);
    return response.data;
  },

  /**
   * Process over-the-counter cash withdrawal from a member savings account
   * @param {Object} data - { accountNo, amount, narration }
   * @param {string} idempotencyKey - UUID idempotency key
   */
  processWithdrawal: async (data, idempotencyKey) => {
    const config = idempotencyKey ? { headers: { 'X-Idempotency-Key': idempotencyKey } } : {};
    const response = await transactionHttpClient.post(`${BASE}/withdraw`, data, config);
    return response.data;
  },

  /**
   * Process member-to-member internal transfer within the same SACCO
   * @param {Object} data - { senderAccountNo, receiverAccountNo, amount, narration }
   * @param {string} idempotencyKey - UUID idempotency key
   */
  processTransfer: async (data, idempotencyKey) => {
    const config = idempotencyKey ? { headers: { 'X-Idempotency-Key': idempotencyKey } } : {};
    const response = await transactionHttpClient.post(`${BASE}/transfer`, data, config);
    return response.data;
  },

  /**
   * Retrieve chronological transaction history for an account
   * @param {string} accountNo
   */
  getAccountTransactions: async (accountNo) => {
    const response = await transactionHttpClient.get(`${BASE}/account/${accountNo}`);
    return response.data;
  },

  /**
   * Retrieve transaction details by transaction reference
   * @param {string} transactionRef
   */
  getTransactionByRef: async (transactionRef) => {
    const response = await transactionHttpClient.get(`${BASE}/${transactionRef}`);
    return response.data;
  },

  /**
   * Retrieve GL double-entry journal lines for a transaction reference
   * @param {string} transactionRef
   */
  getJournalEntryByRef: async (transactionRef) => {
    const response = await transactionHttpClient.get(`${BASE}/${transactionRef}/journal`);
    return response.data;
  }
};
