import { useState } from 'react';
import { authApi } from '../api/authApi';
import { useAuthStore } from '../../../common/store/useAuthStore';

export const useLogin = () => {
  const login = useAuthStore((state) => state.login);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const executeLogin = async (phoneInput, pin) => {
    setIsLoading(true);
    setError(null);

    try {
      // Standardize phone number input into international MSISDN format (+251...)
      let formattedMsisdn = phoneInput.trim();
      if (/^[09]\d{8,9}$/.test(formattedMsisdn)) {
        if (formattedMsisdn.startsWith('0')) {
          formattedMsisdn = '+251' + formattedMsisdn.slice(1);
        } else if (formattedMsisdn.startsWith('9')) {
          formattedMsisdn = '+251' + formattedMsisdn;
        }
      }

      // Transmit request body: { msisdn: "+251...", pin: "1234" }
      const response = await authApi.login({ msisdn: formattedMsisdn, pin });
      
      if (response && response.data) {
        login(response.data);
      } else if (response) {
        login(response);
      }
      
      setIsLoading(false);
      return { success: true, data: response };
    } catch (err) {
      setIsLoading(false);
      const message = err.response?.data?.message || err.message || 'Login failed. Please check your phone number and PIN.';
      setError(message);
      return { success: false, error: message };
    }
  };

  return { executeLogin, isLoading, error, setError };
};
