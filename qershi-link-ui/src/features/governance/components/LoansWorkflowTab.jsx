import React from 'react';
import { FileCheck, DollarSign } from 'lucide-react';
import { RoleClearanceSelector } from './RoleClearanceSelector';

export const LoansWorkflowTab = ({ rules, onToggle, onStringChange }) => {
  return (
    <div className="space-y-6 animate-fadeIn">
      {/* Workflow Policy Switch Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* 1. Loan Underwriting Approval */}
        <div className="bdae-card p-6 border border-[var(--bdae-border)] rounded-2xl flex flex-col justify-between space-y-4">
          <div className="flex items-start justify-between">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 rounded-xl bg-amber-500/10 text-amber-600 flex items-center justify-center shrink-0">
                <FileCheck className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-xs font-bold text-[var(--bdae-text-primary)]">
                  Loan Underwriting Approval
                </h3>
                <span
                  className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                    rules.enableLoanApprovalChecker
                      ? 'bg-emerald-500/10 text-emerald-600 border border-emerald-500/20'
                      : 'bg-gray-500/10 text-gray-500 border border-gray-500/20'
                  }`}
                >
                  {rules.enableLoanApprovalChecker ? 'Committee / Manager' : 'Officer Approval'}
                </span>
              </div>
            </div>

            <button
              type="button"
              onClick={() => onToggle('enableLoanApprovalChecker')}
              className={`w-11 h-6 flex items-center rounded-full p-1 transition-colors shrink-0 ${
                rules.enableLoanApprovalChecker ? 'bg-[#00CDDB]' : 'bg-gray-300 dark:bg-gray-700'
              }`}
            >
              <div
                className={`bg-white w-4 h-4 rounded-full shadow-md transform transition-transform ${
                  rules.enableLoanApprovalChecker ? 'translate-x-5' : 'translate-x-0'
                }`}
              />
            </button>
          </div>
          <p className="text-[11px] text-[var(--bdae-text-secondary)] leading-relaxed">
            Requires formal underwriting review by a Credit Committee member or Branch Manager before loan approval.
          </p>
        </div>

        {/* 2. Loan Disbursement Fund Release */}
        <div className="bdae-card p-6 border border-[var(--bdae-border)] rounded-2xl flex flex-col justify-between space-y-4">
          <div className="flex items-start justify-between">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 rounded-xl bg-emerald-500/10 text-emerald-600 flex items-center justify-center shrink-0">
                <DollarSign className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-xs font-bold text-[var(--bdae-text-primary)]">
                  Disbursement Release Sign-Off
                </h3>
                <span
                  className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                    rules.enableLoanDisbursementChecker
                      ? 'bg-emerald-500/10 text-emerald-600 border border-emerald-500/20'
                      : 'bg-gray-500/10 text-gray-500 border border-gray-500/20'
                  }`}
                >
                  {rules.enableLoanDisbursementChecker ? 'Finance Sign-Off' : 'Auto Disbursement'}
                </span>
              </div>
            </div>

            <button
              type="button"
              onClick={() => onToggle('enableLoanDisbursementChecker')}
              className={`w-11 h-6 flex items-center rounded-full p-1 transition-colors shrink-0 ${
                rules.enableLoanDisbursementChecker ? 'bg-[#00CDDB]' : 'bg-gray-300 dark:bg-gray-700'
              }`}
            >
              <div
                className={`bg-white w-4 h-4 rounded-full shadow-md transform transition-transform ${
                  rules.enableLoanDisbursementChecker ? 'translate-x-5' : 'translate-x-0'
                }`}
              />
            </button>
          </div>
          <p className="text-[11px] text-[var(--bdae-text-secondary)] leading-relaxed">
            Requires Finance or Operations sign-off to execute disbursement voucher and trigger automatic guarantor savings liens.
          </p>
        </div>
      </div>

      {/* Role Clearance Assignment Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <RoleClearanceSelector
          title="Eligible Loan Origination Makers"
          subtitle="Roles permitted to capture applications, evaluate scoring, and collect guarantor pledges"
          type="maker"
          selectedRolesString={rules.loanMakerRoles}
          onChange={(val) => onStringChange('loanMakerRoles', val)}
        />

        <RoleClearanceSelector
          title="Eligible Credit Committee & Disbursement Checkers"
          subtitle="Roles permitted to underwrite loans, approve terms, and authorize disbursement release"
          type="checker"
          selectedRolesString={rules.loanCheckerRoles}
          onChange={(val) => onStringChange('loanCheckerRoles', val)}
        />
      </div>
    </div>
  );
};
