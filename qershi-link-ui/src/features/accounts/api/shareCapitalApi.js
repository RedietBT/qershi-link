import { accountHttpClient } from '../../../common/api/httpClient';

const BASE = '/share-capital';

/**
 * Share Capital API module.
 * All HTTP calls are centralized here, keeping pages free of axios/fetch logic.
 *
 * @author KAB Digital Solution PLC
 */
export const shareCapitalApi = {

  /** Get member share account summary (holdings, compliance, certificates) */
  getMemberSummary: async (memberId) => {
    const res = await accountHttpClient.get(`${BASE}/member/${memberId}`);
    return res.data;
  },

  /** Open or retrieve a share account (idempotent) */
  getOrCreateAccount: async (memberId, saccoCode = 'DEFAULT', branchCode = null) => {
    const res = await accountHttpClient.post(`${BASE}/member/${memberId}/account`, null, {
      params: { saccoCode, branchCode },
    });
    return res.data;
  },

  /** Purchase shares — debits source savings, issues certificate */
  purchaseShares: async (memberId, payload) => {
    const res = await accountHttpClient.post(`${BASE}/member/${memberId}/purchase`, payload);
    return res.data;
  },

  /** Lookup share account by phone number */
  lookupByPhone: async (phoneNumber) => {
    const res = await accountHttpClient.get(`${BASE}/lookup/phone/${phoneNumber}`);
    return res.data;
  },

  /** Get all share certificates for a member */
  getMemberCertificates: async (memberId) => {
    const res = await accountHttpClient.get(`${BASE}/member/${memberId}/certificates`);
    return res.data;
  },

  /** Initiate a peer-to-peer share transfer (creates pending for approval) */
  initiateTransfer: async (payload) => {
    const res = await accountHttpClient.post(`${BASE}/transfer`, payload);
    return res.data;
  },

  /** Four-eyes approval for a pending share transfer */
  approveTransfer: async (transferId) => {
    const res = await accountHttpClient.patch(`${BASE}/transfer/${transferId}/approve`);
    return res.data;
  },

  /** List all pending transfers awaiting approval */
  getPendingTransfers: async () => {
    const res = await accountHttpClient.get(`${BASE}/transfer/pending`);
    return res.data;
  },

  /** Get all transfers for a specific member (incoming + outgoing) */
  getMemberTransfers: async (memberId) => {
    const res = await accountHttpClient.get(`${BASE}/member/${memberId}/transfers`);
    return res.data;
  },
};
