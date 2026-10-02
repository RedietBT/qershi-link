import React, { useState } from 'react';
import { X, Send, Phone, CheckCircle2, AlertCircle, RefreshCw, Radio, Terminal } from 'lucide-react';
import { notificationApi } from '../api/notificationApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS, ROLES } from '../../../common/constants/permissions';

/**
 * Live SMS Gateway Testing Modal.
 * Allows authorized administrators to test provider credentials by sending a live test SMS.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
export const TestSmsModal = ({ isOpen, onClose, providerName }) => {
  const [recipientPhone, setRecipientPhone] = useState('+2519');
  const [message, setMessage] = useState(
    'Qershi-Link: Verification test successful! Your SACCO SMS gateway connection is working.'
  );
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  if (!isOpen) return null;

  const handleSendTest = async (e) => {
    e.preventDefault();
    if (!recipientPhone.trim() || recipientPhone.length < 9) {
      setError('Please provide a valid phone number (e.g. +251911223344).');
      return;
    }
    if (!message.trim()) {
      setError('Message content cannot be empty.');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      setResult(null);

      const response = await notificationApi.testGatewayConnection({
        recipientPhone: recipientPhone.trim(),
        message: message.trim(),
      });

      setResult(response);
    } catch (err) {
      const errDetail =
        err?.response?.data?.message || err?.message || 'Failed to dispatch test message to provider.';
      setError(errDetail);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
      <div className="bg-slate-900 border border-slate-700/80 rounded-3xl max-w-lg w-full shadow-2xl overflow-hidden flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-950/60">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-2xl bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
              <Radio className="w-5 h-5 animate-pulse" />
            </div>
            <div>
              <h2 className="text-base font-black text-white tracking-tight">
                Test SMS Gateway Connection
              </h2>
              <p className="text-xs text-slate-400">
                Target Provider: <span className="text-cyan-400 font-semibold">{providerName || 'Active Provider'}</span>
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-2 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <form onSubmit={handleSendTest} className="p-6 space-y-4">
          {error && (
            <div className="p-3.5 bg-rose-500/10 border border-rose-500/30 rounded-2xl flex items-start gap-3 text-rose-400 text-xs">
              <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
              <div className="flex-1 font-mono break-all">{error}</div>
            </div>
          )}

          {result && (
            <div className="p-4 bg-emerald-500/10 border border-emerald-500/30 rounded-2xl space-y-2 text-xs">
              <div className="flex items-center gap-2 text-emerald-400 font-bold">
                <CheckCircle2 className="w-4 h-4 shrink-0" />
                <span>Test SMS Dispatched Successfully!</span>
              </div>
              <p className="text-slate-300 text-[11px] leading-relaxed">
                {result.message || 'SMS delivery request acknowledged by the provider.'}
              </p>
              {result.provider && (
                <div className="flex items-center gap-2 text-[10px] text-slate-400 font-mono">
                  <span>Provider: <strong className="text-cyan-300">{result.provider}</strong></span>
                  {result.deliveryStatus && (
                    <span>• Status: <strong className="text-emerald-300">{result.deliveryStatus}</strong></span>
                  )}
                </div>
              )}
            </div>
          )}

          {/* Phone Field */}
          <div>
            <label className="block text-xs font-bold text-slate-300 mb-1.5 flex items-center gap-1.5">
              <Phone className="w-3.5 h-3.5 text-cyan-400" />
              Recipient Phone Number (E.164):
            </label>
            <input
              type="text"
              value={recipientPhone}
              onChange={(e) => setRecipientPhone(e.target.value)}
              placeholder="+251911223344"
              className="w-full bg-slate-950 border border-slate-700/80 rounded-2xl px-4 py-2.5 text-xs text-slate-100 placeholder-slate-600 focus:outline-none focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500 font-mono shadow-inner"
              required
            />
            <span className="text-[10px] text-slate-500 mt-1 block">
              Format: +251 followed by 9 digits for Ethiopian mobile networks (Ethio Telecom / Safaricom).
            </span>
          </div>

          {/* Test Message Content */}
          <div>
            <label className="block text-xs font-bold text-slate-300 mb-1.5">
              Test Message Payload:
            </label>
            <textarea
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              rows={3}
              className="w-full bg-slate-950 border border-slate-700/80 rounded-2xl p-3 text-xs text-slate-100 placeholder-slate-600 focus:outline-none focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500 font-sans leading-relaxed resize-none shadow-inner"
            />
            <div className="text-[10px] text-slate-500 mt-1 text-right">
              {message.length} chars
            </div>
          </div>

          {/* Footer Actions */}
          <div className="pt-2 flex items-center justify-between border-t border-slate-800">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs font-semibold text-slate-400 hover:text-white rounded-xl hover:bg-slate-800 transition-colors"
            >
              Close
            </button>

            <PermissionGuard
              roles={[ROLES.SUPER_ADMIN, ROLES.SACCO_ADMIN, ROLES.ADMIN]}
              permissions={[PERMISSIONS.NOTIFICATION_CONFIG_MANAGE, PERMISSIONS.NOTIFICATION_SEND]}
              fallback={
                <button
                  type="button"
                  disabled
                  className="px-5 py-2 text-xs font-bold bg-slate-800 text-slate-500 rounded-xl cursor-not-allowed border border-slate-700"
                >
                  Permission Required
                </button>
              }
            >
              <button
                type="submit"
                disabled={loading}
                className="px-5 py-2.5 text-xs font-bold bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-white rounded-xl shadow-lg shadow-cyan-500/20 flex items-center gap-2 transition-all active:scale-95 disabled:opacity-50"
              >
                {loading ? (
                  <>
                    <RefreshCw className="w-4 h-4 animate-spin" /> Dispatching...
                  </>
                ) : (
                  <>
                    <Send className="w-4 h-4" /> Send Test Message
                  </>
                )}
              </button>
            </PermissionGuard>
          </div>
        </form>
      </div>
    </div>
  );
};
