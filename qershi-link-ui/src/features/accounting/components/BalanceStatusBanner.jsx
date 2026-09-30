import React from 'react';
import { CheckCircle2, AlertTriangle } from 'lucide-react';
import { formatCurrency } from '../../../common/utils/currency';

export const BalanceStatusBanner = ({
  isBalanced,
  title,
  successMessage,
  difference = 0,
  leftLabel,
  leftValue = 0,
  rightLabel,
  rightValue = 0
}) => {
  if (isBalanced) {
    return (
      <div className="p-4 rounded-2xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-700 dark:text-emerald-400 flex items-center justify-between gap-4 animate-fadeIn">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-emerald-500/20 text-emerald-600 dark:text-emerald-400 flex items-center justify-center shrink-0">
            <CheckCircle2 className="w-5 h-5" />
          </div>
          <div>
            <h4 className="text-xs font-extrabold uppercase tracking-wide">
              {title || 'Accounting Equation Verified & Balanced'}
            </h4>
            <p className="text-[11px] opacity-90">
              {successMessage ||
                `${leftLabel}: ${formatCurrency(leftValue)} = ${rightLabel}: ${formatCurrency(rightValue)}`}
            </p>
          </div>
        </div>
        <div className="hidden sm:flex items-center gap-3 text-right">
          <div className="border-r border-emerald-500/20 pr-3">
            <span className="text-[10px] block uppercase font-bold opacity-75">{leftLabel}</span>
            <span className="font-mono text-xs font-bold">{formatCurrency(leftValue)}</span>
          </div>
          <div>
            <span className="text-[10px] block uppercase font-bold opacity-75">{rightLabel}</span>
            <span className="font-mono text-xs font-bold">{formatCurrency(rightValue)}</span>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="p-4 rounded-2xl bg-rose-500/10 border border-rose-500/20 text-rose-700 dark:text-rose-400 flex items-center justify-between gap-4 animate-fadeIn">
      <div className="flex items-center gap-3">
        <div className="w-9 h-9 rounded-xl bg-rose-500/20 text-rose-600 dark:text-rose-400 flex items-center justify-center shrink-0">
          <AlertTriangle className="w-5 h-5" />
        </div>
        <div>
          <h4 className="text-xs font-extrabold uppercase tracking-wide">
            Accounting Disequilibrium Detected!
          </h4>
          <p className="text-[11px] opacity-90">
            {leftLabel} ({formatCurrency(leftValue)}) does not equal {rightLabel} ({formatCurrency(rightValue)}). Variance: {formatCurrency(difference)}
          </p>
        </div>
      </div>
      <div className="text-right">
        <span className="text-[10px] block uppercase font-bold text-rose-500">Net Variance</span>
        <span className="font-mono text-xs font-bold">{formatCurrency(difference)}</span>
      </div>
    </div>
  );
};
