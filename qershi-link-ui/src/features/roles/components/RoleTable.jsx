import React, { useState } from 'react';
import { RoleBadge } from './RoleBadge';
import { 
  getPermissionDisplayName, 
  getPermissionDescription, 
  groupPermissionsByResource 
} from '../utils/permissionUtils';
import { 
  ShieldCheck, 
  RefreshCw, 
  AlertCircle, 
  Edit3, 
  Trash2, 
  Key, 
  Shield, 
  ChevronDown, 
  ChevronUp, 
  Search, 
  FolderCheck, 
  Lock 
} from 'lucide-react';

export const RoleTable = ({
  roles = [],
  isLoading,
  error,
  onEdit,
  onDelete,
  onRefresh
}) => {
  const [expandedRoleId, setExpandedRoleId] = useState(null);
  const [drawerSearchTerm, setDrawerSearchTerm] = useState('');

  const toggleExpand = (roleId) => {
    if (expandedRoleId === roleId) {
      setExpandedRoleId(null);
    } else {
      setExpandedRoleId(roleId);
      setDrawerSearchTerm('');
    }
  };

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
              const roleId = role.roleId || role.id || role.roleName;
              const isExpanded = expandedRoleId === roleId;

              // Filter drawer permissions by live search term
              const filteredDrawerPerms = permsList.filter((perm) => {
                const term = drawerSearchTerm.toLowerCase().trim();
                if (!term) return true;

                const pName = getPermissionDisplayName(perm).toLowerCase();
                const pDesc = getPermissionDescription(perm).toLowerCase();
                const pResource = (perm.resource || '').toLowerCase();

                return pName.includes(term) || pDesc.includes(term) || pResource.includes(term);
              });

              // Group drawer permissions by resource category
              const groupedDrawerPerms = groupPermissionsByResource(filteredDrawerPerms);

              return (
                <React.Fragment key={roleId}>
                  <tr 
                    onClick={() => toggleExpand(roleId)}
                    className="hover:bg-black/5 dark:hover:bg-white/5 cursor-pointer transition-colors group select-none"
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
                          <p className="font-mono font-bold text-[var(--bdae-text-primary)] text-xs flex items-center gap-1.5">
                            <span>{role.roleName}</span>
                            {isExpanded ? (
                              <ChevronUp className="w-3.5 h-3.5 text-[var(--bdae-secondary)]" />
                            ) : (
                              <ChevronDown className="w-3.5 h-3.5 text-[var(--bdae-text-secondary)] opacity-60 group-hover:opacity-100" />
                            )}
                          </p>
                          <p className="text-[10px] text-[var(--bdae-text-secondary)] font-mono">
                            Click to {isExpanded ? 'collapse' : 'inspect permissions by resource'}
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
                          <span>{permsList.length} Permissions Granted</span>
                        </span>

                        {/* Display first 3 permission tags */}
                        {permsList.length > 0 && (
                          <div className="flex flex-wrap gap-1 max-w-md">
                            {permsList.slice(0, 3).map((p, idx) => (
                              <span 
                                key={p.permissionId || p.id || idx}
                                className="text-[9px] font-mono px-1.5 py-0.5 rounded bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] text-[var(--bdae-text-secondary)]"
                              >
                                {getPermissionDisplayName(p)}
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
                      {/* Edit Role Button (Always Available) */}
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          onEdit(role);
                        }}
                        className="px-2.5 py-1.5 rounded-xl border border-[var(--bdae-border)] hover:border-[var(--bdae-secondary)] hover:bg-[var(--bdae-secondary)]/10 text-[var(--bdae-secondary)] text-xs font-bold inline-flex items-center gap-1 transition-all shadow-sm"
                        title="Update Role & Manage Permissions"
                      >
                        <Edit3 className="w-3.5 h-3.5" />
                        <span>Update Role</span>
                      </button>

                      {/* Delete Action (Custom Roles Only) */}
                      {!isSystemDefined ? (
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            onDelete(role);
                          }}
                          className="px-2.5 py-1.5 rounded-xl border border-red-500/30 hover:border-red-500 bg-red-500/10 text-red-600 dark:text-red-400 text-xs font-bold inline-flex items-center gap-1 transition-all shadow-sm"
                          title="Delete Custom Role"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                          <span>Delete</span>
                        </button>
                      ) : (
                        <span className="inline-flex items-center gap-1 text-[10px] font-mono text-[var(--bdae-text-secondary)] italic px-1">
                          <Lock className="w-3 h-3 opacity-60" /> System
                        </span>
                      )}
                    </td>
                  </tr>

                  {/* Expanded Categorized Permission Breakdown Drawer */}
                  {isExpanded && (
                    <tr className="bg-black/5 dark:bg-white/5 border-b border-[var(--bdae-border)]">
                      <td colSpan="4" className="p-4">
                        <div className="p-4 rounded-2xl bdae-surface border border-[var(--bdae-border)] space-y-4 shadow-inner">
                          
                          {/* Drawer Header & Controls */}
                          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-[var(--bdae-border)] pb-3 text-xs">
                            <div className="flex items-center space-x-2">
                              <Key className="w-4 h-4 text-[var(--bdae-secondary)] shrink-0" />
                              <div>
                                <span className="font-bold text-[var(--bdae-text-primary)]">
                                  Resource Permission Breakdown for <code className="font-mono underline font-bold">{role.roleName}</code>
                                </span>
                                <span className="text-[10px] text-[var(--bdae-text-secondary)] block font-mono">
                                  Total: {permsList.length} Granted Authorities
                                </span>
                              </div>
                            </div>

                            <div className="flex items-center space-x-3 self-end sm:self-auto">
                              {/* Edit Button in Drawer */}
                              <button
                                onClick={(e) => {
                                  e.stopPropagation();
                                  onEdit(role);
                                }}
                                className="bdae-btn-primary px-3 py-1.5 text-xs font-bold rounded-xl inline-flex items-center gap-1.5 shadow-sm"
                              >
                                <Edit3 className="w-3.5 h-3.5" />
                                <span>Update Role Permissions</span>
                              </button>
                            </div>
                          </div>

                          {/* Drawer Search Filter */}
                          <div className="relative">
                            <input
                              type="text"
                              value={drawerSearchTerm}
                              onChange={(e) => setDrawerSearchTerm(e.target.value)}
                              placeholder="Filter permissions within this role by name (e.g., ACCOUNT_OPEN) or resource..."
                              className="w-full pl-9 pr-3 py-2 rounded-xl border border-[var(--bdae-border)] focus:border-[var(--bdae-secondary)] text-xs bg-transparent outline-none text-[var(--bdae-text-primary)] font-mono transition-all"
                            />
                            <Search className="w-3.5 h-3.5 text-[var(--bdae-text-secondary)] absolute left-3 top-2.5" />
                          </div>

                          {/* Categorized Permissions Breakdown */}
                          {permsList.length === 0 ? (
                            <p className="text-xs text-[var(--bdae-text-secondary)] italic">No permissions assigned to this role.</p>
                          ) : Object.keys(groupedDrawerPerms).length === 0 ? (
                            <p className="text-xs text-[var(--bdae-text-secondary)] italic text-center py-4">
                              No permissions matching search query "{drawerSearchTerm}".
                            </p>
                          ) : (
                            <div className="space-y-3 max-h-80 overflow-y-auto pr-1">
                              {Object.entries(groupedDrawerPerms).map(([category, categoryPerms]) => (
                                <div key={category} className="space-y-1.5">
                                  {/* Resource Category Header */}
                                  <div className="flex items-center space-x-2 font-mono font-bold text-xs text-[var(--bdae-primary)] dark:text-[var(--bdae-secondary)] bg-black/10 dark:bg-white/10 px-3 py-1 rounded-xl border border-[var(--bdae-border)]">
                                    <FolderCheck className="w-3.5 h-3.5 text-[var(--bdae-secondary)]" />
                                    <span>RESOURCE: {category} ({categoryPerms.length} permissions)</span>
                                  </div>

                                  {/* Permissions Grid */}
                                  <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-2 pl-2">
                                    {categoryPerms.map((perm, idx) => {
                                      const pName = getPermissionDisplayName(perm);
                                      const pDesc = getPermissionDescription(perm);

                                      return (
                                        <div 
                                          key={perm.permissionId || perm.id || idx}
                                          className="p-2.5 rounded-xl border border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 space-y-1 hover:border-[var(--bdae-secondary)] transition-colors"
                                        >
                                          <div className="flex items-center justify-between">
                                            <span className="font-mono text-xs font-bold text-[var(--bdae-text-primary)]">
                                              {pName}
                                            </span>
                                            <span className="text-[9px] font-mono px-1.5 py-0.2 rounded bg-[var(--bdae-secondary)]/10 text-[var(--bdae-secondary)] font-bold">
                                              {category}
                                            </span>
                                          </div>
                                          {pDesc && (
                                            <p className="text-[10px] text-[var(--bdae-text-secondary)] leading-tight">
                                              {pDesc}
                                            </p>
                                          )}
                                        </div>
                                      );
                                    })}
                                  </div>
                                </div>
                              ))}
                            </div>
                          )}

                        </div>
                      </td>
                    </tr>
                  )}
                </React.Fragment>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};
