import React, { useState, useEffect } from 'react';
import { roleApi } from '../api/roleApi';
import { 
  getPermissionDisplayName, 
  getPermissionDescription, 
  groupPermissionsByResource 
} from '../utils/permissionUtils';
import { X, ShieldCheck, CheckCircle2, AlertCircle, RefreshCw, Key, Check, Search, FolderCheck } from 'lucide-react';

export const UpdateRoleModal = ({ role, permissions = [], onClose, onSuccess }) => {
  const [roleNameInput, setRoleNameInput] = useState(role?.roleName || '');
  const [selectedPermissionIds, setSelectedPermissionIds] = useState([]);
  const [permSearchTerm, setPermSearchTerm] = useState('');

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [apiError, setApiError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  useEffect(() => {
    if (role) {
      setRoleNameInput(role.roleName || '');
      const existingIds = (role.permissions || []).map(p => p.permissionId || p.id).filter(Boolean);
      setSelectedPermissionIds(existingIds);
    }
  }, [role]);

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

  const toggleCategory = (categoryPermissions) => {
    const categoryIds = categoryPermissions.map(p => p.permissionId || p.id).filter(Boolean);
    const allSelected = categoryIds.every(id => selectedPermissionIds.includes(id));

    if (allSelected) {
      setSelectedPermissionIds(selectedPermissionIds.filter(id => !categoryIds.includes(id)));
    } else {
      const newSelected = new Set([...selectedPermissionIds, ...categoryIds]);
      setSelectedPermissionIds(Array.from(newSelected));
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
      setApiError('Tenant administrators cannot rename roles to SUPER_ADMIN.');
      return;
    }

    if (selectedPermissionIds.length === 0) {
      setApiError('Please select at least one permission for this custom role.');
      return;
    }

    setIsSubmitting(true);
    try {
      const roleId = role.roleId || role.id;
      await roleApi.updateRole(roleId, {
        roleName: name,
        permissionIds: selectedPermissionIds
      });

      setIsSubmitting(false);
      setSuccessMessage(`Custom local role '${name}' updated successfully!`);

      setTimeout(() => {
        if (onSuccess) onSuccess();
        onClose();
      }, 1500);
    } catch (err) {
      setIsSubmitting(false);
      const msg = err.response?.data?.message || err.response?.data || err.message || 'Failed to update custom role.';
      setApiError(msg);
    }
  };

  // Filter permissions by live search term
  const filteredPermissions = permissions.filter((perm) => {
    const term = permSearchTerm.toLowerCase().trim();
    if (!term) return true;

    const pName = getPermissionDisplayName(perm).toLowerCase();
    const pDesc = getPermissionDescription(perm).toLowerCase();
    const pResource = (perm.resource || '').toLowerCase();

    return pName.includes(term) || pDesc.includes(term) || pResource.includes(term);
  });

  // Group filtered permissions by resource category
  const groupedPermissions = groupPermissionsByResource(filteredPermissions);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
      <div className="bdae-card p-6 md:p-8 max-w-3xl w-full rounded-3xl shadow-2xl border border-[var(--bdae-border)] space-y-6 relative max-h-[92vh] flex flex-col">
        
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
            <ShieldCheck className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-lg font-bold tracking-tight text-[var(--bdae-text-primary)]">
              Update Custom Role Permissions
            </h2>
            <p className="text-xs text-[var(--bdae-text-secondary)]">
              Add or remove granted permissions for <code className="font-mono underline font-bold">{role?.roleName}</code>.
            </p>
          </div>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="space-y-5 text-xs overflow-y-auto pr-1 flex-1">
          
          {/* Role Name Input */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-[var(--bdae-text-primary)]">
              Role Name *
            </label>
            <input
              type="text"
              value={roleNameInput}
              onChange={(e) => setRoleNameInput(e.target.value.toUpperCase())}
              placeholder="e.g. CUSTOM_ROLE_NAME"
              className="w-full px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] focus:border-[var(--bdae-secondary)] text-xs bg-transparent outline-none font-mono text-[var(--bdae-text-primary)] uppercase transition-all"
            />
          </div>

          {/* Permissions Search & Control Bar */}
          <div className="space-y-3 border-t border-[var(--bdae-border)] pt-4">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <label className="block text-xs font-bold text-[var(--bdae-text-primary)] flex items-center gap-1.5">
                <Key className="w-4 h-4 text-[var(--bdae-secondary)]" />
                <span>Assigned Permissions ({selectedPermissionIds.length}/{permissions.length} Selected) *</span>
              </label>

              <button
                type="button"
                onClick={toggleSelectAll}
                className="text-[11px] font-bold text-[var(--bdae-secondary)] hover:underline self-end sm:self-auto"
              >
                {selectedPermissionIds.length === permissions.length ? 'Deselect All Permissions' : 'Select All Permissions'}
              </button>
            </div>

            {/* Permission Live Search Bar */}
            <div className="relative">
              <input
                type="text"
                value={permSearchTerm}
                onChange={(e) => setPermSearchTerm(e.target.value)}
                placeholder="Search permissions by name (e.g., ACCOUNT_OPEN) or resource (LOAN, KYC)..."
                className="w-full pl-10 pr-4 py-2 rounded-xl border border-[var(--bdae-border)] focus:border-[var(--bdae-secondary)] text-xs bg-transparent outline-none text-[var(--bdae-text-primary)] font-mono transition-all"
              />
              <Search className="w-4 h-4 text-[var(--bdae-text-secondary)] absolute left-3.5 top-2.5" />
            </div>
          </div>

          {/* Categorized Permissions Grid */}
          <div className="space-y-4 max-h-72 overflow-y-auto p-3 rounded-2xl border border-[var(--bdae-border)] bg-black/5 dark:bg-white/5">
            {Object.keys(groupedPermissions).length === 0 ? (
              <p className="text-xs text-[var(--bdae-text-secondary)] italic text-center py-6">
                No permissions matching search query "{permSearchTerm}".
              </p>
            ) : (
              Object.entries(groupedPermissions).map(([category, categoryPerms]) => {
                const categoryIds = categoryPerms.map(p => p.permissionId || p.id).filter(Boolean);
                const categorySelectedCount = categoryIds.filter(id => selectedPermissionIds.includes(id)).length;
                const isAllCategorySelected = categorySelectedCount === categoryIds.length;

                return (
                  <div key={category} className="space-y-2">
                    {/* Category Header */}
                    <div className="flex items-center justify-between bg-black/10 dark:bg-white/10 px-3 py-1.5 rounded-xl border border-[var(--bdae-border)]">
                      <div className="flex items-center space-x-2 font-mono font-bold text-xs text-[var(--bdae-primary)] dark:text-[var(--bdae-secondary)]">
                        <FolderCheck className="w-4 h-4 text-[var(--bdae-secondary)]" />
                        <span>RESOURCE: {category}</span>
                        <span className="text-[10px] text-[var(--bdae-text-secondary)] font-normal">
                          ({categorySelectedCount}/{categoryPerms.length} selected)
                        </span>
                      </div>

                      <button
                        type="button"
                        onClick={() => toggleCategory(categoryPerms)}
                        className="text-[10px] font-bold text-[var(--bdae-secondary)] hover:underline"
                      >
                        {isAllCategorySelected ? 'Deselect Category' : 'Select Category'}
                      </button>
                    </div>

                    {/* Permission Items Grid */}
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 pl-2">
                      {categoryPerms.map((perm) => {
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
                );
              })
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
            disabled={isSubmitting}
            className="bdae-btn-primary w-full py-3 text-xs font-bold rounded-xl flex items-center justify-center gap-2 shadow-md shrink-0"
          >
            {isSubmitting ? (
              <>
                <RefreshCw className="w-4 h-4 animate-spin" />
                <span>Updating Role Definition...</span>
              </>
            ) : (
              <>
                <ShieldCheck className="w-4 h-4" />
                <span>Save Role Permissions</span>
              </>
            )}
          </button>
        </form>

      </div>
    </div>
  );
};
