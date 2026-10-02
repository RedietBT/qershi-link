import React, { useState, useEffect } from 'react';
import {
  MessageSquare,
  Search,
  Filter,
  Plus,
  Edit3,
  Send,
  RefreshCw,
  AlertCircle,
  CheckCircle2,
  Sparkles,
  Smartphone,
  Eye,
  Check,
  X,
} from 'lucide-react';
import { notificationApi } from '../api/notificationApi';
import { EditTemplateModal } from '../components/EditTemplateModal';
import { TestSmsModal } from '../components/TestSmsModal';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS, ROLES } from '../../../common/constants/permissions';

/**
 * Standard SACCO Notification Templates Catalog with Defaults
 */
const DEFAULT_FALLBACK_TEMPLATES = [
  {
    templateCode: 'TRANSACTION_DEPOSIT',
    channel: 'SMS',
    language: 'EN',
    content: 'Dear {memberName}, your account {accountNo} has been credited with ETB {amount}. New Balance: ETB {balance}. Thank you for banking with {saccoName}.',
    active: true,
  },
  {
    templateCode: 'TRANSACTION_WITHDRAWAL',
    channel: 'SMS',
    language: 'EN',
    content: 'Dear {memberName}, your account {accountNo} has been debited by ETB {amount}. New Balance: ETB {balance}. Thank you for banking with {saccoName}.',
    active: true,
  },
  {
    templateCode: 'LOAN_DISBURSED',
    channel: 'SMS',
    language: 'EN',
    content: 'Congratulations {memberName}! Your loan #{loanId} for ETB {amount} has been disbursed to account {accountNo}. Repayment schedule is now active. - {saccoName}',
    active: true,
  },
  {
    templateCode: 'LOAN_REPAYMENT',
    channel: 'SMS',
    language: 'EN',
    content: 'Dear {memberName}, repayment of ETB {amount} for Loan #{loanId} was received. Outstanding Principal: ETB {remainingBalance}. - {saccoName}',
    active: true,
  },
  {
    templateCode: 'ACCOUNT_OPENED',
    channel: 'SMS',
    language: 'EN',
    content: 'Welcome to {saccoName}, {memberName}! Your {productName} account {accountNo} is now open and active. Thank you for your partnership.',
    active: true,
  },
];

