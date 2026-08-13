import React from 'react';
import { RoleBadge } from './RoleBadge';
import { ShieldCheck, RefreshCw, AlertCircle, Edit3, Trash2, Key, Shield } from 'lucide-react';

export const RoleTable = ({
  roles = [],
  isLoading,
  error,
  onEdit,
  onDelete,
  onRefresh
}) => {
  if (isLoading) {
    return (
      <div className="bdae-card p-12 text-center space-y-3 border border-[var(--bdae-border)]">
        <RefreshCw className="w-8 h-8 text-[var(--bdae-secondary)] animate-spin mx-auto" />
        <p className="text-xs font-semibold text-[var(--bdae-text-secondary)]">
          Fetching RBAC Roles & Permission Definitions...
        </p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bdae-card p-8 border border-red-500/30 bg-red-500/5 text-center space-y-3">
        <AlertCircle className="w-8 h-8 text-red-500 mx-auto" />
        <p className="text-xs font-bold text-red-600 dark:text-red-400">{error}</p>
        <button
          onClick={onRefresh}
          className="bdae-btn-primary px-4 py-2 text-xs font-bold rounded-xl inline-flex items-center gap-1.5"
        >
          <RefreshCw className="w-3.5 h-3.5" /> Retry Load
        </button>
      </div>
    );
  }

  if (roles.length === 0) {
    return (
      <div className="bdae-card p-12 text-center space-y-3 border border-[var(--bdae-border)]">
        <ShieldCheck className="w-10 h-10 text-[var(--bdae-text-secondary)] mx-auto opacity-50" />
        <p className="text-sm font-bold text-[var(--bdae-text-primary)]">No RBAC Roles Found</p>
        <p className="text-xs text-[var(--bdae-text-secondary)]">Create a custom role to bundle permissions.</p>
      </div>
    );
  }

  return (
    <div className="bdae-card border border-[var(--bdae-border)] shadow-xl overflow-hidden rounded-2xl">
      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs border-collapse">
          <thead>
            <tr className="border-b border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 text-[11px] font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider">
              <th className="py-3.5 px-4">Role Identifier</th>
              <th className="py-3.5 px-4">Role Classification</th>
              <th className="py-3.5 px-4">Granted Permissions</th>
              <th className="py-3.5 px-4 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[var(--bdae-border)]">
            {roles.map((role) => {
              const isSystemDefined = Boolean(role.isSystemDefined || role.systemDefined);
              const permsList = Array.isArray(role.permissions) ? role.permissions : [];
              const roleId = role.roleId || role.id;

              return (
                <tr 
                  key={roleId || role.roleName}
                  className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors group"
                >
                  {/* Role Name */}
                  <td className="py-3.5 px-4">
                    <div className="flex items-center space-x-3">
                      <div 
                        className="w-8 h-8 rounded-lg flex items-center justify-center text-white text-xs font-bold shadow-sm shrink-0"
                        style={{
                          background: isSystemDefined
                            ? 'linear-gradient(135deg, #06b6d4, #3b82f6)'
                            : 'linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))'
                        }}
                      >
                        {isSystemDefined ? <ShieldCheck className="w-4 h-4" /> : <Shield className="w-4 h-4" />}
                      </div>
                      <div>
                        <p className="font-mono font-bold text-[var(--bdae-text-primary)] text-xs">
                          {role.roleName}
                        </p>
                      </div>
                    </div>
                  </td>

                  {/* System vs Custom Badge */}
                  <td className="py-3.5 px-4">
                    <RoleBadge isSystemDefined={isSystemDefined} />
                  </td>

                  {/* Granted Permissions count and tags */}
                  <td className="py-3.5 px-4">
                    <div className="space-y-1.5">
                      <span className="inline-flex items-center space-x-1 font-mono text-[11px] bg-black/5 dark:bg-white/5 px-2 py-0.5 rounded-md border border-[var(--bdae-border)] font-bold text-[var(--bdae-text-primary)]">
                        <Key className="w-3 h-3 text-[var(--bdae-secondary)]" />
                        <span>{permsList.length} Permissions</span>
                      </span>

                      {/* Display first 3 permission tags */}
                      {permsList.length > 0 && (
                        <div className="flex flex-wrap gap-1 max-w-md">
                          {permsList.slice(0, 3).map((p, idx) => (
                            <span 
                              key={p.permissionId || p.id || idx}
                              className="text-[9px] font-mono px-1.5 py-0.5 rounded bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] text-[var(--bdae-text-secondary)]"
                            >
                              {p.permissionName || p.name || p.authority || String(p)}
                            </span>
                          ))}
                          {permsList.length > 3 && (
                            <span className="text-[9px] font-mono px-1.5 py-0.5 rounded bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] text-[var(--bdae-secondary)] font-bold">
                              +{permsList.length - 3} more
                            </span>
                          )}
                        </div>
                      )}
                    </div>
                  </td>

                  {/* Actions */}
                  <td className="py-3.5 px-4 text-right space-x-1.5">
                    {/* Edit Custom Role */}
                    {!isSystemDefined ? (
                      <>
                        <button
                          onClick={() => onEdit(role)}
                          className="px-2.5 py-1.5 rounded-xl border border-[var(--bdae-border)] hover:border-[var(--bdae-secondary)] hover:bg-[var(--bdae-secondary)]/10 text-[var(--bdae-secondary)] text-xs font-bold inline-flex items-center gap-1 transition-all shadow-sm"
                          title="Update Custom Role Permissions"
                        >
                          <Edit3 className="w-3.5 h-3.5" />
                          <span>Edit</span>
                        </button>

                        <button
                          onClick={() => onDelete(role)}
                          className="px-2.5 py-1.5 rounded-xl border border-red-500/30 hover:border-red-500 bg-red-500/10 text-red-600 dark:text-red-400 text-xs font-bold inline-flex items-center gap-1 transition-all shadow-sm"
                          title="Delete Custom Role"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                          <span>Delete</span>
                        </button>
                      </>
                    ) : (
                      <span className="text-[10px] font-mono text-[var(--bdae-text-secondary)] italic">
                        System Protected
                      </span>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};
