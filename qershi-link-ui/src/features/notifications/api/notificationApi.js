import { notificationHttpClient } from '../../../common/api/httpClient';

const BASE_NOTIF = '/notifications';
const BASE_CONFIG = '/notifications/config/sms-gateway';
const BASE_TEMPLATES = '/notifications/templates';

/**
 * Centralized API Module for Notification & SMS Gateway Service.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
export const notificationApi = {
  // --- SMS Gateway Configuration ---
  getGatewayConfig: async () => {
    const response = await notificationHttpClient.get(BASE_CONFIG);
    return response.data;
  },

  saveGatewayConfig: async (configData) => {
    const response = await notificationHttpClient.put(BASE_CONFIG, configData);
    return response.data;
  },

  testGatewayConnection: async (testData) => {
    const response = await notificationHttpClient.post(`${BASE_CONFIG}/test`, testData);
    return response.data;
  },

  // --- Notification Templates ---
  getAllTemplates: async () => {
    const response = await notificationHttpClient.get(BASE_TEMPLATES);
    return response.data;
  },

  getTemplateByCode: async (code) => {
    const response = await notificationHttpClient.get(`${BASE_TEMPLATES}/${encodeURIComponent(code)}`);
    return response.data;
  },

  updateTemplate: async (code, { content, active = true }) => {
    const params = new URLSearchParams();
    if (content !== undefined && content !== null) {
      params.append('content', content);
    }
    params.append('active', String(active));
    const response = await notificationHttpClient.put(`${BASE_TEMPLATES}/${encodeURIComponent(code)}?${params.toString()}`);
    return response.data;
  },

  createTemplate: async (templateData) => {
    const response = await notificationHttpClient.post(BASE_TEMPLATES, templateData);
    return response.data;
  },

  // --- Delivery Audit Logs ---
  getLogs: async () => {
    const response = await notificationHttpClient.get(`${BASE_NOTIF}/logs`);
    return response.data;
  },

  getLogsByRecipient: async (phone) => {
    const response = await notificationHttpClient.get(`${BASE_NOTIF}/logs/recipient/${encodeURIComponent(phone)}`);
    return response.data;
  },

  sendDirectSms: async (payload) => {
    const response = await notificationHttpClient.post(`${BASE_NOTIF}/sms/send`, payload);
    return response.data;
  },
};
