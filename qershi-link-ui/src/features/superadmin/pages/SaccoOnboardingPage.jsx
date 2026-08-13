import React from 'react';
import { SaccoOnboardingForm } from '../components/SaccoOnboardingForm';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { Building2, ShieldAlert, Lock } from 'lucide-react';

export const SaccoOnboardingPage = () => {
  return (
    <PermissionGuard role="ROLE_SUPER_ADMIN" fallback={
      <div className="p-8 text-center max-w-lg mx-auto space-y-4">
        <div className="w-12 h-12 rounded-full bg-amber-500/10 border border-amber-500/30 text-amber-600 mx-auto flex items-center justify-center">
          <Lock className="w-6 h-6" />
        </div>
        <h2 className="text-lg font-bold">Access Restricted</h2>
        <p className="text-xs text-[var(--bdae-text-secondary)]">
          SACCO Tenant Onboarding requires Super Admin authorization (<code className="font-mono bg-black/10 dark:bg-white/10 px-1 py-0.5 rounded">ROLE_SUPER_ADMIN</code>).
        </p>
      </div>
    }>
      <div className="space-y-6 max-w-3xl mx-auto">
        <div className="flex items-center space-x-3 border-b border-[var(--bdae-border)] pb-4">
          <div 
            className="w-10 h-10 rounded-xl flex items-center justify-center text-white shadow-md"
            style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
          >
            <Building2 className="w-5 h-5" />
          </div>
          <div>
            <h1 className="text-xl font-extrabold tracking-tight">Onboard New SACCO Tenant</h1>
            <p className="text-xs text-[var(--bdae-text-secondary)]">
              Register a SACCO tenant and assign initial administrator credentials (<code className="font-mono">POST /api/v1/sacco/onboard</code>).
            </p>
          </div>
        </div>

        <div className="bdae-card p-6 md:p-8 border border-[var(--bdae-border)] shadow-xl">
          <SaccoOnboardingForm />
        </div>
      </div>
    </PermissionGuard>
  );
};
