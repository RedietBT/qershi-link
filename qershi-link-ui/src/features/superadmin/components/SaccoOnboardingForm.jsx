import React, { useState } from 'react';
import { useOnboardSacco } from '../hooks/useOnboardSacco';
import { sanitizeMsisdn, isValidMsisdn, isValidSaccoName } from '../../../common/utils/sanitizers';
import { Building2, Loader2, ToggleLeft, ToggleRight } from 'lucide-react';

export const SaccoOnboardingForm = ({ onSuccess }) => {
  const [saccoName, setSaccoName] = useState('');
  const [isUnion, setIsUnion] = useState(false);
  const [minShareRequirement, setMinShareRequirement] = useState('');
  const [adminMsisdn, setAdminMsisdn] = useState('');
  const [adminName, setAdminName] = useState('');
  const [region, setRegion] = useState('');

  const [touchedPhone, setTouchedPhone] = useState(false);
  const [touchedName, setTouchedName] = useState(false);
  const [touchedAdminName, setTouchedAdminName] = useState(false);

  const { executeOnboard, isLoading, error, successMessage, setError } = useOnboardSacco();

  const sanitizedPhone = sanitizeMsisdn(adminMsisdn);
  const isPhoneValid = isValidMsisdn(sanitizedPhone);
  const isPhoneError = touchedPhone && adminMsisdn.length > 0 && !isPhoneValid;

  const isNameValid = isValidSaccoName(saccoName);
  const isNameError = touchedName && saccoName.length > 0 && !isNameValid;

  const isAdminNameValid = adminName.trim().length >= 2;
  const isAdminNameError = touchedAdminName && adminName.length > 0 && !isAdminNameValid;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setTouchedPhone(true);
    setTouchedName(true);
    setTouchedAdminName(true);

    if (!isPhoneValid || !isNameValid || !isAdminNameValid) return;

    const result = await executeOnboard({
      saccoName,
      isUnion,
      minShareRequirement: minShareRequirement || 0,
      adminMsisdn,
      adminName,
      region
    });

    if (result.success && onSuccess) {
      onSuccess(result.data);
    }
  };

  return (
    <div className="w-full max-w-md mx-auto space-y-4">
      
      {/* Header */}
      <div className="text-center space-y-1">
        <h1 className="text-2xl font-bold tracking-tight text-[var(--bdae-text-primary)]">
          Onboard SACCO Tenant
        </h1>
        <p className="text-xs text-[var(--bdae-text-secondary)] font-medium">
          Fill in institution details to onboard a SACCO.
        </p>
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit} className="space-y-3 text-left">
        
        {/* Alerts */}
        {error && (
          <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-600 dark:text-red-400 text-xs text-center font-medium animate-fadeIn">
            {error}
          </div>
        )}

        {successMessage && (
          <div className="p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-700 dark:text-emerald-300 text-xs text-center font-medium animate-fadeIn">
            {successMessage}
          </div>
        )}

        {/* Row 1: SACCO Name & Region */}
        <div className="grid grid-cols-2 gap-3">
          <div className="space-y-1">
            <label 
              className={`block text-[11px] font-semibold transition-colors ${
                isNameError ? 'text-red-500' : 'text-[var(--bdae-text-primary)]'
              }`}
            >
              SACCO Name
            </label>
            <input
              type="text"
              value={saccoName}
              onChange={(e) => {
                setSaccoName(e.target.value);
                if (error) setError(null);
              }}
              onBlur={() => setTouchedName(true)}
              placeholder="e.g. Awach SACCO"
              required
              disabled={isLoading}
              className={`w-full px-3 py-2 text-xs rounded-xl border font-medium outline-none transition-all ${
                isNameError
                  ? 'border-red-500 text-red-600 ring-2 ring-red-500/20 bg-red-500/5'
                  : 'border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] focus:border-[var(--bdae-secondary)] focus:ring-2 focus:ring-[var(--bdae-secondary)]/20'
              }`}
            />
            {isNameError && (
              <p className="text-[10px] text-red-500 font-medium mt-0.5 animate-fadeIn">
                Name cannot start with a number.
              </p>
            )}
          </div>

          <div className="space-y-1">
            <label className="block text-[11px] font-semibold text-[var(--bdae-text-primary)]">
              Region / Jurisdiction
            </label>
            <input
              type="text"
              value={region}
              onChange={(e) => setRegion(e.target.value)}
              placeholder="e.g. Addis Ababa"
              required
              disabled={isLoading}
              className="w-full px-3 py-2 text-xs rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] focus:border-[var(--bdae-secondary)] outline-none"
            />
          </div>
        </div>

        {/* Row 2: Admin Full Name & Admin MSISDN */}
        <div className="grid grid-cols-2 gap-3">
          <div className="space-y-1">
            <label 
              className={`block text-[11px] font-semibold transition-colors ${
                isAdminNameError ? 'text-red-500' : 'text-[var(--bdae-text-primary)]'
              }`}
            >
              Admin Full Name
            </label>
            <input
              type="text"
              value={adminName}
              onChange={(e) => {
                setAdminName(e.target.value);
                if (error) setError(null);
              }}
              onBlur={() => setTouchedAdminName(true)}
              placeholder="e.g. Arsema Degu"
              required
              disabled={isLoading}
              className={`w-full px-3 py-2 text-xs rounded-xl border font-medium outline-none transition-all ${
                isAdminNameError
                  ? 'border-red-500 text-red-600 ring-2 ring-red-500/20 bg-red-500/5'
                  : 'border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] focus:border-[var(--bdae-secondary)] focus:ring-2 focus:ring-[var(--bdae-secondary)]/20'
              }`}
            />
            {isAdminNameError && (
              <p className="text-[10px] text-red-500 font-medium mt-0.5 animate-fadeIn">
                Admin name required.
              </p>
            )}
          </div>

          <div className="space-y-1">
            <label 
              className={`block text-[11px] font-semibold transition-colors ${
                isPhoneError ? 'text-red-500' : 'text-[var(--bdae-text-primary)]'
              }`}
            >
              Admin Phone (MSISDN)
            </label>
            <input
              type="text"
              value={adminMsisdn}
              onChange={(e) => {
                setAdminMsisdn(e.target.value);
                if (error) setError(null);
              }}
              onBlur={() => setTouchedPhone(true)}
              placeholder="+251987654321 or 0987654321"
              required
              disabled={isLoading}
              className={`w-full px-3 py-2 text-xs rounded-xl border font-medium outline-none transition-all ${
                isPhoneError
                  ? 'border-red-500 text-red-600 ring-2 ring-red-500/20 bg-red-500/5'
                  : 'border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] focus:border-[var(--bdae-secondary)] focus:ring-2 focus:ring-[var(--bdae-secondary)]/20'
              }`}
            />
            {isPhoneError && (
              <p className="text-[10px] text-red-500 font-medium mt-0.5 animate-fadeIn">
                Valid phone required.
              </p>
            )}
          </div>
        </div>

        {/* Row 3: Min Share Requirement & Union Toggle */}
        <div className="grid grid-cols-2 gap-3">
          <div className="space-y-1">
            <label className="block text-[11px] font-semibold text-[var(--bdae-text-primary)]">
              Min Share Requirement (ETB)
            </label>
            <input
              type="number"
              min="0"
              value={minShareRequirement}
              onChange={(e) => setMinShareRequirement(e.target.value)}
              placeholder="e.g. 500"
              required
              disabled={isLoading}
              className="w-full px-3 py-2 text-xs rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] focus:border-[var(--bdae-secondary)] font-mono outline-none"
            />
          </div>

          <div className="space-y-1 flex flex-col justify-end">
            <button
              type="button"
              onClick={() => setIsUnion(!isUnion)}
              className="w-full flex items-center justify-between px-3 py-2 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] hover:border-[var(--bdae-secondary)] transition-all text-xs font-semibold"
            >
              <span>{isUnion ? 'Union Federation' : 'Primary SACCO'}</span>
              {isUnion ? (
                <ToggleRight className="w-5 h-5 text-[var(--bdae-secondary)] shrink-0" />
              ) : (
                <ToggleLeft className="w-5 h-5 text-[var(--bdae-text-secondary)] shrink-0" />
              )}
            </button>
          </div>
        </div>

        {/* Submit Button */}
        <button
          type="submit"
          disabled={isLoading}
          className="bdae-btn-primary w-full py-3 mt-2 flex items-center justify-center space-x-2 text-xs font-bold shadow-lg disabled:opacity-50 disabled:cursor-not-allowed rounded-xl"
        >
          {isLoading ? (
            <>
              <Loader2 className="w-4 h-4 animate-spin" />
              <span>Executing Onboarding...</span>
            </>
          ) : (
            <>
              <Building2 className="w-4 h-4" />
              <span>EXECUTE ONBOARDING</span>
            </>
          )}
        </button>

      </form>
    </div>
  );
};
