import { accountHttpClient } from '../../../common/api/httpClient';

const BASE = '/accounts';

export const accountLedgerApi = {
    getAllAccounts: async () => {
        const response = await accountHttpClient.get(BASE);
        return response.data;
    },

    getAccountsByUserId: async (userId) => {
        const response = await accountHttpClient.get(`${BASE}/user/${userId}`);
        return response.data;
    },

    getAccountByNo: async (accountNo) => {
        const response = await accountHttpClient.get(`${BASE}/${accountNo}`);
        return response.data;
    },

    getAccountsByPhone: async (phone) => {
        const response = await accountHttpClient.get(`${BASE}/phone/${phone}`);
        return response.data;
    },

    openAccount: async (data) => {
        const response = await accountHttpClient.post(BASE, data);
        return response.data;
    },

    approveAccount: async (accountNo) => {
        const response = await accountHttpClient.put(`${BASE}/${accountNo}/approve`);
        return response.data;
    },

    // Lien endpoints
    placeLien: async (accountNo, data) => {
        const response = await accountHttpClient.post(`${BASE}/${accountNo}/liens`, data);
        return response.data;
    },

    releaseLien: async (lienId) => {
        const response = await accountHttpClient.put(`${BASE}/liens/${lienId}/release`);
        return response.data;
    },

    getLiens: async (accountNo) => {
        const response = await accountHttpClient.get(`${BASE}/${accountNo}/liens`);
        return response.data;
    },

    // Freeze endpoint
    freezeAccount: async (accountNo, data) => {
        const response = await accountHttpClient.put(`${BASE}/${accountNo}/freeze`, data);
        return response.data;
    }
};
