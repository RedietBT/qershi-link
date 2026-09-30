import React, { useState, useEffect } from 'react';
import {
  ShieldCheck,
  Lock,
  UserCheck,
  CreditCard,
  FileCheck,
  Save,
  RefreshCw,
  Sliders,
  CheckCircle2,
  AlertTriangle
} from 'lucide-react';
import { makerCheckerApi } from '../api/makerCheckerApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { GlobalGovernanceTab } from '../components/GlobalGovernanceTab';
import { MemberWorkflowTab } from '../components/MemberWorkflowTab';
import { AccountsWorkflowTab } from '../components/AccountsWorkflowTab';
import { LoansWorkflowTab } from '../components/LoansWorkflowTab';

export const MakerCheckerPolicyPage = () => {
  const [activeTab, setActiveTab] = useState('global');

  const [rules, setRules] = useState({
    enableMemberOnboardingChecker: true,
    enableAccountOpeningChecker: true,
    enableAccountFreezeChecker: true,
    enableLoanApprovalChecker: true,
    enableLoanDisbursementChecker: true,
    transactionCheckerThreshold: 50000,
    dailyAccountLimitThreshold: 200000,
    enforceAntiSelfApproval: true,
    memberMakerRoles: 'TELLER,CUSTOMER_SERVICE,ADMIN',
    memberCheckerRoles: 'BRANCH_MANAGER,SACCO_ADMIN,AUDITOR',
    accountMakerRoles: 'TELLER,CUSTOMER_SERVICE,ADMIN',
    accountCheckerRoles: 'BRANCH_MANAGER,SACCO_ADMIN',
    loanMakerRoles: 'LOAN_OFFICER,ADMIN',
    loanCheckerRoles: 'BRANCH_MANAGER,SACCO_ADMIN'
  });

  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);
  const [error, setError] = useState(null);

  const fetchRules = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await makerCheckerApi.getRules();
      if (data) {
        setRules({
          enableMemberOnboardingChecker: data.enableMemberOnboardingChecker ?? true,
          enableAccountOpeningChecker: data.enableAccountOpeningChecker ?? true,
          enableAccountFreezeChecker: data.enableAccountFreezeChecker ?? true,
          enableLoanApprovalChecker: data.enableLoanApprovalChecker ?? true,
          enableLoanDisbursementChecker: data.enableLoanDisbursementChecker ?? true,
          transactionCheckerThreshold: data.transactionCheckerThreshold ?? 50000,
          dailyAccountLimitThreshold: data.dailyAccountLimitThreshold ?? 200000,
          enforceAntiSelfApproval: data.enforceAntiSelfApproval ?? true,
          memberMakerRoles: data.memberMakerRoles || 'TELLER,CUSTOMER_SERVICE,ADMIN',
          memberCheckerRoles: data.memberCheckerRoles || 'BRANCH_MANAGER,SACCO_ADMIN,AUDITOR',
          accountMakerRoles: data.accountMakerRoles || 'TELLER,CUSTOMER_SERVICE,ADMIN',
          accountCheckerRoles: data.accountCheckerRoles || 'BRANCH_MANAGER,SACCO_ADMIN',
          loanMakerRoles: data.loanMakerRoles || 'LOAN_OFFICER,ADMIN',
          loanCheckerRoles: data.loanCheckerRoles || 'BRANCH_MANAGER,SACCO_ADMIN'
        });
      }
    } catch (err) {
      console.error('Failed to load Maker-Checker policy rules:', err);
      setError('Failed to load SACCO policy rules. Using default baseline.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchRules();
  }, []);

  const handleToggle = (key) => {
    setRules((prev) => ({
      ...prev,
      [key]: !prev[key]
    }));
    setSaveSuccess(false);
  };

  const handleNumberChange = (key, value) => {
    const num = parseFloat(value);
    setRules((prev) => ({
      ...prev,
      [key]: isNaN(num) ? 0 : num
    }));
    setSaveSuccess(false);
  };

  const handleStringChange = (key, value) => {
    setRules((prev) => ({
      ...prev,
      [key]: value
    }));
    setSaveSuccess(false);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setIsSaving(true);
    setError(null);
    setSaveSuccess(false);
    try {
      await makerCheckerApi.updateRules(rules);
      setSaveSuccess(true);
      setTimeout(() => setSaveSuccess(false), 4000);
    } catch (err) {
      console.error('Failed to save Maker-Checker rules:', err);
      setError(err.response?.data?.message || 'Failed to update policy rules.');
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading) {
    return (
      <div className="bdae-card p-12 text-center space-y-3 max-w-7xl mx-auto border border-[var(--bdae-border)]">
        <RefreshCw className="w-8 h-8 text-[#00CDDB] animate-spin mx-auto" />
        <p className="text-xs font-semibold text-[var(--bdae-text-secondary)]">
          Loading SACCO Four-Eyes Governance Policies & Rule Engine...
        </p>
      </div>
    );
  }

  const TABS = [
    { id: 'global', label: 'Global Governance', icon: Sliders },
    { id: 'members', label: 'Member Onboarding', icon: UserCheck },
    { id: 'accounts', label: 'Accounts & Products', icon: CreditCard },
    { id: 'loans', label: 'Credit & Lending', icon: FileCheck }
  ];

  return (
    <PermissionGuard
      roles={['SUPER_ADMIN', 'SACCO_ADMIN', 'ADMIN', 'AUDITOR']}
      permissions={['SACCO_CONFIG', 'ACCOUNT_VIEW']}
      fallback={
        <div className="p-8 text-center max-w-lg mx-auto space-y-4">
          <div className="w-12 h-12 rounded-full bg-amber-500/10 border border-amber-500/30 text-amber-600 mx-auto flex items-center justify-center">
            <Lock className="w-6 h-6" />
          </div>
          <h2 className="text-lg font-bold">Access Restricted</h2>
          <p className="text-xs text-[var(--bdae-text-secondary)]">
            Maker-Checker & Governance Rule Management requires Administrator authorization.
          </p>
        </div>
      }
    >
      <div className="space-y-6 animate-fadeIn max-w-7xl mx-auto pb-12">
        {/* Page Header */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-[var(--bdae-border)] pb-4">
          <div className="flex items-center space-x-3">
            <div
              className="w-10 h-10 rounded-xl flex items-center justify-center text-white shadow-md shrink-0"
              style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
            >
              <ShieldCheck className="w-5 h-5 text-white" />
            </div>
            <div>
              <h1 className="text-xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
                Maker-Checker Policy & Four-Eyes Governance
              </h1>
              <p className="text-xs text-[var(--bdae-text-secondary)]">
                Configure organizational dual-control rules, high-value transaction thresholds, and domain Maker/Checker role clearances.
              </p>
            </div>
          </div>

          <div className="flex items-center space-x-3">
            <button
              onClick={fetchRules}
              disabled={isLoading}
              className="px-3.5 py-2 rounded-xl border border-[var(--bdae-border)] hover:border-[var(--bdae-secondary)] text-xs font-bold flex items-center gap-2 transition-all shadow-sm"
            >
              <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
              <span>Reset</span>
            </button>

            <PermissionGuard permissions={['SACCO_CONFIG', 'ROLE_MANAGE']} roles={['SUPER_ADMIN', 'SACCO_ADMIN']}>
              <button
                onClick={handleSave}
                disabled={isSaving}
                className="bdae-btn-primary px-5 py-2 text-xs font-bold rounded-xl flex items-center gap-2 shadow-md"
              >
                {isSaving ? <RefreshCw className="w-4 h-4 animate-spin" /> : <Save className="w-4 h-4" />}
                <span>Save All Rules</span>
              </button>
            </PermissionGuard>
          </div>
        </div>

        {/* Success / Error Feedback */}
        {saveSuccess && (
          <div className="p-4 rounded-2xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-600 dark:text-emerald-400 text-xs font-bold flex items-center gap-2.5 animate-fadeIn">
            <CheckCircle2 className="w-5 h-5 shrink-0" />
            <span>SACCO Maker-Checker governance rules updated and synchronized across all branches!</span>
          </div>
        )}

        {error && (
          <div className="p-4 rounded-2xl bg-red-500/10 border border-red-500/30 text-red-600 dark:text-red-400 text-xs font-bold flex items-center gap-2.5 animate-fadeIn">
            <AlertTriangle className="w-5 h-5 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {/* Tab Navigation Bar */}
        <div className="flex items-center gap-1.5 p-1 bg-black/5 dark:bg-white/5 rounded-2xl border border-[var(--bdae-border)] w-fit">
          {TABS.map((tab) => {
            const Icon = tab.icon;
            const isActive = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`px-4 py-2 rounded-xl text-xs font-bold flex items-center gap-2 transition-all ${
                  isActive
                    ? 'bg-[#00CDDB] text-black shadow-md font-extrabold'
                    : 'text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)] hover:bg-black/5 dark:hover:bg-white/5'
                }`}
              >
                <Icon className="w-4 h-4" />
                <span>{tab.label}</span>
              </button>
            );
          })}
        </div>

        {/* Active Tab View */}
        {activeTab === 'global' && (
          <GlobalGovernanceTab
            rules={rules}
            onToggle={handleToggle}
            onNumberChange={handleNumberChange}
          />
        )}

        {activeTab === 'members' && (
          <MemberWorkflowTab
            rules={rules}
            onToggle={handleToggle}
            onStringChange={handleStringChange}
          />
        )}

        {activeTab === 'accounts' && (
          <AccountsWorkflowTab
            rules={rules}
            onToggle={handleToggle}
            onStringChange={handleStringChange}
          />
        )}

        {activeTab === 'loans' && (
          <LoansWorkflowTab
            rules={rules}
            onToggle={handleToggle}
            onStringChange={handleStringChange}
          />
        )}

      </div>
    </PermissionGuard>
  );
};
