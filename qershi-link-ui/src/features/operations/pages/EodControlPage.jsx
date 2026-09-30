import React, { useState, useEffect } from 'react';
import {
  Moon,
  Play,
  RotateCw,
  RefreshCw,
  AlertTriangle,
  CheckCircle2
} from 'lucide-react';
import { accountHttpClient } from '../../../common/api/httpClient';
import { useAuthStore } from '../../../common/store/useAuthStore';
import { PERMISSIONS } from '../../../common/constants/permissions';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { EodStatusCard } from '../components/EodStatusCard';
import { EodBatchStepsGrid } from '../components/EodBatchStepsGrid';
import { EodBatchHistoryTable } from '../components/EodBatchHistoryTable';
import { EodExecutionModal } from '../components/EodExecutionModal';

export const EodControlPage = () => {
  const [eodStatus, setEodStatus] = useState(null);
  const [batchHistory, setBatchHistory] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isExecuting, setIsExecuting] = useState(false);
  const [selectedBatch, setSelectedBatch] = useState(null);
  const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
  const [actionError, setActionError] = useState(null);
  const [actionSuccess, setActionSuccess] = useState(null);

  const user = useAuthStore((state) => state.user);
  const userPermissions = user?.permissions || [];
  const canExecute =
    user?.globalRole === 'SUPER_ADMIN' ||
    userPermissions.includes(PERMISSIONS.EOD_EXECUTE);

  const fetchEodState = async () => {
    setIsLoading(true);
    setActionError(null);
    try {
      const statusRes = await accountHttpClient.get('/eod/status');
      setEodStatus(statusRes.data);

      const historyRes = await accountHttpClient.get('/eod/history');
      setBatchHistory(Array.isArray(historyRes.data) ? historyRes.data : []);
    } catch (err) {
      console.error('Failed to fetch EOD state', err);
      setActionError(
        err.response?.data?.message || 'Failed to retrieve End-of-Day status from core banking.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchEodState();
  }, []);

  const handleExecuteEod = async () => {
    setIsExecuting(true);
    setActionError(null);
    setActionSuccess(null);
    try {
      const res = await accountHttpClient.post('/eod/run');
      setActionSuccess(res.data?.message || 'End-of-Day batch executed successfully.');
      setIsConfirmModalOpen(false);
      await fetchEodState();
    } catch (err) {
      console.error('EOD Batch Execution Failure', err);
      setActionError(
        err.response?.data?.message || 'End-of-Day execution failed. Inspect step audit logs.'
      );
      setIsConfirmModalOpen(false);
    } finally {
      setIsExecuting(false);
    }
  };

  return (
    <div className="space-y-6 animate-fadeIn pb-12">
      {/* ── Page Header ── */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-[var(--bdae-border)] pb-4">
        <div>
          <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
            <Moon className="w-4 h-4 text-purple-500" />
            <span>Core Banking Operations</span>
          </div>
          <h1 className="text-2xl font-black tracking-tight text-[var(--bdae-text-primary)]">
            End-of-Day (EOD) Batch Engine
          </h1>
          <p className="text-xs text-[var(--bdae-text-secondary)]">
            Automate interest accrual, loan delinquency aging, statutory loss reserves, and daily General Ledger rollups.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={fetchEodState}
            disabled={isLoading || isExecuting}
            className="px-3 py-2 rounded-xl text-xs font-semibold border border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5 text-[var(--bdae-text-primary)] flex items-center gap-1.5 transition-all"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />
            <span>Refresh</span>
          </button>

          {/* Action Button strictly guarded by EOD_EXECUTE */}
          {canExecute && (
            <PermissionGuard permissions={[PERMISSIONS.EOD_EXECUTE]}>
              <button
                onClick={() => setIsConfirmModalOpen(true)}
                disabled={isExecuting}
                className="px-4 py-2 rounded-xl text-xs font-bold bg-purple-600 hover:bg-purple-700 text-white flex items-center gap-2 shadow-lg shadow-purple-600/20 transition-all disabled:opacity-50"
              >
                <Play className="w-3.5 h-3.5" />
                <span>Run End-of-Day Batch</span>
              </button>
            </PermissionGuard>
          )}
        </div>
      </div>

      {/* ── Feedback Alerts ── */}
      {actionError && (
        <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 text-xs flex items-center gap-2.5">
          <AlertTriangle className="w-4 h-4 shrink-0" />
          <span>{actionError}</span>
        </div>
      )}

      {actionSuccess && (
        <div className="p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-600 dark:text-emerald-400 text-xs flex items-center gap-2.5 animate-fadeIn">
          <CheckCircle2 className="w-4 h-4 shrink-0" />
          <span>{actionSuccess}</span>
        </div>
      )}

      {/* ── 1. Modular Operational Status Card ── */}
      <EodStatusCard eodStatus={eodStatus} />

      {/* ── 2. Modular Sequential Pipeline Steps ── */}
      <EodBatchStepsGrid />

      {/* ── 3. Modular Historical EOD Execution Table ── */}
      <EodBatchHistoryTable
        batchHistory={batchHistory}
        isLoading={isLoading}
        selectedBatch={selectedBatch}
        onSelectBatch={setSelectedBatch}
      />

      {/* ── 4. Modular Execution Confirmation Modal ── */}
      <EodExecutionModal
        isOpen={isConfirmModalOpen}
        onClose={() => setIsConfirmModalOpen(false)}
        isExecuting={isExecuting}
        currentDate={eodStatus?.businessDate}
        onConfirm={handleExecuteEod}
      />
    </div>
  );
};
