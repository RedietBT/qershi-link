import React from 'react';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { DepositProductsDashboard } from '../components/DepositProductsDashboard';
import { PackagePlus, Lock } from 'lucide-react';

/**
 * Deposit Products Page — Phase 2 of Account Management.
 * Gated: SACCO_ADMIN / ADMIN or PRODUCT_VIEW permission.
 */
export const DepositProductsPage = () => {
    return (
        <PermissionGuard
            roles={['SACCO_ADMIN', 'ADMIN']}
            permissions={['PRODUCT_VIEW']}
            fallback={
                <div className="p-8 text-center max-w-lg mx-auto space-y-4 mt-10">
                    <div className="w-14 h-14 rounded-2xl bg-red-500/10 border border-red-500/20 text-red-500 mx-auto flex items-center justify-center">
                        <Lock className="w-7 h-7" />
                    </div>
                    <h2 className="text-lg font-bold">Access Restricted</h2>
                    <p className="text-xs text-[var(--bdae-text-secondary)]">
                        Deposit Products requires SACCO Admin authorization or the <span className="font-mono font-bold">PRODUCT_VIEW</span> permission.
                    </p>
                </div>
            }
        >
            <div className="space-y-6 animate-fadeIn">
                {/* Page Header */}
                <div className="flex items-center gap-3 border-b border-[var(--bdae-border)] pb-4">
                    <div
                        className="w-10 h-10 rounded-xl flex items-center justify-center text-white shadow-md shrink-0"
                        style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
                    >
                        <PackagePlus className="w-5 h-5" />
                    </div>
                    <div>
                        <h1 className="text-xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
                            Deposit Product Factory
                        </h1>
                        <p className="text-xs text-[var(--bdae-text-secondary)]">
                            Configure SACCO-wide deposit products. Each product auto-assigns a unique 3-digit product code.
                        </p>
                    </div>
                </div>

                <DepositProductsDashboard />
            </div>
        </PermissionGuard>
    );
};
