import React, { useEffect, useState } from 'react';
import { X, BookOpen, CheckCircle2, AlertTriangle, Loader2, ArrowUpRight, ArrowDownLeft } from 'lucide-react';
import { transactionApi } from '../api/transactionApi';
import { formatCurrency, formatDateTime } from '../../../common/utils/currency';

export const GLJournalModal = ({ transactionRef, isOpen, onClose }) => {
  const [journal, setJournal] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isOpen && transactionRef) {
      loadJournal();
    } else {
      setJournal(null);
      setError(null);
    }
  }, [isOpen, transactionRef]);

  const loadJournal = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await transactionApi.getJournalEntryByRef(transactionRef);
      setJournal(res.data);
    } catch (err) {
      console.error('Failed to load GL journal entry:', err);
      setError(err?.response?.data?.message || 'Could not retrieve GL journal details for this transaction.');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  // Calculate totals
  const totalDebits = journal?.lines?.reduce((sum, line) => {
    return line.entryType === 'DEBIT' ? sum + Number(line.amount || 0) : sum;
  }, 0) || 0;

  const totalCredits = journal?.lines?.reduce((sum, line) => {
    return line.entryType === 'CREDIT' ? sum + Number(line.amount || 0) : sum;
  }, 0) || 0;

  const isBalanced = Math.abs(totalDebits - totalCredits) < 0.0001;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
      <div className="bdae-card w-full max-w-2xl overflow-hidden shadow-2xl border border-[var(--bdae-border)] flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="p-5 border-b border-[var(--bdae-border)] flex items-center justify-between bg-black/5 dark:bg-white/5">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-[var(--bdae-primary)]/10 text-[var(--bdae-primary)] flex items-center justify-center">
              <BookOpen className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-[var(--bdae-text-primary)]">
                General Ledger Double-Entry Audit
              </h2>
              <p className="text-xs text-[var(--bdae-text-secondary)] font-mono">
                Ref: {transactionRef}
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)] hover:bg-black/5 dark:hover:bg-white/5 rounded-xl transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 overflow-y-auto space-y-6 flex-1">
          {loading && (
            <div className="flex flex-col items-center justify-center py-12 space-y-3">
              <Loader2 className="w-8 h-8 animate-spin text-[var(--bdae-primary)]" />
              <p className="text-xs text-[var(--bdae-text-secondary)] font-medium">
                Fetching double-entry journal lines from General Ledger...
              </p>
            </div>
          )}

          {error && !loading && (
            <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 flex items-start space-x-3 text-xs">
              <AlertTriangle className="w-5 h-5 shrink-0 mt-0.5" />
              <div>
                <p className="font-semibold">Unable to fetch GL Journal</p>
                <p className="mt-1 opacity-90">{error}</p>
              </div>
            </div>
          )}

          {journal && !loading && (
            <>
              {/* Journal Meta */}
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 p-4 rounded-xl bg-black/5 dark:bg-white/5 text-xs">
                <div>
                  <span className="text-[var(--bdae-text-secondary)] block text-[10px] uppercase font-bold tracking-wider">
                    Posting Date
                  </span>
                  <span className="font-medium text-[var(--bdae-text-primary)]">
                    {formatDateTime(journal.postingDate || journal.createdAt)}
                  </span>
                </div>
                <div>
                  <span className="text-[var(--bdae-text-secondary)] block text-[10px] uppercase font-bold tracking-wider">
                    Journal Status
                  </span>
                  <span className={`inline-flex items-center gap-1 font-semibold ${isBalanced ? 'text-emerald-500' : 'text-amber-500'}`}>
                    {isBalanced ? <CheckCircle2 className="w-3.5 h-3.5" /> : <AlertTriangle className="w-3.5 h-3.5" />}
                    {isBalanced ? 'Balanced & Posted' : 'Out of Balance'}
                  </span>
                </div>
                <div className="col-span-2 sm:col-span-1">
                  <span className="text-[var(--bdae-text-secondary)] block text-[10px] uppercase font-bold tracking-wider">
                    Description
                  </span>
                  <span className="font-medium text-[var(--bdae-text-primary)] truncate block">
                    {journal.description || 'Core Banking Transaction Posting'}
                  </span>
                </div>
              </div>

              {/* Journal Lines Table */}
              <div>
                <h3 className="text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)] mb-2">
                  Accounting Postings (Double-Entry)
                </h3>
                <div className="overflow-x-auto border border-[var(--bdae-border)] rounded-xl">
                  <table className="w-full text-left border-collapse text-xs">
                    <thead>
                      <tr className="bg-black/5 dark:bg-white/5 border-b border-[var(--bdae-border)] text-[var(--bdae-text-secondary)]">
                        <th className="py-2.5 px-3 font-semibold">GL Account Code</th>
                        <th className="py-2.5 px-3 font-semibold">Entry Type</th>
                        <th className="py-2.5 px-3 font-semibold text-right">Debit (ETB)</th>
                        <th className="py-2.5 px-3 font-semibold text-right">Credit (ETB)</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-[var(--bdae-border)]">
                      {journal.lines?.map((line, idx) => (
                        <tr key={line.lineId || idx} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                          <td className="py-2.5 px-3 font-mono font-bold text-[var(--bdae-text-primary)]">
                            {line.glAccountCode}
                          </td>
                          <td className="py-2.5 px-3">
                            <span
                              className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold ${
                                line.entryType === 'DEBIT'
                                  ? 'bg-blue-500/10 text-blue-600 dark:text-blue-400'
                                  : 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400'
                              }`}
                            >
                              {line.entryType === 'DEBIT' ? (
                                <ArrowDownLeft className="w-3 h-3" />
                              ) : (
                                <ArrowUpRight className="w-3 h-3" />
                              )}
                              {line.entryType}
                            </span>
                          </td>
                          <td className="py-2.5 px-3 text-right font-mono font-medium text-[var(--bdae-text-primary)]">
                            {line.entryType === 'DEBIT' ? formatCurrency(line.amount) : '—'}
                          </td>
                          <td className="py-2.5 px-3 text-right font-mono font-medium text-[var(--bdae-text-primary)]">
                            {line.entryType === 'CREDIT' ? formatCurrency(line.amount) : '—'}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                    <tfoot>
                      <tr className="bg-black/5 dark:bg-white/5 font-bold border-t-2 border-[var(--bdae-border)]">
                        <td className="py-2.5 px-3 text-[var(--bdae-text-primary)]" colSpan={2}>
                          Total Postings
                        </td>
                        <td className="py-2.5 px-3 text-right font-mono text-blue-600 dark:text-blue-400">
                          {formatCurrency(totalDebits)}
                        </td>
                        <td className="py-2.5 px-3 text-right font-mono text-emerald-600 dark:text-emerald-400">
                          {formatCurrency(totalCredits)}
                        </td>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              </div>

              {/* Balance Verification Banner */}
              <div
                className={`p-3.5 rounded-xl border flex items-center justify-between text-xs ${
                  isBalanced
                    ? 'bg-emerald-500/10 border-emerald-500/20 text-emerald-700 dark:text-emerald-300'
                    : 'bg-amber-500/10 border-amber-500/20 text-amber-700 dark:text-amber-300'
                }`}
              >
                <div className="flex items-center space-x-2">
                  <CheckCircle2 className="w-4 h-4" />
                  <span className="font-semibold">
                    {isBalanced ? 'Balanced Ledger: Debits equal Credits.' : 'Ledger Imbalance Detected!'}
                  </span>
                </div>
                <span className="font-mono text-[11px]">
                  Variance: {formatCurrency(Math.abs(totalDebits - totalCredits))}
                </span>
              </div>
            </>
          )}
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 flex justify-end">
          <button
            onClick={onClose}
            className="px-5 py-2 rounded-xl text-xs font-bold bg-[var(--bdae-primary)] text-white hover:opacity-90 transition-opacity shadow-sm"
          >
            Close Audit View
          </button>
        </div>
      </div>
    </div>
  );
};
