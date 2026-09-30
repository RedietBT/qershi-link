import { accountHttpClient } from '../../../common/api/httpClient';

const COA_BASE = '/accounting/coa';
const REPORTS_BASE = '/accounting/reports';

export const accountingApi = {
  /**
   * Fetches the nested hierarchical Tree of GL accounts with rolled up balances.
   */
  getCoaTree: async () => {
    const response = await accountHttpClient.get(`${COA_BASE}/tree`);
    return response.data;
  },

  /**
   * Fetches flat list of all GL accounts for dropdown selector in Account creation.
   */
  getCoaFlat: async () => {
    const response = await accountHttpClient.get(`${COA_BASE}/flat`);
    return response.data;
  },

  /**
   * Creates a new GL account in the Chart of Accounts.
   */
  createGlAccount: async (data) => {
    const response = await accountHttpClient.post(COA_BASE, data);
    return response.data;
  },

  /**
   * Generates Trial Balance report as of a specific date.
   */
  getTrialBalance: async (asOfDate) => {
    const params = asOfDate ? { asOfDate } : {};
    const response = await accountHttpClient.get(`${REPORTS_BASE}/trial-balance`, { params });
    return response.data;
  },

  /**
   * Generates Balance Sheet report as of a specific date.
   */
  getBalanceSheet: async (asOfDate) => {
    const params = asOfDate ? { asOfDate } : {};
    const response = await accountHttpClient.get(`${REPORTS_BASE}/balance-sheet`, { params });
    return response.data;
  },

  /**
   * Generates Profit & Loss Statement across a date range.
   */
  getProfitLoss: async (startDate, endDate) => {
    const params = {};
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    const response = await accountHttpClient.get(`${REPORTS_BASE}/profit-loss`, { params });
    return response.data;
  },
};

export default accountingApi;
