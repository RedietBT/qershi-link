import React, { useState } from 'react';
import {
  FileText,
  Plus,
  Search,
  Filter,
  CheckCircle2,
  XCircle,
  Clock,
  BadgePercent,
  Layers,
  Loader2,
  AlertTriangle,
  FolderOpen
} from 'lucide-react';
import { loanOriginationApi } from '../api/loanOriginationApi';
import { formatCurrency, formatDateTime } from '../../../../common/utils/currency';
import { LoanApplicationModal } from '../components/LoanApplicationModal';
import { UnderwritingDecisionModal } from '../components/UnderwritingDecisionModal';

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
    // Refresh search
    if (userIdSearch) handleSearch();
  };

  const filteredApps = applications.filter((app) => {
    if (statusFilter === 'ALL') return true;
    return app.status === statusFilter;
  });

  const getStatusBadge = (status) => {
    switch (status) {
      case 'APPROVED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-600 dark:text-emerald-400">
            <CheckCircle2 className="w-3 h-3" />
            Approved
          </span>
        );
      case 'DISBURSED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-purple-500/10 text-purple-600 dark:text-purple-400">
            <CheckCircle2 className="w-3 h-3" />
            Disbursed
          </span>
        );
      case 'REJECTED':
      case 'REJECTED_ELIGIBILITY':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-red-500/10 text-red-600 dark:text-red-400">
            <XCircle className="w-3 h-3" />
            Rejected
          </span>
        );
      case 'SUBMITTED':
      case 'UNDER_REVIEW':
      default:
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/10 text-amber-600 dark:text-amber-400">
            <Clock className="w-3 h-3" />
            {status || 'Pending Review'}
          </span>
        );
    }
  };

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

        <button
          type="button"
          onClick={() => setIsApplyModalOpen(true)}
          className="px-4 py-2.5 bg-[var(--bdae-primary)] text-white hover:opacity-90 rounded-xl text-xs font-bold shadow-md transition-all flex items-center gap-2 self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" />
          Originate Loan Application
        </button>
      </div>

      {/* Member Lookup Bar */}
      <div className="bdae-card p-5 border border-[var(--bdae-border)] space-y-4">
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

      {/* Applications Table */}
      <div className="bdae-card border border-[var(--bdae-border)] overflow-hidden shadow-sm">
        <div className="p-4 border-b border-[var(--bdae-border)] flex items-center justify-between">
          <h2 className="text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
            Loan Applications {searched && `(${filteredApps.length} records)`}
          </h2>
        </div>

        {loading ? (
          <div className="flex flex-col items-center justify-center py-16 space-y-3">
            <Loader2 className="w-8 h-8 animate-spin text-[var(--bdae-primary)]" />
            <p className="text-xs text-[var(--bdae-text-secondary)] font-medium">
              Retrieving loan records from origination service...
            </p>
          </div>
        ) : !searched && applications.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 space-y-2 text-center">
            <FolderOpen className="w-10 h-10 text-[var(--bdae-text-secondary)]/50" />
            <p className="text-xs font-bold text-[var(--bdae-text-primary)]">
              No Member Applications Queried
            </p>
            <p className="text-[11px] text-[var(--bdae-text-secondary)] max-w-sm">
              Search by Borrower User ID above or click "Originate Loan Application" to submit a new credit request.
            </p>
          </div>
        ) : filteredApps.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 space-y-2 text-center">
            <FileText className="w-10 h-10 text-[var(--bdae-text-secondary)]/50" />
            <p className="text-xs font-bold text-[var(--bdae-text-primary)]">
              No Loan Applications Found
            </p>
            <p className="text-[11px] text-[var(--bdae-text-secondary)]">
              No loan applications found matching the selected criteria.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="bg-black/5 dark:bg-white/5 border-b border-[var(--bdae-border)] text-[var(--bdae-text-secondary)]">
                  <th className="py-3 px-4 font-semibold">Application Ref</th>
                  <th className="py-3 px-4 font-semibold">Model</th>
                  <th className="py-3 px-4 font-semibold text-right">Requested (ETB)</th>
                  <th className="py-3 px-4 font-semibold text-center">Credit Score</th>
                  <th className="py-3 px-4 font-semibold">Status</th>
                  <th className="py-3 px-4 font-semibold">Date</th>
                  <th className="py-3 px-4 font-semibold text-center">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[var(--bdae-border)]">
                {filteredApps.map((app) => (
                  <tr key={app.applicationId} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                    <td className="py-3 px-4 font-mono font-bold text-[var(--bdae-text-primary)]">
                      {app.applicationNo || app.applicationId.slice(0, 13)}...
                    </td>
                    <td className="py-3 px-4 text-[var(--bdae-text-secondary)] font-medium">
                      {app.scoringType || 'INDIVIDUAL'}
                    </td>
                    <td className="py-3 px-4 text-right font-mono font-black text-[var(--bdae-primary)]">
                      {formatCurrency(app.amountRequested)}
                    </td>
                    <td className="py-3 px-4 text-center">
                      <span className="font-mono font-bold text-xs bg-purple-500/10 text-purple-600 dark:text-purple-400 px-2 py-0.5 rounded-md">
                        {app.creditScoring?.calculatedScore ?? '—'} / 100
                      </span>
                    </td>
                    <td className="py-3 px-4">
                      {getStatusBadge(app.status)}
                    </td>
                    <td className="py-3 px-4 text-[var(--bdae-text-secondary)]">
                      {formatDateTime(app.createdAt)}
                    </td>
                    <td className="py-3 px-4 text-center">
                      <button
                        type="button"
                        onClick={() => {
                          setSelectedApp(app);
                          setIsDecisionModalOpen(true);
                        }}
                        className="px-3 py-1 rounded-lg bg-[var(--bdae-primary)]/10 text-[var(--bdae-primary)] hover:bg-[var(--bdae-primary)] hover:text-white font-bold text-[11px] transition-all"
                      >
                        Inspect / Review
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

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
