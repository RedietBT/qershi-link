import { accountHttpClient } from '../../../common/api/httpClient';

const BASE = '/standing-orders';

/**
 * Standing Orders API module.
 * All HTTP calls are centralized here per the project's API module pattern.
 *
 * @author KAB Digital Solution PLC
 */
export const standingOrderApi = {

  /** Create a new recurring standing order */
  create: async (payload) => {
    const res = await accountHttpClient.post(BASE, payload);
    return res.data;
  },

  /** List all standing orders platform-wide */
  getAll: async () => {
    const res = await accountHttpClient.get(BASE);
    return res.data;
  },

  /** Get standing orders for a specific member by ID */
  getByMemberId: async (memberId) => {
    const res = await accountHttpClient.get(`${BASE}/member/${memberId}`);
    return res.data;
  },

  /** Lookup standing orders by member phone number */
  getByPhone: async (phoneNumber) => {
    const res = await accountHttpClient.get(`${BASE}/lookup/phone/${phoneNumber}`);
    return res.data;
  },

  /** Get a single standing order by its UUID */
  getById: async (id) => {
    const res = await accountHttpClient.get(`${BASE}/${id}`);
    return res.data;
  },

  /** Pause an active standing order */
  pause: async (id) => {
    const res = await accountHttpClient.patch(`${BASE}/${id}/pause`);
    return res.data;
  },

  /** Resume a paused standing order */
  resume: async (id) => {
    const res = await accountHttpClient.patch(`${BASE}/${id}/resume`);
    return res.data;
  },

  /** Cancel a standing order permanently */
  cancel: async (id) => {
    const res = await accountHttpClient.delete(`${BASE}/${id}`);
    return res.data;
  },

  /** Manually trigger daily sweep run for a specific date */
  runSweeps: async (runDate = null) => {
    const params = runDate ? { runDate } : {};
    const res = await accountHttpClient.post(`${BASE}/run-sweeps`, null, { params });
    return res.data;
  },
};
