import React from 'react';
import { Layout } from '../../../common/components/Layout';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { SaccoConfigCard } from '../components/SaccoConfigCard';
import { Settings, Lock } from 'lucide-react';

/**
 * SACCO Configuration Page — Phase 1 of Account Management.
 *
 * Gated to SACCO_ADMIN and ADMIN roles with ACCOUNT_VIEW permission.
 * Read-only view for authorized staff, full edit for ROLE_MANAGE holders.
 */
export const SaccoConfigPage = () => {
    return (
        <PermissionGuard
            roles={['SACCO_ADMIN', 'ADMIN']}
            permissions={['ACCOUNT_VIEW']}
            fallback={
                <div className="p-8 text-center max-w-lg mx-auto space-y-4 mt-10">
                    <div className="w-14 h-14 rounded-2xl bg-red-500/10 border border-red-500/20 text-red-500 mx-auto flex items-center justify-center">
                        <Lock className="w-7 h-7" />
                    </div>
                    <h2 className="text-lg font-bold">Access Restricted</h2>
                    <p className="text-xs text-[var(--bdae-text-secondary)]">
                        SACCO Configuration requires SACCO Admin authorization or the <span className="font-mono font-bold">ACCOUNT_VIEW</span> permission.
                    </p>
                </div>
            }
        >
            <div className="space-y-6 animate-fadeIn max-w-2xl mx-auto">
                {/* Page Header */}
                <div className="flex items-center gap-3 border-b border-[var(--bdae-border)] pb-4">
                    <div
                        className="w-10 h-10 rounded-xl flex items-center justify-center text-white shadow-md shrink-0"
                        style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
                    >
                        <Settings className="w-5 h-5" />
                    </div>
                    <div>
                        <h1 className="text-xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
                            SACCO Entity Configuration
                        </h1>
                        <p className="text-xs text-[var(--bdae-text-secondary)]">
                            Configure the foundational SACCO code used for ISO Luhn account number generation.
                        </p>
                    </div>
                </div>

                {/* The main config card */}
                <SaccoConfigCard />

                {/* Info Notice */}
                <div className="text-[11px] text-[var(--bdae-text-secondary)] border border-[var(--bdae-border)] rounded-xl p-4 bg-black/5 dark:bg-white/5 leading-relaxed">
                    <span className="font-bold text-[var(--bdae-primary)]">⚠ Important:</span> The SACCO Code is a permanent identifier embedded in every ledger account number generated within this tenant.
                    Changing this code after accounts have been opened is <span className="font-bold text-red-500">strongly discouraged</span> as it will break number consistency.
                    Coordinate with your core banking system administrator before any changes.
                </div>
            </div>
        </PermissionGuard>
    );
};
