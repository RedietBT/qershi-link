import React, { useState, useEffect } from 'react';
import { ShieldAlert, AlertCircle, Loader2, X, Plus, Link2Off, Check } from 'lucide-react';
import { accountLedgerApi } from '../api/accountLedgerApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';
import { formatCurrency, formatDateTime } from '../../../common/utils/currency';

export const LienManagementModal = ({ isOpen, account, onClose, onUpdated }) => {
  const [liens, setLiens] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isCreating, setIsCreating] = useState(false);
  const [createMode, setCreateMode] = useState(false);
  const [error, setError] = useState(null);

  const [form, setForm] = useState({
    amount: '',
    reason: '',
    referenceId: ''
  });

  const loadLiens = async () => {
    if (!account?.accountNumber) return;
    setIsLoading(true);
    try {
      const res = await accountLedgerApi.getAccountLiens(account.accountNumber);
      setLiens(res.data || res || []);
    } catch {
      setLiens([]);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (isOpen) {
      loadLiens();
    }
  }, [isOpen, account]);

  if (!isOpen || !account) return null;

  const handleCreate = async (e) => {
    if (e) e.preventDefault();
    if (!form.amount || parseFloat(form.amount) <= 0) {
      setError('A valid positive lien amount is required.');
      return;
    }
    if (!form.reason.trim()) {
      setError('A lien reason is required.');
      return;
    }
    setIsCreating(true);
    setError(null);
    try {
      await accountLedgerApi.placeLien(account.accountNumber, {
        amount: parseFloat(form.amount),
        reason: form.reason,
        referenceId: form.referenceId || null
      });
      setCreateMode(false);
      setForm({ amount: '', reason: '', referenceId: '' });
      await loadLiens();
      onUpdated();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to place lien hold.');
    } finally {
      setIsCreating(false);
    }
  };

  const handleRelease = async (lienId) => {
    if (!window.confirm('Are you sure you want to release this lien hold?')) return;
    try {
      await accountLedgerApi.releaseLien(account.accountNumber, lienId);
      await loadLiens();
      onUpdated();
    } catch (err) {
      alert(err?.response?.data?.message || 'Failed to release lien hold.');
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 animate-fadeIn">
      <div className="bdae-card w-full max-w-lg rounded-2xl border border-[var(--bdae-border)] shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="p-5 flex items-center justify-between bg-amber-600 text-white">
          <div className="flex items-center gap-3">
            <ShieldAlert className="w-5 h-5" />
            <div>
              <h2 className="text-sm font-extrabold">Lien Hold Registry</h2>
              <p className="text-[10px] opacity-80 font-mono">
                {account.accountNumber} — Active Holds
              </p>
            </div>
          </div>
          <button onClick={onClose} className="text-white/70 hover:text-white">
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-6 space-y-4 max-h-[70vh] overflow-y-auto text-xs">
          {error && (
            <div className="flex items-center gap-2 p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 font-bold">
              <AlertCircle className="w-4 h-4 shrink-0" /> {error}
            </div>
          )}

          {/* Toggle Create / List */}
          <div className="flex items-center justify-between">
            <span className="font-bold text-[var(--bdae-text-secondary)] uppercase text-[10px]">
              {createMode ? 'Create New Lien' : `Active Holds (${liens.length})`}
            </span>

            <PermissionGuard permissions={[PERMISSIONS.LIEN_CREATE]}>
              <button
                type="button"
                onClick={() => setCreateMode(!createMode)}
                className="flex items-center gap-1.5 px-3 py-1 rounded-xl text-[10px] font-bold border border-[var(--bdae-border)] hover:bg-black/5"
              >
                {createMode ? 'View Existing' : <><Plus className="w-3 h-3" /> Place Lien</>}
              </button>
            </PermissionGuard>
          </div>

          {createMode ? (
            <form onSubmit={handleCreate} className="space-y-3 pt-2 border-t border-[var(--bdae-border)]">
              <div className="space-y-1">
                <label className="text-[10px] font-bold uppercase text-[var(--bdae-text-secondary)]">
                  Lien Amount (ETB) *
                </label>
                <input
                  type="number"
                  required
                  min="0.01"
                  step="0.01"
                  value={form.amount}
                  onChange={(e) => setForm({ ...form, amount: e.target.value })}
                  className="bdae-input font-mono font-bold"
                  placeholder="5000.00"
                />
              </div>

              <div className="space-y-1">
                <label className="text-[10px] font-bold uppercase text-[var(--bdae-text-secondary)]">
                  Reason *
                </label>
                <input
                  type="text"
                  required
                  value={form.reason}
                  onChange={(e) => setForm({ ...form, reason: e.target.value })}
                  className="bdae-input"
                  placeholder="e.g. Loan collateral hold #LN-0042"
                />
              </div>

              <div className="space-y-1">
                <label className="text-[10px] font-bold uppercase text-[var(--bdae-text-secondary)]">
                  External Reference ID
                </label>
                <input
                  type="text"
                  value={form.referenceId}
                  onChange={(e) => setForm({ ...form, referenceId: e.target.value })}
                  className="bdae-input font-mono"
                  placeholder="e.g. REF-2026-X1"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setCreateMode(false)}
                  className="px-3 py-1.5 rounded-xl border border-[var(--bdae-border)] font-bold text-[var(--bdae-text-secondary)]"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isCreating}
                  className="px-4 py-1.5 rounded-xl font-bold text-white bg-amber-600 hover:bg-amber-700 flex items-center gap-1.5 disabled:opacity-50"
                >
                  {isCreating ? <Loader2 className="w-3 h-3 animate-spin" /> : <Check className="w-3 h-3" />}
                  <span>Place Lien</span>
                </button>
              </div>
            </form>
          ) : (
            <div className="space-y-2">
              {isLoading ? (
                <div className="py-8 text-center text-xs text-[var(--bdae-text-secondary)]">
                  <Loader2 className="w-5 h-5 animate-spin mx-auto text-amber-500 mb-1" />
                  Loading liens...
                </div>
              ) : liens.length === 0 ? (
                <div className="py-8 text-center text-xs text-[var(--bdae-text-secondary)]">
                  No active lien holds placed on this account.
                </div>
              ) : (
                liens.map((l) => (
                  <div
                    key={l.lienId || l.id}
                    className="p-3.5 rounded-xl border border-[var(--bdae-border)] flex items-center justify-between hover:bg-black/5 dark:hover:bg-white/5"
                  >
                    <div>
                      <div className="font-mono font-black text-sm text-[var(--bdae-text-primary)]">
                        {formatCurrency(l.amount)}
                      </div>
                      <div className="text-[11px] text-[var(--bdae-text-secondary)] mt-0.5">
                        {l.reason}
                      </div>
                      {l.placedAt && (
                        <div className="text-[10px] text-[var(--bdae-text-secondary)] opacity-60">
                          Placed: {formatDateTime(l.placedAt)}
                        </div>
                      )}
                    </div>

                    <PermissionGuard permissions={[PERMISSIONS.LIEN_RELEASE]}>
                      <button
                        type="button"
                        onClick={() => handleRelease(l.lienId || l.id)}
                        className="px-3 py-1 rounded-lg text-rose-600 bg-rose-500/10 hover:bg-rose-500/20 font-bold text-[11px] flex items-center gap-1 transition-all"
                        title="Release Lien Hold"
                      >
                        <Link2Off className="w-3.5 h-3.5" />
                        <span>Release</span>
                      </button>
                    </PermissionGuard>
                  </div>
                ))
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
