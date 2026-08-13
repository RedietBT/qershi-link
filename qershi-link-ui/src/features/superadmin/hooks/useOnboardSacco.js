import { useState } from 'react';
import { saccoApi } from '../api/saccoApi';
import { sanitizeMsisdn, isValidMsisdn, sanitizePositiveNumber } from '../../../common/utils/sanitizers';

export const useOnboardSacco = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  const executeOnboard = async (formData) => {
    setIsLoading(true);
    setError(null);
    setSuccessMessage(null);

    try {
      // Live payload sanitization before transmission
      const formattedAdminMsisdn = sanitizeMsisdn(formData.adminMsisdn);
      
      if (!isValidMsisdn(formattedAdminMsisdn)) {
        throw new Error('Please provide a valid Ethiopian MSISDN (+251912345678 or 0912345678).');
      }

      const payload = {
        saccoName: formData.saccoName.trim(),
        isUnion: Boolean(formData.isUnion),
        minShareRequirement: sanitizePositiveNumber(formData.minShareRequirement),
        adminMsisdn: formattedAdminMsisdn,
        adminName: formData.adminName.trim(),
        region: formData.region.trim(),
      };

      const response = await saccoApi.onboardSacco(payload);
      
      setIsLoading(false);
      setSuccessMessage(`SACCO "${payload.saccoName}" successfully onboarded! Admin credentials assigned.`);
      return { success: true, data: response };
    } catch (err) {
      setIsLoading(false);
      const message = err.response?.data?.message || err.message || 'Failed to onboard SACCO.';
      setError(message);
      return { success: false, error: message };
    }
  };

  return { executeOnboard, isLoading, error, successMessage, setError, setSuccessMessage };
};
