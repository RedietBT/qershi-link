import React from 'react';
import {
  ArrowDownLeft,
  ArrowUpRight,
  ArrowLeftRight,
  BookOpen,
  FileSpreadsheet,
} from 'lucide-react';
import { formatCurrency, formatDateTime } from '../../../common/utils/currency';
import { MaskedDataField } from '../../../common/components/MaskedDataField';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';

/**
 * Renders badges for specific transaction categories
 */
const TransactionTypeBadge = ({ type }) => {
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

/**
 * Transaction statements data table with GL inspect actions
 */
export const TransactionTable = ({
  transactions,
  searched,
  accountNo,
  onInspectGL,
}) => {
  if (transactions.length === 0) {
    return (
      <div className="bdae-card p-12 text-center border border-[var(--bdae-border)] space-y-3">
        <FileSpreadsheet className="w-10 h-10 text-[var(--bdae-text-secondary)] opacity-40 mx-auto" />
        <h3 className="text-sm font-bold text-[var(--bdae-text-primary)]">
          {searched ? 'No Transaction Postings Found' : 'No Account Specified'}
        </h3>
        <p className="text-xs text-[var(--bdae-text-secondary)] max-w-sm mx-auto">
          {searched
            ? `No ledger records found for account ${accountNo}. Verify the account number and try again.`
            : 'Enter a valid SACCO member account number above to inquire its transaction history.'}
        </p>
      </div>
    );
  }

  return (
    <div className="bdae-card border border-[var(--bdae-border)] rounded-2xl overflow-hidden shadow-lg">
      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs border-collapse">
          <thead>
            <tr className="border-b border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)]">
              <th className="py-3 px-4">Date & Time</th>
              <th className="py-3 px-4">Type</th>
              <th className="py-3 px-4">Account</th>
              <th className="py-3 px-4">Narration</th>
              <th className="py-3 px-4 text-right">Amount</th>
              <th className="py-3 px-4 text-right">Balance After</th>
              <th className="py-3 px-4 text-center">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[var(--bdae-border)]">
            {transactions.map((tx) => {
              const isCredit =
                tx.transactionType === 'DEPOSIT' ||
                tx.transactionType === 'LOAN_REPAYMENT';

              return (
                <tr
                  key={tx.transactionId || tx.transactionRef}
                  className="hover:bg-black/[0.02] dark:hover:bg-white/[0.02] transition-colors"
                >
                  <td className="py-3 px-4 text-[11px] whitespace-nowrap">
                    {formatDateTime(tx.createdAt)}
                  </td>
                  <td className="py-3 px-4 whitespace-nowrap">
                    <TransactionTypeBadge type={tx.transactionType} />
                  </td>
                  <td className="py-3 px-4 font-mono font-bold">
                    <MaskedDataField value={tx.accountNo} type="account" allowReveal={true} />
                  </td>
                  <td className="py-3 px-4 text-[var(--bdae-text-secondary)] max-w-xs truncate">
                    {tx.narration || tx.description || 'Transaction Posting'}
                  </td>
                  <td
                    className={`py-3 px-4 text-right font-mono font-bold whitespace-nowrap ${
                      isCredit
                        ? 'text-emerald-600 dark:text-emerald-400'
                        : 'text-amber-600 dark:text-amber-400'
                    }`}
                  >
                    {isCredit ? '+' : '-'}
                    {formatCurrency(tx.amount)}
                  </td>
                  <td className="py-3 px-4 text-right font-mono font-medium whitespace-nowrap text-[var(--bdae-text-secondary)]">
                    {tx.balanceAfter != null ? formatCurrency(tx.balanceAfter) : '—'}
                  </td>
                  <td className="py-3 px-4 text-center whitespace-nowrap">
                    {/* View GL lines button strictly guarded with TRANSACTION_VIEW */}
                    <PermissionGuard permissions={[PERMISSIONS.TRANSACTION_VIEW]}>
                      <button
                        type="button"
                        onClick={() => onInspectGL(tx.transactionRef)}
                        className="px-2.5 py-1 rounded-lg border border-[var(--bdae-border)] hover:bg-[var(--bdae-primary)]/10 hover:text-[var(--bdae-primary)] text-[11px] font-bold flex items-center gap-1.5 mx-auto transition-colors"
                        title="View balanced double-entry GL journal lines"
                      >
                        <BookOpen className="w-3.5 h-3.5" />
                        <span>GL Lines</span>
                      </button>
                    </PermissionGuard>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};
