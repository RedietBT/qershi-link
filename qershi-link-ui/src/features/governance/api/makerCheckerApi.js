import { accountHttpClient } from '../../../common/api/httpClient';

const RULES_BASE = '/sacco-config/maker-checker-rules';

export const makerCheckerApi = {
  /**
   * Fetch active SACCO Maker-Checker policy rules and transaction thresholds.
   */
  getRules: async () => {
    const response = await accountHttpClient.get(RULES_BASE);
    return response.data?.data || response.data;
  },

  /**
   * Update Four-Eyes policy toggles, supervisor thresholds, and domain role clearances.
   */
  updateRules: async (ruleData) => {
    const response = await accountHttpClient.put(RULES_BASE, ruleData);
    return response.data?.data || response.data;
  },

  /**
   * Fetch all per-product Maker-Checker rules and risk limits.
   */
  getProductRules: async () => {
    const response = await accountHttpClient.get(`${RULES_BASE}/products`);
    return response.data?.data || response.data || [];
  },

  /**
   * Update risk limits and Maker-Checker override for a specific account product.
   */
  updateProductRule: async (productCode, ruleData) => {
    const response = await accountHttpClient.put(`${RULES_BASE}/products/${productCode}`, ruleData);
    return response.data?.data || response.data;
  }
};
