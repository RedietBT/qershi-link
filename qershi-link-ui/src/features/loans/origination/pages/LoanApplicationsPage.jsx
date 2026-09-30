import React, { useState } from 'react';
import {
  FileText,
  Plus,
  Search,
  Filter,
  Loader2,
  AlertTriangle
} from 'lucide-react';
import { loanOriginationApi } from '../api/loanOriginationApi';
import { LoanApplicationModal } from '../components/LoanApplicationModal';
import { UnderwritingDecisionModal } from '../components/UnderwritingDecisionModal';
import { LoanApplicationsMetrics } from '../components/LoanApplicationsMetrics';
import { LoanApplicationsTable } from '../components/LoanApplicationsTable';
import { PermissionGuard } from '../../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../../common/constants/permissions';

export const LoanApplicationsPage = () => {
  const [userIdSearch, setUserIdSearch] = useState('');
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [error, setError] = useState(null);

  // Status Filter
  const [statusFilter, setStatusFilter] = useState('ALL');

  // Modals
  const [isApplyModalOpen, setIsApplyModalOpen] = useState(false);
  const [selectedApp, setSelectedApp] = useState(null);
  const [isDecisionModalOpen, setIsDecisionModalOpen] = useState(false);

  const handleSearch = async (e) => {
    if (e) e.preventDefault();
    const query = userIdSearch.trim();
    if (!query) return;

    try {
      setLoading(true);
      setError(null);
      setSearched(true);
      const res = await loanOriginationApi.listApplicationsForUser(query);
      const data = res.data || res || [];
      setApplications(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Failed to list applications for user:', err);
      setError(err?.response?.data?.message || 'Could not retrieve applications for this member.');
      setApplications([]);
    } finally {
      setLoading(false);
    }
  };

  const handleApplicationCreated = (newApp) => {
    if (newApp) {
      setApplications((prev) => [newApp, ...prev]);
    }
  };

  const handleDecisionSuccess = () => {
    if (userIdSearch) handleSearch();
  };

  const filteredApps = applications.filter((app) => {
    if (statusFilter === 'ALL') return true;
    return app.status === statusFilter;
  });

  return (
    <div className="space-y-6 animate-fadeIn max-w-6xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-[var(--bdae-border)] pb-4">
        <div>
          <h1 className="text-xl font-black text-[var(--bdae-text-primary)] flex items-center gap-2.5">
            <FileText className="w-6 h-6 text-[var(--bdae-primary)]" />
            Loan Origination & Intake Portfolio
          </h1>
          <p className="text-xs text-[var(--bdae-text-secondary)] mt-0.5">
            Register credit requests, calculate multi-factor eligibility scores, and track underwriting workflows.
          </p>
        </div>

        {/* Action Button strictly guarded by LOAN_APPLICATION_CREATE */}
        <PermissionGuard permissions={[PERMISSIONS.LOAN_APPLICATION_CREATE]}>
          <button
            type="button"
            onClick={() => setIsApplyModalOpen(true)}
            className="px-4 py-2.5 bg-[var(--bdae-primary)] text-white hover:opacity-90 rounded-xl text-xs font-bold shadow-md transition-all flex items-center gap-2 self-start sm:self-auto"
          >
            <Plus className="w-4 h-4" />
            Originate Loan Application
          </button>
        </PermissionGuard>
      </div>

      {/* KPI Metrics */}
      {applications.length > 0 && (
        <LoanApplicationsMetrics applications={applications} />
      )}

      {/* Member Lookup Bar */}
      <div className="bdae-card p-5 border border-[var(--bdae-border)] space-y-4 rounded-2xl">
        <form onSubmit={handleSearch} className="flex flex-col sm:flex-row gap-3">
          <div className="relative flex-1">
            <input
              type="text"
              placeholder="Search member loan requests by Borrower User ID..."
              value={userIdSearch}
              onChange={(e) => setUserIdSearch(e.target.value)}
              className="bdae-input font-mono text-xs pl-10"
            />
            <Search className="w-4 h-4 text-[var(--bdae-text-secondary)] absolute left-3.5 top-3" />
          </div>

          <button
            type="submit"
            disabled={loading || !userIdSearch.trim()}
            className="px-5 py-2.5 bg-[var(--bdae-primary)] text-white rounded-xl text-xs font-bold flex items-center justify-center gap-2 hover:opacity-90 disabled:opacity-50 shadow-md transition-all shrink-0"
          >
            {loading ? <Loader2 className="w-4 h-4 animate-spin" /> : <Search className="w-4 h-4" />}
            Query Applications
          </button>
        </form>

        {/* Status Filter */}
        <div className="flex flex-wrap items-center gap-2 pt-2 border-t border-[var(--bdae-border)] text-xs">
          <span className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)] flex items-center gap-1 mr-1">
            <Filter className="w-3 h-3" /> Status:
          </span>
          {['ALL', 'SUBMITTED', 'APPROVED', 'DISBURSED', 'REJECTED'].map((st) => (
            <button
              key={st}
              type="button"
              onClick={() => setStatusFilter(st)}
              className={`px-3 py-1 rounded-lg text-[11px] font-bold transition-all ${
                statusFilter === st
                  ? 'bg-[var(--bdae-primary)] text-white shadow-sm'
                  : 'bg-black/5 dark:bg-white/5 text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]'
              }`}
            >
              {st}
            </button>
          ))}
        </div>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 flex items-start space-x-3 text-xs">
          <AlertTriangle className="w-5 h-5 shrink-0 mt-0.5" />
          <div>
            <p className="font-semibold">Query Alert</p>
            <p className="mt-0.5">{error}</p>
          </div>
        </div>
      )}

      {/* Modular Applications Table */}
      <LoanApplicationsTable
        applications={filteredApps}
        loading={loading}
        searched={searched}
        onSelectApplication={(app) => {
          setSelectedApp(app);
          setIsDecisionModalOpen(true);
        }}
      />

      {/* Originate Modal */}
      <LoanApplicationModal
        isOpen={isApplyModalOpen}
        onClose={() => setIsApplyModalOpen(false)}
        onSuccess={handleApplicationCreated}
      />

      {/* Underwriting Decision Modal */}
      <UnderwritingDecisionModal
        isOpen={isDecisionModalOpen}
        application={selectedApp}
        onClose={() => setIsDecisionModalOpen(false)}
        onSuccess={handleDecisionSuccess}
      />
    </div>
  );
};
