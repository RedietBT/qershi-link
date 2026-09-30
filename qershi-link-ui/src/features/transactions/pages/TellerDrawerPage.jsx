import React, { useState, useEffect } from 'react';
import { Banknote, Lock, Unlock, RefreshCw } from 'lucide-react';
import { tillApi } from '../api/tillApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';
import { TillPositionCards } from '../components/TillPositionCards';
import { OpenTillModal } from '../components/OpenTillModal';
import { ReconcileTillModal } from '../components/ReconcileTillModal';
import { TillReconciliationTable } from '../components/TillReconciliationTable';

/**
 * Modular Teller Cash Drawer (Till) Management & Reconciliation Page
 */
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
  const [notes, setNotes] = useState({
    notes200: 0,
    notes100: 0,
    notes50: 0,
    notes10: 0,
    notes5: 0,
  });
  const [recNotes, setRecNotes] = useState('');

  const loadTillData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [tillRes, recsRes] = await Promise.all([
        tillApi.getMyTill(),
        tillApi.getMyReconciliations(),
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

  const totalPhysicalCash =
    notes.notes200 * 200 +
    notes.notes100 * 100 +
    notes.notes50 * 50 +
    notes.notes10 * 10 +
    notes.notes5 * 5;

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
        notes200Count: Number(notes.notes200) || 0,
        notes100Count: Number(notes.notes100) || 0,
        notes50Count: Number(notes.notes50) || 0,
        notes10Count: Number(notes.notes10) || 0,
        notes5Count: Number(notes.notes5) || 0,
        reconciliationNotes:
          recNotes || (variance === 0 ? 'Balanced drawer' : `Variance: ETB ${variance}`),
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
      roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER', 'BRANCH_MANAGER']}
      permissions={[PERMISSIONS.TELLER_TILL_VIEW, PERMISSIONS.CASH_DEPOSIT]}
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
        {/* Header & Controls */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-[var(--bdae-border)] pb-5">
          <div className="flex items-center gap-3">
            <div
              className="w-11 h-11 rounded-2xl flex items-center justify-center text-white shadow-lg shrink-0"
              style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
            >
              <Banknote className="w-6 h-6" />
            </div>
            <div>
              <h1 className="text-xl font-black text-[var(--bdae-text-primary)]">
                Teller Cash Drawer (Till)
              </h1>
              <p className="text-xs text-[var(--bdae-text-secondary)]">
                Physical vault allocations, shift closures, and banknote reconciliations.
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={loadTillData}
              disabled={loading}
              className="p-2.5 rounded-xl border border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5 text-[var(--bdae-text-secondary)] transition-colors"
              title="Refresh Drawer State"
            >
              <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            </button>

            {/* Open Drawer CTA strictly guarded */}
            {!isTillOpen && (
              <PermissionGuard
                roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER', 'BRANCH_MANAGER']}
                permissions={[PERMISSIONS.TELLER_TILL_VIEW, PERMISSIONS.CASH_DEPOSIT]}
              >
                <button
                  type="button"
                  onClick={() => setOpenModalOpen(true)}
                  className="px-4 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold flex items-center gap-2 shadow-md transition-all"
                >
                  <Unlock className="w-4 h-4" />
                  <span>Open Shift Drawer</span>
                </button>
              </PermissionGuard>
            )}

            {/* Close Drawer CTA strictly guarded */}
            {isTillOpen && (
              <PermissionGuard
                roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'TELLER', 'BRANCH_MANAGER']}
                permissions={[PERMISSIONS.TELLER_TILL_VIEW, PERMISSIONS.CASH_DEPOSIT]}
              >
                <button
                  type="button"
                  onClick={() => setCloseModalOpen(true)}
                  className="px-4 py-2.5 bg-amber-600 hover:bg-amber-700 text-white rounded-xl text-xs font-bold flex items-center gap-2 shadow-md transition-all"
                >
                  <Lock className="w-4 h-4" />
                  <span>Close & Reconcile</span>
                </button>
              </PermissionGuard>
            )}
          </div>
        </div>

        {/* Position Summary Cards Component */}
        <TillPositionCards
          till={till}
          isTillOpen={isTillOpen}
          electronicBalance={electronicBalance}
        />

        {/* Reconciliations History Table Component */}
        <TillReconciliationTable reconciliations={reconciliations} />

        {/* Open Drawer Modal Component */}
        <OpenTillModal
          isOpen={openModalOpen}
          openingCashInput={openingCashInput}
          openSubmitting={openSubmitting}
          onCashChange={setOpeningCashInput}
          onClose={() => setOpenModalOpen(false)}
          onSubmit={handleOpenTill}
        />

        {/* Reconcile & Close Modal Component */}
        <ReconcileTillModal
          isOpen={closeModalOpen}
          electronicBalance={electronicBalance}
          totalPhysicalCash={totalPhysicalCash}
          variance={variance}
          notes200={notes.notes200}
          notes100={notes.notes100}
          notes50={notes.notes50}
          notes10={notes.notes10}
          notes5={notes.notes5}
          recNotes={recNotes}
          closeSubmitting={closeSubmitting}
          closeError={closeError}
          closeSuccess={closeSuccess}
          onNotesChange={(key, val) => setNotes((prev) => ({ ...prev, [key]: val }))}
          onRecNotesChange={setRecNotes}
          onClose={() => setCloseModalOpen(false)}
          onSubmit={handleCloseAndReconcile}
        />
      </div>
    </PermissionGuard>
  );
};
