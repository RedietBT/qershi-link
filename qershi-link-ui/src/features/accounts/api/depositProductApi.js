import { accountHttpClient } from '../../../common/api/httpClient';

const BASE = '/accounts/products';

export const depositProductApi = {
    getAllProducts: async () => {
        const response = await accountHttpClient.get(BASE);
        return response.data;
    },

    getProductByCode: async (productCode) => {
        const response = await accountHttpClient.get(`${BASE}/${productCode}`);
        return response.data;
    },

    createProduct: async (data) => {
        const response = await accountHttpClient.post(BASE, data);
        return response.data;
    }
};
