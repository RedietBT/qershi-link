import React, { useState, useEffect } from 'react';
import {
  Moon,
  Sun,
  Play,
  RotateCw,
  Clock,
  ShieldCheck,
  ShieldAlert,
  AlertTriangle,
  CheckCircle2,
  XCircle,
  HelpCircle,
  FileText,
  Calendar,
  Layers,
  ArrowRight,
  TrendingUp,
  UserX,
  ChevronRight,
  RefreshCw,
  Database
} from 'lucide-react';
import { accountHttpClient } from '../../../common/api/httpClient';
import { useAuthStore } from '../../../common/store/useAuthStore';
import { PERMISSIONS } from '../../../common/constants/permissions';

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
  const canExecute = user?.globalRole === 'SUPER_ADMIN' ||
    user?.globalRole === 'SACCO_ADMIN' ||
    userPermissions.includes(PERMISSIONS.EOD_EXECUTE);

  const fetchStatusAndHistory = async () => {
    setIsLoading(true);
    setActionError(null);
    try {
      const [statusRes, historyRes] = await Promise.all([
        accountHttpClient.get('/eod/status'),
        accountHttpClient.get('/eod/history')
      ]);
      setEodStatus(statusRes.data);
      setBatchHistory(historyRes.data || []);
    } catch (err) {
      console.error('Failed to load EOD batch status:', err);
      setActionError(err.response?.data?.message || 'Failed to load Core Banking EOD status.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchStatusAndHistory();
  }, []);

  const handleRunEodBatch = async () => {
    setIsConfirmModalOpen(false);
    setIsExecuting(true);
    setActionError(null);
    setActionSuccess(null);

    try {
      const res = await accountHttpClient.post('/eod/run');
      setActionSuccess(`EOD Batch executed successfully for ${res.data.businessDate}! Advanced to next operational date.`);
      await fetchStatusAndHistory();
      if (res.data) {
        setSelectedBatch(res.data);
      }
    } catch (err) {
      console.error('Failed to execute EOD batch:', err);
      setActionError(err.response?.data?.message || err.message || 'Error occurred while executing EOD batch.');
    } finally {
      setIsExecuting(false);
    }
  };

  const handleViewBatchDetails = async (batchId) => {
    try {
      const res = await accountHttpClient.get(`/eod/history/${batchId}`);
      setSelectedBatch(res.data);
    } catch (err) {
      console.error('Failed to fetch batch details:', err);
    }
  };

  const formatCurrency = (amount) => {
    if (amount === undefined || amount === null) return '0.00 ETB';
    return Number(amount).toLocaleString('en-ET', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' ETB';
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    try {
      return new Date(dateStr).toLocaleString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    } catch {
      return dateStr;
    }
  };

  return (
    <div className="space-y-6 animate-fadeIn pb-12">
      {/* ── Page Header ── */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
            <Moon className="w-4 h-4 text-[var(--bdae-primary)]" />
            <span>Core Banking Operations</span>
          </div>
          <h1 className="text-2xl font-black tracking-tight text-[var(--bdae-text-primary)]">
            End-of-Day (EOD) Batch Console
          </h1>
          <p className="text-xs text-[var(--bdae-text-secondary)]">
            Coordinates daytime transaction cutoff, daily interest accruals, delinquency aging, dormancy sweeps, and financial date rollover.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={fetchStatusAndHistory}
            disabled={isLoading || isExecuting}
            className="px-3 py-2 rounded-xl text-xs font-semibold border border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5 text-[var(--bdae-text-primary)] flex items-center gap-1.5 transition-all"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />
            <span>Refresh</span>
          </button>

          {canExecute && (
            <button
              onClick={() => setIsConfirmModalOpen(true)}
              disabled={isExecuting || eodStatus?.status === 'PROCESSING_EOD'}
              className="px-4 py-2 rounded-xl text-xs font-bold bg-[var(--bdae-primary)] hover:opacity-90 text-white flex items-center gap-2 shadow-lg shadow-[var(--bdae-primary)]/20 transition-all disabled:opacity-50"
            >
              <Play className="w-4 h-4 fill-white" />
              <span>{isExecuting ? 'Running Batch Pipeline...' : 'Run End-of-Day Batch'}</span>
            </button>
          )}
        </div>
      </div>

      {/* ── Notification Feedback ── */}
      {actionError && (
        <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 text-xs flex items-center gap-2.5">
          <AlertTriangle className="w-4 h-4 shrink-0" />
          <span>{actionError}</span>
        </div>
      )}
      {actionSuccess && (
        <div className="p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-600 dark:text-emerald-400 text-xs flex items-center gap-2.5">
          <CheckCircle2 className="w-4 h-4 shrink-0" />
          <span>{actionSuccess}</span>
        </div>
      )}

      {/* ── Financial Business Date Hero Card ── */}
      <div className="bdae-card p-6 rounded-2xl border border-[var(--bdae-border)] bg-gradient-to-br from-[var(--bdae-surface)] to-black/5 dark:to-white/5 relative overflow-hidden shadow-xl">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 items-center">
          {/* Current Business Date */}
          <div className="space-y-1 md:border-r border-[var(--bdae-border)] md:pr-4">
            <span className="text-[10px] font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
              Current Financial Business Date
            </span>
            <div className="flex items-center gap-2.5">
              <Calendar className="w-5 h-5 text-[var(--bdae-primary)]" />
              <span className="text-2xl font-black font-mono text-[var(--bdae-text-primary)]">
                {eodStatus?.currentBusinessDate || '---'}
              </span>
            </div>
            <div className="flex items-center gap-2 pt-1">
              {eodStatus?.isMonthEnd ? (
                <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/15 text-amber-600 dark:text-amber-400">
                  Month-End Capitalization Active
                </span>
              ) : (
                <span className="px-2 py-0.5 rounded-full text-[10px] font-medium bg-black/5 dark:bg-white/5 text-[var(--bdae-text-secondary)]">
                  Standard Daily Cycle
                </span>
              )}
            </div>
          </div>

          {/* Operational Status */}
          <div className="space-y-1 md:border-r border-[var(--bdae-border)] md:pr-4">
            <span className="text-[10px] font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
              Daytime Transaction Gate
            </span>
            <div className="flex items-center gap-2">
              {eodStatus?.status === 'OPEN' && (
                <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border border-emerald-500/20">
                  <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
                  OPEN (Postings Permitted)
                </span>
              )}
              {eodStatus?.status === 'CUTOFF_LOCKED' && (
                <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-amber-500/15 text-amber-600 dark:text-amber-400 border border-amber-500/20">
                  <ShieldAlert className="w-3.5 h-3.5" />
                  CUTOFF LOCKED
                </span>
              )}
              {eodStatus?.status === 'PROCESSING_EOD' && (
                <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-blue-500/15 text-blue-600 dark:text-blue-400 border border-blue-500/20">
                  <RotateCw className="w-3.5 h-3.5 animate-spin" />
                  PROCESSING BATCH
                </span>
              )}
            </div>
            <p className="text-[10px] text-[var(--bdae-text-secondary)]">
              {eodStatus?.status === 'OPEN'
                ? 'Member deposits, withdrawals, and transfers active.'
                : 'Daytime transactions temporarily locked during batch window.'}
            </p>
          </div>

          {/* Last Completed EOD */}
          <div className="space-y-1 md:border-r border-[var(--bdae-border)] md:pr-4">
            <span className="text-[10px] font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
              Last Completed Batch
            </span>
            <div className="flex items-center gap-2 text-xs font-semibold text-[var(--bdae-text-primary)]">
              <Clock className="w-4 h-4 text-[var(--bdae-text-secondary)]" />
              <span>{formatDate(eodStatus?.lastEodCompletedAt)}</span>
            </div>
            <p className="text-[10px] text-[var(--bdae-text-secondary)]">
              Status: <span className="font-bold uppercase">{eodStatus?.lastBatchStatus || 'N/A'}</span>
            </p>
          </div>

          {/* Automated Midnight Schedule */}
          <div className="space-y-1">
            <span className="text-[10px] font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
              Scheduled Automation
            </span>
            <div className="flex items-center gap-2 text-xs font-bold text-[var(--bdae-text-primary)]">
              <Sun className="w-4 h-4 text-amber-500" />
              <span>Midnight Cron (00:00:00)</span>
            </div>
            <p className="text-[10px] text-[var(--bdae-text-secondary)]">
              Automatically triggers daily actual/365 accruals and rolls business date.
            </p>
          </div>
        </div>
      </div>

      {/* ── 5-Stage Core Banking Batch Stepper Overview ── */}
      <div className="bdae-card p-6 rounded-2xl border border-[var(--bdae-border)] space-y-4">
        <h2 className="text-sm font-bold text-[var(--bdae-text-primary)] uppercase tracking-wider flex items-center gap-2">
          <Layers className="w-4 h-4 text-[var(--bdae-primary)]" />
          <span>EOD Operational Processing Pipeline</span>
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-5 gap-3">
          {[
            {
              step: '1',
              title: 'Cutoff Lock',
              desc: 'Locks OTC cash desks & daytime API transactions',
              icon: ShieldCheck
            },
            {
              step: '2',
              title: 'Savings Accrual',
              desc: 'Actual/365 daily yield calculation & monthly capitalization',
              icon: TrendingUp
            },
            {
              step: '3',
              title: 'Dormancy Sweep',
              desc: 'Identifies accounts inactive for >180 days',
              icon: UserX
            },
            {
              step: '4',
              title: 'PAR Delinquency',
              desc: 'Computes loan DPD & classifies PAR 1-90+ buckets',
              icon: AlertTriangle
            },
            {
              step: '5',
              title: 'Date Rollover',
              desc: 'Advances financial business date to T+1 & re-opens gate',
              icon: Calendar
            }
          ].map((item) => (
            <div
              key={item.step}
              className="p-3.5 rounded-xl border border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 space-y-2 relative"
            >
              <div className="flex items-center justify-between">
                <span className="w-6 h-6 rounded-full bg-[var(--bdae-primary)]/15 text-[var(--bdae-primary)] font-bold text-xs flex items-center justify-center">
                  {item.step}
                </span>
                <item.icon className="w-4 h-4 text-[var(--bdae-text-secondary)]" />
              </div>
              <h3 className="text-xs font-bold text-[var(--bdae-text-primary)]">{item.title}</h3>
              <p className="text-[11px] text-[var(--bdae-text-secondary)] leading-relaxed">
                {item.desc}
              </p>
            </div>
          ))}
        </div>
      </div>

      {/* ── EOD Batch History Table ── */}
      <div className="bdae-card p-6 rounded-2xl border border-[var(--bdae-border)] space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-bold text-[var(--bdae-text-primary)] uppercase tracking-wider flex items-center gap-2">
            <History className="w-4 h-4 text-[var(--bdae-primary)]" />
            <span>Historical EOD Batch Executions</span>
          </h2>
          <span className="text-xs text-[var(--bdae-text-secondary)]">
            Total Runs: <span className="font-bold text-[var(--bdae-text-primary)]">{batchHistory.length}</span>
          </span>
        </div>

        {batchHistory.length === 0 ? (
          <div className="text-center py-12 text-xs text-[var(--bdae-text-secondary)] space-y-2">
            <Database className="w-8 h-8 mx-auto opacity-40 text-[var(--bdae-text-secondary)]" />
            <p>No historical EOD batch runs recorded yet for this SACCO tenant.</p>
            <p className="text-[11px] opacity-75">Click "Run End-of-Day Batch" to execute the first operational cycle.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="border-b border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] uppercase text-[10px] font-bold tracking-wider">
                  <th className="py-3 px-3">Business Date</th>
                  <th className="py-3 px-3">Status</th>
                  <th className="py-3 px-3">Trigger</th>
                  <th className="py-3 px-3">Savings Accrued</th>
                  <th className="py-3 px-3">Interest Yield</th>
                  <th className="py-3 px-3">Loans Evaluated</th>
                  <th className="py-3 px-3">Dormant Accounts</th>
                  <th className="py-3 px-3">Started At</th>
                  <th className="py-3 px-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[var(--bdae-border)]">
                {batchHistory.map((batch) => (
                  <tr
                    key={batch.batchId}
                    className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
                  >
                    <td className="py-3 px-3 font-mono font-bold text-[var(--bdae-text-primary)]">
                      {batch.businessDate}
                    </td>
                    <td className="py-3 px-3">
                      {batch.status === 'COMPLETED' ? (
                        <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/15 text-emerald-600 dark:text-emerald-400">
                          COMPLETED
                        </span>
                      ) : batch.status === 'IN_PROGRESS' ? (
                        <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-blue-500/15 text-blue-600 dark:text-blue-400">
                          IN PROGRESS
                        </span>
                      ) : (
                        <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-red-500/15 text-red-600 dark:text-red-400">
                          FAILED
                        </span>
                      )}
                    </td>
                    <td className="py-3 px-3 font-semibold text-[var(--bdae-text-secondary)]">
                      {batch.triggeredBy}
                    </td>
                    <td className="py-3 px-3 text-[var(--bdae-text-primary)] font-semibold">
                      {batch.totalAccountsAccrued} accounts
                    </td>
                    <td className="py-3 px-3 font-mono text-[var(--bdae-primary)] font-bold">
                      {formatCurrency(batch.totalInterestAccrued)}
                    </td>
                    <td className="py-3 px-3 text-[var(--bdae-text-primary)]">
                      {batch.totalLoansEvaluated} loans
                    </td>
                    <td className="py-3 px-3 text-[var(--bdae-text-secondary)]">
                      {batch.totalAccountsDormant} flagged
                    </td>
                    <td className="py-3 px-3 text-[var(--bdae-text-secondary)]">
                      {formatDate(batch.startedAt)}
                    </td>
                    <td className="py-3 px-3 text-right">
                      <button
                        onClick={() => handleViewBatchDetails(batch.batchId)}
                        className="px-2.5 py-1 text-[11px] font-bold rounded-lg border border-[var(--bdae-border)] hover:bg-[var(--bdae-primary)] hover:text-white transition-all"
                      >
                        Inspect Steps
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* ── Step-by-Step Batch Details Modal ── */}
      {selectedBatch && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
          <div className="bdae-card max-w-2xl w-full p-6 rounded-2xl border border-[var(--bdae-border)] shadow-2xl space-y-4 max-h-[85vh] overflow-y-auto">
            <div className="flex items-center justify-between pb-3 border-b border-[var(--bdae-border)]">
              <div>
                <h3 className="text-base font-black text-[var(--bdae-text-primary)]">
                  Batch Pipeline Execution Audit
                </h3>
                <p className="text-xs text-[var(--bdae-text-secondary)]">
                  Business Date: <span className="font-bold text-[var(--bdae-primary)]">{selectedBatch.businessDate}</span> • Status: <span className="font-bold uppercase">{selectedBatch.status}</span>
                </p>
              </div>
              <button
                onClick={() => setSelectedBatch(null)}
                className="p-1 rounded-lg text-[var(--bdae-text-secondary)] hover:bg-black/5 dark:hover:bg-white/5"
              >
                ✕
              </button>
            </div>

            {selectedBatch.summaryNotes && (
              <div className="p-3 rounded-xl bg-black/5 dark:bg-white/5 text-xs text-[var(--bdae-text-secondary)] border border-[var(--bdae-border)]">
                <span className="font-bold text-[var(--bdae-text-primary)]">Summary:</span> {selectedBatch.summaryNotes}
              </div>
            )}

            <div className="space-y-2">
              <h4 className="text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
                Granular Pipeline Steps
              </h4>
              {selectedBatch.steps && selectedBatch.steps.length > 0 ? (
                <div className="space-y-2">
                  {selectedBatch.steps.map((step, idx) => (
                    <div
                      key={step.stepId || idx}
                      className="p-3 rounded-xl border border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 flex items-center justify-between gap-4"
                    >
                      <div className="flex items-center gap-3">
                        {step.status === 'SUCCESS' ? (
                          <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0" />
                        ) : (
                          <XCircle className="w-4 h-4 text-red-500 shrink-0" />
                        )}
                        <div>
                          <p className="text-xs font-bold text-[var(--bdae-text-primary)]">
                            {step.stepName}
                          </p>
                          <p className="text-[11px] text-[var(--bdae-text-secondary)]">
                            Records Affected: <span className="font-bold">{step.recordsAffected}</span>
                          </p>
                        </div>
                      </div>

                      <div className="text-right">
                        <span className="text-xs font-mono font-bold text-[var(--bdae-text-primary)]">
                          {step.durationMs} ms
                        </span>
                        <p className="text-[10px] text-[var(--bdae-text-secondary)]">
                          {formatDate(step.createdAt)}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-xs text-[var(--bdae-text-secondary)] italic">
                  No step audit records found for this batch.
                </p>
              )}
            </div>

            <div className="pt-3 border-t border-[var(--bdae-border)] flex justify-end">
              <button
                onClick={() => setSelectedBatch(null)}
                className="px-4 py-2 rounded-xl text-xs font-bold bg-[var(--bdae-primary)] text-white hover:opacity-90"
              >
                Close Audit View
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Confirmation Modal ── */}
      {isConfirmModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
          <div className="bdae-card max-w-md w-full p-6 rounded-2xl border border-[var(--bdae-border)] shadow-2xl space-y-4">
            <div className="flex items-center gap-3 text-amber-500">
              <AlertTriangle className="w-6 h-6 shrink-0" />
              <h3 className="text-base font-black text-[var(--bdae-text-primary)]">
                Confirm Manual EOD Batch Execution
              </h3>
            </div>

            <p className="text-xs text-[var(--bdae-text-secondary)] leading-relaxed">
              You are about to initiate the End-of-Day batch for financial business date{' '}
              <span className="font-bold font-mono text-[var(--bdae-text-primary)]">
                {eodStatus?.currentBusinessDate}
              </span>
              . This process will:
            </p>

            <ul className="text-xs text-[var(--bdae-text-secondary)] space-y-1.5 list-disc list-inside">
              <li>Engage the transaction lock preventing daytime postings</li>
              <li>Accrue actual/365 daily savings interest on all active accounts</li>
              <li>Flag accounts inactive over 180 days as DORMANT</li>
              <li>Recalculate loan DPD, PAR buckets, and loan loss reserves</li>
              <li>Roll the financial business date to the next operational date</li>
            </ul>

            <div className="pt-3 border-t border-[var(--bdae-border)] flex items-center justify-end gap-2">
              <button
                onClick={() => setIsConfirmModalOpen(false)}
                className="px-4 py-2 rounded-xl text-xs font-semibold border border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5 text-[var(--bdae-text-primary)]"
              >
                Cancel
              </button>
              <button
                onClick={handleRunEodBatch}
                className="px-4 py-2 rounded-xl text-xs font-bold bg-[var(--bdae-primary)] text-white hover:opacity-90 shadow-md shadow-[var(--bdae-primary)]/20"
              >
                Confirm & Run EOD
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
