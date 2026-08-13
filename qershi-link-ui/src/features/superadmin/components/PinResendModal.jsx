import React, { useState, useEffect } from 'react';
import { pinApi } from '../api/pinApi';
import { sanitizeMsisdn } from '../../../common/utils/sanitizers';
import { X, KeyRound, Phone, User, Send, CheckCircle2, AlertCircle, RefreshCw } from 'lucide-react';

export const PinResendModal = ({ initialMode = 'phone', initialTarget = '', onClose }) => {
  const [mode, setMode] = useState(initialMode); // 'phone' | 'userId'
  const [phoneInput, setPhoneInput] = useState(initialMode === 'phone' ? initialTarget : '');
  const [userIdInput, setUserIdInput] = useState(initialMode === 'userId' ? initialTarget : '');
  
  const [phoneError, setPhoneError] = useState('');
  const [userIdError, setUserIdError] = useState('');
  
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [apiError, setApiError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  useEffect(() => {
    if (initialMode === 'phone') {
      setPhoneInput(initialTarget);
    } else if (initialMode === 'userId') {
      setUserIdInput(initialTarget);
    }
  }, [initialMode, initialTarget]);

  const handlePhoneChange = (e) => {
    const raw = e.target.value;
    const formatted = sanitizeMsisdn(raw);
    setPhoneInput(formatted);
    setApiError(null);
    setSuccessMessage(null);

    if (raw && !formatted.match(/^\+251\d{9}$/)) {
      setPhoneError('Enter valid Ethiopian number (+251 9/7... or 09/07...)');
    } else {
      setPhoneError('');
    }
  };

  const handleUserIdChange = (e) => {
    const val = e.target.value;
    setUserIdInput(val);
    setApiError(null);
    setSuccessMessage(null);

    const uuidRegex = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/;
    if (val && !uuidRegex.test(val)) {
      setUserIdError('Invalid UUID format (e.g. 3fa85f64-5717-4562-b3fc-2c963f66afa6)');
    } else {
      setUserIdError('');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setApiError(null);
    setSuccessMessage(null);

    if (mode === 'phone') {
      const formatted = sanitizeMsisdn(phoneInput);
      if (!formatted || !formatted.match(/^\+251\d{9}$/)) {
        setPhoneError('Please enter a valid phone number (+2519...)');
        return;
      }

      setIsSubmitting(true);
      try {
        const response = await pinApi.resendPinByMsisdn(formatted);
        setIsSubmitting(false);
        setSuccessMessage(
          typeof response === 'string' 
            ? response 
            : `Fresh 6-digit initial PIN dispatched via AfroMessage SMS to ${formatted}!`
        );
      } catch (err) {
        setIsSubmitting(false);
        const msg = err.response?.data?.message || err.response?.data || err.message || 'Failed to dispatch SMS PIN.';
        setApiError(msg);
      }
    } else {
      if (!userIdInput) {
        setUserIdError('Please provide a valid User ID');
        return;
      }

      setIsSubmitting(true);
      try {
        const response = await pinApi.resendPinByUserId(userIdInput);
        setIsSubmitting(false);
        setSuccessMessage(
          typeof response === 'string' 
            ? response 
            : `Fresh 6-digit initial PIN updated and dispatched for User ID: ${userIdInput}!`
        );
      } catch (err) {
        setIsSubmitting(false);
        const msg = err.response?.data?.message || err.response?.data || err.message || 'Failed to dispatch SMS PIN by User ID.';
        setApiError(msg);
      }
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
            <KeyRound className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-lg font-bold tracking-tight text-[var(--bdae-text-primary)]">
              Resend Initial PIN via SMS
            </h2>
            <p className="text-xs text-[var(--bdae-text-secondary)]">
              Dispatches AfroMessage SMS credential reset.
            </p>
          </div>
        </div>

        {/* Dispatch Mode Selector */}
        <div className="grid grid-cols-2 gap-2 p-1 bg-black/5 dark:bg-white/5 rounded-2xl border border-[var(--bdae-border)]">
          <button
            type="button"
            onClick={() => { setMode('phone'); setApiError(null); setSuccessMessage(null); }}
            className={`py-2 px-3 rounded-xl text-xs font-bold flex items-center justify-center gap-1.5 transition-all ${
              mode === 'phone'
                ? 'bg-[var(--bdae-primary)] text-white shadow-md'
                : 'text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]'
            }`}
          >
            <Phone className="w-3.5 h-3.5" />
            <span>By Phone Number</span>
          </button>

          <button
            type="button"
            onClick={() => { setMode('userId'); setApiError(null); setSuccessMessage(null); }}
            className={`py-2 px-3 rounded-xl text-xs font-bold flex items-center justify-center gap-1.5 transition-all ${
              mode === 'userId'
                ? 'bg-[var(--bdae-primary)] text-white shadow-md'
                : 'text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]'
            }`}
          >
            <User className="w-3.5 h-3.5" />
            <span>By User ID</span>
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="space-y-4 text-xs">
          
          {mode === 'phone' ? (
            <div className="space-y-1.5">
              <label className="block text-xs font-bold text-[var(--bdae-text-primary)]">
                Recipient Phone Number (MSISDN)
              </label>
              <div className="relative">
                <input
                  type="text"
                  value={phoneInput}
                  onChange={handlePhoneChange}
                  placeholder="+251912345678 or 0912345678"
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
          ) : (
            <div className="space-y-1.5">
              <label className="block text-xs font-bold text-[var(--bdae-text-primary)]">
                Target User ID UUID
              </label>
              <div className="relative">
                <input
                  type="text"
                  value={userIdInput}
                  onChange={handleUserIdChange}
                  placeholder="3fa85f64-5717-4562-b3fc-2c963f66afa6"
                  className={`w-full px-4 py-2.5 rounded-xl border text-xs bg-transparent transition-all outline-none font-mono ${
                    userIdError
                      ? 'border-red-500 text-red-500 focus:ring-1 focus:ring-red-500'
                      : 'border-[var(--bdae-border)] focus:border-[var(--bdae-secondary)] text-[var(--bdae-text-primary)]'
                  }`}
                />
                <User className="w-4 h-4 text-[var(--bdae-text-secondary)] absolute right-3.5 top-3" />
              </div>
              {userIdError && (
                <p className="text-[11px] text-red-500 font-semibold">{userIdError}</p>
              )}
            </div>
          )}

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
                <span>Dispatching AfroMessage SMS...</span>
              </>
            ) : (
              <>
                <Send className="w-4 h-4" />
                <span>Dispatch Fresh Initial PIN</span>
              </>
            )}
          </button>
        </form>

      </div>
    </div>
  );
};
