import React, { useState } from 'react';
import {
  History,
  Search,
  BookOpen,
  ArrowDownLeft,
  ArrowUpRight,
  ArrowLeftRight,
  Filter,
  Loader2,
  Calendar,
  AlertTriangle,
  FileSpreadsheet
} from 'lucide-react';
import { transactionApi } from '../api/transactionApi';
import { formatCurrency, formatDateTime } from '../../../common/utils/currency';
import { GLJournalModal } from '../components/GLJournalModal';

export const TransactionHistoryPage = () => {
  const [accountNo, setAccountNo] = useState('');
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [error, setError] = useState(null);

  // Filters
  const [typeFilter, setTypeFilter] = useState('ALL');

  // GL Journal Audit Modal
  const [glModalOpen, setGlModalOpen] = useState(false);
  const [selectedTxRef, setSelectedTxRef] = useState(null);

  const handleSearch = async (e) => {
    if (e) e.preventDefault();
    const query = accountNo.trim();
    if (!query) return;

    try {
      setLoading(true);
      setError(null);
      setSearched(true);
      const res = await transactionApi.getAccountTransactions(query);
      const data = Array.isArray(res) ? res : (Array.isArray(res?.data) ? res.data : (res?.data?.data || []));
      setTransactions(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Failed to fetch account transactions:', err);
      setError(err?.response?.data?.message || 'Failed to fetch transaction history for this account.');
      setTransactions([]);
    } finally {
      setLoading(false);
    }
  };

  const filteredTransactions = transactions.filter((tx) => {
    if (typeFilter === 'ALL') return true;
    return tx.transactionType === typeFilter;
  });

  const getTypeBadge = (type) => {
    switch (type) {
      case 'DEPOSIT':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-600 dark:text-emerald-400">
            <ArrowDownLeft className="w-3 h-3" />
            Deposit
          </span>
        );
      case 'WITHDRAWAL':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/10 text-amber-600 dark:text-amber-400">
            <ArrowUpRight className="w-3 h-3" />
            Withdrawal
          </span>
        );
      case 'TRANSFER':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-blue-500/10 text-blue-600 dark:text-blue-400">
            <ArrowLeftRight className="w-3 h-3" />
            Transfer
          </span>
        );
      case 'LOAN_DISBURSEMENT':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-purple-500/10 text-purple-600 dark:text-purple-400">
            <ArrowDownLeft className="w-3 h-3" />
            Loan Disbursed
          </span>
        );
      case 'LOAN_REPAYMENT':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-teal-500/10 text-teal-600 dark:text-teal-400">
            <ArrowUpRight className="w-3 h-3" />
            Loan Repaid
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-gray-500/10 text-gray-600">
            {type || 'TRANSACTION'}
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
            <History className="w-6 h-6 text-[var(--bdae-primary)]" />
            General Ledger & Account Statements
          </h1>
          <p className="text-xs text-[var(--bdae-text-secondary)] mt-0.5">
            Chronological audit trail of member transactions, balanced double-entry GL journal lines, and postings.
          </p>
        </div>
      </div>

      {/* Search Header */}
      <div className="bdae-card p-5 border border-[var(--bdae-border)] space-y-4">
        <form onSubmit={handleSearch} className="flex flex-col sm:flex-row gap-3">
          <div className="relative flex-1">
            <input
              type="text"
              placeholder="Enter Account Number (e.g. ACC-AW-2026-0001)..."
              value={accountNo}
              onChange={(e) => setAccountNo(e.target.value)}
              className="bdae-input font-mono uppercase text-xs font-bold pl-10"
            />
            <Search className="w-4 h-4 text-[var(--bdae-text-secondary)] absolute left-3.5 top-3" />
          </div>

          <button
            type="submit"
            disabled={loading || !accountNo.trim()}
            className="px-5 py-2.5 bg-[var(--bdae-primary)] text-white rounded-xl text-xs font-bold flex items-center justify-center gap-2 hover:opacity-90 disabled:opacity-50 shadow-md transition-all shrink-0"
          >
            {loading ? <Loader2 className="w-4 h-4 animate-spin" /> : <Search className="w-4 h-4" />}
            Inquire Statements
          </button>
        </form>

        {/* Filter Pills */}
        <div className="flex flex-wrap items-center gap-2 pt-2 border-t border-[var(--bdae-border)] text-xs">
          <span className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)] flex items-center gap-1 mr-1">
            <Filter className="w-3 h-3" /> Filter Type:
          </span>
          {['ALL', 'DEPOSIT', 'WITHDRAWAL', 'TRANSFER', 'LOAN_DISBURSEMENT', 'LOAN_REPAYMENT'].map((type) => (
            <button
              key={type}
              type="button"
              onClick={() => setTypeFilter(type)}
              className={`px-3 py-1 rounded-lg text-[11px] font-bold transition-all ${
                typeFilter === type
                  ? 'bg-[var(--bdae-primary)] text-white shadow-sm'
                  : 'bg-black/5 dark:bg-white/5 text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]'
              }`}
            >
              {type === 'ALL' ? 'All Transactions' : type.replace('_', ' ')}
            </button>
          ))}
        </div>
      </div>

      {/* Error Banner */}
      {error && (
        <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 flex items-start space-x-3 text-xs">
          <AlertTriangle className="w-5 h-5 shrink-0 mt-0.5" />
          <div>
            <p className="font-semibold">Statement Query Alert</p>
            <p className="mt-0.5">{error}</p>
          </div>
        </div>
      )}

      {/* Results Table */}
      <div className="bdae-card border border-[var(--bdae-border)] overflow-hidden shadow-sm">
        <div className="p-4 border-b border-[var(--bdae-border)] flex items-center justify-between">
          <h2 className="text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
            Account Statement Records {searched && `(${filteredTransactions.length} entries)`}
          </h2>
        </div>

        {loading ? (
          <div className="flex flex-col items-center justify-center py-16 space-y-3">
            <Loader2 className="w-8 h-8 animate-spin text-[var(--bdae-primary)]" />
            <p className="text-xs text-[var(--bdae-text-secondary)] font-medium">
              Retrieving statement from General Ledger...
            </p>
          </div>
        ) : !searched ? (
          <div className="flex flex-col items-center justify-center py-16 space-y-2 text-center">
            <FileSpreadsheet className="w-10 h-10 text-[var(--bdae-text-secondary)]/50" />
            <p className="text-xs font-bold text-[var(--bdae-text-primary)]">
              No Account Statement Inquired
            </p>
            <p className="text-[11px] text-[var(--bdae-text-secondary)] max-w-sm">
              Enter a valid member account number above to inspect statements, transaction history, and underlying GL double-entry journal postings.
            </p>
          </div>
        ) : filteredTransactions.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 space-y-2 text-center">
            <History className="w-10 h-10 text-[var(--bdae-text-secondary)]/50" />
            <p className="text-xs font-bold text-[var(--bdae-text-primary)]">
              No Transactions Found
            </p>
            <p className="text-[11px] text-[var(--bdae-text-secondary)]">
              No matching transactions recorded for account "{accountNo}".
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="bg-black/5 dark:bg-white/5 border-b border-[var(--bdae-border)] text-[var(--bdae-text-secondary)]">
                  <th className="py-3 px-4 font-semibold">Tx Reference</th>
                  <th className="py-3 px-4 font-semibold">Type</th>
                  <th className="py-3 px-4 font-semibold text-right">Amount (ETB)</th>
                  <th className="py-3 px-4 font-semibold">Narration</th>
                  <th className="py-3 px-4 font-semibold">Timestamp</th>
                  <th className="py-3 px-4 font-semibold text-center">GL Audit</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[var(--bdae-border)]">
                {filteredTransactions.map((tx) => (
                  <tr key={tx.transactionRef || tx.transactionId} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                    <td className="py-3 px-4 font-mono font-bold text-[var(--bdae-text-primary)]">
                      {tx.transactionRef}
                    </td>
                    <td className="py-3 px-4">
                      {getTypeBadge(tx.transactionType)}
                    </td>
                    <td className="py-3 px-4 text-right font-mono font-bold">
                      <span
                        className={
                          tx.transactionType === 'DEPOSIT' || tx.transactionType === 'LOAN_DISBURSEMENT'
                            ? 'text-emerald-600 dark:text-emerald-400'
                            : 'text-amber-600 dark:text-amber-400'
                        }
                      >
                        {formatCurrency(tx.amount, tx.currency || 'ETB')}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-[var(--bdae-text-secondary)] max-w-xs truncate">
                      {tx.narration || '—'}
                    </td>
                    <td className="py-3 px-4 text-[var(--bdae-text-secondary)]">
                      {formatDateTime(tx.createdAt)}
                    </td>
                    <td className="py-3 px-4 text-center">
                      <button
                        type="button"
                        onClick={() => {
                          setSelectedTxRef(tx.transactionRef);
                          setGlModalOpen(true);
                        }}
                        className="p-1.5 rounded-lg bg-[var(--bdae-primary)]/10 text-[var(--bdae-primary)] hover:bg-[var(--bdae-primary)] hover:text-white transition-all inline-flex items-center gap-1 font-bold text-[11px]"
                        title="View GL Double-Entry Lines"
                      >
                        <BookOpen className="w-3.5 h-3.5" />
                        <span>GL Lines</span>
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* GL Double-Entry Audit Modal */}
      <GLJournalModal
        isOpen={glModalOpen}
        transactionRef={selectedTxRef}
        onClose={() => setGlModalOpen(false)}
      />
    </div>
  );
};
