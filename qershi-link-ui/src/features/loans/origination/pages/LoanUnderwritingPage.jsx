import React, { useState } from 'react';
import {
  ClipboardCheck,
  Search,
  CheckCircle2,
  XCircle,
  Clock,
  AlertTriangle,
  BadgePercent,
  Layers,
  Loader2,
  ShieldAlert,
  ArrowRight
} from 'lucide-react';
import { loanOriginationApi } from '../api/loanOriginationApi';
import { useAuthStore } from '../../../../common/store/useAuthStore';
import { formatCurrency, formatDateTime } from '../../../../common/utils/currency';
import { UnderwritingDecisionModal } from '../components/UnderwritingDecisionModal';

export const LoanUnderwritingPage = () => {
  const currentUser = useAuthStore((state) => state.user);

  const [searchId, setSearchId] = useState('');
  const [application, setApplication] = useState(null);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [error, setError] = useState(null);

  // Decision Modal
  const [selectedApp, setSelectedApp] = useState(null);
  const [isDecisionModalOpen, setIsDecisionModalOpen] = useState(false);

  const handleLookup = async (e) => {
    if (e) e.preventDefault();
    const query = searchId.trim();
    if (!query) return;

    try {
      setLoading(true);
      setError(null);
      setSearched(true);
      setApplication(null);

      // Try application ID lookup first
      const res = await loanOriginationApi.getApplicationById(query);
      const data = res.data || res;
      setApplication(data);
    } catch (err) {
      console.warn('ID lookup failed, trying user applications list...', err);
      // Fallback: try user applications query
      try {
        const userRes = await loanOriginationApi.listApplicationsForUser(query);
        const list = userRes.data || userRes || [];
        if (Array.isArray(list) && list.length > 0) {
          setApplication(list[0]);
        } else {
          setError('No loan applications found matching this Application UUID or Borrower ID.');
        }
      } catch (userErr) {
        console.error('Lookup error:', userErr);
        setError('Loan application not found. Please verify the Application UUID.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleDecisionSuccess = () => {
    if (searchId) handleLookup();
  };

  const isApplicant =
    application &&
    (currentUser?.userId === application.userId || currentUser?.id === application.userId);

  return (
    <div className="space-y-6 animate-fadeIn max-w-5xl mx-auto">
      {/* Title */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-[var(--bdae-border)] pb-4">
        <div>
          <h1 className="text-xl font-black text-[var(--bdae-text-primary)] flex items-center gap-2.5">
            <ClipboardCheck className="w-6 h-6 text-purple-600" />
            Maker-Checker Underwriting Queue
          </h1>
          <p className="text-xs text-[var(--bdae-text-secondary)] mt-0.5">
            Execute dual-control credit underwriting assessments with multi-factor risk scoring and self-approval guards.
          </p>
        </div>

        <div className="flex items-center gap-2 text-xs bg-purple-500/10 text-purple-700 dark:text-purple-300 px-3 py-1.5 rounded-xl border border-purple-500/20 font-bold">
          <ShieldAlert className="w-4 h-4 shrink-0" />
          <span>Four-Eye Separation Active</span>
        </div>
      </div>

      {/* Lookup Card */}
      <div className="bdae-card p-5 border border-[var(--bdae-border)] space-y-4">
        <h2 className="text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
          Inquire Loan Application for Underwriting Decision
        </h2>

        <form onSubmit={handleLookup} className="flex flex-col sm:flex-row gap-3">
          <div className="relative flex-1">
            <input
              type="text"
              placeholder="Enter Loan Application UUID or Borrower User ID..."
              value={searchId}
              onChange={(e) => setSearchId(e.target.value)}
              className="bdae-input font-mono text-xs pl-10 font-bold"
            />
            <Search className="w-4 h-4 text-[var(--bdae-text-secondary)] absolute left-3.5 top-3" />
          </div>

          <button
            type="submit"
            disabled={loading || !searchId.trim()}
            className="px-5 py-2.5 bg-purple-600 text-white rounded-xl text-xs font-bold flex items-center justify-center gap-2 hover:bg-purple-700 disabled:opacity-50 shadow-md transition-all shrink-0"
          >
            {loading ? <Loader2 className="w-4 h-4 animate-spin" /> : <Search className="w-4 h-4" />}
            Inquire Application
          </button>
        </form>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 flex items-start space-x-3 text-xs">
          <AlertTriangle className="w-5 h-5 shrink-0 mt-0.5" />
          <div>
            <p className="font-semibold">Underwriting Query Alert</p>
            <p className="mt-0.5">{error}</p>
          </div>
        </div>
      )}

      {/* Application Underwriting Inspection Dossier */}
      {application && (
        <div className="bdae-card p-6 border border-[var(--bdae-border)] space-y-6 shadow-md animate-fadeIn">
          {/* Header */}
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-[var(--bdae-border)] pb-4">
            <div>
              <span className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)]">
                Credit Application Dossier
              </span>
              <h2 className="text-lg font-black text-[var(--bdae-text-primary)] font-mono">
                {application.applicationNo || application.applicationId}
              </h2>
              <p className="text-xs text-[var(--bdae-text-secondary)] mt-0.5">
                Submitted on: {formatDateTime(application.createdAt)}
              </p>
            </div>

            <div className="flex items-center gap-2">
              <span className="px-3 py-1 rounded-full text-xs font-bold bg-amber-500/10 text-amber-600 dark:text-amber-400">
                {application.status}
              </span>
            </div>
          </div>

          {/* Self-Approval Warning */}
          {isApplicant && (
            <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 flex items-start space-x-3 text-xs">
              <ShieldAlert className="w-5 h-5 shrink-0 mt-0.5" />
              <div>
                <p className="font-bold">Maker-Checker Dual Control Guard Triggered</p>
                <p className="mt-0.5 opacity-90">
                  You are the borrower for this application. Core banking rules strictly prevent self-approval. Another loan officer or supervisor must underwrite this loan.
                </p>
              </div>
            </div>
          )}

          {/* Financial Breakdown Grid */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 p-4 rounded-xl bg-black/5 dark:bg-white/5 text-xs">
            <div>
              <span className="text-[10px] text-[var(--bdae-text-secondary)] uppercase font-bold block">
                Requested Principal
              </span>
              <span className="font-mono text-base font-black text-[var(--bdae-primary)]">
                {formatCurrency(application.amountRequested)}
              </span>
            </div>
            <div>
              <span className="text-[10px] text-[var(--bdae-text-secondary)] uppercase font-bold block">
                Approved Principal
              </span>
              <span className="font-mono text-base font-black text-emerald-600 dark:text-emerald-400">
                {application.amountApproved ? formatCurrency(application.amountApproved) : 'Pending Review'}
              </span>
            </div>
            <div>
              <span className="text-[10px] text-[var(--bdae-text-secondary)] uppercase font-bold block">
                Borrower User ID
              </span>
              <span className="font-mono font-medium text-[var(--bdae-text-primary)] truncate block">
                {application.userId}
              </span>
            </div>
            <div>
              <span className="text-[10px] text-[var(--bdae-text-secondary)] uppercase font-bold block">
                Scoring Model
              </span>
              <span className="font-bold text-[var(--bdae-text-primary)]">
                {application.scoringType || 'INDIVIDUAL'}
              </span>
            </div>
          </div>

          {/* Credit Scoring Analysis */}
          {application.creditScoring && (
            <div className="p-4 rounded-xl border border-[var(--bdae-border)] space-y-3">
              <div className="flex items-center justify-between">
                <h3 className="text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-primary)] flex items-center gap-1.5">
                  <BadgePercent className="w-4 h-4 text-purple-600" />
                  Credit Scoring Analysis
                </h3>
                <span
                  className={`px-2.5 py-0.5 rounded-full text-[10px] font-bold ${
                    application.creditScoring.passedEligibility
                      ? 'bg-emerald-500/10 text-emerald-600'
                      : 'bg-red-500/10 text-red-600'
                  }`}
                >
                  {application.creditScoring.passedEligibility ? 'Eligibility Passed' : 'Eligibility Failed'}
                </span>
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
                <div>
                  <span className="text-[10px] text-[var(--bdae-text-secondary)] block">Risk Score</span>
                  <span className="font-mono font-black text-sm text-purple-600">
                    {application.creditScoring.calculatedScore ?? '—'} / 100
                  </span>
                </div>
                <div>
                  <span className="text-[10px] text-[var(--bdae-text-secondary)] block">Savings Consistency</span>
                  <span className="font-mono font-bold text-[var(--bdae-text-primary)]">
                    {application.creditScoring.savingsConsistency ?? '—'}%
                  </span>
                </div>
                <div>
                  <span className="text-[10px] text-[var(--bdae-text-secondary)] block">Historical Yield</span>
                  <span className="font-mono font-bold text-[var(--bdae-text-primary)]">
                    {formatCurrency(application.creditScoring.historicalYield || 0)}
                  </span>
                </div>
                <div>
                  <span className="text-[10px] text-[var(--bdae-text-secondary)] block">Land Size</span>
                  <span className="font-mono font-bold text-[var(--bdae-text-primary)]">
                    {application.creditScoring.landSizeHectares ?? '—'} Hectares
                  </span>
                </div>
              </div>
            </div>
          )}

          {/* Underwriting Action CTA */}
          <div className="flex items-center justify-between pt-4 border-t border-[var(--bdae-border)]">
            <span className="text-xs text-[var(--bdae-text-secondary)]">
              {isApplicant
                ? 'Self-approval disabled by Maker-Checker separation.'
                : 'Click below to review credit dossier and enter dual-control underwriting decision.'}
            </span>

            <button
              type="button"
              disabled={isApplicant || application.status === 'APPROVED' || application.status === 'DISBURSED'}
              onClick={() => {
                setSelectedApp(application);
                setIsDecisionModalOpen(true);
              }}
              className="px-5 py-2.5 bg-purple-600 hover:bg-purple-700 disabled:opacity-50 text-white rounded-xl text-xs font-bold shadow-md transition-all flex items-center gap-2"
            >
              <ClipboardCheck className="w-4 h-4" />
              Perform Underwriting Decision
            </button>
          </div>
        </div>
      )}

      {/* Decision Modal */}
      <UnderwritingDecisionModal
        isOpen={isDecisionModalOpen}
        application={selectedApp}
        onClose={() => setIsDecisionModalOpen(false)}
        onSuccess={handleDecisionSuccess}
      />
    </div>
  );
};
