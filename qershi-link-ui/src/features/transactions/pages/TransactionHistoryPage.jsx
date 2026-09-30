import React, { useState } from 'react';
import { History, AlertTriangle } from 'lucide-react';
import { transactionApi } from '../api/transactionApi';
import { GLJournalModal } from '../components/GLJournalModal';
import { TransactionFilterBar } from '../components/TransactionFilterBar';
import { TransactionTable } from '../components/TransactionTable';

/**
 * Modular General Ledger & Account Statements Inquiry Page
 */
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
      const data = Array.isArray(res)
        ? res
        : Array.isArray(res?.data)
        ? res.data
        : res?.data?.data || [];
      setTransactions(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Failed to fetch account transactions:', err);
      setError(
        err?.response?.data?.message || 'Failed to fetch transaction history for this account.'
      );
      setTransactions([]);
    } finally {
      setLoading(false);
    }
  };

  const filteredTransactions = transactions.filter((tx) => {
    if (typeFilter === 'ALL') return true;
    return tx.transactionType === typeFilter;
  });

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

      {/* Search & Filter Bar Component */}
      <TransactionFilterBar
        accountNo={accountNo}
        onAccountChange={setAccountNo}
        loading={loading}
        onSubmit={handleSearch}
        typeFilter={typeFilter}
        onTypeFilterChange={setTypeFilter}
      />

      {/* Error Alert */}
      {error && (
        <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 flex items-start space-x-3 text-xs">
          <AlertTriangle className="w-5 h-5 shrink-0 mt-0.5" />
          <div>
            <p className="font-semibold">Statement Inquiry Alert</p>
            <p className="mt-0.5">{error}</p>
          </div>
        </div>
      )}

      {/* Transaction Statements Table Component */}
      <TransactionTable
        transactions={filteredTransactions}
        searched={searched}
        accountNo={accountNo}
        onInspectGL={(ref) => {
          setSelectedTxRef(ref);
          setGlModalOpen(true);
        }}
      />

      {/* GL Double-Entry Audit Modal */}
      <GLJournalModal
        isOpen={glModalOpen}
        transactionRef={selectedTxRef}
        onClose={() => setGlModalOpen(false)}
      />
    </div>
  );
};
