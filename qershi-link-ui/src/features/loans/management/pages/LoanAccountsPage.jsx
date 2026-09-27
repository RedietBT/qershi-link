import React, { useState } from 'react';
import {
  BadgePercent,
  Search,
  Plus,
  Calendar,
  CreditCard,
  CheckCircle2,
  Clock,
  AlertTriangle,
  Loader2,
  FolderOpen,
  Banknote
} from 'lucide-react';
import { loanManagementApi } from '../api/loanManagementApi';
import { formatCurrency, formatDateTime } from '../../../../common/utils/currency';
import { RepaymentScheduleModal } from '../components/RepaymentScheduleModal';
import { ProcessRepaymentModal } from '../components/ProcessRepaymentModal';
import { DisburseLoanModal } from '../components/DisburseLoanModal';

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

      // Try user loan accounts lookup
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

  const getStatusBadge = (status) => {
    switch (status) {
      case 'ACTIVE':
      case 'DISBURSED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-600 dark:text-emerald-400">
            <CheckCircle2 className="w-3 h-3" />
            Active / Disbursed
          </span>
        );
      case 'PENDING_DISBURSEMENT':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/10 text-amber-600 dark:text-amber-400">
            <Clock className="w-3 h-3" />
            Pending Checker Approval
          </span>
        );
      case 'CLOSED':
      case 'SETTLED':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-blue-500/10 text-blue-600 dark:text-blue-400">
            <CheckCircle2 className="w-3 h-3" />
            Settled / Closed
          </span>
        );
      case 'DEFAULTED':
      case 'OVERDUE':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-red-500/10 text-red-600 dark:text-red-400">
            <AlertTriangle className="w-3 h-3" />
            Defaulted
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-gray-500/10 text-gray-600">
            {status || 'ACTIVE'}
          </span>
        );
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

        <button
          type="button"
          onClick={() => setDisburseModalOpen(true)}
          className="px-4 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold shadow-md transition-all flex items-center gap-2 self-start sm:self-auto"
        >
          <Banknote className="w-4 h-4" />
          Initiate Disbursement
        </button>
      </div>

      {/* Member Lookup Bar */}
      <div className="bdae-card p-5 border border-[var(--bdae-border)] space-y-4">
        <form onSubmit={handleSearch} className="flex flex-col sm:flex-row gap-3">
          <div className="relative flex-1">
            <input
              type="text"
              placeholder="Search active loan accounts by Borrower Member User ID or Account UUID..."
              value={userIdSearch}
              onChange={(e) => setUserIdSearch(e.target.value)}
              className="bdae-input font-mono text-xs pl-10"
            />
            <Search className="w-4 h-4 text-[var(--bdae-text-secondary)] absolute left-3.5 top-3" />
          </div>

          <button
            type="submit"
            disabled={loading || !userIdSearch.trim()}
            className="px-5 py-2.5 bg-emerald-600 text-white rounded-xl text-xs font-bold flex items-center justify-center gap-2 hover:bg-emerald-700 disabled:opacity-50 shadow-md transition-all shrink-0"
          >
            {loading ? <Loader2 className="w-4 h-4 animate-spin" /> : <Search className="w-4 h-4" />}
            Inquire Portfolio
          </button>
        </form>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 flex items-start space-x-3 text-xs">
          <AlertTriangle className="w-5 h-5 shrink-0 mt-0.5" />
          <div>
            <p className="font-semibold">Portfolio Inquiry Alert</p>
            <p className="mt-0.5">{error}</p>
          </div>
        </div>
      )}

      {/* Loan Accounts Table */}
      <div className="bdae-card border border-[var(--bdae-border)] overflow-hidden shadow-sm">
        <div className="p-4 border-b border-[var(--bdae-border)] flex items-center justify-between">
          <h2 className="text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
            Active Loan Accounts {searched && `(${accounts.length} records)`}
          </h2>
        </div>

        {loading ? (
          <div className="flex flex-col items-center justify-center py-16 space-y-3">
            <Loader2 className="w-8 h-8 animate-spin text-emerald-600" />
            <p className="text-xs text-[var(--bdae-text-secondary)] font-medium">
              Retrieving loan portfolio from loan management service...
            </p>
          </div>
        ) : !searched && accounts.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 space-y-2 text-center">
            <FolderOpen className="w-10 h-10 text-[var(--bdae-text-secondary)]/50" />
            <p className="text-xs font-bold text-[var(--bdae-text-primary)]">
              No Member Portfolio Inquired
            </p>
            <p className="text-[11px] text-[var(--bdae-text-secondary)] max-w-sm">
              Enter a Borrower Member User ID or Account UUID above to inspect active loans, amortization schedules, and process repayments.
            </p>
          </div>
        ) : accounts.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 space-y-2 text-center">
            <BadgePercent className="w-10 h-10 text-[var(--bdae-text-secondary)]/50" />
            <p className="text-xs font-bold text-[var(--bdae-text-primary)]">
              No Loan Accounts Found
            </p>
            <p className="text-[11px] text-[var(--bdae-text-secondary)]">
              No active or historical loan accounts found for this borrower.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="bg-black/5 dark:bg-white/5 border-b border-[var(--bdae-border)] text-[var(--bdae-text-secondary)]">
                  <th className="py-3 px-4 font-semibold">Account No</th>
                  <th className="py-3 px-4 font-semibold text-right">Principal</th>
                  <th className="py-3 px-4 font-semibold text-right">Outstanding</th>
                  <th className="py-3 px-4 font-semibold text-center">Rate</th>
                  <th className="py-3 px-4 font-semibold text-center">Term</th>
                  <th className="py-3 px-4 font-semibold">Status</th>
                  <th className="py-3 px-4 font-semibold text-center">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[var(--bdae-border)]">
                {accounts.map((acc) => (
                  <tr key={acc.accountId} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                    <td className="py-3 px-4 font-mono font-bold text-[var(--bdae-text-primary)]">
                      {acc.accountNo || acc.accountId.slice(0, 13)}...
                    </td>
                    <td className="py-3 px-4 text-right font-mono font-bold text-[var(--bdae-text-primary)]">
                      {formatCurrency(acc.principalAmount)}
                    </td>
                    <td className="py-3 px-4 text-right font-mono font-black text-red-600 dark:text-red-400">
                      {formatCurrency(acc.outstandingBalance ?? acc.principalAmount)}
                    </td>
                    <td className="py-3 px-4 text-center font-mono font-medium">
                      {acc.interestRatePct ?? '14.00'}%
                    </td>
                    <td className="py-3 px-4 text-center text-[var(--bdae-text-secondary)]">
                      {acc.termMonths ?? '12'} Mo.
                    </td>
                    <td className="py-3 px-4">
                      {getStatusBadge(acc.status)}
                    </td>
                    <td className="py-3 px-4 text-center">
                      <div className="flex items-center justify-center gap-2">
                        <button
                          type="button"
                          onClick={() => {
                            setSelectedAccountId(acc.accountId);
                            setSelectedAccountNo(acc.accountNo || acc.accountId);
                            setScheduleModalOpen(true);
                          }}
                          className="px-2.5 py-1 rounded-lg bg-black/5 dark:bg-white/5 hover:bg-purple-500/10 hover:text-purple-600 border border-[var(--bdae-border)] font-bold text-[11px] flex items-center gap-1 transition-colors"
                          title="View Amortization Schedule"
                        >
                          <Calendar className="w-3.5 h-3.5" />
                          <span>Schedule</span>
                        </button>

                        <button
                          type="button"
                          onClick={() => {
                            setSelectedRepayAccount(acc);
                            setRepayModalOpen(true);
                          }}
                          className="px-2.5 py-1 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-[11px] flex items-center gap-1 transition-colors shadow-sm"
                          title="Process Waterfall Repayment"
                        >
                          <CreditCard className="w-3.5 h-3.5" />
                          <span>Repay</span>
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

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
