import { profileHttpClient } from '../../../common/api/httpClient';

const BASE_URL = '/profiles';

export const memberProfileApi = {
    getAllProfiles: async (status = null) => {
        const params = status ? { status } : {};
        const response = await profileHttpClient.get(BASE_URL, { params });
        return response.data;
    },

    getProfileByUserId: async (userId) => {
        const response = await profileHttpClient.get(`${BASE_URL}/${userId}`);
        return response.data;
    },

    createProfile: async (data) => {
        const response = await profileHttpClient.post(BASE_URL, data);
        return response.data;
    },

    updateDemographics: async (userId, data) => {
        const response = await profileHttpClient.put(`${BASE_URL}/${userId}/demographics`, data);
        return response.data;
    },

    saveAddress: async (userId, data) => {
        const response = await profileHttpClient.post(`${BASE_URL}/${userId}/address`, data);
        return response.data;
    },

    saveEmployment: async (userId, data) => {
        const response = await profileHttpClient.post(`${BASE_URL}/${userId}/employment`, data);
        return response.data;
    },

    changeStatus: async (userId, status) => {
        const response = await profileHttpClient.put(`${BASE_URL}/${userId}/status`, { status });
        return response.data;
    },

    approveOnboarding: async (userId, remarks) => {
        const response = await profileHttpClient.put(`${BASE_URL}/${userId}/approve`, { remarks });
        return response.data;
    }
};
