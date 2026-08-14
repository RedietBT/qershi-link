import { profileHttpClient } from '../../../common/api/httpClient';

const BASE_URL = '/profiles/audit';

export const profileAuditApi = {
    getAuditLogs: async (page = 0, size = 50) => {
        const response = await profileHttpClient.get(`${BASE_URL}`, { params: { page, size } });
        return response.data;
    },

    getAuditLogsByUserId: async (userId) => {
        const response = await profileHttpClient.get(`${BASE_URL}/user/${userId}`);
        return response.data;
    }
};
