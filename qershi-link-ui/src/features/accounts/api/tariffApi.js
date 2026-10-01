import { accountHttpClient } from '../../../common/api/httpClient';

const BASE = '/tariffs';

export const tariffApi = {
    getAllTariffs: async () => {
        const response = await accountHttpClient.get(BASE);
        return response.data;
    },

    createTariff: async (data) => {
        const response = await accountHttpClient.post(BASE, data);
        return response.data;
    },

    updateTariff: async (id, data) => {
        const response = await accountHttpClient.put(`${BASE}/${id}`, data);
        return response.data;
    },

    toggleTariff: async (id, active) => {
        const response = await accountHttpClient.patch(`${BASE}/${id}/toggle?active=${active}`);
        return response.data;
    },

    calculateFee: async (transactionType, amount) => {
        const response = await accountHttpClient.get(`${BASE}/calculate`, {
            params: { transactionType, amount }
        });
        return response.data;
    },

    getTaxLogs: async (accountNo) => {
        const response = await accountHttpClient.get(`${BASE}/tax-logs`, {
            params: accountNo ? { accountNo } : {}
        });
        return response.data;
    },

    getTaxSummary: async (startDate, endDate) => {
        const response = await accountHttpClient.get(`${BASE}/tax-logs/summary`, {
            params: { startDate, endDate }
        });
        return response.data;
    }
};
