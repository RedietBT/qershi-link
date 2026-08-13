import React, { useState } from 'react';
import { roleApi } from '../api/roleApi';
import { getPermissionDisplayName, getPermissionDescription } from '../utils/permissionUtils';
import { X, ShieldPlus, CheckCircle2, AlertCircle, RefreshCw, Key, Check } from 'lucide-react';

export const CreateRoleModal = ({ permissions = [], onClose, onSuccess }) => {
  const [roleNameInput, setRoleNameInput] = useState('');
  const [selectedPermissionIds, setSelectedPermissionIds] = useState([]);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [apiError, setApiError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  const togglePermission = (permId) => {
    if (selectedPermissionIds.includes(permId)) {
      setSelectedPermissionIds(selectedPermissionIds.filter(id => id !== permId));
    } else {
      setSelectedPermissionIds([...selectedPermissionIds, permId]);
    }
  };

  const toggleSelectAll = () => {
    if (selectedPermissionIds.length === permissions.length) {
      setSelectedPermissionIds([]);
    } else {
      const allIds = permissions.map(p => p.permissionId || p.id).filter(Boolean);
      setSelectedPermissionIds(allIds);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setApiError(null);

    const name = roleNameInput.trim().toUpperCase();
    if (!name) {
      setApiError('Role Name is required.');
      return;
    }

    if (name.includes('SUPER_ADMIN')) {
      setApiError('Tenant administrators cannot create SUPER_ADMIN role names.');
      return;
    }

    if (selectedPermissionIds.length === 0) {
      setApiError('Please select at least one permission for this custom role.');
      return;
    }

    setIsSubmitting(true);
    try {
      await roleApi.createRole({
        roleName: name,
        permissionIds: selectedPermissionIds
      });

      setIsSubmitting(false);
      setSuccessMessage(`Custom local role '${name}' created successfully!`);

      setTimeout(() => {
        if (onSuccess) onSuccess();
        onClose();
      }, 1500);
    } catch (err) {
      setIsSubmitting(false);
      const msg = err.response?.data?.message || err.response?.data || err.message || 'Failed to create local custom role.';
      setApiError(msg);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
      <div className="bdae-card p-6 md:p-8 max-w-2xl w-full rounded-3xl shadow-2xl border border-[var(--bdae-border)] space-y-6 relative max-h-[90vh] flex flex-col">
        
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-5 right-5 p-2 rounded-full text-[var(--bdae-text-secondary)] hover:bg-black/10 dark:hover:bg-white/10 transition-all"
        >
          <X className="w-5 h-5" />
        </button>

        {/* Modal Header */}
        <div className="flex items-center space-x-3 border-b border-[var(--bdae-border)] pb-4 shrink-0">
          <div 
            className="w-12 h-12 rounded-2xl flex items-center justify-center text-white shadow-md shrink-0"
            style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
          >
            <ShieldPlus className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-lg font-bold tracking-tight text-[var(--bdae-text-primary)]">
              Create Custom Local Role
            </h2>
            <p className="text-xs text-[var(--bdae-text-secondary)]">
              Bundle permissions into a custom RBAC role for your tenant workspace.
            </p>
          </div>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="space-y-5 text-xs overflow-y-auto pr-1 flex-1">
          
          {/* Role Name Input */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-[var(--bdae-text-primary)]">
              Custom Role Name *
            </label>
            <input
              type="text"
              value={roleNameInput}
              onChange={(e) => setRoleNameInput(e.target.value.toUpperCase())}
              placeholder="e.g. SENIOR_LOAN_OFFICER or TELLER_SUPERVISOR"
              className="w-full px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] focus:border-[var(--bdae-secondary)] text-xs bg-transparent outline-none font-mono text-[var(--bdae-text-primary)] uppercase transition-all"
            />
          </div>

          {/* Permissions Selection Checklist */}
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <label className="block text-xs font-bold text-[var(--bdae-text-primary)] flex items-center gap-1.5">
                <Key className="w-4 h-4 text-[var(--bdae-secondary)]" />
                <span>Select Granted Permissions ({selectedPermissionIds.length}/{permissions.length}) *</span>
              </label>

              <button
                type="button"
                onClick={toggleSelectAll}
                className="text-[11px] font-bold text-[var(--bdae-secondary)] hover:underline"
              >
                {selectedPermissionIds.length === permissions.length ? 'Deselect All' : 'Select All'}
              </button>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5 max-h-60 overflow-y-auto p-3 rounded-2xl border border-[var(--bdae-border)] bg-black/5 dark:bg-white/5">
              {permissions.map((perm) => {
                const permId = perm.permissionId || perm.id;
                const permName = getPermissionDisplayName(perm);
                const permDesc = getPermissionDescription(perm);
                const isChecked = selectedPermissionIds.includes(permId);

                return (
                  <div
                    key={permId || permName}
                    onClick={() => togglePermission(permId)}
                    className={`p-2.5 rounded-xl border cursor-pointer transition-all flex items-center justify-between select-none ${
                      isChecked
                        ? 'border-[var(--bdae-secondary)] bg-[var(--bdae-secondary)]/10 text-[var(--bdae-text-primary)] shadow-sm'
                        : 'border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5 text-[var(--bdae-text-secondary)]'
                    }`}
                  >
                    <div className="space-y-0.5 pr-2">
                      <p className="font-mono text-xs font-bold text-[var(--bdae-text-primary)]">{permName}</p>
                      {permDesc && (
                        <p className="text-[10px] opacity-75 leading-tight">{permDesc}</p>
                      )}
                    </div>

                    <div className={`w-4 h-4 rounded-md border shrink-0 flex items-center justify-center transition-all ${
                      isChecked ? 'bg-[var(--bdae-secondary)] border-[var(--bdae-secondary)] text-white' : 'border-[var(--bdae-border)]'
                    }`}>
                      {isChecked && <Check className="w-3 h-3" />}
                    </div>
                  </div>
                );
              })}
            </div>
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
            className="bdae-btn-primary w-full py-3 text-xs font-bold rounded-xl flex items-center justify-center gap-2 shadow-md shrink-0"
          >
            {isSubmitting ? (
              <>
                <RefreshCw className="w-4 h-4 animate-spin" />
                <span>Creating Custom Role...</span>
              </>
            ) : (
              <>
                <ShieldPlus className="w-4 h-4" />
                <span>Create Custom Role</span>
              </>
            )}
          </button>
        </form>

      </div>
    </div>
  );
};
