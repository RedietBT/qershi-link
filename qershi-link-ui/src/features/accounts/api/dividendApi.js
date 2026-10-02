import { accountHttpClient } from '../../../common/api/httpClient';

const BASE = '/dividends';

/**
 * Dividend Distribution API module.
 * All HTTP calls are centralized here per the project's API module pattern.
 *
 * @author KAB Digital Solution PLC
 */
export const dividendApi = {

  /** Dry-run simulation — calculates per-member allocation without posting */
  simulate: async (payload) => {
    const res = await accountHttpClient.post(`${BASE}/simulate`, payload);
    return res.data;
  },

  /** Batch-post a simulated distribution to member savings accounts */
  post: async (distributionId) => {
    const res = await accountHttpClient.post(`${BASE}/${distributionId}/post`);
    return res.data;
  },

  /** List all dividend distribution headers */
  getAllDistributions: async () => {
    const res = await accountHttpClient.get(BASE);
    return res.data;
  },

  /** Get distribution header for a specific fiscal year */
  getByFiscalYear: async (fiscalYear) => {
    const res = await accountHttpClient.get(`${BASE}/year/${fiscalYear}`);
    return res.data;
  },

  /** Get per-member allocation lines for a distribution run */
  getAllocations: async (distributionId) => {
    const res = await accountHttpClient.get(`${BASE}/${distributionId}/allocations`);
    return res.data;
  },
};
