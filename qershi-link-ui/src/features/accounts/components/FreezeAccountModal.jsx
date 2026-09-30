import React, { useState } from 'react';
import { Snowflake, AlertCircle, Loader2, X, Check } from 'lucide-react';
import { accountLedgerApi } from '../api/accountLedgerApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';

const FREEZE_OPTIONS = ['NONE', 'DEBIT_FREEZE', 'CREDIT_FREEZE', 'FULL_FREEZE'];

export const FreezeAccountModal = ({ isOpen, account, onClose, onUpdated }) => {
  const [freezeStatus, setFreezeStatus] = useState(account?.freezeStatus || 'NONE');
  const [reason, setReason] = useState('');
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState(null);

  if (!isOpen || !account) return null;

  const handleSubmit = async (e) => {
    if (e) e.preventDefault();
    setIsSaving(true);
    setError(null);
    try {
      if (freezeStatus === 'NONE') {
        await accountLedgerApi.unfreezeAccount(account.accountNumber, reason);
      } else {
        await accountLedgerApi.freezeAccount(account.accountNumber, freezeStatus, reason);
      }
      onUpdated();
      onClose();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to update account restriction status.');
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 animate-fadeIn">
      <div className="bdae-card w-full max-w-md rounded-2xl border border-[var(--bdae-border)] shadow-2xl overflow-hidden">
        <div className="p-5 flex items-center justify-between bg-blue-600 text-white">
          <div className="flex items-center gap-3">
            <Snowflake className="w-5 h-5" />
            <div>
              <h2 className="text-sm font-extrabold">Account Restriction Controls</h2>
              <p className="text-[10px] opacity-80 font-mono">
                {account.accountNumber}
              </p>
            </div>
          </div>
          <button onClick={onClose} className="text-white/70 hover:text-white">
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4 text-xs">
          {error && (
            <div className="flex items-center gap-2 p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 font-bold">
              <AlertCircle className="w-4 h-4 shrink-0" /> {error}
            </div>
          )}

          <div className="space-y-1.5">
            <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">
              Restriction Mode
            </label>
            <div className="grid grid-cols-2 gap-2">
              {FREEZE_OPTIONS.map((opt) => (
                <button
                  key={opt}
                  type="button"
                  onClick={() => setFreezeStatus(opt)}
                  className={`p-2.5 rounded-xl border text-[11px] font-bold transition-all text-left ${
                    freezeStatus === opt
                      ? 'border-blue-500 bg-blue-500/10 text-blue-600 dark:text-blue-400'
                      : 'border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] hover:bg-black/5'
                  }`}
                >
                  {opt.replace('_', ' ')}
                </button>
              ))}
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">
              Reason / Legal Authorization *
            </label>
            <textarea
              required
              rows={3}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="e.g. Court order ref #4912 or AML investigation"
              className="bdae-input resize-none"
            />
          </div>

          <div className="pt-4 border-t border-[var(--bdae-border)] flex justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-xl border border-[var(--bdae-border)] font-bold text-[var(--bdae-text-secondary)] hover:bg-black/5"
            >
              Cancel
            </button>

            <PermissionGuard
              permissions={[PERMISSIONS.ACCOUNT_FREEZE]}
              fallback={
                <span className="text-xs text-red-500 font-bold self-center">
                  Account Freeze Restricted
                </span>
              }
            >
              <button
                type="submit"
                disabled={isSaving}
                className="px-5 py-2 rounded-xl font-bold text-white bg-blue-600 hover:bg-blue-700 flex items-center gap-2 shadow-md disabled:opacity-50 transition-all"
              >
                {isSaving ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <Check className="w-3.5 h-3.5" />}
                <span>Apply Restriction</span>
              </button>
            </PermissionGuard>
          </div>
        </form>
      </div>
    </div>
  );
};
