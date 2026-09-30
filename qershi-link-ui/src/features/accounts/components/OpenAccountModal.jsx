import React, { useState, useEffect } from 'react';
import { CreditCard, AlertCircle, Loader2, X, Check } from 'lucide-react';
import { accountLedgerApi } from '../api/accountLedgerApi';
import { depositProductApi } from '../api/depositProductApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';

export const OpenAccountModal = ({ isOpen, userId, onClose, onOpened }) => {
  const [products, setProducts] = useState([]);
  const [productCode, setProductCode] = useState('');
  const [branchCode, setBranchCode] = useState('0001');
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isOpen) {
      depositProductApi
        .getAllProducts()
        .then((res) => setProducts(res.data || res || []))
        .catch(() => {});
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    if (e) e.preventDefault();
    if (!productCode) {
      setError('Please select a deposit product.');
      return;
    }
    setIsSaving(true);
    setError(null);
    try {
      await accountLedgerApi.openAccount({ userId, branchCode, productCode });
      onOpened();
      onClose();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to open account.');
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 animate-fadeIn">
      <div className="bdae-card w-full max-w-md rounded-2xl border border-[var(--bdae-border)] shadow-2xl overflow-hidden">
        <div
          className="p-5 flex items-center justify-between text-white"
          style={{
            background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))`
          }}
        >
          <div className="flex items-center gap-3">
            <CreditCard className="w-5 h-5" />
            <div>
              <h2 className="text-sm font-extrabold">Open New Account</h2>
              <p className="text-[10px] opacity-80">
                Account will enter PENDING_APPROVAL status for four-eye clearance
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
              Deposit Product *
            </label>
            <select
              value={productCode}
              onChange={(e) => setProductCode(e.target.value)}
              className="bdae-input font-bold"
            >
              <option value="">— Select a product —</option>
              {products.map((p) => (
                <option key={p.productCode} value={p.productCode}>
                  [{p.productCode}] {p.productName}
                </option>
              ))}
            </select>
          </div>

          <div className="space-y-1.5">
            <label className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">
              Branch Code *
            </label>
            <input
              type="text"
              required
              value={branchCode}
              onChange={(e) => setBranchCode(e.target.value)}
              className="bdae-input font-mono font-bold tracking-widest"
              placeholder="0001"
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
              permissions={[PERMISSIONS.ACCOUNT_OPEN]}
              fallback={
                <span className="text-xs text-red-500 font-bold self-center">
                  Account Opening Restricted
                </span>
              }
            >
              <button
                type="submit"
                disabled={isSaving}
                className="px-5 py-2 rounded-xl font-bold text-white flex items-center gap-2 shadow-md hover:opacity-90 disabled:opacity-50"
                style={{
                  background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))`
                }}
              >
                {isSaving ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <Check className="w-3.5 h-3.5" />}
                <span>Submit Opening</span>
              </button>
            </PermissionGuard>
          </div>
        </form>
      </div>
    </div>
  );
};
