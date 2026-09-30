import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Moon } from 'lucide-react';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';

/**
 * End-of-Day (EOD) & Batch Operations Dashboard Deck
 */
export const BatchOperationsDeck = () => {
  const navigate = useNavigate();

  return (
    <PermissionGuard permissions={[PERMISSIONS.EOD_VIEW, PERMISSIONS.EOD_EXECUTE]}>
      <div className="bdae-card p-6 space-y-4 border border-[var(--bdae-border)] shadow-xl">
        <h2 className="text-sm font-bold border-b border-[var(--bdae-border)] pb-2 flex items-center gap-2">
          <Moon className="w-4 h-4 text-amber-500" />
          <span>Core Batch & Operations Engine</span>
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 text-xs">
          {/* Card 1: End-of-Day Batch Operations */}
          <PermissionGuard permissions={[PERMISSIONS.EOD_VIEW, PERMISSIONS.EOD_EXECUTE]}>
            <div
              onClick={() => navigate('/operations/eod')}
              className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-amber-500 cursor-pointer space-y-2 transition-all shadow-sm group"
            >
              <div className="w-9 h-9 rounded-xl bg-amber-500/10 text-amber-600 flex items-center justify-center font-bold">
                <Moon className="w-5 h-5" />
              </div>
              <div>
                <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-amber-500 transition-colors">
                  End-of-Day (EOD) Batch
                </p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                  Interest accruals, fee capitalizations, till closures, and financial cutoff sequences.
                </p>
              </div>
            </div>
          </PermissionGuard>
        </div>
      </div>
    </PermissionGuard>
  );
};
