import React, { useState } from 'react';
import { authApi } from '../api/authApi';
import { sanitizeMsisdn } from '../../../common/utils/sanitizers';
import { X, KeyRound, Phone, CheckCircle2, AlertCircle, RefreshCw, Eye, EyeOff } from 'lucide-react';

export const ChangePinModal = ({ initialMsisdn = '', onClose, onSuccess }) => {
  const [msisdnInput, setMsisdnInput] = useState(initialMsisdn);
  const [oldPin, setOldPin] = useState('');
  const [newPin, setNewPin] = useState('');
  const [confirmPin, setConfirmPin] = useState('');

  const [showOldPin, setShowOldPin] = useState(false);
  const [showNewPin, setShowNewPin] = useState(false);

  const [phoneError, setPhoneError] = useState('');
  const [pinError, setPinError] = useState('');
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
    setPinError('');

    const formatted = sanitizeMsisdn(msisdnInput);
    if (!formatted || !formatted.match(/^\+251\d{9}$/)) {
      setPhoneError('Please enter a valid phone number (+2519...)');
      return;
    }

    if (!oldPin || oldPin.length < 4) {
      setPinError('Please enter your current initial PIN.');
      return;
    }

    if (!newPin || newPin.length < 4) {
      setPinError('New PIN must be at least 4 digits.');
      return;
    }

    if (newPin !== confirmPin) {
      setPinError('New PIN and Confirm PIN do not match.');
      return;
    }

    setIsSubmitting(true);
    try {
      const response = await authApi.changePassword({
        msisdn: formatted,
        oldPin,
        newPin
      });

      setIsSubmitting(false);
      setSuccessMessage(
        typeof response === 'string'
          ? response
          : 'Initial PIN changed successfully! You can now log in using your new PIN.'
      );

      setTimeout(() => {
        if (onSuccess) onSuccess();
        onClose();
      }, 1800);
    } catch (err) {
      setIsSubmitting(false);
      const msg = err.response?.data?.message || err.response?.data || err.message || 'Failed to rotate PIN. Verify current initial PIN.';
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
            <KeyRound className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-lg font-bold tracking-tight text-[var(--bdae-text-primary)]">
              First-Time Initial PIN Rotation
            </h2>
            <p className="text-xs text-[var(--bdae-text-secondary)]">
              POST /api/v1/auth/change-password
            </p>
          </div>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-4 text-xs">
          
          {/* Phone Input */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-[var(--bdae-text-primary)]">
              Phone Number (MSISDN) *
            </label>
            <div className="relative">
              <input
                type="text"
                value={msisdnInput}
                onChange={handlePhoneChange}
                placeholder="+251995220266"
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

          {/* Current Initial PIN */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-[var(--bdae-text-primary)]">
              Current Initial PIN (from SMS) *
            </label>
            <div className="relative">
              <input
                type={showOldPin ? 'text' : 'password'}
                value={oldPin}
                onChange={(e) => setOldPin(e.target.value)}
                placeholder="Enter 6-digit SMS initial PIN"
                className="w-full px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] focus:border-[var(--bdae-secondary)] text-xs bg-transparent outline-none font-mono text-[var(--bdae-text-primary)] transition-all"
              />
              <button
                type="button"
                onClick={() => setShowOldPin(!showOldPin)}
                className="absolute right-3.5 top-3 text-[var(--bdae-text-secondary)]"
              >
                {showOldPin ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
          </div>

          {/* New PIN */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-[var(--bdae-text-primary)]">
              Create New Security PIN *
            </label>
            <div className="relative">
              <input
                type={showNewPin ? 'text' : 'password'}
                value={newPin}
                onChange={(e) => setNewPin(e.target.value)}
                placeholder="Enter new PIN"
                className="w-full px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] focus:border-[var(--bdae-secondary)] text-xs bg-transparent outline-none font-mono text-[var(--bdae-text-primary)] transition-all"
              />
              <button
                type="button"
                onClick={() => setShowNewPin(!showNewPin)}
                className="absolute right-3.5 top-3 text-[var(--bdae-text-secondary)]"
              >
                {showNewPin ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
          </div>

          {/* Confirm New PIN */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-[var(--bdae-text-primary)]">
              Confirm New Security PIN *
            </label>
            <input
              type="password"
              value={confirmPin}
              onChange={(e) => setConfirmPin(e.target.value)}
              placeholder="Re-enter new PIN"
              className="w-full px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] focus:border-[var(--bdae-secondary)] text-xs bg-transparent outline-none font-mono text-[var(--bdae-text-primary)] transition-all"
            />
          </div>

          {pinError && (
            <p className="text-[11px] text-red-500 font-semibold">{pinError}</p>
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
                <span>Rotating Security PIN...</span>
              </>
            ) : (
              <>
                <KeyRound className="w-4 h-4" />
                <span>Update PIN & Activate Account</span>
              </>
            )}
          </button>
        </form>

      </div>
    </div>
  );
};
