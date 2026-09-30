import React from 'react';
import { Trash2, AlertCircle, RefreshCw } from 'lucide-react';

export const RoleDeleteModal = ({
  role,
  isDeleting,
  deleteError,
  onClose,
  onConfirm
}) => {
  if (!role) return null;

  return (
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
            You are about to delete custom role <strong className="font-mono text-[var(--bdae-text-primary)]">{role.roleName}</strong>. This action requires that zero active users are assigned to this role.
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
            onClick={onClose}
            className="px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] text-xs font-bold text-[var(--bdae-text-primary)] hover:bg-black/5 dark:hover:bg-white/5"
          >
            Cancel
          </button>

          <button
            onClick={onConfirm}
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
  );
};
