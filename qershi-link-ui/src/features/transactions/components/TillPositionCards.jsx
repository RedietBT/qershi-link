import React from 'react';
import { DollarSign, ShieldCheck, CheckCircle2, Lock, Unlock } from 'lucide-react';
import { formatCurrency } from '../../../common/utils/currency';

/**
 * Visual summary of electronic drawer balances, status, and opening float
 */
export const TillPositionCards = ({ till, isTillOpen, electronicBalance }) => {
  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      {/* Current Electronic Cash */}
      <div className="bdae-card p-5 border border-[var(--bdae-border)] space-y-2">
        <div className="flex items-center justify-between text-xs text-[var(--bdae-text-secondary)] font-bold uppercase tracking-wider">
          <span>Electronic Cash Position</span>
          <DollarSign className="w-4 h-4 text-emerald-500" />
        </div>
        <p className="text-2xl font-black text-[var(--bdae-text-primary)] font-mono">
          {formatCurrency(electronicBalance)}
        </p>
        <p className="text-[11px] text-[var(--bdae-text-secondary)]">
          Real-time General Ledger cash account balance
        </p>
      </div>

      {/* Opening Float */}
      <div className="bdae-card p-5 border border-[var(--bdae-border)] space-y-2">
        <div className="flex items-center justify-between text-xs text-[var(--bdae-text-secondary)] font-bold uppercase tracking-wider">
          <span>Opening Cash Float</span>
          <ShieldCheck className="w-4 h-4 text-cyan-500" />
        </div>
        <p className="text-2xl font-black text-[var(--bdae-text-primary)] font-mono">
          {formatCurrency(Number(till?.openingCash) || 0)}
        </p>
        <p className="text-[11px] text-[var(--bdae-text-secondary)]">
          Vault allocation recorded at shift open
        </p>
      </div>

      {/* Shift Operating State */}
      <div className="bdae-card p-5 border border-[var(--bdae-border)] space-y-2">
        <div className="flex items-center justify-between text-xs text-[var(--bdae-text-secondary)] font-bold uppercase tracking-wider">
          <span>Drawer Shift State</span>
          {isTillOpen ? (
            <Unlock className="w-4 h-4 text-emerald-500" />
          ) : (
            <Lock className="w-4 h-4 text-amber-500" />
          )}
        </div>
        <div className="flex items-center gap-2">
          <span
            className={`w-3 h-3 rounded-full ${
              isTillOpen ? 'bg-emerald-500 animate-pulse' : 'bg-amber-500'
            }`}
          />
          <span className="text-xl font-black text-[var(--bdae-text-primary)]">
            {isTillOpen ? 'DRAWER ACTIVE' : 'DRAWER CLOSED'}
          </span>
        </div>
        <p className="text-[11px] text-[var(--bdae-text-secondary)]">
          {isTillOpen ? 'Authorized for OTC deposits & withdrawals' : 'Must open shift to process OTC cash'}
        </p>
      </div>
    </div>
  );
};
