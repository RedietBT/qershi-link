import { accountHttpClient } from '../../../common/api/httpClient';

const BASE = '/account-mgmt/audit';

export const accountAuditApi = {
    /** GET /api/v1/account-mgmt/audit?page=0&size=50 */
    getAuditLogs: async (page = 0, size = 50) => {
        const response = await accountHttpClient.get(BASE, { params: { page, size } });
        return response.data;
    },

    /** GET /api/v1/account-mgmt/audit/account/{accountNo} */
    getLogsByAccountNo: async (accountNo) => {
        const response = await accountHttpClient.get(`${BASE}/account/${accountNo}`);
        return response.data;
    },

    /** GET /api/v1/account-mgmt/audit/user/{userId} */
    getLogsByUserId: async (userId) => {
        const response = await accountHttpClient.get(`${BASE}/user/${userId}`);
        return response.data;
    },
};
