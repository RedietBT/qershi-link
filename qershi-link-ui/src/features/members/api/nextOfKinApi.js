import { profileHttpClient } from '../../../common/api/httpClient';

const BASE_URL = '/kin';

export const nextOfKinApi = {
    addNextOfKin: async (userId, data) => {
        const response = await profileHttpClient.post(`${BASE_URL}/${userId}`, data);
        return response.data;
    },

    getNextOfKinByUserId: async (userId) => {
        const response = await profileHttpClient.get(`${BASE_URL}/${userId}`);
        return response.data;
    },

    updateNextOfKin: async (kinId, data) => {
        const response = await profileHttpClient.put(`${BASE_URL}/${kinId}`, data);
        return response.data;
    },

    deleteNextOfKin: async (kinId) => {
        const response = await profileHttpClient.delete(`${BASE_URL}/${kinId}`);
        return response.data;
    }
};
