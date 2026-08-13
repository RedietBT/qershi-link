import React, { useState, useEffect } from 'react';
import { userApi } from '../api/userApi';
import { roleApi } from '../../roles/api/roleApi';
import { X, ShieldCheck, CheckCircle2, AlertCircle, RefreshCw } from 'lucide-react';

export const AssignRoleModal = ({ user, onClose, onSuccess }) => {
  const [availableRoles, setAvailableRoles] = useState([]);
  const [selectedRoleId, setSelectedRoleId] = useState('');
  const [isLoadingRoles, setIsLoadingRoles] = useState(true);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [apiError, setApiError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  useEffect(() => {
    const loadRoles = async () => {
      setIsLoadingRoles(true);
      try {
        const rolesData = await roleApi.getRoles();
        const list = Array.isArray(rolesData) ? rolesData : [];
        setAvailableRoles(list);
        if (list.length > 0) {
          const firstId = list[0].roleId || list[0].id;
          setSelectedRoleId(firstId);
        }
        setIsLoadingRoles(false);
      } catch (err) {
        setIsLoadingRoles(false);
        // Fallback default static options if endpoint fails
        const fallbackList = [
          { roleName: 'SACCO_ADMIN', roleId: '018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e0f' },
          { roleName: 'BRANCH_MANAGER', roleId: '018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e10' },
          { roleName: 'TELLER', roleId: '018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e11' },
          { roleName: 'LOAN_OFFICER', roleId: '018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e12' },
          { roleName: 'MEMBER', roleId: '018f3b23-1a2b-7c3d-be4f-5a6b7c8d9e13' }
        ];
        setAvailableRoles(fallbackList);
        setSelectedRoleId(fallbackList[0].roleId);
      }
    };

    loadRoles();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setApiError(null);

    if (!selectedRoleId) {
      setApiError('Please select a role to assign.');
      return;
    }

    const saccoIdContext = user?.saccoId || 'c8e55276-25d5-4fe3-bc59-1c008b25b7da';

    setIsSubmitting(true);
    try {
      await userApi.assignRole(user.userId, selectedRoleId, saccoIdContext);

      setIsSubmitting(false);
      setSuccessMessage('Tenant Role successfully assigned to user!');

      setTimeout(() => {
        if (onSuccess) onSuccess();
        onClose();
      }, 1500);
    } catch (err) {
      setIsSubmitting(false);
      const msg = err.response?.data?.message || err.response?.data || err.message || 'Failed to assign role to user.';
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
            <ShieldCheck className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-lg font-bold tracking-tight text-[var(--bdae-text-primary)]">
              Assign Tenant Role to User
            </h2>
            <p className="text-xs text-[var(--bdae-text-secondary)]">
              Select tenant operational role for user account.
            </p>
          </div>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-4 text-xs">
          
          {/* Target User Info */}
          <div className="p-3 rounded-xl bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] space-y-1">
            <p className="text-[10px] uppercase font-bold text-[var(--bdae-text-secondary)]">Target Account</p>
            <p className="font-bold text-xs text-[var(--bdae-text-primary)]">{user?.msisdn} ({user?.globalRole})</p>
          </div>

          {/* Role Selection Dropdown */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-[var(--bdae-text-primary)]">
              Select Operational Role *
            </label>
            {isLoadingRoles ? (
              <div className="p-3 border border-[var(--bdae-border)] rounded-xl flex items-center gap-2 text-[var(--bdae-text-secondary)]">
                <RefreshCw className="w-4 h-4 animate-spin text-[var(--bdae-secondary)]" />
                <span>Loading available roles...</span>
              </div>
            ) : (
              <select
                value={selectedRoleId}
                onChange={(e) => setSelectedRoleId(e.target.value)}
                className="w-full px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] focus:border-[var(--bdae-secondary)] text-xs bg-transparent outline-none text-[var(--bdae-text-primary)] font-bold transition-all"
              >
                {availableRoles.map((role) => {
                  const rId = role.roleId || role.id;
                  return (
                    <option key={rId} value={rId} className="bg-[var(--bdae-bg)]">
                      {role.roleName} {role.isSystemDefined ? '(System Defined)' : '(Custom Role)'}
                    </option>
                  );
                })}
              </select>
            )}
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
            disabled={isSubmitting || isLoadingRoles}
            className="bdae-btn-primary w-full py-3 text-xs font-bold rounded-xl flex items-center justify-center gap-2 shadow-md"
          >
            {isSubmitting ? (
              <>
                <RefreshCw className="w-4 h-4 animate-spin" />
                <span>Assigning Role...</span>
              </>
            ) : (
              <>
                <ShieldCheck className="w-4 h-4" />
                <span>Assign Role to User</span>
              </>
            )}
          </button>
        </form>

      </div>
    </div>
  );
};
