import React, { useState, useEffect } from 'react';
import {
  ShieldCheck,
  Lock,
  UserCheck,
  CreditCard,
  Ban,
  FileCheck,
  DollarSign,
  AlertTriangle,
  Save,
  RefreshCw,
  Sliders,
  CheckCircle2,
  Info
} from 'lucide-react';
import { makerCheckerApi } from '../api/makerCheckerApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';

export const MakerCheckerPolicyPage = () => {
  const [rules, setRules] = useState({
    enableMemberOnboardingChecker: true,
    enableAccountOpeningChecker: true,
    enableAccountFreezeChecker: true,
    enableLoanApprovalChecker: true,
    enableLoanDisbursementChecker: true,
    transactionCheckerThreshold: 50000,
    dailyAccountLimitThreshold: 200000,
    enforceAntiSelfApproval: true
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
          enforceAntiSelfApproval: data.enforceAntiSelfApproval ?? true
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
                Configure organizational dual-control rules, high-value transaction thresholds, and anti-self-approval enforcement.
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
                <span>Save Policy Rules</span>
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

        {/* Regulatory Banner: Anti-Self-Approval Principle */}
        <div className="p-5 rounded-2xl bg-gradient-to-r from-[#00CDDB]/10 via-[#004B87]/5 to-transparent border border-[#00CDDB]/30 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 shadow-sm">
          <div className="flex items-start space-x-3.5">
            <div className="w-9 h-9 rounded-xl bg-[#00CDDB]/20 border border-[#00CDDB]/40 flex items-center justify-center text-[#00CDDB] shrink-0 mt-0.5 sm:mt-0">
              <Lock className="w-5 h-5" />
            </div>
            <div className="space-y-1">
              <span className="text-xs font-extrabold text-[var(--bdae-text-primary)] block">
                Banking Regulatory Standard: Anti-Self-Approval Enforcement (Maker ≠ Checker)
              </span>
              <p className="text-[11px] text-[var(--bdae-text-secondary)] leading-relaxed">
                Under the Four-Eyes principle, any transaction or request initiated by a staff member (<code className="font-mono text-[var(--bdae-text-primary)]">maker_user_id</code>) can <strong>never</strong> be authorized or approved by that same user, even if they hold Senior Supervisor or Branch Manager credentials.
              </p>
            </div>
          </div>

          <div className="shrink-0 flex items-center space-x-2 px-3.5 py-1.5 rounded-full border border-emerald-500/30 bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 text-xs font-bold">
            <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
            <span>Enforcement Active</span>
          </div>
        </div>

        {/* Section 1: Domain Four-Eyes Workflow Toggles */}
        <div className="space-y-4">
          <div className="flex items-center space-x-2">
            <Sliders className="w-4 h-4 text-[#00CDDB]" />
            <h2 className="text-sm font-bold tracking-tight text-[var(--bdae-text-primary)] uppercase">
              Domain Approval Workflows
            </h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {/* 1. Member Onboarding */}
            <div className="bdae-card p-5 border border-[var(--bdae-border)] rounded-2xl flex flex-col justify-between space-y-4 hover:border-[#00CDDB]/40 transition-all">
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <div className="w-8 h-8 rounded-lg bg-purple-500/10 text-purple-600 flex items-center justify-center">
                    <UserCheck className="w-4 h-4" />
                  </div>
                  <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                    rules.enableMemberOnboardingChecker
                      ? 'bg-emerald-500/10 text-emerald-600 border border-emerald-500/20'
                      : 'bg-gray-500/10 text-gray-500 border border-gray-500/20'
                  }`}>
                    {rules.enableMemberOnboardingChecker ? 'Four-Eyes Active' : 'Direct Activation'}
                  </span>
                </div>
                <h3 className="text-xs font-bold text-[var(--bdae-text-primary)]">
                  Member Onboarding & KYC
                </h3>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] leading-relaxed">
                  Requires Customer Service Representative to register applicant, and a separate Compliance Officer or Branch Manager to verify KYC and approve.
                </p>
              </div>

              <div className="pt-2 border-t border-[var(--bdae-border)] flex items-center justify-between">
                <span className="text-[11px] font-semibold text-[var(--bdae-text-secondary)]">Require Checker</span>
                <button
                  type="button"
                  onClick={() => handleToggle('enableMemberOnboardingChecker')}
                  className={`w-11 h-6 flex items-center rounded-full p-1 transition-colors ${
                    rules.enableMemberOnboardingChecker ? 'bg-[#00CDDB]' : 'bg-gray-300 dark:bg-gray-700'
                  }`}
                >
                  <div
                    className={`bg-white w-4 h-4 rounded-full shadow-md transform transition-transform ${
                      rules.enableMemberOnboardingChecker ? 'translate-x-5' : 'translate-x-0'
                    }`}
                  />
                </button>
              </div>
            </div>

            {/* 2. Account Opening */}
            <div className="bdae-card p-5 border border-[var(--bdae-border)] rounded-2xl flex flex-col justify-between space-y-4 hover:border-[#00CDDB]/40 transition-all">
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <div className="w-8 h-8 rounded-lg bg-blue-500/10 text-blue-600 flex items-center justify-center">
                    <CreditCard className="w-4 h-4" />
                  </div>
                  <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                    rules.enableAccountOpeningChecker
                      ? 'bg-emerald-500/10 text-emerald-600 border border-emerald-500/20'
                      : 'bg-gray-500/10 text-gray-500 border border-gray-500/20'
                  }`}>
                    {rules.enableAccountOpeningChecker ? 'Four-Eyes Active' : 'Direct Activation'}
                  </span>
                </div>
                <h3 className="text-xs font-bold text-[var(--bdae-text-primary)]">
                  Savings & Deposit Account Opening
                </h3>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] leading-relaxed">
                  Holds new accounts in <code className="font-mono text-[10px]">PENDING_APPROVAL</code> until initial deposit, ledger binding, and product rules are verified by supervisor.
                </p>
              </div>

              <div className="pt-2 border-t border-[var(--bdae-border)] flex items-center justify-between">
                <span className="text-[11px] font-semibold text-[var(--bdae-text-secondary)]">Require Checker</span>
                <button
                  type="button"
                  onClick={() => handleToggle('enableAccountOpeningChecker')}
                  className={`w-11 h-6 flex items-center rounded-full p-1 transition-colors ${
                    rules.enableAccountOpeningChecker ? 'bg-[#00CDDB]' : 'bg-gray-300 dark:bg-gray-700'
                  }`}
                >
                  <div
                    className={`bg-white w-4 h-4 rounded-full shadow-md transform transition-transform ${
                      rules.enableAccountOpeningChecker ? 'translate-x-5' : 'translate-x-0'
                    }`}
                  />
                </button>
              </div>
            </div>

            {/* 3. Account Freeze & Lien Holds */}
            <div className="bdae-card p-5 border border-[var(--bdae-border)] rounded-2xl flex flex-col justify-between space-y-4 hover:border-[#00CDDB]/40 transition-all">
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <div className="w-8 h-8 rounded-lg bg-red-500/10 text-red-600 flex items-center justify-center">
                    <Ban className="w-4 h-4" />
                  </div>
                  <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                    rules.enableAccountFreezeChecker
                      ? 'bg-emerald-500/10 text-emerald-600 border border-emerald-500/20'
                      : 'bg-gray-500/10 text-gray-500 border border-gray-500/20'
                  }`}>
                    {rules.enableAccountFreezeChecker ? 'Four-Eyes Active' : 'Direct Application'}
                  </span>
                </div>
                <h3 className="text-xs font-bold text-[var(--bdae-text-primary)]">
                  Account Freeze & Administrative Liens
                </h3>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] leading-relaxed">
                  Requires supervisor sign-off before blocking member balances, placing collateral liens, or enacting court-mandated account debit freezes.
                </p>
              </div>

              <div className="pt-2 border-t border-[var(--bdae-border)] flex items-center justify-between">
                <span className="text-[11px] font-semibold text-[var(--bdae-text-secondary)]">Require Checker</span>
                <button
                  type="button"
                  onClick={() => handleToggle('enableAccountFreezeChecker')}
                  className={`w-11 h-6 flex items-center rounded-full p-1 transition-colors ${
                    rules.enableAccountFreezeChecker ? 'bg-[#00CDDB]' : 'bg-gray-300 dark:bg-gray-700'
                  }`}
                >
                  <div
                    className={`bg-white w-4 h-4 rounded-full shadow-md transform transition-transform ${
                      rules.enableAccountFreezeChecker ? 'translate-x-5' : 'translate-x-0'
                    }`}
                  />
                </button>
              </div>
            </div>

            {/* 4. Loan Underwriting Approval */}
            <div className="bdae-card p-5 border border-[var(--bdae-border)] rounded-2xl flex flex-col justify-between space-y-4 hover:border-[#00CDDB]/40 transition-all">
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <div className="w-8 h-8 rounded-lg bg-amber-500/10 text-amber-600 flex items-center justify-center">
                    <FileCheck className="w-4 h-4" />
                  </div>
                  <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                    rules.enableLoanApprovalChecker
                      ? 'bg-emerald-500/10 text-emerald-600 border border-emerald-500/20'
                      : 'bg-gray-500/10 text-gray-500 border border-gray-500/20'
                  }`}>
                    {rules.enableLoanApprovalChecker ? 'Committee / Manager' : 'Officer Approval'}
                  </span>
                </div>
                <h3 className="text-xs font-bold text-[var(--bdae-text-primary)]">
                  Loan Underwriting & Credit Committee
                </h3>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] leading-relaxed">
                  Loan Officers submit loan applications, but final underwriting and interest rate approval must be performed by Credit Committee or Branch Manager.
                </p>
              </div>

              <div className="pt-2 border-t border-[var(--bdae-border)] flex items-center justify-between">
                <span className="text-[11px] font-semibold text-[var(--bdae-text-secondary)]">Require Checker</span>
                <button
                  type="button"
                  onClick={() => handleToggle('enableLoanApprovalChecker')}
                  className={`w-11 h-6 flex items-center rounded-full p-1 transition-colors ${
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
            </div>

            {/* 5. Loan Disbursement Release */}
            <div className="bdae-card p-5 border border-[var(--bdae-border)] rounded-2xl flex flex-col justify-between space-y-4 hover:border-[#00CDDB]/40 transition-all">
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <div className="w-8 h-8 rounded-lg bg-emerald-500/10 text-emerald-600 flex items-center justify-center">
                    <DollarSign className="w-4 h-4" />
                  </div>
                  <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                    rules.enableLoanDisbursementChecker
                      ? 'bg-emerald-500/10 text-emerald-600 border border-emerald-500/20'
                      : 'bg-gray-500/10 text-gray-500 border border-gray-500/20'
                  }`}>
                    {rules.enableLoanDisbursementChecker ? 'Finance Sign-off' : 'Auto Disbursement'}
                  </span>
                </div>
                <h3 className="text-xs font-bold text-[var(--bdae-text-primary)]">
                  Loan Disbursement Fund Release
                </h3>
                <p className="text-[11px] text-[var(--bdae-text-secondary)] leading-relaxed">
                  Requires Finance or Operations Manager to independently authorize releasing loan funds and placing guarantor liens after loan approval.
                </p>
              </div>

              <div className="pt-2 border-t border-[var(--bdae-border)] flex items-center justify-between">
                <span className="text-[11px] font-semibold text-[var(--bdae-text-secondary)]">Require Checker</span>
                <button
                  type="button"
                  onClick={() => handleToggle('enableLoanDisbursementChecker')}
                  className={`w-11 h-6 flex items-center rounded-full p-1 transition-colors ${
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
            </div>
          </div>
        </div>

        {/* Section 2: Financial Threshold Limits & Risk Limits */}
        <div className="space-y-4 pt-4 border-t border-[var(--bdae-border)]">
          <div className="flex items-center space-x-2">
            <DollarSign className="w-4 h-4 text-[#00CDDB]" />
            <h2 className="text-sm font-bold tracking-tight text-[var(--bdae-text-primary)] uppercase">
              Financial Risk Thresholds & Transaction Limits
            </h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Single Transaction Supervisor Threshold */}
            <div className="bdae-card p-6 border border-[var(--bdae-border)] rounded-2xl space-y-4">
              <div className="flex items-start justify-between">
                <div>
                  <h3 className="text-xs font-bold text-[var(--bdae-text-primary)]">
                    Single-Transaction Supervisor Override Threshold
                  </h3>
                  <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                    Cash deposits, withdrawals, or transfers exceeding this amount automatically intercept teller processing and require supervisor authorization.
                  </p>
                </div>
                <div className="w-9 h-9 rounded-xl bg-[#00CDDB]/10 text-[#00CDDB] flex items-center justify-center shrink-0">
                  <AlertTriangle className="w-4 h-4" />
                </div>
              </div>

              <div className="space-y-1.5">
                <label className="text-[10px] font-extrabold uppercase text-[var(--bdae-text-secondary)] tracking-wider">
                  Threshold Limit (ETB)
                </label>
                <div className="relative">
                  <input
                    type="number"
                    min="0"
                    step="500"
                    value={rules.transactionCheckerThreshold}
                    onChange={(e) => handleNumberChange('transactionCheckerThreshold', e.target.value)}
                    className="w-full pl-4 pr-16 py-2.5 rounded-xl border border-[var(--bdae-border)] focus:border-[#00CDDB] bg-black/5 dark:bg-white/5 text-sm font-mono font-bold text-[var(--bdae-text-primary)] outline-none transition-all"
                  />
                  <span className="absolute right-3.5 top-1/2 -translate-y-1/2 text-xs font-bold text-[var(--bdae-text-secondary)] font-mono">
                    ETB
                  </span>
                </div>
                <span className="text-[10px] text-[var(--bdae-text-secondary)] flex items-center gap-1">
                  <Info className="w-3 h-3 text-[#00CDDB]" />
                  Standard SACCO Default: 50,000.00 ETB
                </span>
              </div>
            </div>

            {/* Daily Account Limit Threshold */}
            <div className="bdae-card p-6 border border-[var(--bdae-border)] rounded-2xl space-y-4">
              <div className="flex items-start justify-between">
                <div>
                  <h3 className="text-xs font-bold text-[var(--bdae-text-primary)]">
                    Default Daily Cumulative Account Limit
                  </h3>
                  <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                    Default maximum withdrawal volume per member account per calendar day across all branches before mandatory risk review.
                  </p>
                </div>
                <div className="w-9 h-9 rounded-xl bg-purple-500/10 text-purple-600 flex items-center justify-center shrink-0">
                  <CreditCard className="w-4 h-4" />
                </div>
              </div>

              <div className="space-y-1.5">
                <label className="text-[10px] font-extrabold uppercase text-[var(--bdae-text-secondary)] tracking-wider">
                  Daily Limit (ETB)
                </label>
                <div className="relative">
                  <input
                    type="number"
                    min="0"
                    step="5000"
                    value={rules.dailyAccountLimitThreshold}
                    onChange={(e) => handleNumberChange('dailyAccountLimitThreshold', e.target.value)}
                    className="w-full pl-4 pr-16 py-2.5 rounded-xl border border-[var(--bdae-border)] focus:border-[#00CDDB] bg-black/5 dark:bg-white/5 text-sm font-mono font-bold text-[var(--bdae-text-primary)] outline-none transition-all"
                  />
                  <span className="absolute right-3.5 top-1/2 -translate-y-1/2 text-xs font-bold text-[var(--bdae-text-secondary)] font-mono">
                    ETB
                  </span>
                </div>
                <span className="text-[10px] text-[var(--bdae-text-secondary)] flex items-center gap-1">
                  <Info className="w-3 h-3 text-[#00CDDB]" />
                  Standard SACCO Default: 200,000.00 ETB
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Section 3: Anti-Self-Approval Enforcement Setting */}
        <div className="bdae-card p-6 border border-[var(--bdae-border)] rounded-2xl flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="space-y-1">
            <h3 className="text-xs font-bold text-[var(--bdae-text-primary)] flex items-center gap-2">
              <Lock className="w-4 h-4 text-[#00CDDB]" />
              Strict Anti-Self-Approval Enforcement (<code className="font-mono text-[10px]">maker != checker</code>)
            </h3>
            <p className="text-[11px] text-[var(--bdae-text-secondary)]">
              When active, the approve button is disabled for the creating user across all approval screens. Recommended to remain active for all production SACCOs.
            </p>
          </div>

          <button
            type="button"
            onClick={() => handleToggle('enforceAntiSelfApproval')}
            className={`w-14 h-7 flex items-center rounded-full p-1 transition-colors shrink-0 ${
              rules.enforceAntiSelfApproval ? 'bg-[#00CDDB]' : 'bg-gray-300 dark:bg-gray-700'
            }`}
          >
            <div
              className={`bg-white w-5 h-5 rounded-full shadow-md transform transition-transform ${
                rules.enforceAntiSelfApproval ? 'translate-x-7' : 'translate-x-0'
              }`}
            />
          </button>
        </div>

      </div>
    </PermissionGuard>
  );
};
