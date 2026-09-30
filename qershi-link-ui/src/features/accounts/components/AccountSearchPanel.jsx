import React, { useState } from 'react';
import { Search, Phone, Hash, AlertCircle, RefreshCw, ChevronRight } from 'lucide-react';
import { accountLedgerApi } from '../api/accountLedgerApi';
import { MaskedDataField } from '../../../common/components/MaskedDataField';
import { formatCurrency } from '../../../common/utils/currency';

const STATUS_STYLES = {
  ACTIVE: 'bg-emerald-500/10 text-emerald-600 border-emerald-500/20',
  PENDING_APPROVAL: 'bg-amber-500/10 text-amber-600 border-amber-500/20',
  DORMANT: 'bg-gray-500/10 text-gray-500 border-gray-500/20',
  CLOSED: 'bg-red-500/10 text-red-500 border-red-500/20'
};

export const AccountSearchPanel = ({ onSelectMember }) => {
  const [mode, setMode] = useState('phone'); // 'phone' | 'accountNo'
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [searched, setSearched] = useState(false);

  const handleSearch = async () => {
    if (!query.trim()) return;
    setIsLoading(true);
    setError(null);
    setSearched(true);
    setResults([]);
    try {
      let res;
      if (mode === 'phone') {
        res = await accountLedgerApi.getAccountsByPhone(query.trim());
      } else {
        const single = await accountLedgerApi.getAccountByNo(query.trim());
        res = { data: [single.data || single] };
      }
      setResults(res.data || res || []);
    } catch (err) {
      setError(err?.response?.data?.message || 'No accounts found matching this query.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="bdae-card border border-[var(--bdae-border)] rounded-2xl p-5 space-y-4">
      <div className="flex items-center gap-2 text-sm font-extrabold text-[var(--bdae-text-primary)]">
        <Search className="w-4 h-4 text-[var(--bdae-secondary)]" />
        Account Lookup & Fast Discovery
      </div>

      {/* Mode Toggle */}
      <div className="flex gap-2">
        {[
          { key: 'phone', label: 'By Phone Number', icon: Phone },
          { key: 'accountNo', label: 'By Account No.', icon: Hash }
        ].map(({ key, label, icon: Icon }) => (
          <button
            key={key}
            onClick={() => {
              setMode(key);
              setResults([]);
              setQuery('');
              setSearched(false);
              setError(null);
            }}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-[10px] font-bold border transition-all ${
              mode === key
                ? 'border-[var(--bdae-primary)] bg-[var(--bdae-primary)]/10 text-[var(--bdae-primary)]'
                : 'border-[var(--bdae-border)] text-[var(--bdae-text-secondary)]'
            }`}
          >
            <Icon className="w-3 h-3" /> {label}
          </button>
        ))}
      </div>

      {/* Search Input */}
      <div className="flex gap-2">
        <div className="relative flex-1">
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            placeholder={
              mode === 'phone'
                ? 'Enter phone number (e.g. 0911234567)'
                : 'Enter account number (e.g. 0001-001-101-0000427)'
            }
            className="w-full pl-9 pr-4 py-2.5 rounded-xl border border-[var(--bdae-border)] focus:border-[var(--bdae-secondary)] bg-transparent text-xs text-[var(--bdae-text-primary)] outline-none font-mono"
          />
          {mode === 'phone' ? (
            <Phone className="w-3.5 h-3.5 text-[var(--bdae-text-secondary)] absolute left-3 top-3" />
          ) : (
            <Hash className="w-3.5 h-3.5 text-[var(--bdae-text-secondary)] absolute left-3 top-3" />
          )}
        </div>
        <button
          onClick={handleSearch}
          disabled={isLoading || !query.trim()}
          className="px-4 py-2.5 rounded-xl text-xs font-bold text-white flex items-center gap-1.5 disabled:opacity-50 transition-all shadow-md hover:opacity-90"
          style={{
            background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))`
          }}
        >
          {isLoading ? <RefreshCw className="w-3.5 h-3.5 animate-spin" /> : <Search className="w-3.5 h-3.5" />}
          <span>Search</span>
        </button>
      </div>

      {/* Error */}
      {error && (
        <div className="flex items-center gap-2 p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 text-xs font-bold">
          <AlertCircle className="w-4 h-4 shrink-0" /> {error}
        </div>
      )}

      {/* Results */}
      {searched && !isLoading && results.length === 0 && !error && (
        <div className="p-4 text-center text-xs text-[var(--bdae-text-secondary)]">
          No matching core accounts found.
        </div>
      )}

      {results.length > 0 && (
        <div className="space-y-2 pt-2 border-t border-[var(--bdae-border)]">
          <div className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">
            Matching Accounts ({results.length})
          </div>
          {results.map((acc) => (
            <div
              key={acc.accountId || acc.accountNumber}
              className="p-3.5 rounded-xl border border-[var(--bdae-border)] hover:border-[var(--bdae-secondary)] transition-all flex flex-col sm:flex-row sm:items-center justify-between gap-2"
            >
              <div>
                <div className="flex items-center gap-2 flex-wrap">
                  <MaskedDataField
                    value={acc.accountNumber}
                    maskType="accountNumber"
                    allowReveal={true}
                  />
                  <span
                    className={`text-[9px] font-black uppercase px-2 py-0.5 rounded-full border ${
                      STATUS_STYLES[acc.status] ||
                      'bg-gray-500/10 text-gray-500 border-gray-500/20'
                    }`}
                  >
                    {acc.status}
                  </span>
                </div>
                <div className="text-[10px] text-[var(--bdae-text-secondary)] mt-1 flex items-center gap-2">
                  <span>Branch: {acc.branchCode || '0001'}</span>
                  <span>Product: #{acc.productCode || '101'}</span>
                  {acc.userId && (
                    <span>
                      Borrower ID:{' '}
                      <MaskedDataField
                        value={acc.userId}
                        maskType="memberId"
                        allowReveal={false}
                      />
                    </span>
                  )}
                </div>
              </div>

              <div className="flex items-center gap-3 self-end sm:self-auto">
                <div className="text-right">
                  <div className="font-mono text-sm font-black text-emerald-600 dark:text-emerald-400">
                    {formatCurrency(acc.availableBalance)}
                  </div>
                  <div className="text-[9px] text-[var(--bdae-text-secondary)]">
                    Available Balance
                  </div>
                </div>

                {onSelectMember && acc.userId && (
                  <button
                    onClick={() => onSelectMember(acc.userId)}
                    className="p-1.5 rounded-lg border border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-primary)] hover:border-[var(--bdae-primary)] transition-all"
                    title="View Member Account Portfolio"
                  >
                    <ChevronRight className="w-4 h-4" />
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
