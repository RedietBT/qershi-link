import React from 'react';
import { UserCheck, ShieldCheck } from 'lucide-react';
import { RoleClearanceSelector } from './RoleClearanceSelector';

export const MemberWorkflowTab = ({ rules, onToggle, onStringChange }) => {
  return (
    <div className="space-y-6 animate-fadeIn">
      {/* Workflow Policy Switch Card */}
      <div className="bdae-card p-6 border border-[var(--bdae-border)] rounded-2xl flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-start space-x-3.5">
          <div className="w-10 h-10 rounded-xl bg-purple-500/10 text-purple-600 flex items-center justify-center shrink-0">
            <UserCheck className="w-5 h-5" />
          </div>
          <div className="space-y-1">
            <div className="flex items-center space-x-2">
              <h3 className="text-xs font-bold text-[var(--bdae-text-primary)]">
                Member Onboarding & KYC Dual-Control (Four-Eyes)
              </h3>
              <span
                className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                  rules.enableMemberOnboardingChecker
                    ? 'bg-emerald-500/10 text-emerald-600 border border-emerald-500/20'
                    : 'bg-gray-500/10 text-gray-500 border border-gray-500/20'
                }`}
              >
                {rules.enableMemberOnboardingChecker ? 'Four-Eyes Active' : 'Direct Activation'}
              </span>
            </div>
            <p className="text-[11px] text-[var(--bdae-text-secondary)] leading-relaxed max-w-3xl">
              When active, new member profiles remain in <code className="font-mono text-[10px]">PENDING_ONBOARDING</code> until an authorized Checker reviews the Kebele ID, verifies identity specimen, and submits supervisor approval.
            </p>
          </div>
        </div>

        <button
          type="button"
          onClick={() => onToggle('enableMemberOnboardingChecker')}
          className={`w-14 h-7 flex items-center rounded-full p-1 transition-colors shrink-0 ${
            rules.enableMemberOnboardingChecker ? 'bg-[#00CDDB]' : 'bg-gray-300 dark:bg-gray-700'
          }`}
        >
          <div
            className={`bg-white w-5 h-5 rounded-full shadow-md transform transition-transform ${
              rules.enableMemberOnboardingChecker ? 'translate-x-7' : 'translate-x-0'
            }`}
          />
        </button>
      </div>

      {/* Role Clearance Assignment Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <RoleClearanceSelector
          title="Eligible Member Registration Makers"
          subtitle="Roles permitted to capture member demographics and upload KYC documents"
          type="maker"
          selectedRolesString={rules.memberMakerRoles}
          onChange={(val) => onStringChange('memberMakerRoles', val)}
        />

        <RoleClearanceSelector
          title="Eligible KYC Verification Checkers"
          subtitle="Roles permitted to inspect documents, approve onboarding, and activate members"
          type="checker"
          selectedRolesString={rules.memberCheckerRoles}
          onChange={(val) => onStringChange('memberCheckerRoles', val)}
        />
      </div>
    </div>
  );
};
