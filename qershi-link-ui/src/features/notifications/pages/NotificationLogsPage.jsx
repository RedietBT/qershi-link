import React, { useState, useEffect } from 'react';
import {
  History,
  Search,
  Filter,
  RefreshCw,
  Phone,
  AlertCircle,
  CheckCircle2,
  Clock,
  Eye,
  Send,
  X,
  FileText,
  ShieldCheck,
} from 'lucide-react';
import { notificationApi } from '../api/notificationApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS, ROLES } from '../../../common/constants/permissions';

/**
 * Notification Logs & Delivery Audit Table Page.
 *
 * @author KAB Digital Solution PLC
 * @version 1.0.0
 */
export const NotificationLogsPage = () => {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [phoneSearch, setPhoneSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [error, setError] = useState(null);

  // Detail Modal State
  const [selectedLog, setSelectedLog] = useState(null);

  const fetchLogs = async (phone = null) => {
    try {
      setLoading(true);
      setError(null);
      let data;
      if (phone && phone.trim().length > 3) {
        data = await notificationApi.getLogsByRecipient(phone.trim());
      } else {
        data = await notificationApi.getLogs();
      }
      setLogs(Array.isArray(data) ? data : []);
    } catch (err) {
      console.warn('Failed to load logs:', err);
      setError(err?.response?.data?.message || err?.message || 'Failed to fetch delivery audit logs.');
      setLogs([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, []);

  const handlePhoneFilterSubmit = (e) => {
    e.preventDefault();
    fetchLogs(phoneSearch);
  };

  const handleClearPhoneFilter = () => {
    setPhoneSearch('');
    fetchLogs(null);
  };

  // Filtered Logs
  const filteredLogs = logs.filter((log) => {
    const matchesSearch =
      (log.recipient || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
      (log.content || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
      (log.templateCode || '').toLowerCase().includes(searchTerm.toLowerCase());

    const matchesStatus =
      statusFilter === 'ALL' ||
      (log.status || '').toUpperCase() === statusFilter.toUpperCase();

    return matchesSearch && matchesStatus;
  });

  const getStatusBadge = (status) => {
    const s = (status || '').toUpperCase();
    if (s === 'SENT' || s === 'DELIVERED') {
      return (
        <span className="inline-flex items-center gap-1 text-[11px] font-mono font-bold px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
          <CheckCircle2 className="w-3 h-3" />
          {s}
        </span>
      );
    }
    if (s === 'FAILED') {
      return (
        <span className="inline-flex items-center gap-1 text-[11px] font-mono font-bold px-2 py-0.5 rounded-full bg-rose-500/10 text-rose-400 border border-rose-500/30">
          <AlertCircle className="w-3 h-3" />
          FAILED
        </span>
      );
    }
    return (
      <span className="inline-flex items-center gap-1 text-[11px] font-mono font-bold px-2 py-0.5 rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/30">
        <Clock className="w-3 h-3" />
        {s || 'QUEUED'}
      </span>
    );
  };

  return (
    <div className="p-6 max-w-7xl mx-auto space-y-6 animate-fadeIn">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800 pb-5">
        <div className="flex items-center gap-3">
          <div className="p-3 rounded-2xl bg-cyan-500/10 text-cyan-400 border border-cyan-500/20 shadow-lg shadow-cyan-500/5">
            <History className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-xl font-black text-white tracking-tight flex items-center gap-2">
              Delivery Audit Trail
              <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-cyan-500/10 text-cyan-400 border border-cyan-500/30">
                Live GSM Logs
              </span>
            </h1>
            <p className="text-xs text-slate-400 mt-0.5">
              Comprehensive tamper-evident delivery audit log for all member transactional SMS and alerts.
            </p>
          </div>
        </div>

        {/* Action Controls */}
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={() => fetchLogs(phoneSearch)}
            disabled={loading}
            className="px-3.5 py-2 text-xs font-semibold text-slate-300 hover:text-white bg-slate-800/80 hover:bg-slate-700 border border-slate-700 rounded-xl flex items-center gap-2 transition-all"
            title="Reload logs"
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

      {/* Filter and Search Bar */}
      <div className="grid grid-cols-1 md:grid-cols-12 gap-3 p-4 bg-slate-900/80 border border-slate-800 rounded-2xl">
        {/* Recipient Phone Search Form */}
        <form onSubmit={handlePhoneFilterSubmit} className="md:col-span-5 flex items-center gap-2">
          <div className="relative flex-1">
            <Phone className="w-4 h-4 absolute left-3.5 top-3 text-slate-400" />
            <input
              type="text"
              value={phoneSearch}
              onChange={(e) => setPhoneSearch(e.target.value)}
              placeholder="Search by Phone (e.g. +251911223344)..."
              className="w-full bg-slate-950 border border-slate-700/80 rounded-xl pl-9 pr-8 py-2 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500 font-mono shadow-inner"
            />
            {phoneSearch && (
              <button
                type="button"
                onClick={handleClearPhoneFilter}
                className="absolute right-2.5 top-2.5 text-slate-400 hover:text-white"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            )}
          </div>
          <button
            type="submit"
            className="px-3.5 py-2 text-xs font-bold text-cyan-400 bg-cyan-500/10 hover:bg-cyan-500/20 border border-cyan-500/30 rounded-xl transition-all"
          >
            Filter Phone
          </button>
        </form>

        {/* Content Search */}
        <div className="md:col-span-4 relative">
          <Search className="w-4 h-4 absolute left-3.5 top-3 text-slate-400" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search in message text or template..."
            className="w-full bg-slate-950 border border-slate-700/80 rounded-xl pl-9 pr-4 py-2 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500 font-sans shadow-inner"
          />
        </div>

        {/* Status Dropdown */}
        <div className="md:col-span-3 flex items-center justify-end gap-2">
          <span className="text-xs text-slate-400">Status:</span>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-cyan-500"
          >
            <option value="ALL">All Statuses</option>
            <option value="SENT">Delivered / Sent</option>
            <option value="FAILED">Failed</option>
            <option value="QUEUED">Pending / Queued</option>
          </select>
        </div>
      </div>

      {/* Audit Log Table */}
      <div className="bg-slate-900/90 border border-slate-800 rounded-3xl overflow-hidden shadow-2xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs text-slate-300">
            <thead className="bg-slate-950/80 border-b border-slate-800 text-[11px] font-extrabold uppercase tracking-wider text-slate-400">
              <tr>
                <th className="px-5 py-3.5">Timestamp</th>
                <th className="px-5 py-3.5">Recipient</th>
                <th className="px-5 py-3.5">Channel</th>
                <th className="px-5 py-3.5">Template / Event</th>
                <th className="px-5 py-3.5">Message Excerpt</th>
                <th className="px-5 py-3.5 text-center">Status</th>
                <th className="px-5 py-3.5 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-sans">
              {loading && logs.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-6 py-12 text-center text-slate-400">
                    <RefreshCw className="w-6 h-6 animate-spin mx-auto text-cyan-400 mb-2" />
                    <span>Loading delivery audit logs...</span>
                  </td>
                </tr>
              ) : filteredLogs.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-6 py-12 text-center text-slate-400">
                    <FileText className="w-8 h-8 mx-auto text-slate-600 mb-2" />
                    <p className="text-sm font-semibold text-slate-300">No delivery logs found</p>
                    <p className="text-xs text-slate-500 mt-1">
                      No transactional messages match your selected search filters.
                    </p>
                  </td>
                </tr>
              ) : (
                filteredLogs.map((log, idx) => (
                  <tr
                    key={log.id || idx}
                    className="hover:bg-slate-800/40 transition-colors duration-150"
                  >
                    <td className="px-5 py-3.5 font-mono text-[11px] text-slate-400 whitespace-nowrap">
                      {log.sentAt
                        ? new Date(log.sentAt).toLocaleString()
                        : log.createdAt
                        ? new Date(log.createdAt).toLocaleString()
                        : 'Just now'}
                    </td>
                    <td className="px-5 py-3.5 font-mono text-cyan-300 font-bold whitespace-nowrap">
                      {log.recipient}
                    </td>
                    <td className="px-5 py-3.5">
                      <span className="px-2 py-0.5 rounded-md bg-slate-800 text-slate-300 font-mono text-[10px] border border-slate-700">
                        {log.channel || 'SMS'}
                      </span>
                    </td>
                    <td className="px-5 py-3.5 font-mono font-bold text-slate-200">
                      {log.templateCode || 'DIRECT_ALERT'}
                    </td>
                    <td className="px-5 py-3.5 max-w-xs truncate text-slate-300 text-[11px]" title={log.content}>
                      {log.content}
                    </td>
                    <td className="px-5 py-3.5 text-center whitespace-nowrap">
                      {getStatusBadge(log.status)}
                    </td>
                    <td className="px-5 py-3.5 text-right whitespace-nowrap">
                      <button
                        type="button"
                        onClick={() => setSelectedLog(log)}
                        className="px-2.5 py-1 text-[11px] font-semibold text-cyan-400 hover:text-cyan-300 bg-cyan-500/10 hover:bg-cyan-500/20 border border-cyan-500/30 rounded-lg inline-flex items-center gap-1 transition-all"
                      >
                        <Eye className="w-3 h-3" />
                        Details
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Log Detail Drawer / Modal */}
      {selectedLog && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
          <div className="bg-slate-900 border border-slate-700/80 rounded-3xl max-w-lg w-full shadow-2xl overflow-hidden flex flex-col">
            <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-950/60">
              <div className="flex items-center gap-3">
                <div className="p-2.5 rounded-2xl bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
                  <FileText className="w-5 h-5" />
                </div>
                <div>
                  <h2 className="text-base font-black text-white tracking-tight">
                    Delivery Log Details
                  </h2>
                  <p className="text-xs text-slate-400">
                    Recipient: <span className="font-mono text-cyan-400 font-bold">{selectedLog.recipient}</span>
                  </p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setSelectedLog(null)}
                className="p-2 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="p-6 space-y-4">
              <div>
                <label className="text-xs font-bold text-slate-400 block mb-1">Status</label>
                <div>{getStatusBadge(selectedLog.status)}</div>
              </div>

              <div>
                <label className="text-xs font-bold text-slate-400 block mb-1">Template Code</label>
                <div className="font-mono text-xs font-bold text-white">
                  {selectedLog.templateCode || 'DIRECT_ALERT'}
                </div>
              </div>

              <div>
                <label className="text-xs font-bold text-slate-400 block mb-1">Full Rendered Message</label>
                <div className="p-3.5 bg-slate-950 border border-slate-800 rounded-2xl text-xs text-slate-100 font-sans leading-relaxed select-text">
                  {selectedLog.content}
                </div>
              </div>

              {selectedLog.errorMessage && (
                <div>
                  <label className="text-xs font-bold text-rose-400 block mb-1">Error Diagnostic</label>
                  <div className="p-3 bg-rose-500/10 border border-rose-500/20 rounded-xl text-rose-300 font-mono text-[11px] break-all">
                    {selectedLog.errorMessage}
                  </div>
                </div>
              )}

              <div className="pt-2 flex justify-end">
                <button
                  type="button"
                  onClick={() => setSelectedLog(null)}
                  className="px-4 py-2 text-xs font-bold bg-slate-800 hover:bg-slate-700 text-white rounded-xl transition-colors"
                >
                  Close
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
