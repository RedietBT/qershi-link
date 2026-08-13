import React, { useState } from 'react';
import { useUserManagement } from '../hooks/useUserManagement';
import { UserStatsBar } from '../components/UserStatsBar';
import { UserFilterBar } from '../components/UserFilterBar';
import { UserTable } from '../components/UserTable';
import { CreateUserModal } from '../components/CreateUserModal';
import { UpdateUserModal } from '../components/UpdateUserModal';
import { AssignRoleModal } from '../components/AssignRoleModal';
import { PinResendModal } from '../../superadmin/components/PinResendModal';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { userApi } from '../api/userApi';
import { Users, UserPlus, RefreshCw, Lock, Trash2, AlertCircle } from 'lucide-react';

export const UserManagementPage = () => {
  const {
    users,
    isLoading,
    error,
    saccoIdFilter,
    searchTerm,
    setSearchTerm,
    statusFilter,
    setStatusFilter,
    refreshUsers,
    isCreateOpen,
    setIsCreateOpen,
    editingUser,
    setEditingUser,
    roleAssignUser,
    setRoleAssignUser,
    deletingUser,
    setDeletingUser
  } = useUserManagement();

  // Resend PIN modal state
  const [resendPinUserId, setResendPinUserId] = useState(null);

  // Deletion submit state
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState(null);

  const handleConfirmDelete = async () => {
    if (!deletingUser) return;
    setIsDeleting(true);
    setDeleteError(null);
    try {
      await userApi.deleteUser(deletingUser.userId);
      setIsDeleting(false);
      setDeletingUser(null);
      refreshUsers();
    } catch (err) {
      setIsDeleting(false);
      const msg = err.response?.data?.message || err.message || 'Failed to purge user identity record.';
      setDeleteError(msg);
    }
  };

  return (
    <PermissionGuard role="SUPER_ADMIN" fallback={
      <div className="p-8 text-center max-w-lg mx-auto space-y-4">
        <div className="w-12 h-12 rounded-full bg-amber-500/10 border border-amber-500/30 text-amber-600 mx-auto flex items-center justify-center">
          <Lock className="w-6 h-6" />
        </div>
        <h2 className="text-lg font-bold">Access Restricted</h2>
        <p className="text-xs text-[var(--bdae-text-secondary)]">
          User Account Management requires Super Admin authorization (<code className="font-mono bg-black/10 dark:bg-white/10 px-1 py-0.5 rounded">ROLE_SUPER_ADMIN</code>).
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
              <Users className="w-5 h-5" />
            </div>
            <div>
              <h1 className="text-xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
                User Account Management
              </h1>
              <p className="text-xs text-[var(--bdae-text-secondary)]">
                Track, register, update security parameters, and purge identity records (<code className="font-mono">GET /api/v1/users</code>).
              </p>
            </div>
          </div>

          <div className="flex items-center space-x-3">
            <button
              onClick={refreshUsers}
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
              <UserPlus className="w-4 h-4" />
              <span>Register New User</span>
            </button>
          </div>
        </div>

        {/* Stats Bar */}
        <UserStatsBar users={users} />

        {/* Filter Bar */}
        <UserFilterBar 
          searchTerm={searchTerm} 
          setSearchTerm={setSearchTerm}
          statusFilter={statusFilter}
          setStatusFilter={setStatusFilter}
        />

        {/* User Table */}
        <UserTable 
          users={users} 
          isLoading={isLoading} 
          error={error}
          onEdit={(user) => setEditingUser(user)}
          onAssignRole={(user) => setRoleAssignUser(user)}
          onResendPin={(userId) => setResendPinUserId(userId)}
          onDelete={(user) => setDeletingUser(user)}
          onRefresh={refreshUsers}
        />

        {/* Create User Modal */}
        {isCreateOpen && (
          <CreateUserModal
            defaultSaccoId={saccoIdFilter || ''}
            onClose={() => setIsCreateOpen(false)}
            onSuccess={refreshUsers}
          />
        )}

        {/* Update Security Parameters Modal */}
        {editingUser && (
          <UpdateUserModal
            user={editingUser}
            onClose={() => setEditingUser(null)}
            onSuccess={refreshUsers}
          />
        )}

        {/* Assign Role Modal */}
        {roleAssignUser && (
          <AssignRoleModal
            user={roleAssignUser}
            onClose={() => setRoleAssignUser(null)}
            onSuccess={refreshUsers}
          />
        )}

        {/* Resend PIN Modal */}
        {resendPinUserId && (
          <PinResendModal
            initialMode="userId"
            initialTarget={resendPinUserId}
            onClose={() => setResendPinUserId(null)}
          />
        )}

        {/* Delete Confirmation Modal */}
        {deletingUser && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
            <div className="bdae-card p-6 max-w-md w-full rounded-3xl shadow-2xl border border-red-500/30 space-y-5 text-center">
              <div className="w-14 h-14 rounded-2xl bg-red-500/10 text-red-500 mx-auto flex items-center justify-center border border-red-500/20">
                <Trash2 className="w-7 h-7" />
              </div>

              <div>
                <h2 className="text-lg font-bold text-red-600 dark:text-red-400">
                  Purge User Identity Record?
                </h2>
                <p className="text-xs text-[var(--bdae-text-secondary)] mt-1">
                  You are about to issue a cascading deletion for user <strong className="font-mono text-[var(--bdae-text-primary)]">{deletingUser.msisdn}</strong> (<code className="font-mono">{deletingUser.userId}</code>). This operation cannot be undone.
                </p>
              </div>

              {deleteError && (
                <div className="p-3 rounded-xl bg-red-500/10 text-red-500 text-xs flex items-center gap-2 text-left">
                  <AlertCircle className="w-4 h-4 shrink-0" />
                  <span>{deleteError}</span>
                </div>
              )}

              <div className="grid grid-cols-2 gap-3 pt-2">
                <button
                  onClick={() => setDeletingUser(null)}
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
                  <span>Purge User</span>
                </button>
              </div>

            </div>
          </div>
        )}

      </div>
    </PermissionGuard>
  );
};
