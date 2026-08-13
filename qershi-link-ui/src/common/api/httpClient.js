import axios from 'axios';
import { useAuthStore } from '../store/useAuthStore';

/**
 * Service Port Registry mapping local microservice ports (or production Gateway).
 */
export const SERVICE_ENDPOINTS = {
  AUTH: import.meta.env.VITE_AUTH_URL || 'http://localhost:8080/api/v1',
  PROFILE: import.meta.env.VITE_PROFILE_URL || 'http://localhost:8081/api/v1',
  ACCOUNT: import.meta.env.VITE_ACCOUNT_URL || 'http://localhost:8082/api/v1',
  TRANSACTION: import.meta.env.VITE_TRANSACTION_URL || 'http://localhost:8083/api/v1',
  LOAN_ORIG: import.meta.env.VITE_LOAN_ORIG_URL || 'http://localhost:8084/api/v1',
  LOAN_MGMT: import.meta.env.VITE_LOAN_MGMT_URL || 'http://localhost:8085/api/v1',
  NOTIFICATION: import.meta.env.VITE_NOTIFICATION_URL || 'http://localhost:8086/api/v1',
};

/**
 * Factory creating Axios instances with shared security interceptors.
 * Reads JWT Token & Tenant Schema directly from Zustand store state!
 */
const createServiceHttpClient = (baseURL) => {
  const instance = axios.create({
    baseURL,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  // Request Interceptor: Reads live token & tenantSchema from Zustand store
  instance.interceptors.request.use(
    (config) => {
      const { token, tenantSchema } = useAuthStore.getState();

      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      if (tenantSchema) {
        config.headers['X-Tenant-Id'] = tenantSchema;
      }
      return config;
    },
    (error) => Promise.reject(error)
  );

  // Response Interceptor: 401 Session Handling
  instance.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401) {
        console.warn('Session expired or unauthorized request.');
        useAuthStore.getState().logout();
      }
      return Promise.reject(error);
    }
  );

  return instance;
};

// Export dedicated Microservice HTTP Clients sharing security interceptors
export const authHttpClient = createServiceHttpClient(SERVICE_ENDPOINTS.AUTH);
export const profileHttpClient = createServiceHttpClient(SERVICE_ENDPOINTS.PROFILE);
export const accountHttpClient = createServiceHttpClient(SERVICE_ENDPOINTS.ACCOUNT);
export const transactionHttpClient = createServiceHttpClient(SERVICE_ENDPOINTS.TRANSACTION);
export const loanOrigHttpClient = createServiceHttpClient(SERVICE_ENDPOINTS.LOAN_ORIG);
export const loanMgmtHttpClient = createServiceHttpClient(SERVICE_ENDPOINTS.LOAN_MGMT);
export const notificationHttpClient = createServiceHttpClient(SERVICE_ENDPOINTS.NOTIFICATION);

export default authHttpClient;
