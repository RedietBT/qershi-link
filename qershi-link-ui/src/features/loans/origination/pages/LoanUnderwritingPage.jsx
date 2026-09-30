import React, { useState } from 'react';
import {
  ClipboardCheck,
  ShieldAlert,
  AlertTriangle
} from 'lucide-react';
import { loanOriginationApi } from '../api/loanOriginationApi';
import { useAuthStore } from '../../../../common/store/useAuthStore';
import { UnderwritingDecisionModal } from '../components/UnderwritingDecisionModal';
import { UnderwritingLookupCard } from '../components/UnderwritingLookupCard';
import { UnderwritingDossierCard } from '../components/UnderwritingDossierCard';

export const LoanUnderwritingPage = () => {
  const currentUser = useAuthStore((state) => state.user);

  const [searchId, setSearchId] = useState('');
  const [application, setApplication] = useState(null);
  const [loading, setLoading] = useState(false);
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

      {/* Modular Lookup Card */}
      <UnderwritingLookupCard
        searchId={searchId}
        setSearchId={setSearchId}
        loading={loading}
        onLookup={handleLookup}
      />

      {error && (
        <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 flex items-start space-x-3 text-xs">
          <AlertTriangle className="w-5 h-5 shrink-0 mt-0.5" />
          <div>
            <p className="font-semibold">Underwriting Query Alert</p>
            <p className="mt-0.5">{error}</p>
          </div>
        </div>
      )}

      {/* Modular Application Underwriting Dossier */}
      <UnderwritingDossierCard
        application={application}
        isApplicant={isApplicant}
        onOpenDecision={(app) => {
          setSelectedApp(app);
          setIsDecisionModalOpen(true);
        }}
      />

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
