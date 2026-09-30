import React from 'react';
import { Search, Filter, Loader2, FileSpreadsheet } from 'lucide-react';
import { formatCurrency, formatDateTime } from '../../../../common/utils/currency';
import { MaskedDataField } from '../../../../common/components/MaskedDataField';

export const DelinquencyLoanTable = ({
  loans = [],
  isLoading = false,
  searchQuery = '',
  setSearchQuery,
  selectedBucket = 'ALL',
  setSelectedBucket
}) => {
  const getBucketBadge = (bucket) => {
    switch (bucket) {
      case 'CURRENT':
        return (
          <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/15 text-emerald-600 dark:text-emerald-400">
            Current (0 DPD)
          </span>
        );
      case 'WATCHLIST_PAR_30':
        return (
          <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/15 text-amber-600 dark:text-amber-400">
            PAR 1-30
          </span>
        );
      case 'SUBSTANDARD_PAR_60':
        return (
          <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-orange-500/15 text-orange-600 dark:text-orange-400">
            PAR 31-60
          </span>
        );
      case 'DOUBTFUL_PAR_90':
        return (
          <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-rose-500/15 text-rose-600 dark:text-rose-400">
            PAR 61-90
          </span>
        );
      case 'LOSS_PAR_90_PLUS':
        return (
          <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-red-500/15 text-red-600 dark:text-red-400">
            NPL / Loss (90+ DPD)
          </span>
        );
      default:
        return (
          <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-gray-500/15 text-gray-600">
            {bucket}
          </span>
        );
    }
  };

  const filteredLoans = loans.filter((l) => {
    const matchesBucket =
      selectedBucket === 'ALL' || l.delinquencyBucket === selectedBucket;
    const matchesSearch =
      !searchQuery ||
      l.accountNo?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      l.borrowerUserId?.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesBucket && matchesSearch;
  });

  return (
    <div className="bdae-card rounded-2xl border border-[var(--bdae-border)] overflow-hidden space-y-4 p-6 shadow-sm">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-sm font-bold text-[var(--bdae-text-primary)] uppercase tracking-wider">
            Delinquent Portfolio Ledger
          </h2>
          <p className="text-xs text-[var(--bdae-text-secondary)]">
            Showing {filteredLoans.length} accounts classified under risk aging rules.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <div className="relative">
            <Search className="w-3.5 h-3.5 absolute left-3 top-2.5 text-[var(--bdae-text-secondary)]" />
            <input
              type="text"
              placeholder="Search account or borrower..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-9 pr-3 py-1.5 rounded-xl border border-[var(--bdae-border)] bg-transparent text-xs text-[var(--bdae-text-primary)] focus:outline-none focus:ring-1 focus:ring-[var(--bdae-primary)] w-56 font-mono"
            />
          </div>
        </div>
      </div>

      <div className="overflow-x-auto -mx-6">
        <table className="w-full text-left text-xs border-collapse">
          <thead>
            <tr className="border-y border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 text-[var(--bdae-text-secondary)]">
              <th className="py-3 px-6 font-semibold">Loan Account</th>
              <th className="py-3 px-4 font-semibold">Borrower Identity</th>
              <th className="py-3 px-4 font-semibold text-center">Days Past Due</th>
              <th className="py-3 px-4 font-semibold">Classification</th>
              <th className="py-3 px-4 font-semibold text-right">Overdue Principal</th>
              <th className="py-3 px-4 font-semibold text-right">Overdue Interest</th>
              <th className="py-3 px-4 font-semibold text-right">Provision (ETB)</th>
              <th className="py-3 px-6 font-semibold">Evaluated On</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[var(--bdae-border)]">
            {isLoading ? (
              <tr>
                <td colSpan="8" className="py-12 text-center text-xs text-[var(--bdae-text-secondary)]">
                  <Loader2 className="w-6 h-6 animate-spin mx-auto text-[var(--bdae-primary)]" />
                  <p className="mt-2 font-medium">Loading delinquency aging records...</p>
                </td>
              </tr>
            ) : filteredLoans.length === 0 ? (
              <tr>
                <td colSpan="8" className="py-12 text-center text-xs text-[var(--bdae-text-secondary)]">
                  <FileSpreadsheet className="w-8 h-8 mx-auto opacity-30 text-[var(--bdae-text-secondary)]" />
                  <p className="mt-2 font-bold text-[var(--bdae-text-primary)]">
                    No Delinquent Loans Match Filters
                  </p>
                  <p className="text-[11px] opacity-75">
                    All accounts in this category are fully performing or within permissible grace periods.
                  </p>
                </td>
              </tr>
            ) : (
              filteredLoans.map((l) => (
                <tr
                  key={l.delinquencyId || l.loanAccountId}
                  className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
                >
                  <td className="py-3.5 px-6 font-mono font-bold text-[var(--bdae-text-primary)]">
                    <MaskedDataField
                      value={l.accountNo || l.loanAccountId}
                      maskType="accountNumber"
                      allowReveal={true}
                    />
                  </td>
                  <td className="py-3.5 px-4">
                    <MaskedDataField
                      value={l.borrowerUserId}
                      maskType="memberId"
                      allowReveal={true}
                    />
                  </td>
                  <td className="py-3.5 px-4 text-center">
                    <span
                      className={`font-mono font-bold text-xs px-2 py-0.5 rounded ${
                        l.daysPastDue > 90
                          ? 'bg-red-500/20 text-red-600 dark:text-red-400'
                          : l.daysPastDue > 30
                          ? 'bg-amber-500/20 text-amber-600 dark:text-amber-400'
                          : 'bg-emerald-500/20 text-emerald-600 dark:text-emerald-400'
                      }`}
                    >
                      {l.daysPastDue} DPD
                    </span>
                  </td>
                  <td className="py-3.5 px-4">{getBucketBadge(l.delinquencyBucket)}</td>
                  <td className="py-3.5 px-4 text-right font-mono font-bold text-[var(--bdae-text-primary)]">
                    {formatCurrency(l.overduePrincipal)}
                  </td>
                  <td className="py-3.5 px-4 text-right font-mono text-[var(--bdae-text-secondary)]">
                    {formatCurrency(l.overdueInterest)}
                  </td>
                  <td className="py-3.5 px-4 text-right font-mono font-bold text-purple-600 dark:text-purple-400">
                    {formatCurrency(l.provisionAmount)}
                  </td>
                  <td className="py-3.5 px-6 text-[var(--bdae-text-secondary)]">
                    {formatDateTime(l.evaluatedAt)}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
