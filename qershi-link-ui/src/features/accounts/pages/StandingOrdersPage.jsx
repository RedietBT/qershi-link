import React, { useState, useEffect, useCallback } from 'react';
import {
  CalendarClock, RefreshCw, Plus, PlayCircle, PauseCircle, StopCircle,
  CheckCircle2, AlertCircle, Clock, Zap, ArrowRight, BarChart3, Activity, Phone,
} from 'lucide-react';
import { standingOrderApi } from '../api/standingOrderApi';
import { useAuthStore } from '../../../common/store/useAuthStore';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';
import { formatCurrency } from '../../../common/utils/currency';

const STATUS_COLORS = {
  ACTIVE:    'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
  PAUSED:    'bg-amber-500/20 text-amber-400 border-amber-500/30',
  COMPLETED: 'bg-blue-500/20 text-blue-400 border-blue-500/30',
  FAILED:    'bg-red-500/20 text-red-400 border-red-500/30',
  CANCELLED: 'bg-gray-500/20 text-gray-400 border-gray-500/30',
};
const FREQ_LABELS = { DAILY: 'Daily', WEEKLY: 'Weekly', BI_WEEKLY: 'Bi-Weekly', MONTHLY: 'Monthly' };

/**
 * Standing Orders Management Page — Day 5 (refactored)
 * Uses standingOrderApi module + PermissionGuard on all action buttons.
 */
