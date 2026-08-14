import { profileHttpClient } from '../../../common/api/httpClient';

const BASE_URL = '/kyc';

export const kycApi = {
    submitIdentification: async (userId, data) => {
        const response = await profileHttpClient.post(`${BASE_URL}/${userId}/identifications`, data);
        return response.data;
    },

    getAllIdentifications: async (status = null) => {
        const params = status ? { status } : {};
        const response = await profileHttpClient.get(`${BASE_URL}/identifications`, { params });
        return response.data;
    },

    getIdentificationsByUserId: async (userId) => {
        const response = await profileHttpClient.get(`${BASE_URL}/${userId}/identifications`);
        return response.data;
    },

    getIdentificationById: async (identificationId) => {
        const response = await profileHttpClient.get(`${BASE_URL}/identifications/${identificationId}`);
        return response.data;
    },

    verifyKycIdentification: async (identificationId, notes) => {
        const response = await profileHttpClient.put(`${BASE_URL}/identifications/${identificationId}/verify`, { notes });
        return response.data;
    },

    rejectKycIdentification: async (identificationId, notes) => {
        const response = await profileHttpClient.put(`${BASE_URL}/identifications/${identificationId}/reject`, { notes });
        return response.data;
    }
};
