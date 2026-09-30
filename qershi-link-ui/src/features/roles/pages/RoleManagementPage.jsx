import React, { useState } from 'react';
import { useRoleManagement } from '../hooks/useRoleManagement';
import { RoleStatsBar } from '../components/RoleStatsBar';
import { RoleFilterBar } from '../components/RoleFilterBar';
import { RoleTable } from '../components/RoleTable';
import { CreateRoleModal } from '../components/CreateRoleModal';
import { UpdateRoleModal } from '../components/UpdateRoleModal';
import { RoleDeleteModal } from '../components/RoleDeleteModal';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { roleApi } from '../api/roleApi';
import { ShieldCheck, ShieldPlus, RefreshCw, Lock } from 'lucide-react';

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

            <PermissionGuard permissions={['ROLE_MANAGE']} roles={['SUPER_ADMIN', 'SACCO_ADMIN']}>
              <button
                onClick={() => setIsCreateOpen(true)}
                className="bdae-btn-primary px-4 py-2 text-xs font-bold rounded-xl flex items-center gap-2 shadow-md"
              >
                <ShieldPlus className="w-4 h-4" />
                <span>Create Custom Role</span>
              </button>
            </PermissionGuard>
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
        <RoleDeleteModal
          role={deletingRole}
          isDeleting={isDeleting}
          deleteError={deleteError}
          onClose={() => setDeletingRole(null)}
          onConfirm={handleConfirmDelete}
        />

      </div>
    </PermissionGuard>
  );
};
