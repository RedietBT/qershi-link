import React, { useState } from 'react';
import { useLogin } from '../hooks/useLogin';
import { sanitizeMsisdn, isValidMsisdn } from '../../../common/utils/sanitizers';
import { Eye, EyeOff, LogIn, Loader2 } from 'lucide-react';

export const LoginForm = ({ onSuccess }) => {
  const [msisdn, setMsisdn] = useState('');
  const [pin, setPin] = useState('');
  const [showPin, setShowPin] = useState(false);

  const [touchedPhone, setTouchedPhone] = useState(false);
  const [touchedPin, setTouchedPin] = useState(false);

  const { executeLogin, isLoading, error, setError } = useLogin();

  const sanitizedPhone = sanitizeMsisdn(msisdn);
  const isPhoneValid = isValidMsisdn(sanitizedPhone);
  const isPhoneError = touchedPhone && msisdn.length > 0 && !isPhoneValid;

  const isPinValid = pin.trim().length >= 4;
  const isPinError = touchedPin && pin.length > 0 && !isPinValid;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setTouchedPhone(true);
    setTouchedPin(true);

    if (!isPhoneValid || !isPinValid) return;

    const result = await executeLogin(msisdn, pin);
    if (result.success && onSuccess) {
      onSuccess(result.data);
    }
  };

  return (
    <div className="w-full max-w-sm mx-auto space-y-5">
      
      {/* Header */}
      <div className="text-center space-y-1">
        <h1 className="text-2xl font-bold tracking-tight text-[var(--bdae-text-primary)]">
          Sign In to Qershi-Link
        </h1>
        <p className="text-xs text-[var(--bdae-text-secondary)] font-medium">
          Enter your SACCO credentials to login.
        </p>
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit} className="space-y-4 text-left">
        
        {/* Global Error */}
        {error && (
          <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-600 dark:text-red-400 text-xs text-center font-medium animate-fadeIn">
            {error}
          </div>
        )}

        {/* Phone Number (MSISDN) */}
        <div className="space-y-1">
          <label 
            className={`block text-xs font-semibold transition-colors ${
              isPhoneError ? 'text-red-500' : 'text-[var(--bdae-text-primary)]'
            }`}
          >
            Phone Number (MSISDN)
          </label>
          <input
            type="text"
            value={msisdn}
            onChange={(e) => {
              setMsisdn(e.target.value);
              if (error) setError(null);
            }}
            onBlur={() => setTouchedPhone(true)}
            placeholder="+251912345678 or 0912345678"
            required
            disabled={isLoading}
            className={`w-full px-3.5 py-2.5 text-xs rounded-xl border font-medium outline-none transition-all ${
              isPhoneError
                ? 'border-red-500 text-red-600 ring-2 ring-red-500/20 bg-red-500/5'
                : 'border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] focus:border-[var(--bdae-secondary)] focus:ring-2 focus:ring-[var(--bdae-secondary)]/20'
            }`}
          />
          {isPhoneError && (
            <p className="text-[11px] text-red-500 font-medium mt-1 animate-fadeIn">
              Please enter a valid Ethiopian phone number (+2519... or 09...).
            </p>
          )}
        </div>

        {/* Security PIN */}
        <div className="space-y-1">
          <div className="flex items-center justify-between">
            <label 
              className={`block text-xs font-semibold transition-colors ${
                isPinError ? 'text-red-500' : 'text-[var(--bdae-text-primary)]'
              }`}
            >
              Security PIN
            </label>
            <a href="#forgot" className="text-[11px] text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-secondary)] font-semibold transition-colors">
              Forgot PIN?
            </a>
          </div>
          <div className="relative">
            <input
              type={showPin ? 'text' : 'password'}
              value={pin}
              onChange={(e) => {
                setPin(e.target.value);
                if (error) setError(null);
              }}
              onBlur={() => setTouchedPin(true)}
              placeholder="Enter your security PIN"
              required
              disabled={isLoading}
              className={`w-full pl-3.5 pr-10 py-2.5 text-xs rounded-xl border font-mono outline-none transition-all ${
                isPinError
                  ? 'border-red-500 text-red-600 ring-2 ring-red-500/20 bg-red-500/5'
                  : 'border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] focus:border-[var(--bdae-secondary)] focus:ring-2 focus:ring-[var(--bdae-secondary)]/20'
              }`}
            />
            <button
              type="button"
              onClick={() => setShowPin(!showPin)}
              className="absolute inset-y-0 right-0 pr-3.5 flex items-center text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]"
            >
              {showPin ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
            </button>
          </div>
          {isPinError && (
            <p className="text-[11px] text-red-500 font-medium mt-1 animate-fadeIn">
              Security PIN must be at least 4 digits.
            </p>
          )}
        </div>

        {/* Submit Button */}
        <button
          type="submit"
          disabled={isLoading}
          className="bdae-btn-primary w-full py-3 mt-3 flex items-center justify-center space-x-2 text-xs font-bold shadow-lg disabled:opacity-50 disabled:cursor-not-allowed rounded-xl"
        >
          {isLoading ? (
            <>
              <Loader2 className="w-4 h-4 animate-spin" />
              <span>Logging in...</span>
            </>
          ) : (
            <>
              <LogIn className="w-4 h-4" />
              <span>SIGN IN</span>
            </>
          )}
        </button>

      </form>
    </div>
  );
};
