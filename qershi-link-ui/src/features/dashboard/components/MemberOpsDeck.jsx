import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Contact, ShieldCheck, UserCheck } from 'lucide-react';
import { PermissionGuard } from '../../../common/components/PermissionGuard';

/**
 * Member Onboarding & KYC Operations Dashboard Deck
 */
export const MemberOpsDeck = () => {
  const navigate = useNavigate();

  return (
    <PermissionGuard
      roles={['SUPER_ADMIN', 'SACCO_ADMIN']}
      authorities={['MEMBER_VIEW_BASIC', 'MEMBER_VIEW_FULL', 'KYC_VIEW']}
    >
      <div className="bdae-card p-6 space-y-4 border border-[var(--bdae-border)] shadow-xl">
        <h2 className="text-sm font-bold border-b border-[var(--bdae-border)] pb-2 flex items-center gap-2">
          <UserCheck className="w-4 h-4 text-purple-500" />
          <span>Member Services & Identity Operations</span>
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
          {/* Card 1: Member Profiles */}
          <PermissionGuard roles={['SUPER_ADMIN', 'SACCO_ADMIN']} authorities={['MEMBER_VIEW_BASIC', 'MEMBER_VIEW_FULL']}>
            <div
              onClick={() => navigate('/members')}
              className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-purple-500 cursor-pointer space-y-2 transition-all shadow-sm group"
            >
              <div className="w-9 h-9 rounded-xl bg-purple-500/10 text-purple-600 flex items-center justify-center font-bold">
                <Contact className="w-5 h-5" />
              </div>
              <div>
                <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-purple-500 transition-colors">
                  Member Profile Ops
                </p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                  Onboard, approve, and manage SACCO member profiles and registry details.
                </p>
              </div>
            </div>
          </PermissionGuard>

          {/* Card 2: KYC Validations */}
          <PermissionGuard roles={['SUPER_ADMIN', 'SACCO_ADMIN']} authorities={['KYC_VIEW']}>
            <div
              onClick={() => navigate('/kyc-verifications')}
              className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-indigo-500 cursor-pointer space-y-2 transition-all shadow-sm group"
            >
              <div className="w-9 h-9 rounded-xl bg-indigo-500/10 text-indigo-600 flex items-center justify-center font-bold">
                <ShieldCheck className="w-5 h-5" />
              </div>
              <div>
                <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-indigo-500 transition-colors">
                  KYC Verification Queue
                </p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                  Review government IDs, document dossiers, and execute Maker-Checker compliance.
                </p>
              </div>
            </div>
          </PermissionGuard>
        </div>
      </div>
    </PermissionGuard>
  );
};
