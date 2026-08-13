import React, { useState } from 'react';
import { useRoleManagement } from '../hooks/useRoleManagement';
import { RoleStatsBar } from '../components/RoleStatsBar';
import { RoleFilterBar } from '../components/RoleFilterBar';
import { RoleTable } from '../components/RoleTable';
import { CreateRoleModal } from '../components/CreateRoleModal';
import { UpdateRoleModal } from '../components/UpdateRoleModal';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { roleApi } from '../api/roleApi';
import { ShieldCheck, ShieldPlus, RefreshCw, Lock, Trash2, AlertCircle } from 'lucide-react';

export const RoleManagementPage = () => {
  const {
    roles,
    rawRoles,
    permissions,
    isLoading,
    error,
    searchTerm,
    setSearchTerm,
    typeFilter,
    setTypeFilter,
    refreshRoles,
    isCreateOpen,
    setIsCreateOpen,
    editingRole,
    setEditingRole,
    deletingRole,
    setDeletingRole
  } = useRoleManagement();

  // Deletion submit state
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState(null);

  const handleConfirmDelete = async () => {
    if (!deletingRole) return;
    setIsDeleting(true);
    setDeleteError(null);
    try {
      const roleId = deletingRole.roleId || deletingRole.id;
      await roleApi.deleteRole(roleId);
      setIsDeleting(false);
      setDeletingRole(null);
      refreshRoles();
    } catch (err) {
      setIsDeleting(false);
      const msg = err.response?.data?.message || err.response?.data || err.message || 'Failed to delete custom role definition.';
      setDeleteError(msg);
    }
  };

  return (
    <PermissionGuard roles={['SUPER_ADMIN', 'SACCO_ADMIN']} fallback={
      <div className="p-8 text-center max-w-lg mx-auto space-y-4">
        <div className="w-12 h-12 rounded-full bg-amber-500/10 border border-amber-500/30 text-amber-600 mx-auto flex items-center justify-center">
          <Lock className="w-6 h-6" />
        </div>
        <h2 className="text-lg font-bold">Access Restricted</h2>
        <p className="text-xs text-[var(--bdae-text-secondary)]">
          Role & RBAC Management requires Super Admin or SACCO Admin authorization.
        </p>
      </div>
    }>
      <div className="space-y-6 animate-fadeIn max-w-7xl mx-auto">
        {/* Page Header */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-[var(--bdae-border)] pb-4">
          <div className="flex items-center space-x-3">
            <div 
              className="w-10 h-10 rounded-xl flex items-center justify-center text-white shadow-md shrink-0"
              style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
            >
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <h1 className="text-xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
                Role & RBAC Management
              </h1>
              <p className="text-xs text-[var(--bdae-text-secondary)]">
                Manage system platform roles, custom tenant role definitions, and permission bundles (<code className="font-mono">GET /api/v1/roles</code>).
              </p>
            </div>
          </div>

          <div className="flex items-center space-x-3">
            <button
              onClick={refreshRoles}
              disabled={isLoading}
              className="px-3.5 py-2 rounded-xl border border-[var(--bdae-border)] hover:border-[var(--bdae-secondary)] text-xs font-bold flex items-center gap-2 transition-all shadow-sm"
            >
              <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
              <span>Refresh</span>
            </button>

            <button
              onClick={() => setIsCreateOpen(true)}
              className="bdae-btn-primary px-4 py-2 text-xs font-bold rounded-xl flex items-center gap-2 shadow-md"
            >
              <ShieldPlus className="w-4 h-4" />
              <span>Create Custom Role</span>
            </button>
          </div>
        </div>

        {/* Stats Bar */}
        <RoleStatsBar roles={rawRoles} permissions={permissions} />

        {/* Filter Bar */}
        <RoleFilterBar 
          searchTerm={searchTerm} 
          setSearchTerm={setSearchTerm}
          typeFilter={typeFilter}
          setTypeFilter={setTypeFilter}
        />

        {/* Role Table */}
        <RoleTable 
          roles={roles} 
          isLoading={isLoading} 
          error={error}
          onEdit={(role) => setEditingRole(role)}
          onDelete={(role) => setDeletingRole(role)}
          onRefresh={refreshRoles}
        />

        {/* Create Role Modal */}
        {isCreateOpen && (
          <CreateRoleModal
            permissions={permissions}
            onClose={() => setIsCreateOpen(false)}
            onSuccess={refreshRoles}
          />
        )}

        {/* Update Role Modal */}
        {editingRole && (
          <UpdateRoleModal
            role={editingRole}
            permissions={permissions}
            onClose={() => setEditingRole(null)}
            onSuccess={refreshRoles}
          />
        )}

        {/* Delete Confirmation Modal */}
        {deletingRole && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
            <div className="bdae-card p-6 max-w-md w-full rounded-3xl shadow-2xl border border-red-500/30 space-y-5 text-center">
              <div className="w-14 h-14 rounded-2xl bg-red-500/10 text-red-500 mx-auto flex items-center justify-center border border-red-500/20">
                <Trash2 className="w-7 h-7" />
              </div>

              <div>
                <h2 className="text-lg font-bold text-red-600 dark:text-red-400">
                  Delete Custom Role?
                </h2>
                <p className="text-xs text-[var(--bdae-text-secondary)] mt-1">
                  You are about to delete custom role <strong className="font-mono text-[var(--bdae-text-primary)]">{deletingRole.roleName}</strong>. This action requires that zero active users are assigned to this role.
                </p>
              </div>

              {deleteError && (
                <div className="p-3 rounded-xl bg-red-500/10 text-red-500 text-xs flex items-center gap-2 text-left">
                  <AlertCircle className="w-4 h-4 shrink-0 text-red-500" />
                  <span>{deleteError}</span>
                </div>
              )}

              <div className="grid grid-cols-2 gap-3 pt-2">
                <button
                  onClick={() => setDeletingRole(null)}
                  className="px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] text-xs font-bold text-[var(--bdae-text-primary)] hover:bg-black/5 dark:hover:bg-white/5"
                >
                  Cancel
                </button>

                <button
                  onClick={handleConfirmDelete}
                  disabled={isDeleting}
                  className="px-4 py-2.5 rounded-xl bg-red-600 hover:bg-red-700 text-white text-xs font-bold flex items-center justify-center gap-2 shadow-md"
                >
                  {isDeleting ? (
                    <RefreshCw className="w-4 h-4 animate-spin" />
                  ) : (
                    <Trash2 className="w-4 h-4" />
                  )}
                  <span>Delete Role</span>
                </button>
              </div>

            </div>
          </div>
        )}

      </div>
    </PermissionGuard>
  );
};
