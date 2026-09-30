import React from 'react';
import { User, CreditCard } from 'lucide-react';
import { formatCurrency } from '../../../common/utils/currency';
import { MaskedDataField } from '../../../common/components/MaskedDataField';

/**
 * Reusable card displaying verified account details for fund transfers
 */
export const TransferAccountCard = ({
  accountDetails,
  title = 'Account Details',
  badgeColor = 'emerald',
}) => {
  if (!accountDetails) return null;

  const availableBal = Number(accountDetails.availableBalance ?? accountDetails.clearedBalance ?? 0);

  return (
    <div className="p-4 rounded-xl bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] space-y-3 animate-fadeIn">
      <div className="flex items-start justify-between">
        <div>
          <span className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)]">
            {title}
          </span>
          <p className="text-sm font-bold text-[var(--bdae-text-primary)] flex items-center gap-1.5 mt-0.5">
            <User className="w-3.5 h-3.5 text-[var(--bdae-primary)]" />
            {accountDetails.holderName || accountDetails.userId || 'Verified Member'}
          </p>
          <div className="mt-1 flex flex-col gap-0.5">
            <div className="text-[11px] text-[var(--bdae-text-secondary)]">
              Acct: <MaskedDataField value={accountDetails.accountNo} type="account" allowReveal={true} />
            </div>
            {accountDetails.phone && (
              <div className="text-[11px] text-[var(--bdae-text-secondary)]">
                Tel: <MaskedDataField value={accountDetails.phone} type="phone" allowReveal={true} />
              </div>
            )}
          </div>
        </div>
        <span
          className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
            accountDetails.status === 'ACTIVE'
              ? 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400'
              : 'bg-amber-500/10 text-amber-600 dark:text-amber-400'
          }`}
        >
          {accountDetails.status || 'ACTIVE'}
        </span>
      </div>

      <div className="pt-2 border-t border-[var(--bdae-border)] flex items-center justify-between text-xs">
        <div>
          <span className="text-[10px] text-[var(--bdae-text-secondary)] uppercase block">Product</span>
          <span className="font-semibold text-[var(--bdae-text-primary)]">
            {accountDetails.productCode || accountDetails.accountType || 'Savings Account'}
          </span>
        </div>
        <div className="text-right">
          <span className="text-[10px] text-[var(--bdae-text-secondary)] uppercase block">Available Balance</span>
          <span className="font-mono font-bold text-xs text-[var(--bdae-primary)]">
            {formatCurrency(availableBal, accountDetails.currency || 'ETB')}
          </span>
        </div>
      </div>
    </div>
  );
};
