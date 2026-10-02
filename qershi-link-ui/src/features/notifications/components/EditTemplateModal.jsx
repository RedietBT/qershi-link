import React, { useState, useRef } from 'react';
import { X, Sparkles, Save, CheckCircle2, AlertCircle, RefreshCw, MessageSquare } from 'lucide-react';
import { SmsPhonePreview } from './SmsPhonePreview';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS, ROLES } from '../../../common/constants/permissions';

const PLACEHOLDERS = [
  { token: '{memberName}', label: 'Member Name', desc: 'Abebe Bikila' },
  { token: '{amount}', label: 'Amount', desc: '5,000.00' },
  { token: '{accountNo}', label: 'Account No', desc: '100010042' },
  { token: '{balance}', label: 'New Balance', desc: '18,450.00' },
  { token: '{saccoName}', label: 'SACCO Name', desc: 'Awash SACCO' },
  { token: '{productName}', label: 'Product Name', desc: 'Regular Savings' },
  { token: '{receiverName}', label: 'Receiver Name', desc: 'Chaltu Tadesse' },
  { token: '{receiverAccountNo}', label: 'Receiver Acc', desc: '100010088' },
  { token: '{loanId}', label: 'Loan ID', desc: 'LN-202610-A19F' },
  { token: '{remainingBalance}', label: 'Remaining Bal', desc: '25,000.00' },
];

export const EditTemplateModal = ({ template, onClose, onSave }) => {
  const [content, setContent] = useState(template?.content || '');
  const [isActive, setIsActive] = useState(template?.active ?? true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const textareaRef = useRef(null);

  const handleInsertToken = (token) => {
    if (!textareaRef.current) return;
    const textarea = textareaRef.current;
    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const updated = content.substring(0, start) + token + content.substring(end);
    setContent(updated);

    setTimeout(() => {
      textarea.focus();
      textarea.setSelectionRange(start + token.length, start + token.length);
    }, 50);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!content.trim()) {
      setError('Template content cannot be empty.');
      return;
    }

    try {
      setSaving(true);
      setError(null);
      await onSave(template.templateCode, { content, active: isActive });
      onClose();
    } catch (err) {
      setError(err?.response?.data?.message || err.message || 'Failed to update template.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
      <div className="bg-slate-900 border border-slate-700/80 rounded-3xl max-w-4xl w-full shadow-2xl overflow-hidden flex flex-col max-h-[92vh]">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-950/60">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-2xl bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
              <MessageSquare className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-black text-white tracking-tight">
                Customize Message: <span className="text-cyan-400">{template?.templateCode}</span>
              </h2>
              <p className="text-xs text-slate-400">
                Channel: {template?.channel} • Language: {template?.language}
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Body (Side-by-side Editor & Live Phone Preview) */}
        <div className="flex-1 overflow-y-auto p-6 grid grid-cols-1 lg:grid-cols-12 gap-6">
          {/* Left Column: Form & Variable Chips */}
          <div className="lg:col-span-7 space-y-4">
            {error && (
              <div className="p-3.5 bg-rose-500/10 border border-rose-500/30 rounded-2xl flex items-center gap-3 text-rose-400 text-xs">
                <AlertCircle className="w-4 h-4 shrink-0" />
                <span>{error}</span>
              </div>
            )}

            {/* Token Chips */}
            <div>
              <label className="block text-xs font-bold text-slate-300 mb-2 flex items-center gap-1.5">
                <Sparkles className="w-3.5 h-3.5 text-cyan-400" />
                Dynamic Placeholders (Click to insert):
              </label>
              <div className="flex flex-wrap gap-1.5 p-3 bg-slate-950/60 border border-slate-800 rounded-2xl">
                {PLACEHOLDERS.map((p) => (
                  <button
                    key={p.token}
                    type="button"
                    onClick={() => handleInsertToken(p.token)}
                    title={`Inserts demo value: ${p.desc}`}
                    className="px-2.5 py-1 rounded-lg bg-slate-800/80 hover:bg-cyan-500/20 text-cyan-300 hover:text-cyan-200 border border-slate-700/60 hover:border-cyan-500/40 text-[11px] font-mono transition-all duration-150 flex items-center gap-1 active:scale-95"
                  >
                    <span>+</span> {p.token}
                  </button>
                ))}
              </div>
            </div>

            {/* Message Textarea */}
            <div>
              <label className="block text-xs font-bold text-slate-300 mb-1.5">
                Message Content:
              </label>
              <textarea
                ref={textareaRef}
                value={content}
                onChange={(e) => setContent(e.target.value)}
                rows={7}
                placeholder="Write your custom SMS message here..."
                className="w-full bg-slate-950 border border-slate-700/80 rounded-2xl p-4 text-xs text-slate-100 placeholder-slate-600 focus:outline-none focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500 font-sans leading-relaxed resize-none shadow-inner"
              />
              <div className="flex items-center justify-between text-[11px] text-slate-400 mt-1 px-1">
                <span>{content.length} characters</span>
                <span className="text-slate-500">Standard GSM SMS = 160 characters</span>
              </div>
            </div>

            {/* Active Toggle */}
            <div className="flex items-center justify-between p-4 bg-slate-950/40 border border-slate-800 rounded-2xl">
              <div>
                <div className="text-xs font-bold text-white">Active Status</div>
                <div className="text-[11px] text-slate-400">If disabled, this alert will not be sent to members</div>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  checked={isActive}
                  onChange={(e) => setIsActive(e.target.checked)}
                  className="sr-only peer"
                />
                <div className="w-11 h-6 bg-slate-800 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-cyan-500"></div>
              </label>
            </div>
          </div>

          {/* Right Column: Interactive Phone Mockup */}
          <div className="lg:col-span-5 flex flex-col items-center justify-center bg-slate-950/40 p-4 rounded-3xl border border-slate-800/80">
            <div className="text-[11px] font-extrabold uppercase tracking-wider text-cyan-400 mb-3 flex items-center gap-1.5">
              <Sparkles className="w-3.5 h-3.5" /> Live Member Preview
            </div>
            <SmsPhonePreview content={content} senderId="AWASH_SACCO" />
          </div>
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t border-slate-800 bg-slate-950/60 flex items-center justify-between">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-xs font-semibold text-slate-400 hover:text-white rounded-xl hover:bg-slate-800 transition-colors"
          >
            Cancel
          </button>

          <PermissionGuard
            roles={[ROLES.SUPER_ADMIN, ROLES.SACCO_ADMIN, ROLES.ADMIN]}
            permissions={[PERMISSIONS.NOTIFICATION_TEMPLATE_MANAGE]}
            fallback={
              <button
                disabled
                className="px-5 py-2 text-xs font-bold bg-slate-800 text-slate-500 rounded-xl cursor-not-allowed border border-slate-700"
              >
                Permission Required to Save
              </button>
            }
          >
            <button
              type="button"
              onClick={handleSubmit}
              disabled={saving}
              className="px-5 py-2.5 text-xs font-bold bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-white rounded-xl shadow-lg shadow-cyan-500/20 flex items-center gap-2 transition-all active:scale-95 disabled:opacity-50"
            >
              {saving ? (
                <>
                  <RefreshCw className="w-4 h-4 animate-spin" /> Saving...
                </>
              ) : (
                <>
                  <Save className="w-4 h-4" /> Save Template
                </>
              )}
            </button>
          </PermissionGuard>
        </div>
      </div>
    </div>
  );
};