/**
 * SACCO Custom SMS Template Editor & Management Page.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
export const SmsTemplateEditorPage = () => {
  const [templates, setTemplates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  // Modal states
  const [selectedTemplate, setSelectedTemplate] = useState(null);
  const [isTestModalOpen, setIsTestModalOpen] = useState(false);
  const [testPayload, setTestPayload] = useState({ phone: '', message: '' });

  const fetchTemplates = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await notificationApi.getAllTemplates();
      if (Array.isArray(data) && data.length > 0) {
        setTemplates(data);
      } else {
        setTemplates(DEFAULT_FALLBACK_TEMPLATES);
      }
    } catch (err) {
      console.warn('Failed to load templates from server, displaying defaults:', err);
      setTemplates(DEFAULT_FALLBACK_TEMPLATES);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTemplates();
  }, []);

  const handleSaveTemplate = async (templateCode, payload) => {
    await notificationApi.updateTemplate(templateCode, payload);
    setSuccessMsg(`Template '${templateCode}' updated successfully.`);
    setTimeout(() => setSuccessMsg(null), 4000);
    await fetchTemplates();
  };

  const handleToggleActive = async (tpl) => {
    try {
      const newStatus = !tpl.active;
      await notificationApi.updateTemplate(tpl.templateCode, {
        content: tpl.content,
        active: newStatus,
      });
      setTemplates((prev) =>
        prev.map((t) => (t.templateCode === tpl.templateCode ? { ...t, active: newStatus } : t))
      );
      setSuccessMsg(`Template ${tpl.templateCode} is now ${newStatus ? 'ACTIVE' : 'INACTIVE'}.`);
      setTimeout(() => setSuccessMsg(null), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || err.message || 'Failed to toggle status.');
    }
  };

  const openTestWithTemplate = (tpl) => {
    let sampleMsg = tpl.content
      .replace(/{memberName}/g, 'Abebe Bikila')
      .replace(/{amount}/g, '5,000.00')
      .replace(/{accountNo}/g, '100010042')
      .replace(/{balance}/g, '18,450.00')
      .replace(/{saccoName}/g, 'Awash SACCO')
      .replace(/{productName}/g, 'Regular Savings')
      .replace(/{loanId}/g, 'LN-202610-A19F')
      .replace(/{remainingBalance}/g, '25,000.00');

    setTestPayload({
      phone: '+2519',
      message: sampleMsg,
    });
    setIsTestModalOpen(true);
  };

  // Filtered Templates
  const filteredTemplates = templates.filter((t) => {
    const matchesSearch =
      t.templateCode.toLowerCase().includes(searchTerm.toLowerCase()) ||
      t.content.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus =
      statusFilter === 'ALL' ||
      (statusFilter === 'ACTIVE' && t.active) ||
      (statusFilter === 'INACTIVE' && !t.active);
    return matchesSearch && matchesStatus;
  });

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6 animate-fadeIn">
      {/* Top Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800 pb-5">
        <div className="flex items-center gap-3">
          <div className="p-3 rounded-2xl bg-cyan-500/10 text-cyan-400 border border-cyan-500/20 shadow-lg shadow-cyan-500/5">
            <MessageSquare className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-xl font-black text-white tracking-tight flex items-center gap-2">
              SMS Notification Templates
              <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-cyan-500/10 text-cyan-400 border border-cyan-500/30">
                Wording Editor
              </span>
            </h1>
            <p className="text-xs text-slate-400 mt-0.5">
              Customize real-time SMS wording, placeholders, and event triggers for all SACCO members.
            </p>
          </div>
        </div>

        {/* Action Controls */}
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={fetchTemplates}
            disabled={loading}
            className="px-3.5 py-2 text-xs font-semibold text-slate-300 hover:text-white bg-slate-800/80 hover:bg-slate-700 border border-slate-700 rounded-xl flex items-center gap-2 transition-all"
            title="Reload templates"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            Refresh
          </button>
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

      {/* Filter and Search Bar */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-3 p-4 bg-slate-900/80 border border-slate-800 rounded-2xl">
        <div className="relative w-full sm:w-80">
          <Search className="w-4 h-4 absolute left-3.5 top-3 text-slate-400" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search templates or keywords..."
            className="w-full bg-slate-950 border border-slate-700/80 rounded-xl pl-9 pr-4 py-2 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500 font-sans shadow-inner"
          />
        </div>

        <div className="flex items-center gap-3 w-full sm:w-auto justify-end">
          <div className="flex items-center gap-2 text-xs text-slate-400">
            <Filter className="w-3.5 h-3.5 text-cyan-400" />
            <span>Filter Status:</span>
          </div>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="bg-slate-950 border border-slate-700 rounded-xl px-3 py-1.5 text-xs text-slate-200 focus:outline-none focus:border-cyan-500"
          >
            <option value="ALL">All Templates ({templates.length})</option>
            <option value="ACTIVE">Active Only</option>
            <option value="INACTIVE">Inactive Only</option>
          </select>
        </div>
      </div>

      {/* Templates Grid List */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        {filteredTemplates.map((tpl) => (
          <div
            key={tpl.templateCode}
            className="bg-slate-900/90 border border-slate-800 hover:border-slate-700/80 rounded-3xl p-5 shadow-xl transition-all duration-200 flex flex-col justify-between group hover:shadow-cyan-500/5"
          >
            <div>
              {/* Header Badges */}
              <div className="flex items-center justify-between mb-3">
                <span className="text-[11px] font-mono font-bold px-2.5 py-1 rounded-xl bg-cyan-500/10 text-cyan-400 border border-cyan-500/30">
                  {tpl.templateCode}
                </span>

                <div className="flex items-center gap-2">
                  <span className="text-[10px] font-mono px-2 py-0.5 rounded-md bg-slate-800 text-slate-300 border border-slate-700">
                    {tpl.channel || 'SMS'}
                  </span>
                  <span
                    className={`text-[10px] font-mono px-2 py-0.5 rounded-full border ${
                      tpl.active
                        ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
                        : 'bg-rose-500/10 text-rose-400 border-rose-500/30'
                    }`}
                  >
                    {tpl.active ? 'ACTIVE' : 'INACTIVE'}
                  </span>
                </div>
              </div>

              {/* Message Content Preview */}
              <div className="p-3.5 bg-slate-950/80 border border-slate-800/80 rounded-2xl text-xs text-slate-200 leading-relaxed font-sans min-h-[90px] select-text">
                {tpl.content}
              </div>

              {/* Character Count */}
              <div className="flex items-center justify-between text-[10px] text-slate-500 mt-2 px-1">
                <span>{tpl.content?.length || 0} characters</span>
                <span>Language: {tpl.language || 'EN'}</span>
              </div>
            </div>

            {/* Action Buttons with Granular Permissions */}
            <div className="pt-4 mt-3 border-t border-slate-800/80 flex items-center justify-between gap-2">
              {/* Toggle Active Switch */}
              <PermissionGuard
                roles={[ROLES.SUPER_ADMIN, ROLES.SACCO_ADMIN]}
                permissions={[PERMISSIONS.NOTIFICATION_TEMPLATE_MANAGE]}
              >
                <button
                  type="button"
                  onClick={() => handleToggleActive(tpl)}
                  className={`text-[11px] font-semibold px-2.5 py-1.5 rounded-lg border transition-all ${
                    tpl.active
                      ? 'text-slate-400 hover:text-rose-400 border-slate-700/60 hover:border-rose-500/40 bg-slate-800/40'
                      : 'text-emerald-400 hover:text-emerald-300 border-emerald-500/30 hover:border-emerald-500/60 bg-emerald-500/10'
                  }`}
                  title={tpl.active ? 'Disable this message' : 'Enable this message'}
                >
                  {tpl.active ? 'Disable' : 'Enable'}
                </button>
              </PermissionGuard>

              <div className="flex items-center gap-1.5 ml-auto">
                {/* Send Test SMS Button */}
                <PermissionGuard
                  roles={[ROLES.SUPER_ADMIN, ROLES.SACCO_ADMIN, ROLES.ADMIN]}
                  permissions={[PERMISSIONS.NOTIFICATION_SEND]}
                >
                  <button
                    type="button"
                    onClick={() => openTestWithTemplate(tpl)}
                    className="p-2 text-xs font-semibold text-slate-300 hover:text-cyan-400 bg-slate-800/80 hover:bg-slate-700 border border-slate-700 rounded-xl transition-all"
                    title="Send test SMS with this wording"
                  >
                    <Send className="w-3.5 h-3.5" />
                  </button>
                </PermissionGuard>

                {/* Edit Message Button */}
                <PermissionGuard
                  roles={[ROLES.SUPER_ADMIN, ROLES.SACCO_ADMIN, ROLES.ADMIN]}
                  permissions={[PERMISSIONS.NOTIFICATION_TEMPLATE_MANAGE]}
                  fallback={
                    <span className="text-[10px] text-slate-500 italic">View Only</span>
                  }
                >
                  <button
                    type="button"
                    onClick={() => setSelectedTemplate(tpl)}
                    className="px-3 py-1.5 text-xs font-bold text-cyan-400 hover:text-cyan-300 bg-cyan-500/10 hover:bg-cyan-500/20 border border-cyan-500/30 rounded-xl flex items-center gap-1.5 transition-all shadow-sm active:scale-95"
                  >
                    <Edit3 className="w-3 h-3" />
                    Customize
                  </button>
                </PermissionGuard>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Edit Template Modal */}
      {selectedTemplate && (
        <EditTemplateModal
          template={selectedTemplate}
          onClose={() => setSelectedTemplate(null)}
          onSave={handleSaveTemplate}
        />
      )}

      {/* Test SMS Modal */}
      <TestSmsModal
        isOpen={isTestModalOpen}
        onClose={() => setIsTestModalOpen(false)}
        providerName="Active Gateway"
      />
    </div>
  );
};
