import React, { useState, useEffect } from 'react';
import {
  Radio,
  Save,
  RefreshCw,
  AlertCircle,
  CheckCircle2,
  Send,
  ShieldCheck,
  Server,
  Lock,
  Globe,
  Sliders,
  ExternalLink,
  Info,
} from 'lucide-react';
import { notificationApi } from '../api/notificationApi';
import { TestSmsModal } from '../components/TestSmsModal';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS, ROLES } from '../../../common/constants/permissions';

const PROVIDER_METADATA = [
  {
    type: 'AFROMESSAGE',
    name: 'AfroMessage',
    tagline: 'Leading Ethiopian SMS & OTP Aggregator',
    color: 'from-amber-500/20 to-orange-500/20 text-orange-400 border-orange-500/30',
    badgeColor: 'bg-orange-500/10 text-orange-400 border-orange-500/20',
    description: 'Direct SMS delivery across Ethio Telecom & Safaricom Ethiopia networks with shortcode support.',
    docsUrl: 'https://afromessage.com/docs',
  },
  {
    type: 'ETHIO_TELECOM',
    name: 'Ethio Telecom Gateway',
    tagline: 'Direct National Telecom Enterprise Portal',
    color: 'from-emerald-500/20 to-teal-500/20 text-emerald-400 border-emerald-500/30',
    badgeColor: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
    description: 'Direct HTTP REST/SMPP integration with Ethio Telecom enterprise bulk SMS platform.',
    docsUrl: 'https://www.ethiotelecom.et',
  },
  {
    type: 'INFOBIP',
    name: 'Infobip Enterprise',
    tagline: 'Global Enterprise Cloud Communications',
    color: 'from-orange-500/20 to-red-500/20 text-orange-300 border-orange-500/30',
    badgeColor: 'bg-orange-500/10 text-orange-300 border-orange-500/20',
    description: 'High-availability global tier-1 carrier routes with real-time delivery status reports.',
    docsUrl: 'https://www.infobip.com/docs/api',
  },
  {
    type: 'CUSTOM_WEBHOOK',
    name: 'Custom Webhook / SMS Box',
    tagline: 'Local GSM Gateway or On-Premise Relay',
    color: 'from-blue-500/20 to-indigo-500/20 text-blue-400 border-blue-500/30',
    badgeColor: 'bg-blue-500/10 text-blue-400 border-blue-500/20',
    description: 'HTTP POST webhook payload forwarder for proprietary or on-premise hardware GSM modems.',
    docsUrl: null,
  },
  {
    type: 'SIMULATED',
    name: 'Simulated Gateway (Dev/Sandbox)',
    tagline: 'Zero-Cost Sandbox Testing Engine',
    color: 'from-cyan-500/20 to-sky-500/20 text-cyan-400 border-cyan-500/30',
    badgeColor: 'bg-cyan-500/10 text-cyan-400 border-cyan-500/20',
    description: 'Emulates SMS delivery in local logs and database without calling external telecom APIs.',
    docsUrl: null,
  },
];

