import { accountHttpClient } from '../../../common/api/httpClient';

const SACCO_CONFIG_URL = '/sacco-config';

export const accountConfigApi = {
    getSaccoConfig: async () => {
        const response = await accountHttpClient.get(SACCO_CONFIG_URL);
        return response.data;
    },

    setSaccoConfig: async (data) => {
        // POST for first-time creation
        const response = await accountHttpClient.post(SACCO_CONFIG_URL, data);
        return response.data;
    },

    updateSaccoConfig: async (data) => {
        // PUT for updates
        const response = await accountHttpClient.put(SACCO_CONFIG_URL, data);
        return response.data;
    }
};