export const StandingOrdersPage = () => {
  const user = useAuthStore((s) => s.user);
  const [orders, setOrders] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [sweepResult, setSweepResult] = useState(null);
  const [isSweeping, setIsSweeping] = useState(false);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [isCreating, setIsCreating] = useState(false);

  // Phone lookup
  const [phoneSearch, setPhoneSearch] = useState('');
  const [isSearchingPhone, setIsSearchingPhone] = useState(false);

  const [createForm, setCreateForm] = useState({
    memberId: user?.id || user?.userId || '',
    sourceAccountNo: '',
    targetAccountNo: '',
    amount: '',
    frequency: 'MONTHLY',
    dayOfMonth: '',
    startDate: new Date().toISOString().split('T')[0],
    endDate: '',
    description: '',
  });

  const fetchOrders = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await standingOrderApi.getAll();
      setOrders(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load standing orders.');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => { fetchOrders(); }, [fetchOrders]);

  const handlePhoneLookup = async (e) => {
    e.preventDefault();
    if (!phoneSearch.trim()) return;
    setIsSearchingPhone(true);
    setError(null);
    try {
      const data = await standingOrderApi.getByPhone(phoneSearch.trim());
      setOrders(Array.isArray(data) ? data : []);
      setSuccess(`📱 Showing ${data.length} order(s) for phone ${phoneSearch}`);
    } catch (err) {
      setError(err.response?.data?.message || 'No standing orders found for that phone number.');
    } finally {
      setIsSearchingPhone(false);
    }
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    setIsCreating(true);
    setError(null);
    try {
      await standingOrderApi.create({
        ...createForm,
        amount: parseFloat(createForm.amount),
        dayOfMonth: createForm.dayOfMonth ? parseInt(createForm.dayOfMonth) : null,
        endDate: createForm.endDate || null,
      });
      setSuccess('✅ Standing order created successfully.');
      setShowCreateModal(false);
      fetchOrders();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create standing order.');
    } finally {
      setIsCreating(false);
    }
  };

  const handleAction = async (id, action) => {
    try {
      if (action === 'cancel') await standingOrderApi.cancel(id);
      else if (action === 'pause') await standingOrderApi.pause(id);
      else if (action === 'resume') await standingOrderApi.resume(id);
      setSuccess(`✅ Standing order ${action}d.`);
      fetchOrders();
    } catch (err) {
      setError(err.response?.data?.message || `Failed to ${action} order.`);
    }
  };

  const handleRunSweep = async () => {
    setIsSweeping(true);
    setSweepResult(null);
    setError(null);
    try {
      const data = await standingOrderApi.runSweeps();
      setSweepResult(data);
      setSuccess(`✅ Sweep complete: ${data.successfulSweeps} succeeded, ${data.failedSweeps} failed.`);
      fetchOrders();
    } catch (err) {
      setError(err.response?.data?.message || 'Sweep run failed.');
    } finally {
      setIsSweeping(false);
    }
  };

  const activeCount = orders.filter((o) => o.status === 'ACTIVE').length;
  const dueToday = orders.filter((o) => o.status === 'ACTIVE' && o.nextRunDate && new Date(o.nextRunDate) <= new Date()).length;

  return (
    <div className="p-6 space-y-6 animate-fadeIn">
      {/* ── Header ── */}
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div>
          <h1 className="text-2xl font-extrabold text-[var(--bdae-text-primary)] flex items-center gap-2">
            <CalendarClock className="w-7 h-7 text-[#00CDDB]" />
            Standing Orders
          </h1>
          <p className="text-sm text-[var(--bdae-text-secondary)] mt-0.5">
            Automated recurring sweep engine — member contributions & loan repayments
          </p>
        </div>
        <div className="flex gap-2 flex-wrap">
          <button onClick={fetchOrders} className="p-2 rounded-xl border border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] hover:text-[#00CDDB] transition-colors">
            <RefreshCw className="w-4 h-4" />
          </button>
          {/* Sweep — STANDING_ORDER_SWEEP or EOD_EXECUTE */}
          <PermissionGuard
            roles={['SUPER_ADMIN', 'SACCO_ADMIN']}
            permissions={[PERMISSIONS.STANDING_ORDER_SWEEP, PERMISSIONS.EOD_EXECUTE]}
          >
            <button
              id="run-sweep-btn"
              onClick={handleRunSweep}
              disabled={isSweeping}
              className="flex items-center gap-2 px-4 py-2 rounded-xl border border-amber-500/40 text-amber-400 text-xs font-bold hover:bg-amber-500/10 disabled:opacity-50 transition-all"
            >
              <Zap className="w-3.5 h-3.5" />{isSweeping ? 'Running Sweeps…' : 'Run Daily Sweeps'}
            </button>
          </PermissionGuard>
          {/* Create — STANDING_ORDER_MANAGE or ACCOUNT_CREATE */}
          <PermissionGuard
            roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER']}
            permissions={[PERMISSIONS.STANDING_ORDER_MANAGE, PERMISSIONS.ACCOUNT_CREATE]}
          >
            <button
              id="create-standing-order-btn"
              onClick={() => setShowCreateModal(true)}
              className="flex items-center gap-2 px-4 py-2 rounded-xl bg-[#00CDDB] text-white text-xs font-bold hover:bg-[#00CDDB]/90 shadow-sm shadow-[#00CDDB]/30 transition-all"
            >
              <Plus className="w-3.5 h-3.5" /> New Standing Order
            </button>
          </PermissionGuard>
        </div>
      </div>

      {/* ── Alerts ── */}
      {error && (
        <div className="flex items-center gap-3 px-4 py-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-400 text-sm">
          <AlertCircle className="w-4 h-4 shrink-0" /> {error}
          <button onClick={() => setError(null)} className="ml-auto text-xs opacity-60 hover:opacity-100">✕</button>
        </div>
      )}
      {success && (
        <div className="flex items-center gap-3 px-4 py-3 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-sm">
          <CheckCircle2 className="w-4 h-4 shrink-0" /> {success}
          <button onClick={() => setSuccess(null)} className="ml-auto text-xs opacity-60 hover:opacity-100">✕</button>
        </div>
      )}

      {/* ── Phone Lookup ── */}
      <PermissionGuard
        roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER']}
        permissions={[PERMISSIONS.STANDING_ORDER_VIEW, PERMISSIONS.ACCOUNT_VIEW]}
      >
        <form onSubmit={handlePhoneLookup} className="bdae-surface rounded-2xl border border-[var(--bdae-border)] p-4 flex items-center gap-3">
          <Phone className="w-4 h-4 text-[var(--bdae-text-secondary)] shrink-0" />
          <input
            id="so-phone-lookup"
            type="text"
            placeholder="Lookup standing orders by member phone number…"
            value={phoneSearch}
            onChange={(e) => setPhoneSearch(e.target.value)}
            className="flex-1 bg-transparent text-sm text-[var(--bdae-text-primary)] focus:outline-none placeholder:text-[var(--bdae-text-secondary)]"
          />
          <button
            type="submit"
            id="so-phone-lookup-btn"
            disabled={isSearchingPhone}
            className="px-4 py-1.5 rounded-lg bg-[#00CDDB]/10 text-[#00CDDB] text-xs font-bold hover:bg-[#00CDDB]/20 disabled:opacity-50 transition-colors"
          >
            {isSearchingPhone ? 'Searching…' : 'Lookup'}
          </button>
          {phoneSearch && (
            <button type="button" onClick={() => { setPhoneSearch(''); fetchOrders(); }} className="text-[var(--bdae-text-secondary)] text-xs hover:text-red-400">
              Clear
            </button>
          )}
        </form>
      </PermissionGuard>

      {/* ── Sweep Result ── */}
      {sweepResult && (
        <div className="bdae-surface rounded-2xl border border-[#00CDDB]/30 p-4">
          <div className="flex items-center gap-2 mb-3">
            <Activity className="w-4 h-4 text-[#00CDDB]" />
            <h3 className="text-sm font-bold text-[var(--bdae-text-primary)]">Last Sweep — {sweepResult.runDate}</h3>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            {[
              { label: 'Total Processed', value: sweepResult.totalProcessed, color: 'text-[var(--bdae-text-primary)]' },
              { label: 'Successful', value: sweepResult.successfulSweeps, color: 'text-emerald-400' },
              { label: 'Failed', value: sweepResult.failedSweeps, color: 'text-red-400' },
              { label: 'Amount Swept', value: `${formatCurrency(sweepResult.totalAmountSwept)} ETB`, color: 'text-[#00CDDB]' },
            ].map(({ label, value, color }) => (
              <div key={label} className="text-center">
                <p className={`text-xl font-black ${color}`}>{value}</p>
                <p className="text-[10px] text-[var(--bdae-text-secondary)] uppercase tracking-wider">{label}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* ── Summary Pills ── */}
      <div className="flex gap-4 flex-wrap">
        <div className="bdae-surface rounded-xl px-4 py-2.5 border border-[var(--bdae-border)] flex items-center gap-2">
          <BarChart3 className="w-4 h-4 text-[#00CDDB]" />
          <span className="text-sm font-bold text-[var(--bdae-text-primary)]">{orders.length} total</span>
        </div>
        <div className="bdae-surface rounded-xl px-4 py-2.5 border border-emerald-500/30 flex items-center gap-2">
          <PlayCircle className="w-4 h-4 text-emerald-400" />
          <span className="text-sm font-bold text-emerald-400">{activeCount} active</span>
        </div>
        {dueToday > 0 && (
          <div className="bdae-surface rounded-xl px-4 py-2.5 border border-amber-500/40 flex items-center gap-2 animate-pulse">
            <Clock className="w-4 h-4 text-amber-400" />
            <span className="text-sm font-bold text-amber-400">{dueToday} due today</span>
          </div>
        )}
      </div>

      {/* ── Orders Table ── */}
      {isLoading ? (
        <div className="flex items-center justify-center py-20"><RefreshCw className="w-6 h-6 text-[#00CDDB] animate-spin" /></div>
      ) : orders.length === 0 ? (
        <div className="bdae-surface rounded-2xl border border-[var(--bdae-border)] py-16 text-center">
          <CalendarClock className="w-10 h-10 text-[var(--bdae-text-secondary)] mx-auto mb-3 opacity-40" />
          <p className="text-[var(--bdae-text-secondary)] text-sm">No standing orders found.</p>
        </div>
      ) : (
        <div className="bdae-surface rounded-2xl border border-[var(--bdae-border)] overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-xs">
              <thead>
                <tr className="border-b border-[var(--bdae-border)] bg-black/[0.02] dark:bg-white/[0.02]">
                  {['Order #', 'Route', 'Amount', 'Frequency', 'Next Run', 'Executions', 'Status', 'Actions'].map((h) => (
                    <th key={h} className="text-left px-4 py-3 font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider whitespace-nowrap">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-[var(--bdae-border)]">
                {orders.map((order) => {
                  const isDue = order.status === 'ACTIVE' && order.nextRunDate && new Date(order.nextRunDate) <= new Date();
                  return (
                    <tr key={order.id} className={`hover:bg-black/[0.01] dark:hover:bg-white/[0.01] transition-colors ${isDue ? 'bg-amber-500/5' : ''}`}>
                      <td className="px-4 py-3 font-mono font-bold text-[11px] text-[var(--bdae-text-primary)]">{order.standingOrderNo}</td>
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-1.5 text-[var(--bdae-text-secondary)]">
                          <span className="font-mono text-[10px] truncate max-w-[80px]">{order.sourceAccountNo}</span>
                          <ArrowRight className="w-3 h-3 shrink-0 text-[#00CDDB]" />
                          <span className="font-mono text-[10px] truncate max-w-[80px]">{order.targetAccountNo}</span>
                        </div>
                      </td>
                      <td className="px-4 py-3 font-bold text-[var(--bdae-text-primary)]">{formatCurrency(order.amount)} ETB</td>
                      <td className="px-4 py-3 text-[var(--bdae-text-secondary)]">{FREQ_LABELS[order.frequency] || order.frequency}</td>
                      <td className="px-4 py-3">
                        <span className={isDue ? 'text-amber-400 font-bold' : 'text-[var(--bdae-text-secondary)]'}>
                          {isDue ? '⚡ ' : ''}{order.nextRunDate || '—'}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-center">
                        <span className="text-emerald-400 font-bold">{order.totalExecutionsCount}</span>
                        {order.failedAttemptsCount > 0 && <span className="text-red-400 ml-1">/ {order.failedAttemptsCount}✗</span>}
                      </td>
                      <td className="px-4 py-3">
                        <span className={`px-2 py-0.5 rounded-full border text-[10px] font-bold uppercase ${STATUS_COLORS[order.status] || 'bg-gray-500/20 text-gray-400'}`}>{order.status}</span>
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-1">
                          {/* Pause / Resume — STANDING_ORDER_MANAGE */}
                          <PermissionGuard
                            roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER']}
                            permissions={[PERMISSIONS.STANDING_ORDER_MANAGE]}
                          >
                            {order.status === 'ACTIVE' && (
                              <button id={`pause-${order.id}`} onClick={() => handleAction(order.id, 'pause')} title="Pause"
                                className="p-1.5 rounded-lg hover:bg-amber-500/10 text-amber-400 transition-colors">
                                <PauseCircle className="w-4 h-4" />
                              </button>
                            )}
                            {order.status === 'PAUSED' && (
                              <button id={`resume-${order.id}`} onClick={() => handleAction(order.id, 'resume')} title="Resume"
                                className="p-1.5 rounded-lg hover:bg-emerald-500/10 text-emerald-400 transition-colors">
                                <PlayCircle className="w-4 h-4" />
                              </button>
                            )}
                          </PermissionGuard>
                          {/* Cancel — requires elevated STANDING_ORDER_MANAGE + ADMIN/above */}
                          <PermissionGuard
                            roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN']}
                            permissions={[PERMISSIONS.STANDING_ORDER_MANAGE]}
                          >
                            {['ACTIVE', 'PAUSED'].includes(order.status) && (
                              <button id={`cancel-${order.id}`} onClick={() => handleAction(order.id, 'cancel')} title="Cancel"
                                className="p-1.5 rounded-lg hover:bg-red-500/10 text-red-400 transition-colors">
                                <StopCircle className="w-4 h-4" />
                              </button>
                            )}
                          </PermissionGuard>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* ── Create Modal ── */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bdae-surface rounded-2xl border border-[var(--bdae-border)] w-full max-w-lg shadow-2xl max-h-[90vh] overflow-y-auto">
            <div className="px-6 py-4 border-b border-[var(--bdae-border)] flex items-center justify-between sticky top-0 bdae-surface z-10">
              <h3 className="font-bold text-[var(--bdae-text-primary)] flex items-center gap-2"><CalendarClock className="w-4 h-4 text-[#00CDDB]" /> New Standing Order</h3>
              <button onClick={() => setShowCreateModal(false)} className="text-[var(--bdae-text-secondary)] text-lg leading-none">✕</button>
            </div>
            <form onSubmit={handleCreate} className="p-6 space-y-4">
              <div>
                <label className="block text-xs font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider mb-1.5">Member ID *</label>
                <input id="so-member-id" type="text" value={createForm.memberId}
                  onChange={(e) => setCreateForm({ ...createForm, memberId: e.target.value })}
                  className="w-full px-3 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] text-sm focus:outline-none focus:border-[#00CDDB]" required />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider mb-1.5">Source Account *</label>
                  <input id="so-source-account" type="text" placeholder="SAV-KAB-XXXXXX" value={createForm.sourceAccountNo}
                    onChange={(e) => setCreateForm({ ...createForm, sourceAccountNo: e.target.value })}
                    className="w-full px-3 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] text-sm focus:outline-none focus:border-[#00CDDB]" required />
                </div>
                <div>
                  <label className="block text-xs font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider mb-1.5">Target Account *</label>
                  <input id="so-target-account" type="text" placeholder="SAV-KAB-YYYYYY" value={createForm.targetAccountNo}
                    onChange={(e) => setCreateForm({ ...createForm, targetAccountNo: e.target.value })}
                    className="w-full px-3 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] text-sm focus:outline-none focus:border-[#00CDDB]" required />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider mb-1.5">Amount (ETB) *</label>
                  <input id="so-amount" type="number" min="1" step="0.01" value={createForm.amount}
                    onChange={(e) => setCreateForm({ ...createForm, amount: e.target.value })}
                    className="w-full px-3 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] text-sm focus:outline-none focus:border-[#00CDDB]" required />
                </div>
                <div>
                  <label className="block text-xs font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider mb-1.5">Frequency *</label>
                  <select id="so-frequency" value={createForm.frequency}
                    onChange={(e) => setCreateForm({ ...createForm, frequency: e.target.value })}
                    className="w-full px-3 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] text-sm focus:outline-none focus:border-[#00CDDB]">
                    <option value="DAILY">Daily</option>
                    <option value="WEEKLY">Weekly</option>
                    <option value="BI_WEEKLY">Bi-Weekly</option>
                    <option value="MONTHLY">Monthly</option>
                  </select>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider mb-1.5">Start Date *</label>
                  <input id="so-start-date" type="date" value={createForm.startDate}
                    onChange={(e) => setCreateForm({ ...createForm, startDate: e.target.value })}
                    className="w-full px-3 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] text-sm focus:outline-none focus:border-[#00CDDB]" required />
                </div>
                <div>
                  <label className="block text-xs font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider mb-1.5">End Date (optional)</label>
                  <input id="so-end-date" type="date" value={createForm.endDate}
                    onChange={(e) => setCreateForm({ ...createForm, endDate: e.target.value })}
                    className="w-full px-3 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] text-sm focus:outline-none focus:border-[#00CDDB]" />
                </div>
              </div>
              <div>
                <label className="block text-xs font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider mb-1.5">Description</label>
                <input id="so-description" type="text" placeholder="Monthly loan repayment contribution" value={createForm.description}
                  onChange={(e) => setCreateForm({ ...createForm, description: e.target.value })}
                  className="w-full px-3 py-2.5 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] text-sm focus:outline-none focus:border-[#00CDDB]" />
              </div>
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => setShowCreateModal(false)} className="flex-1 py-2.5 rounded-xl border border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] text-sm font-bold">Cancel</button>
                <button type="submit" id="confirm-create-so-btn" disabled={isCreating} className="flex-1 py-2.5 rounded-xl bg-[#00CDDB] text-white text-sm font-bold disabled:opacity-50">
                  {isCreating ? 'Creating…' : 'Create Standing Order'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
