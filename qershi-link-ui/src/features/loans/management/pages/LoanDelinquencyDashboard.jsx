import React, { useState, useEffect } from 'react';
import {
  AlertTriangle,
  RefreshCw,
  RotateCw,
  CheckCircle2
} from 'lucide-react';
import { loanMgmtHttpClient } from '../../../../common/api/httpClient';
import { useAuthStore } from '../../../../common/store/useAuthStore';
import { PERMISSIONS } from '../../../../common/constants/permissions';
import { PermissionGuard } from '../../../../common/components/PermissionGuard';
import { DelinquencyMetricsDeck } from '../components/DelinquencyMetricsDeck';
import { DelinquencyAgingBuckets } from '../components/DelinquencyAgingBuckets';
import { DelinquencyLoanTable } from '../components/DelinquencyLoanTable';
import { Ifrs9ComplianceCard } from '../components/Ifrs9ComplianceCard';

export const LoanDelinquencyDashboard = () => {
  const [summary, setSummary] = useState(null);
  const [loans, setLoans] = useState([]);
  const [selectedBucket, setSelectedBucket] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isEvaluating, setIsEvaluating] = useState(false);
  const [error, setError] = useState(null);
  const [actionSuccess, setActionSuccess] = useState(null);

  const user = useAuthStore((state) => state.user);
  const userPermissions = user?.permissions || [];
  const canEvaluate =
    user?.globalRole === 'SUPER_ADMIN' ||
    userPermissions.includes(PERMISSIONS.LOAN_APPLICATION_APPROVE) ||
    userPermissions.includes(PERMISSIONS.LOAN_DELINQUENCY_VIEW);

  const fetchData = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const summaryRes = await loanMgmtHttpClient.get('/loans/delinquency/summary');
      setSummary(summaryRes.data);

      const loansRes = await loanMgmtHttpClient.get('/loans/delinquency/loans');
      setLoans(Array.isArray(loansRes.data) ? loansRes.data : []);
    } catch (err) {
      console.error('Failed to fetch delinquency data', err);
      setError(
        err.response?.data?.message ||
          'Failed to load Portfolio-at-Risk delinquency summary from core banking service.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleTriggerEvaluation = async () => {
    if (isEvaluating) return;
    setIsEvaluating(true);
    setError(null);
    setActionSuccess(null);
    try {
      const res = await loanMgmtHttpClient.post('/loans/delinquency/evaluate');
      setActionSuccess(res.data?.message || 'Delinquency evaluation completed successfully.');
      await fetchData();
    } catch (err) {
      console.error('Failed to trigger delinquency evaluation', err);
      setError(
        err.response?.data?.message ||
          'Automated delinquency evaluation failed. Check audit logs.'
      );
    } finally {
      setIsEvaluating(false);
    }
  };

  return (
    <div className="space-y-6 animate-fadeIn pb-12">
      {/* ── Page Header ── */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
            <AlertTriangle className="w-4 h-4 text-amber-500" />
            <span>Credit & Risk Governance</span>
          </div>
          <h1 className="text-2xl font-black tracking-tight text-[var(--bdae-text-primary)]">
            Portfolio at Risk (PAR) & Delinquency Aging
          </h1>
          <p className="text-xs text-[var(--bdae-text-secondary)]">
            Inspect Days Past Due (DPD), non-performing loans (NPL), and statutory loan loss provisioning reserves.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={fetchData}
            disabled={isLoading || isEvaluating}
            className="px-3 py-2 rounded-xl text-xs font-semibold border border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5 text-[var(--bdae-text-primary)] flex items-center gap-1.5 transition-all"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />
            <span>Refresh</span>
          </button>

          {/* Action button strictly guarded by LOAN_DELINQUENCY_VIEW or LOAN_APPLICATION_APPROVE */}
          {canEvaluate && (
            <PermissionGuard
              permissions={[
                PERMISSIONS.LOAN_DELINQUENCY_VIEW,
                PERMISSIONS.LOAN_APPLICATION_APPROVE
              ]}
            >
              <button
                onClick={handleTriggerEvaluation}
                disabled={isEvaluating}
                className="px-4 py-2 rounded-xl text-xs font-bold bg-[var(--bdae-primary)] hover:opacity-90 text-white flex items-center gap-2 shadow-lg shadow-[var(--bdae-primary)]/20 transition-all disabled:opacity-50"
              >
                <RotateCw className={`w-3.5 h-3.5 ${isEvaluating ? 'animate-spin' : ''}`} />
                <span>{isEvaluating ? 'Evaluating Aging...' : 'Recalculate PAR Aging'}</span>
              </button>
            </PermissionGuard>
          )}
        </div>
      </div>

      {/* ── Feedback Alerts ── */}
      {error && (
        <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 text-xs flex items-center gap-2.5">
          <AlertTriangle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {actionSuccess && (
        <div className="p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-600 dark:text-emerald-400 text-xs flex items-center gap-2.5 animate-fadeIn">
          <CheckCircle2 className="w-4 h-4 shrink-0" />
          <span>{actionSuccess}</span>
        </div>
      )}

      {/* ── 1. Modular High-Level Portfolio at Risk Metric Cards ── */}
      <DelinquencyMetricsDeck summary={summary} />

      {/* ── 2. Modular Aging Breakdown Buckets ── */}
      <DelinquencyAgingBuckets
        bucketSummaries={summary?.bucketSummaries || []}
        selectedBucket={selectedBucket}
        onSelectBucket={setSelectedBucket}
      />

      {/* ── 3. Modular Delinquent Portfolio Ledger ── */}
      <DelinquencyLoanTable
        loans={loans}
        isLoading={isLoading}
        searchQuery={searchQuery}
        setSearchQuery={setSearchQuery}
        selectedBucket={selectedBucket}
        setSelectedBucket={setSelectedBucket}
      />

      {/* ── 4. IFRS 9 / NBE Regulatory Compliance Card ── */}
      <Ifrs9ComplianceCard canTrigger={canEvaluate} />
    </div>
  );
};
