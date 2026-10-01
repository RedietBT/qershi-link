import { pricingHttpClient } from '../../../common/api/httpClient';

const BASE = '/tariffs';

export const pricingApi = {
    getAllTariffs: async () => {
        const response = await pricingHttpClient.get(BASE);
        return response.data;
    },

    createTariff: async (data) => {
        const response = await pricingHttpClient.post(BASE, data);
        return response.data;
    },

    updateTariff: async (id, data) => {
        const response = await pricingHttpClient.put(`${BASE}/${id}`, data);
        return response.data;
    },

    toggleTariff: async (id, active) => {
        const response = await pricingHttpClient.patch(`${BASE}/${id}/toggle?active=${active}`);
        return response.data;
    },

    calculateFee: async (transactionType, amount) => {
        const response = await pricingHttpClient.get(`${BASE}/calculate`, {
            params: { transactionType, amount }
        });
        return response.data;
    },

    getTaxLogs: async (accountNo) => {
        const response = await pricingHttpClient.get(`${BASE}/tax-logs`, {
            params: accountNo ? { accountNo } : {}
        });
        return response.data;
    },

    getTaxSummary: async (startDate, endDate) => {
        const response = await pricingHttpClient.get(`${BASE}/tax-logs/summary`, {
            params: { startDate, endDate }
        });
        return response.data;
    }
};

export const tariffApi = pricingApi; // backwards-compatible alias
