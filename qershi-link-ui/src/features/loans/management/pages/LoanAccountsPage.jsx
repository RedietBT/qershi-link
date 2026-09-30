import React, { useState } from 'react';
import {
  BadgePercent,
  Banknote,
  AlertTriangle
} from 'lucide-react';
import { loanManagementApi } from '../api/loanManagementApi';
import { RepaymentScheduleModal } from '../components/RepaymentScheduleModal';
import { ProcessRepaymentModal } from '../components/ProcessRepaymentModal';
import { DisburseLoanModal } from '../components/DisburseLoanModal';
import { LoanPortfolioMetrics } from '../components/LoanPortfolioMetrics';
import { LoanAccountsLookupBar } from '../components/LoanAccountsLookupBar';
import { LoanAccountsTable } from '../components/LoanAccountsTable';
import { PermissionGuard } from '../../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../../common/constants/permissions';

export const LoanAccountsPage = () => {
  const [userIdSearch, setUserIdSearch] = useState('');
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [error, setError] = useState(null);

  // Modals
  const [scheduleModalOpen, setScheduleModalOpen] = useState(false);
  const [selectedAccountId, setSelectedAccountId] = useState(null);
  const [selectedAccountNo, setSelectedAccountNo] = useState(null);

  const [repayModalOpen, setRepayModalOpen] = useState(false);
  const [selectedRepayAccount, setSelectedRepayAccount] = useState(null);

  const [disburseModalOpen, setDisburseModalOpen] = useState(false);

  const handleSearch = async (e) => {
    if (e) e.preventDefault();
    const query = userIdSearch.trim();
    if (!query) return;

    try {
      setLoading(true);
      setError(null);
      setSearched(true);

      const res = await loanManagementApi.getUserAccounts(query);
      const data = res.data || res || [];
      if (Array.isArray(data) && data.length > 0) {
        setAccounts(data);
      } else {
        // Fallback: try account by UUID
        try {
          const singleRes = await loanManagementApi.getAccountById(query);
          const singleData = singleRes.data || singleRes;
          setAccounts(singleData ? [singleData] : []);
        } catch {
          setAccounts([]);
        }
      }
    } catch (err) {
      console.error('Failed to lookup loan accounts:', err);
      setError(err?.response?.data?.message || 'Could not find loan accounts for this member or ID.');
      setAccounts([]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6 animate-fadeIn max-w-6xl mx-auto">
      {/* Title */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-[var(--bdae-border)] pb-4">
        <div>
          <h1 className="text-xl font-black text-[var(--bdae-text-primary)] flex items-center gap-2.5">
            <BadgePercent className="w-6 h-6 text-emerald-600" />
            Active Loan Portfolios & Repayments
          </h1>
          <p className="text-xs text-[var(--bdae-text-secondary)] mt-0.5">
            Manage active loan accounts, inspect amortization repayment schedules, and execute waterfall installment repayments.
          </p>
        </div>

        {/* Action Button strictly guarded by LOAN_DISBURSE_PROCESS */}
        <PermissionGuard permissions={[PERMISSIONS.LOAN_DISBURSE_PROCESS]}>
          <button
            type="button"
            onClick={() => setDisburseModalOpen(true)}
            className="px-4 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold shadow-md transition-all flex items-center gap-2 self-start sm:self-auto"
          >
            <Banknote className="w-4 h-4" />
            Initiate Disbursement
          </button>
        </PermissionGuard>
      </div>

      {/* Portfolio KPI Metrics */}
      {accounts.length > 0 && <LoanPortfolioMetrics accounts={accounts} />}

      {/* Member Lookup Bar */}
      <LoanAccountsLookupBar
        userIdSearch={userIdSearch}
        setUserIdSearch={setUserIdSearch}
        loading={loading}
        onSearch={handleSearch}
      />

      {error && (
        <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 flex items-start space-x-3 text-xs">
          <AlertTriangle className="w-5 h-5 shrink-0 mt-0.5" />
          <div>
            <p className="font-semibold">Portfolio Inquiry Alert</p>
            <p className="mt-0.5">{error}</p>
          </div>
        </div>
      )}

      {/* Modular Loan Accounts Table */}
      <LoanAccountsTable
        accounts={accounts}
        loading={loading}
        searched={searched}
        onOpenSchedule={(acc) => {
          setSelectedAccountId(acc.accountId);
          setSelectedAccountNo(acc.accountNo || acc.accountId);
          setScheduleModalOpen(true);
        }}
        onOpenRepay={(acc) => {
          setSelectedRepayAccount(acc);
          setRepayModalOpen(true);
        }}
      />

      {/* Schedule Modal */}
      <RepaymentScheduleModal
        accountId={selectedAccountId}
        accountNo={selectedAccountNo}
        isOpen={scheduleModalOpen}
        onClose={() => setScheduleModalOpen(false)}
      />

      {/* Repayment Modal */}
      <ProcessRepaymentModal
        account={selectedRepayAccount}
        isOpen={repayModalOpen}
        onClose={() => setRepayModalOpen(false)}
        onSuccess={() => {
          if (userIdSearch) handleSearch();
        }}
      />

      {/* Disburse Modal */}
      <DisburseLoanModal
        isOpen={disburseModalOpen}
        onClose={() => setDisburseModalOpen(false)}
        onSuccess={() => {
          if (userIdSearch) handleSearch();
        }}
      />
    </div>
  );
};
