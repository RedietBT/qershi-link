import React, { useState } from 'react';
import { userApi } from '../api/userApi';
import { sanitizeMsisdn } from '../../../common/utils/sanitizers';
import { X, UserPlus, Phone, ShieldCheck, CheckCircle2, AlertCircle, RefreshCw } from 'lucide-react';

export const CreateUserModal = ({ defaultSaccoId = '', onClose, onSuccess }) => {
  const [msisdnInput, setMsisdnInput] = useState('');
  const [globalRoleInput, setGlobalRoleInput] = useState('SACCO_USER');
  const [saccoIdInput, setSaccoIdInput] = useState(defaultSaccoId);

  const [phoneError, setPhoneError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [apiError, setApiError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  const handlePhoneChange = (e) => {
    const raw = e.target.value;
    const formatted = sanitizeMsisdn(raw);
    setMsisdnInput(formatted);
    setApiError(null);

    if (raw && !formatted.match(/^\+251\d{9}$/)) {
      setPhoneError('Enter valid Ethiopian number (+251 9/7... or 09/07...)');
    } else {
      setPhoneError('');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setApiError(null);

    const formatted = sanitizeMsisdn(msisdnInput);
    if (!formatted || !formatted.match(/^\+251\d{9}$/)) {
      setPhoneError('Please enter a valid phone number (+2519...)');
      return;
    }

    setIsSubmitting(true);
    try {
      const response = await userApi.createUser(
        { msisdn: formatted, globalRole: globalRoleInput },
        saccoIdInput || null
      );

      setIsSubmitting(false);
      setSuccessMessage(
        typeof response === 'string'
          ? response
          : `User account created successfully! Initial PIN dispatched via SMS to ${formatted}.`
      );

      setTimeout(() => {
        if (onSuccess) onSuccess();
        onClose();
      }, 1800);
    } catch (err) {
      setIsSubmitting(false);
      const msg = err.response?.data?.message || err.response?.data || err.message || 'Failed to register user account.';
      setApiError(msg);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
      <div className="bdae-card p-6 md:p-8 max-w-md w-full rounded-3xl shadow-2xl border border-[var(--bdae-border)] space-y-6 relative">
        
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-5 right-5 p-2 rounded-full text-[var(--bdae-text-secondary)] hover:bg-black/10 dark:hover:bg-white/10 transition-all"
        >
          <X className="w-5 h-5" />
        </button>

        {/* Modal Header */}
        <div className="flex items-center space-x-3 border-b border-[var(--bdae-border)] pb-4">
          <div 
            className="w-12 h-12 rounded-2xl flex items-center justify-center text-white shadow-md shrink-0"
            style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
          >
            <UserPlus className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-lg font-bold tracking-tight text-[var(--bdae-text-primary)]">
              Register New User Account
            </h2>
            <p className="text-xs text-[var(--bdae-text-secondary)]">
              Creates identity profile & dispatches initial PIN via SMS.
            </p>
          </div>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-4 text-xs">
          
          {/* MSISDN Input */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-[var(--bdae-text-primary)]">
              Mobile Phone Number (MSISDN) *
            </label>
            <div className="relative">
              <input
                type="text"
                value={msisdnInput}
                onChange={handlePhoneChange}
                placeholder="+251995220266 or 0995220266"
                className={`w-full px-4 py-2.5 rounded-xl border text-xs bg-transparent transition-all outline-none font-mono ${
                  phoneError
                    ? 'border-red-500 text-red-500 focus:ring-1 focus:ring-red-500'
                    : 'border-[var(--bdae-border)] focus:border-[var(--bdae-secondary)] text-[var(--bdae-text-primary)]'
                }`}
              />
              <Phone className="w-4 h-4 text-[var(--bdae-text-secondary)] absolute right-3.5 top-3" />
            </div>
            {phoneError && (
              <p className="text-[11px] text-red-500 font-semibold">{phoneError}</p>
            )}
          </div>

          {/* Global Role Selector */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-[var(--bdae-text-primary)]">
              Global Platform Role *
            </label>
            <select
              value={globalRoleInput}
              onChange={(e) => setGlobalRoleInput(e.target.value)}
              className="w-full px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] focus:border-[var(--bdae-secondary)] text-xs bg-transparent outline-none text-[var(--bdae-text-primary)] font-bold transition-all"
            >
              <option value="SACCO_USER" className="bg-[var(--bdae-bg)]">SACCO_USER (Standard Tenant User)</option>
              <option value="SACCO_ADMIN" className="bg-[var(--bdae-bg)]">SACCO_ADMIN (SACCO Workspace Administrator)</option>
              <option value="SUPER_ADMIN" className="bg-[var(--bdae-bg)]">SUPER_ADMIN (Master Platform Super Admin)</option>
            </select>
          </div>

          {/* SACCO ID Input */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-[var(--bdae-text-primary)]">
              Target SACCO UUID Context
            </label>
            <input
              type="text"
              value={saccoIdInput}
              onChange={(e) => setSaccoIdInput(e.target.value)}
              placeholder="3fa85f64-5717-4562-b3fc-2c963f66afa6"
              className="w-full px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] focus:border-[var(--bdae-secondary)] text-xs bg-transparent outline-none font-mono text-[var(--bdae-text-primary)] transition-all"
            />
          </div>

          {/* Error Alert */}
          {apiError && (
            <div className="p-3.5 rounded-xl bg-red-500/10 border border-red-500/30 text-red-600 dark:text-red-400 text-xs flex items-center gap-2">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{apiError}</span>
            </div>
          )}

          {/* Success Toast */}
          {successMessage && (
            <div className="p-3.5 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-600 dark:text-emerald-400 text-xs flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4 shrink-0" />
              <span>{successMessage}</span>
            </div>
          )}

          {/* Submit Action */}
          <button
            type="submit"
            disabled={isSubmitting}
            className="bdae-btn-primary w-full py-3 text-xs font-bold rounded-xl flex items-center justify-center gap-2 shadow-md"
          >
            {isSubmitting ? (
              <>
                <RefreshCw className="w-4 h-4 animate-spin" />
                <span>Registering & Dispatching Initial PIN...</span>
              </>
            ) : (
              <>
                <ShieldCheck className="w-4 h-4" />
                <span>Register User & Dispatch PIN</span>
              </>
            )}
          </button>
        </form>

      </div>
    </div>
  );
};
