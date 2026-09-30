import React from 'react';
import { TrendingUp, Clock } from 'lucide-react';
import { formatCurrency } from '../../../common/utils/currency';

const CATEGORY_COLORS = {
  SAVINGS: 'bg-blue-500/10 text-blue-600 border-blue-500/20',
  FIXED_DEPOSIT: 'bg-amber-500/10 text-amber-600 border-amber-500/20',
  CURRENT: 'bg-emerald-500/10 text-emerald-600 border-emerald-500/20',
  SHARES: 'bg-purple-500/10 text-purple-600 border-purple-500/20',
  RECURRING: 'bg-cyan-500/10 text-cyan-600 border-cyan-500/20'
};

export const DepositProductCard = ({ product }) => {
  return (
    <div className="bdae-card border border-[var(--bdae-border)] rounded-2xl p-5 space-y-4 hover:border-[var(--bdae-secondary)] transition-all hover:scale-[1.01] shadow-sm flex flex-col justify-between">
      {/* Card Header */}
      <div className="flex items-start justify-between gap-3">
        <div>
          <div className="flex items-center gap-2">
            <span className="font-mono text-[10px] font-black px-2 py-0.5 rounded-md bg-black/5 dark:bg-white/5 text-[var(--bdae-text-secondary)] border border-[var(--bdae-border)]">
              #{product.productCode}
            </span>
            <span
              className={`text-[9px] font-black uppercase px-2 py-0.5 rounded-full border ${
                CATEGORY_COLORS[product.category] ||
                'bg-gray-500/10 text-gray-500 border-gray-500/20'
              }`}
            >
              {product.category}
            </span>
          </div>
          <h3 className="text-sm font-extrabold text-[var(--bdae-text-primary)] mt-1.5 leading-snug">
            {product.productName}
          </h3>
        </div>
      </div>

      {/* Financial Metrics Grid */}
      <div className="grid grid-cols-2 gap-3 text-xs pt-2 border-t border-[var(--bdae-border)]">
        <div>
          <div className="text-[10px] text-[var(--bdae-text-secondary)] font-bold flex items-center gap-1">
            <TrendingUp className="w-3 h-3 text-emerald-500" />
            Interest Rate
          </div>
          <div className="font-mono text-base font-black text-emerald-600 dark:text-emerald-400 mt-0.5">
            {product.interestRatePa != null ? `${product.interestRatePa}%` : '0%'}
            <span className="text-[9px] text-[var(--bdae-text-secondary)] font-normal ml-1">
              p.a.
            </span>
          </div>
        </div>

        <div>
          <div className="text-[10px] text-[var(--bdae-text-secondary)] font-bold flex items-center gap-1">
            <Clock className="w-3 h-3 text-[var(--bdae-primary)]" />
            Posting
          </div>
          <div className="font-mono text-xs font-bold text-[var(--bdae-text-primary)] mt-1">
            {product.postingFrequency || 'MONTHLY'}
          </div>
        </div>

        <div>
          <div className="text-[10px] text-[var(--bdae-text-secondary)]">Min. Balance</div>
          <div className="font-mono text-xs font-bold text-[var(--bdae-text-primary)] mt-0.5">
            {formatCurrency(product.minOperatingBalance)}
          </div>
        </div>

        <div>
          <div className="text-[10px] text-[var(--bdae-text-secondary)]">Currency</div>
          <div className="font-mono text-xs font-bold text-[var(--bdae-text-primary)] mt-0.5">
            {product.currency || 'ETB'}
          </div>
        </div>
      </div>

      {/* Card Footer Details */}
      <div className="pt-2 border-t border-[var(--bdae-border)] flex items-center justify-between text-[10px] text-[var(--bdae-text-secondary)]">
        {product.termPeriodMonths ? (
          <span>Tenure: <b className="text-[var(--bdae-text-primary)]">{product.termPeriodMonths} Mo.</b></span>
        ) : (
          <span>Open-ended</span>
        )}
        {product.earlyWithdrawalPenaltyPct > 0 && (
          <span className="text-amber-500 font-bold">
            Penalty: {product.earlyWithdrawalPenaltyPct}%
          </span>
        )}
      </div>
    </div>
  );
};