/**
 * SMS Gateway Configuration Page.
 * Allows SACCO administrators to dynamically configure credentials and test external SMS providers.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
export const SmsGatewayConfigPage = () => {
  const [config, setConfig] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);
  const [isTestModalOpen, setIsTestModalOpen] = useState(false);

  // Form State
  const [providerType, setProviderType] = useState('AFROMESSAGE');
  const [senderId, setSenderId] = useState('');
  const [apiKey, setApiKey] = useState('');
  const [apiSecret, setApiSecret] = useState('');
  const [apiEndpointUrl, setApiEndpointUrl] = useState('');
  const [serviceAccountId, setServiceAccountId] = useState('');
  const [callbackUrl, setCallbackUrl] = useState('');
  const [isActive, setIsActive] = useState(true);

  const fetchConfig = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await notificationApi.getGatewayConfig();
      if (data) {
        setConfig(data);
        setProviderType(data.providerType || 'AFROMESSAGE');
        setSenderId(data.senderId || '');
        setApiSecret(data.apiSecret || '');
        setApiEndpointUrl(data.apiEndpointUrl || '');
        setServiceAccountId(data.serviceAccountId || '');
        setCallbackUrl(data.callbackUrl || '');
        setIsActive(data.active ?? true);
        // We leave apiKey empty so that if unchanged, user leaves it blank or fills new
      }
    } catch (err) {
      console.warn('No active gateway config found or failed to load:', err);
      setError('Unable to load SMS gateway settings. A default template is provided.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchConfig();
  }, []);

  const handleSave = async (e) => {
    e.preventDefault();
    try {
      setSaving(true);
      setError(null);
      setSuccessMsg(null);

      const payload = {
        providerType,
        senderId: senderId.trim(),
        apiSecret: apiSecret.trim(),
        apiEndpointUrl: apiEndpointUrl.trim(),
        serviceAccountId: serviceAccountId.trim(),
        callbackUrl: callbackUrl.trim(),
        active: isActive,
      };

      // Only pass apiKey if operator entered a new key
      if (apiKey.trim()) {
        payload.apiKey = apiKey.trim();
      }

      const updated = await notificationApi.saveGatewayConfig(payload);
      setConfig(updated);
      setApiKey(''); // Clear plain text input
      setSuccessMsg(`SMS Gateway settings for ${providerType} saved successfully.`);
      setTimeout(() => setSuccessMsg(null), 5000);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to save gateway configuration.');
    } finally {
      setSaving(false);
    }
  };

  const currentProviderMeta = PROVIDER_METADATA.find((p) => p.type === providerType) || PROVIDER_METADATA[0];

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6 animate-fadeIn">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800 pb-5">
        <div className="flex items-center gap-3">
          <div className="p-3 rounded-2xl bg-cyan-500/10 text-cyan-400 border border-cyan-500/20 shadow-lg shadow-cyan-500/5">
            <Radio className="w-6 h-6 animate-pulse" />
          </div>
          <div>
            <h1 className="text-xl font-black text-white tracking-tight flex items-center gap-2">
              SMS Gateway Provider
              <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-cyan-500/10 text-cyan-400 border border-cyan-500/30">
                Tenant Config
              </span>
            </h1>
            <p className="text-xs text-slate-400 mt-0.5">
              Manage multi-channel credentials, API tokens, and live connection tests for member alerts.
            </p>
          </div>
        </div>

        {/* Action Controls */}
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={fetchConfig}
            disabled={loading}
            className="px-3.5 py-2 text-xs font-semibold text-slate-300 hover:text-white bg-slate-800/80 hover:bg-slate-700 border border-slate-700 rounded-xl flex items-center gap-2 transition-all"
            title="Reload from server"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            Refresh
          </button>

          {/* Test Connection Button */}
          <PermissionGuard
            roles={[ROLES.SUPER_ADMIN, ROLES.SACCO_ADMIN, ROLES.ADMIN]}
            permissions={[PERMISSIONS.NOTIFICATION_CONFIG_MANAGE, PERMISSIONS.NOTIFICATION_SEND]}
            fallback={
              <button
                disabled
                className="px-4 py-2 text-xs font-bold bg-slate-800 text-slate-500 rounded-xl cursor-not-allowed border border-slate-700"
              >
                Test Restricted
              </button>
            }
          >
            <button
              type="button"
              onClick={() => setIsTestModalOpen(true)}
              className="px-4 py-2 text-xs font-bold text-cyan-400 hover:text-cyan-300 bg-cyan-500/10 hover:bg-cyan-500/20 border border-cyan-500/30 rounded-xl flex items-center gap-2 transition-all shadow-sm active:scale-95"
            >
              <Send className="w-3.5 h-3.5" />
              Test Connection
            </button>
          </PermissionGuard>
        </div>
      </div>

      {/* Notifications / Alerts */}
      {error && (
        <div className="p-4 bg-rose-500/10 border border-rose-500/30 rounded-2xl flex items-start gap-3 text-rose-400 text-xs">
          <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
          <div className="flex-1 font-mono">{error}</div>
        </div>
      )}

      {successMsg && (
        <div className="p-4 bg-emerald-500/10 border border-emerald-500/30 rounded-2xl flex items-center gap-3 text-emerald-400 text-xs animate-fadeIn">
          <CheckCircle2 className="w-4 h-4 shrink-0" />
          <span className="font-semibold">{successMsg}</span>
        </div>
      )}

      {/* Provider Selector Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-3">
        {PROVIDER_METADATA.map((p) => {
          const isSelected = providerType === p.type;
          return (
            <button
              key={p.type}
              type="button"
              onClick={() => setProviderType(p.type)}
              className={`text-left p-4 rounded-2xl border transition-all duration-200 flex flex-col justify-between ${
                isSelected
                  ? 'bg-slate-800/90 border-cyan-500/60 shadow-lg shadow-cyan-500/10 ring-1 ring-cyan-500/50'
                  : 'bg-slate-900/60 hover:bg-slate-800/50 border-slate-800 hover:border-slate-700'
              }`}
            >
              <div>
                <div className="flex items-center justify-between mb-2">
                  <span
                    className={`text-[10px] font-mono font-bold px-2 py-0.5 rounded-full border ${p.badgeColor}`}
                  >
                    {p.type}
                  </span>
                  {isSelected && (
                    <span className="w-2 h-2 rounded-full bg-cyan-400 shadow-sm shadow-cyan-400" />
                  )}
                </div>
                <div className="text-xs font-black text-white">{p.name}</div>
                <div className="text-[11px] text-slate-400 mt-1 line-clamp-2 leading-tight">
                  {p.tagline}
                </div>
              </div>

              {p.docsUrl && (
                <div className="mt-3 pt-2 border-t border-slate-800/80 flex items-center gap-1 text-[10px] text-cyan-400/80 hover:text-cyan-300">
                  <span>API Specs</span>
                  <ExternalLink className="w-2.5 h-2.5" />
                </div>
              )}
            </button>
          );
        })}
      </div>

      {/* Main Configuration Card */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Column: Form Settings */}
        <div className="lg:col-span-8 bg-slate-900/80 border border-slate-800 rounded-3xl p-6 shadow-xl">
          <form onSubmit={handleSave} className="space-y-5">
            <div className="flex items-center justify-between border-b border-slate-800 pb-4">
              <div>
                <h2 className="text-sm font-black text-white flex items-center gap-2">
                  <Sliders className="w-4 h-4 text-cyan-400" />
                  Credentials for {currentProviderMeta.name}
                </h2>
                <p className="text-xs text-slate-400 mt-0.5">
                  {currentProviderMeta.description}
                </p>
              </div>

              {/* Status Toggle */}
              <div className="flex items-center gap-2">
                <span className="text-xs font-bold text-slate-300">Active</span>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={isActive}
                    onChange={(e) => setIsActive(e.target.checked)}
                    className="sr-only peer"
                  />
                  <div className="w-10 h-5 bg-slate-800 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-cyan-500"></div>
                </label>
              </div>
            </div>

            {/* Form Inputs */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Sender ID / Alphanumeric Mask */}
              <div>
                <label className="block text-xs font-bold text-slate-300 mb-1.5 flex items-center gap-1.5">
                  Sender ID / Header Mask:
                </label>
                <input
                  type="text"
                  value={senderId}
                  onChange={(e) => setSenderId(e.target.value)}
                  placeholder="e.g. AWASH_SACCO"
                  maxLength={11}
                  className="w-full bg-slate-950 border border-slate-700/80 rounded-2xl px-4 py-2.5 text-xs text-slate-100 placeholder-slate-600 focus:outline-none focus:border-cyan-500 font-mono shadow-inner"
                />
                <span className="text-[10px] text-slate-500 mt-1 block">
                  Telecommunication standard: Max 11 alphanumeric characters.
                </span>
              </div>

              {/* Service Account ID (e.g. Ethio Telecom or sub-account) */}
              <div>
                <label className="block text-xs font-bold text-slate-300 mb-1.5 flex items-center gap-1.5">
                  Service / Customer Account ID:
                </label>
                <input
                  type="text"
                  value={serviceAccountId}
                  onChange={(e) => setServiceAccountId(e.target.value)}
                  placeholder="e.g. ET-BULK-99214"
                  className="w-full bg-slate-950 border border-slate-700/80 rounded-2xl px-4 py-2.5 text-xs text-slate-100 placeholder-slate-600 focus:outline-none focus:border-cyan-500 font-mono shadow-inner"
                />
                <span className="text-[10px] text-slate-500 mt-1 block">
                  Account reference provided by the telecom operator.
                </span>
              </div>

              {/* API Key / Token */}
              <div className="md:col-span-2">
                <label className="block text-xs font-bold text-slate-300 mb-1.5 flex items-center gap-1.5">
                  <Lock className="w-3.5 h-3.5 text-cyan-400" />
                  API Key / Bearer Authentication Token:
                </label>
                <input
                  type="password"
                  value={apiKey}
                  onChange={(e) => setApiKey(e.target.value)}
                  placeholder={
                    config?.maskedApiKey
                      ? `Stored: ${config.maskedApiKey} (Leave blank to keep unchanged)`
                      : 'Paste API Token / Key here...'
                  }
                  className="w-full bg-slate-950 border border-slate-700/80 rounded-2xl px-4 py-2.5 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500 font-mono shadow-inner"
                />
                {config?.maskedApiKey && !apiKey && (
                  <span className="text-[11px] text-cyan-400/80 mt-1 block font-mono">
                    ✓ Currently secured on backend: {config.maskedApiKey}
                  </span>
                )}
              </div>

              {/* API Secret (Optional) */}
              <div>
                <label className="block text-xs font-bold text-slate-300 mb-1.5">
                  API Secret (Optional):
                </label>
                <input
                  type="password"
                  value={apiSecret}
                  onChange={(e) => setApiSecret(e.target.value)}
                  placeholder="Optional signing secret..."
                  className="w-full bg-slate-950 border border-slate-700/80 rounded-2xl px-4 py-2.5 text-xs text-slate-100 placeholder-slate-600 focus:outline-none focus:border-cyan-500 font-mono shadow-inner"
                />
              </div>

              {/* Custom API Endpoint URL */}
              <div>
                <label className="block text-xs font-bold text-slate-300 mb-1.5 flex items-center gap-1.5">
                  <Globe className="w-3.5 h-3.5 text-cyan-400" />
                  API Endpoint URL:
                </label>
                <input
                  type="text"
                  value={apiEndpointUrl}
                  onChange={(e) => setApiEndpointUrl(e.target.value)}
                  placeholder={
                    providerType === 'AFROMESSAGE'
                      ? 'https://api.afromessage.com/api/send'
                      : providerType === 'ETHIO_TELECOM'
                      ? 'https://bulk-sms.ethiotelecom.et/api/v1/send'
                      : 'https://api.yourprovider.com/sms'
                  }
                  className="w-full bg-slate-950 border border-slate-700/80 rounded-2xl px-4 py-2.5 text-xs text-slate-100 placeholder-slate-600 focus:outline-none focus:border-cyan-500 font-mono shadow-inner"
                />
              </div>

              {/* Callback / Delivery Webhook URL */}
              <div className="md:col-span-2">
                <label className="block text-xs font-bold text-slate-300 mb-1.5">
                  Delivery Report (DLR) Webhook URL:
                </label>
                <input
                  type="text"
                  value={callbackUrl}
                  onChange={(e) => setCallbackUrl(e.target.value)}
                  placeholder="https://api.qershilink.com/api/v1/notifications/dlr"
                  className="w-full bg-slate-950 border border-slate-700/80 rounded-2xl px-4 py-2.5 text-xs text-slate-100 placeholder-slate-600 focus:outline-none focus:border-cyan-500 font-mono shadow-inner"
                />
              </div>
            </div>

            {/* Bottom Save Action Button */}
            <div className="pt-4 border-t border-slate-800 flex items-center justify-between">
              <span className="text-[11px] text-slate-500">
                Credentials are stored in tenant-isolated schema with AES-256 masking.
              </span>

              <PermissionGuard
                roles={[ROLES.SUPER_ADMIN, ROLES.SACCO_ADMIN]}
                permissions={[PERMISSIONS.NOTIFICATION_CONFIG_MANAGE]}
                fallback={
                  <button
                    type="button"
                    disabled
                    className="px-5 py-2.5 text-xs font-bold bg-slate-800 text-slate-500 rounded-xl cursor-not-allowed border border-slate-700"
                  >
                    Permission Required to Modify
                  </button>
                }
              >
                <button
                  type="submit"
                  disabled={saving}
                  className="px-6 py-2.5 text-xs font-bold bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-white rounded-xl shadow-lg shadow-cyan-500/20 flex items-center gap-2 transition-all active:scale-95 disabled:opacity-50"
                >
                  {saving ? (
                    <>
                      <RefreshCw className="w-4 h-4 animate-spin" /> Saving Settings...
                    </>
                  ) : (
                    <>
                      <Save className="w-4 h-4" /> Save Gateway Configuration
                    </>
                  )}
                </button>
              </PermissionGuard>
            </div>
          </form>
        </div>

        {/* Right Column: Architectural Context & Live Diagnostics Card */}
        <div className="lg:col-span-4 space-y-4">
          <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-5 shadow-xl space-y-4">
            <div className="flex items-center gap-2 text-xs font-bold text-white uppercase tracking-wider">
              <Server className="w-4 h-4 text-cyan-400" />
              <span>Current Engine Status</span>
            </div>

            <div className="space-y-3">
              <div className="p-3 bg-slate-950/60 rounded-2xl border border-slate-800 flex items-center justify-between">
                <span className="text-xs text-slate-400">Active Provider</span>
                <span className="text-xs font-mono font-bold text-cyan-300">
                  {config?.providerType || providerType}
                </span>
              </div>

              <div className="p-3 bg-slate-950/60 rounded-2xl border border-slate-800 flex items-center justify-between">
                <span className="text-xs text-slate-400">Sender Mask</span>
                <span className="text-xs font-mono font-bold text-white">
                  {config?.senderId || senderId || 'DEFAULT'}
                </span>
              </div>

              <div className="p-3 bg-slate-950/60 rounded-2xl border border-slate-800 flex items-center justify-between">
                <span className="text-xs text-slate-400">Tenant Isolation</span>
                <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
                  SECURE_SCHEMA
                </span>
              </div>

              <div className="p-3 bg-slate-950/60 rounded-2xl border border-slate-800 flex items-center justify-between">
                <span className="text-xs text-slate-400">Status</span>
                <span
                  className={`text-[10px] font-mono px-2 py-0.5 rounded-full border ${
                    isActive
                      ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
                      : 'bg-rose-500/10 text-rose-400 border-rose-500/30'
                  }`}
                >
                  {isActive ? 'ENABLED' : 'DISABLED'}
                </span>
              </div>
            </div>

            <div className="p-3.5 bg-cyan-500/5 border border-cyan-500/20 rounded-2xl text-[11px] text-slate-300 leading-relaxed space-y-1">
              <div className="flex items-center gap-1.5 font-bold text-cyan-300">
                <Info className="w-3.5 h-3.5" />
                <span>Zero-Cost Sandbox Testing</span>
              </div>
              <p className="text-slate-400">
                When switched to <strong className="text-cyan-200">SIMULATED</strong>, real external telecom charges are waived while message delivery logs and UI previews continue recording seamlessly.
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Test SMS Modal */}
      <TestSmsModal
        isOpen={isTestModalOpen}
        onClose={() => setIsTestModalOpen(false)}
        providerName={providerType}
      />
    </div>
  );
};
