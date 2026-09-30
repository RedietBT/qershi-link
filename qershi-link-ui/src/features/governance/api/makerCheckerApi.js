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
   * Update Four-Eyes policy toggles, supervisor thresholds, and anti-self-approval settings.
   */
  updateRules: async (ruleData) => {
    const response = await accountHttpClient.put(RULES_BASE, ruleData);
    return response.data?.data || response.data;
  }
};
