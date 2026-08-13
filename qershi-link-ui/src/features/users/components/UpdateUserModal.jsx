import React, { useState, useEffect } from 'react';
import { userApi } from '../api/userApi';
import { sanitizeMsisdn } from '../../../common/utils/sanitizers';
import { X, UserCheck, Phone, CheckCircle2, AlertCircle, RefreshCw } from 'lucide-react';

export const UpdateUserModal = ({ user, onClose, onSuccess }) => {
  const [msisdnInput, setMsisdnInput] = useState(user?.msisdn || '');
  const [statusInput, setStatusInput] = useState(user?.status || 'ACTIVE');

  const [phoneError, setPhoneError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [apiError, setApiError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  useEffect(() => {
    if (user) {
      setMsisdnInput(user.msisdn || '');
      // If current status is PASSWORD_CHANGE_REQUIRED, fallback default selection to ACTIVE or PENDING
      const current = (user.status || 'ACTIVE').toUpperCase();
      if (current === 'PASSWORD_CHANGE_REQUIRED') {
        setStatusInput('ACTIVE');
      } else {
        setStatusInput(current);
      }
    }
  }, [user]);

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
      await userApi.updateUser(user.userId, {
        msisdn: formatted,
        status: statusInput
      });

      setIsSubmitting(false);
      setSuccessMessage('User security parameters updated successfully!');

      setTimeout(() => {
        if (onSuccess) onSuccess();
        onClose();
      }, 1500);
    } catch (err) {
      setIsSubmitting(false);
      const msg = err.response?.data?.message || err.response?.data || err.message || 'Failed to update user security parameters.';
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
            <UserCheck className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-lg font-bold tracking-tight text-[var(--bdae-text-primary)]">
              Update User Security Parameters
            </h2>
            <p className="text-xs text-[var(--bdae-text-secondary)]">
              PUT /api/v1/users/{'{id}'}
            </p>
          </div>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-4 text-xs">
          
          {/* MSISDN Input */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-[var(--bdae-text-primary)]">
              Mobile Phone Handle (MSISDN) *
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

          {/* Account Status Selector */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-[var(--bdae-text-primary)]">
              Account Security Status *
            </label>
            <select
              value={statusInput}
              onChange={(e) => setStatusInput(e.target.value)}
              className="w-full px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] focus:border-[var(--bdae-secondary)] text-xs bg-transparent outline-none text-[var(--bdae-text-primary)] font-bold transition-all"
            >
              <option value="ACTIVE" className="bg-[var(--bdae-bg)] text-emerald-500">ACTIVE (Operational Identity)</option>
              <option value="PENDING" className="bg-[var(--bdae-bg)] text-amber-500">PENDING (Initial Setup)</option>
              <option value="PENDING_APPROVAL" className="bg-[var(--bdae-bg)] text-amber-500">PENDING_APPROVAL (Awaiting Admin Review)</option>
              <option value="PENDING_SHARE" className="bg-[var(--bdae-bg)] text-amber-500">PENDING_SHARE (Awaiting Share Requirement)</option>
              <option value="BLOCKED" className="bg-[var(--bdae-bg)] text-red-500">BLOCKED (Account Frozen)</option>
              <option value="DEACTIVATED" className="bg-[var(--bdae-bg)] text-red-500">DEACTIVATED (Account Disabled)</option>
            </select>
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
                <span>Updating Security Parameters...</span>
              </>
            ) : (
              <>
                <UserCheck className="w-4 h-4" />
                <span>Save Security Parameters</span>
              </>
            )}
          </button>
        </form>

      </div>
    </div>
  );
};
