import React, { useState, useEffect } from 'react';
import {
  Banknote, Lock, Unlock, RefreshCw, CheckCircle2,
  AlertTriangle, History, ArrowDownToLine, DollarSign,
  ShieldCheck, Calculator, X, ChevronRight, FileText
} from 'lucide-react';
import { tillApi } from '../api/tillApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { formatCurrency, formatDateTime } from '../../../common/utils/currency';

export const TellerDrawerPage = () => {
  const [till, setTill] = useState(null);
  const [reconciliations, setReconciliations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Open Drawer Modal State
  const [openModalOpen, setOpenModalOpen] = useState(false);
  const [openingCashInput, setOpeningCashInput] = useState('10000');
  const [openSubmitting, setOpenSubmitting] = useState(false);

  // Close & Reconcile Modal State
  const [closeModalOpen, setCloseModalOpen] = useState(false);
  const [closeSubmitting, setCloseSubmitting] = useState(false);
  const [closeError, setCloseError] = useState(null);
  const [closeSuccess, setCloseSuccess] = useState(null);

  // Banknote Denomination Counts
  const [notes200, setNotes200] = useState(0);
  const [notes100, setNotes100] = useState(0);
  const [notes50, setNotes50] = useState(0);
  const [notes10, setNotes10] = useState(0);
  const [notes5, setNotes5] = useState(0);
  const [recNotes, setRecNotes] = useState('');

  const loadTillData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [tillRes, recsRes] = await Promise.all([
        tillApi.getMyTill(),
        tillApi.getMyReconciliations()
      ]);
      setTill(tillRes.data || null);
      setReconciliations(recsRes.data || []);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load teller drawer information.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTillData();
  }, []);

  // Compute calculated physical cash from banknote counts
  const totalPhysicalCash =
    notes200 * 200 +
    notes100 * 100 +
    notes50 * 50 +
    notes10 * 10 +
    notes5 * 5;

  const electronicBalance = Number(till?.currentCash) || 0;
  const variance = totalPhysicalCash - electronicBalance;

  const handleOpenTill = async (e) => {
    e.preventDefault();
    setOpenSubmitting(true);
    try {
      await tillApi.openTill(openingCashInput);
      setOpenModalOpen(false);
      await loadTillData();
    } catch (err) {
      alert(err?.response?.data?.message || 'Failed to open drawer.');
    } finally {
      setOpenSubmitting(false);
    }
  };

  const handleCloseAndReconcile = async (e) => {
    e.preventDefault();
    setCloseSubmitting(true);
    setCloseError(null);
    setCloseSuccess(null);

    try {
      const payload = {
        physicalCashCounted: totalPhysicalCash,
        notes200Count: Number(notes200) || 0,
        notes100Count: Number(notes100) || 0,
        notes50Count: Number(notes50) || 0,
        notes10Count: Number(notes10) || 0,
        notes5Count: Number(notes5) || 0,
        reconciliationNotes: recNotes || (variance === 0 ? 'Balanced drawer' : `Variance: ETB ${variance}`)
      };

      const res = await tillApi.closeTill(payload);
      setCloseSuccess(`Drawer closed! Variance: ETB ${res.data?.cashVariance || variance}`);
      await loadTillData();
      setTimeout(() => {
        setCloseModalOpen(false);
      }, 1200);
    } catch (err) {
      setCloseError(err?.response?.data?.message || 'Failed to close drawer.');
    } finally {
      setCloseSubmitting(false);
    }
  };

  const isTillOpen = till?.status === 'OPEN';

  return (
    <PermissionGuard
      roles={['SACCO_ADMIN', 'ADMIN', 'TELLER', 'BRANCH_MANAGER']}
      permissions={['TELLER_TILL:VIEW', 'TRANSACTION_DEPOSIT']}
      fallback={
        <div className="p-8 text-center max-w-lg mx-auto space-y-4 mt-10">
          <div className="w-14 h-14 rounded-2xl bg-red-500/10 border border-red-500/20 text-red-500 mx-auto flex items-center justify-center">
            <Lock className="w-7 h-7" />
          </div>
          <h2 className="text-lg font-bold">Access Restricted</h2>
          <p className="text-xs text-[var(--bdae-text-secondary)]">
            Teller Cash Drawer requires Teller, Branch Manager, or Administrator credentials.
          </p>
        </div>
      }
    >
      <div className="space-y-6 animate-fadeIn">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-[var(--bdae-border)] pb-5">
          <div className="flex items-center gap-3">
            <div
              className="w-11 h-11 rounded-2xl flex items-center justify-center text-white shadow-lg shrink-0"
              style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
            >
              <Banknote className="w-6 h-6" />
            </div>
            <div>
              <h1 className="text-xl font-black tracking-tight text-[var(--bdae-text-primary)]">
                Teller Cash Drawer & Banknote Reconciliation
              </h1>
              <p className="text-xs text-[var(--bdae-text-secondary)]">
                Live cash drawer balancing, vault cash intake, and physical denomination counting.
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={loadTillData}
              className="p-2.5 rounded-xl border border-[var(--bdae-border)] hover:bg-[var(--bdae-border)]/20 text-[var(--bdae-text-secondary)] transition-all"
              title="Refresh till"
            >
              <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            </button>

            {!isTillOpen ? (
              <button
                onClick={() => setOpenModalOpen(true)}
                className="flex items-center gap-2 px-4 py-2.5 rounded-xl font-bold text-xs text-white shadow-md transition-all hover:opacity-95"
                style={{ background: `linear-gradient(135deg, #10b981, #059669)` }}
              >
                <Unlock className="w-4 h-4" />
                Open Drawer
              </button>
            ) : (
              <button
                onClick={() => {
                  setNotes200(0); setNotes100(0); setNotes50(0); setNotes10(0); setNotes5(0);
                  setRecNotes(''); setCloseError(null); setCloseSuccess(null);
                  setCloseModalOpen(true);
                }}
                className="flex items-center gap-2 px-4 py-2.5 rounded-xl font-bold text-xs text-white shadow-md transition-all hover:opacity-95"
                style={{ background: `linear-gradient(135deg, #ef4444, #b91c1c)` }}
              >
                <Calculator className="w-4 h-4" />
                Close & Reconcile
              </button>
            )}
          </div>
        </div>

        {error && (
          <div className="p-4 rounded-xl border border-red-500/20 bg-red-500/10 text-red-500 text-xs flex items-center gap-2">
            <AlertTriangle className="w-4 h-4 shrink-0" />
            {error}
          </div>
        )}

        {/* Live Drawer Status Card */}
        {till && (
          <div className="bdae-card border border-[var(--bdae-border)] rounded-2xl p-6 relative overflow-hidden shadow-lg">
            <div className="absolute top-0 right-0 w-64 h-64 bg-[var(--bdae-primary)]/5 rounded-full blur-3xl pointer-events-none" />

            <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 relative z-10">
              <div className="space-y-2">
                <div className="flex items-center gap-3">
                  <span
                    className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-black uppercase tracking-wider border ${
                      isTillOpen
                        ? 'bg-emerald-500/10 text-emerald-600 border-emerald-500/20'
                        : 'bg-gray-500/10 text-gray-500 border-gray-500/20'
                    }`}
                  >
                    <span
                      className={`w-2 h-2 rounded-full ${
                        isTillOpen ? 'bg-emerald-500 animate-pulse' : 'bg-gray-400'
                      }`}
                    />
                    {till.status}
                  </span>
                  <span className="text-xs font-mono text-[var(--bdae-text-secondary)]">
                    Branch {till.branchCode} • GL: <b>{till.tillGlCode}</b>
                  </span>
                </div>

                <div className="text-xs text-[var(--bdae-text-secondary)] font-bold">
                  {till.tillName}
                </div>

                <div className="text-4xl font-black text-[var(--bdae-text-primary)] tracking-tight">
                  {formatCurrency(till.currentCash || 0)}
                </div>

                <div className="text-[11px] text-[var(--bdae-text-secondary)] flex items-center gap-4 pt-1">
                  <span>Opening Cash: <b>{formatCurrency(till.openingCash || 0)}</b></span>
                  <span>Drawer Limit: <b>{formatCurrency(till.maxCashLimit || 200000)}</b></span>
                </div>
              </div>

              <div className="flex flex-col gap-2 shrink-0 md:border-l md:border-[var(--bdae-border)] md:pl-6 text-xs">
                <div className="text-[10px] font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
                  Session Timestamps
                </div>
                <div>
                  Opened At: <b className="text-[var(--bdae-text-primary)]">{till.openedAt ? formatDateTime(till.openedAt) : 'Not Open'}</b>
                </div>
                <div>
                  Last Closed: <b className="text-[var(--bdae-text-primary)]">{till.closedAt ? formatDateTime(till.closedAt) : 'Never'}</b>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Historical Reconciliation Sheets Table */}
        <div className="bdae-card border border-[var(--bdae-border)] rounded-2xl p-6 space-y-4 shadow-sm">
          <div className="flex items-center justify-between border-b border-[var(--bdae-border)] pb-3">
            <div className="flex items-center gap-2">
              <History className="w-5 h-5 text-[var(--bdae-secondary)]" />
              <h3 className="font-black text-sm text-[var(--bdae-text-primary)]">
                Cash Reconciliation Audit History
              </h3>
            </div>
            <span className="text-[11px] text-[var(--bdae-text-secondary)] font-mono">
              {reconciliations.length} records found
            </span>
          </div>

          {reconciliations.length === 0 ? (
            <div className="py-10 text-center space-y-2">
              <FileText className="w-8 h-8 text-[var(--bdae-text-secondary)] mx-auto opacity-30" />
              <p className="text-xs text-[var(--bdae-text-secondary)]">
                No past drawer reconciliations recorded yet.
              </p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left">
                <thead>
                  <tr className="border-b border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] uppercase text-[10px] font-extrabold tracking-wider">
                    <th className="py-2.5 px-3">Date & Time</th>
                    <th className="py-2.5 px-3">Electronic Book</th>
                    <th className="py-2.5 px-3">Physical Counted</th>
                    <th className="py-2.5 px-3">Cash Variance</th>
                    <th className="py-2.5 px-3">Banknotes Breakdown</th>
                    <th className="py-2.5 px-3">Notes</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-[var(--bdae-border)]/50">
                  {reconciliations.map((r) => {
                    const varNum = Number(r.cashVariance) || 0;
                    return (
                      <tr key={r.reconciliationId} className="hover:bg-black/5 dark:hover:bg-white/5 transition-all">
                        <td className="py-3 px-3 font-mono text-[11px] text-[var(--bdae-text-primary)]">
                          {formatDateTime(r.createdAt)}
                        </td>
                        <td className="py-3 px-3 font-bold text-[var(--bdae-text-primary)]">
                          {formatCurrency(r.electronicCashBalance)}
                        </td>
                        <td className="py-3 px-3 font-bold text-[var(--bdae-text-primary)]">
                          {formatCurrency(r.physicalCashCounted)}
                        </td>
                        <td className="py-3 px-3">
                          <span
                            className={`px-2 py-0.5 rounded-full text-[10px] font-black border ${
                              varNum === 0
                                ? 'bg-emerald-500/10 text-emerald-600 border-emerald-500/20'
                                : varNum > 0
                                ? 'bg-amber-500/10 text-amber-600 border-amber-500/20'
                                : 'bg-red-500/10 text-red-500 border-red-500/20'
                            }`}
                          >
                            {varNum === 0 ? 'BALANCED' : formatCurrency(varNum)}
                          </span>
                        </td>
                        <td className="py-3 px-3 font-mono text-[10px] text-[var(--bdae-text-secondary)]">
                          200x{r.notes200Count} • 100x{r.notes100Count} • 50x{r.notes50Count} • 10x{r.notes10Count} • 5x{r.notes5Count}
                        </td>
                        <td className="py-3 px-3 text-[11px] text-[var(--bdae-text-secondary)] max-w-xs truncate">
                          {r.reconciliationNotes || '—'}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Modal: Open Till */}
        {openModalOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
            <div className="bdae-card border border-[var(--bdae-border)] rounded-2xl w-full max-w-md p-6 space-y-5 bg-[var(--bdae-bg-surface,#18181b)] shadow-2xl">
              <div className="flex items-center justify-between border-b border-[var(--bdae-border)] pb-3">
                <div className="flex items-center gap-2">
                  <Unlock className="w-5 h-5 text-emerald-500" />
                  <h3 className="font-black text-sm text-[var(--bdae-text-primary)]">
                    Open Teller Drawer
                  </h3>
                </div>
                <button onClick={() => setOpenModalOpen(false)} className="p-1 rounded-lg text-[var(--bdae-text-secondary)] hover:bg-black/10">
                  <X className="w-4 h-4" />
                </button>
              </div>

              <form onSubmit={handleOpenTill} className="space-y-4 text-xs">
                <div>
                  <label className="block font-bold text-[var(--bdae-text-secondary)] mb-1">
                    Opening Vault Cash (ETB) *
                  </label>
                  <input
                    type="number"
                    min="0"
                    step="100"
                    required
                    value={openingCashInput}
                    onChange={(e) => setOpeningCashInput(e.target.value)}
                    className="w-full px-3 py-2 bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] rounded-xl text-base font-bold text-[var(--bdae-text-primary)] focus:outline-none focus:border-emerald-500"
                  />
                  <span className="text-[10px] text-[var(--bdae-text-secondary)] mt-1 block">
                    Cash physically transferred from Branch Vault into your till drawer.
                  </span>
                </div>

                <div className="flex items-center justify-end gap-3 pt-3 border-t border-[var(--bdae-border)]">
                  <button
                    type="button"
                    onClick={() => setOpenModalOpen(false)}
                    className="px-4 py-2 rounded-xl border border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] font-bold"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={openSubmitting}
                    className="px-5 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold shadow-md flex items-center gap-2"
                  >
                    {openSubmitting && <RefreshCw className="w-3.5 h-3.5 animate-spin" />}
                    Confirm & Open
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {/* Modal: Interactive Banknote Counter & Close Drawer */}
        {closeModalOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
            <div className="bdae-card border border-[var(--bdae-border)] rounded-2xl w-full max-w-xl p-6 space-y-5 bg-[var(--bdae-bg-surface,#18181b)] shadow-2xl">
              <div className="flex items-center justify-between border-b border-[var(--bdae-border)] pb-3">
                <div className="flex items-center gap-2">
                  <Calculator className="w-5 h-5 text-red-500" />
                  <div>
                    <h3 className="font-black text-sm text-[var(--bdae-text-primary)]">
                      Banknote Denomination Reconciliation
                    </h3>
                    <p className="text-[10px] text-[var(--bdae-text-secondary)]">
                      Count each physical banknote stack to verify drawer against electronic ledger.
                    </p>
                  </div>
                </div>
                <button onClick={() => setCloseModalOpen(false)} className="p-1 rounded-lg text-[var(--bdae-text-secondary)] hover:bg-black/10">
                  <X className="w-4 h-4" />
                </button>
              </div>

              {closeError && (
                <div className="p-3 rounded-xl border border-red-500/20 bg-red-500/10 text-red-500 text-xs flex items-center gap-2">
                  <AlertTriangle className="w-4 h-4 shrink-0" />
                  {closeError}
                </div>
              )}

              {closeSuccess && (
                <div className="p-3 rounded-xl border border-emerald-500/20 bg-emerald-500/10 text-emerald-500 text-xs flex items-center gap-2">
                  <CheckCircle2 className="w-4 h-4 shrink-0" />
                  {closeSuccess}
                </div>
              )}

              <form onSubmit={handleCloseAndReconcile} className="space-y-4 text-xs">
                {/* Benchmark Electronic vs Physical */}
                <div className="grid grid-cols-3 gap-3 p-3 rounded-xl bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)]">
                  <div>
                    <div className="text-[10px] text-[var(--bdae-text-secondary)] font-bold uppercase">System Book</div>
                    <div className="text-sm font-black text-[var(--bdae-text-primary)] mt-0.5">
                      {formatCurrency(electronicBalance)}
                    </div>
                  </div>
                  <div>
                    <div className="text-[10px] text-[var(--bdae-text-secondary)] font-bold uppercase">Physical Count</div>
                    <div className="text-sm font-black text-blue-500 mt-0.5">
                      {formatCurrency(totalPhysicalCash)}
                    </div>
                  </div>
                  <div>
                    <div className="text-[10px] text-[var(--bdae-text-secondary)] font-bold uppercase">Variance</div>
                    <div
                      className={`text-sm font-black mt-0.5 ${
                        variance === 0
                          ? 'text-emerald-500'
                          : variance > 0
                          ? 'text-amber-500'
                          : 'text-red-500'
                      }`}
                    >
                      {variance === 0 ? '0.00 (Balanced)' : formatCurrency(variance)}
                    </div>
                  </div>
                </div>

                {/* Banknotes Grid */}
                <div className="space-y-2 border border-[var(--bdae-border)] rounded-xl p-3">
                  <div className="text-[11px] font-bold text-[var(--bdae-text-primary)]">
                    Physical Denomination Stacks
                  </div>

                  {[
                    { label: '200 ETB Notes', value: notes200, setter: setNotes200, denom: 200 },
                    { label: '100 ETB Notes', value: notes100, setter: setNotes100, denom: 100 },
                    { label: '50 ETB Notes', value: notes50, setter: setNotes50, denom: 50 },
                    { label: '10 ETB Notes', value: notes10, setter: setNotes10, denom: 10 },
                    { label: '5 ETB Notes', value: notes5, setter: setNotes5, denom: 5 },
                  ].map(({ label, value, setter, denom }) => (
                    <div key={denom} className="flex items-center justify-between gap-3 py-1">
                      <span className="font-mono font-bold text-[var(--bdae-text-secondary)] w-32">
                        {label}
                      </span>
                      <div className="flex items-center gap-2">
                        <input
                          type="number"
                          min="0"
                          value={value}
                          onChange={(e) => setter(Math.max(0, parseInt(e.target.value) || 0))}
                          className="w-20 px-2 py-1 bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] rounded-lg text-right font-mono text-xs focus:outline-none focus:border-[var(--bdae-primary)]"
                        />
                        <span className="text-[var(--bdae-text-secondary)] font-mono text-[11px] w-24 text-right">
                          = {formatCurrency(value * denom)}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>

                <div>
                  <label className="block font-bold text-[var(--bdae-text-secondary)] mb-1">
                    Reconciliation Notes / Vault Transfer Reason
                  </label>
                  <textarea
                    rows={2}
                    value={recNotes}
                    onChange={(e) => setRecNotes(e.target.value)}
                    placeholder="e.g. Balanced drawer without variance. Cash transferred to main vault."
                    className="w-full px-3 py-2 bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] rounded-xl text-xs focus:outline-none focus:border-[var(--bdae-primary)]"
                  />
                </div>

                <div className="flex items-center justify-end gap-3 pt-3 border-t border-[var(--bdae-border)]">
                  <button
                    type="button"
                    onClick={() => setCloseModalOpen(false)}
                    className="px-4 py-2 rounded-xl border border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] font-bold"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={closeSubmitting}
                    className="px-5 py-2 rounded-xl bg-red-600 hover:bg-red-700 text-white font-bold shadow-md flex items-center gap-2"
                  >
                    {closeSubmitting && <RefreshCw className="w-3.5 h-3.5 animate-spin" />}
                    Lock & Close Drawer
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </PermissionGuard>
  );
};
