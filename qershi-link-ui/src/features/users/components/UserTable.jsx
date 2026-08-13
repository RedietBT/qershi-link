import React from 'react';
import { UserStatusBadge } from './UserStatusBadge';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { Users, Phone, Calendar, Building2, RefreshCw, AlertCircle, Edit3, Trash2, KeyRound, ShieldCheck, Crown } from 'lucide-react';

export const UserTable = ({
  users = [],
  isLoading,
  error,
  onEdit,
  onAssignRole,
  onResendPin,
  onDelete,
  onRefresh
}) => {
  if (isLoading) {
    return (
      <div className="bdae-card p-12 text-center space-y-3 border border-[var(--bdae-border)]">
        <RefreshCw className="w-8 h-8 text-[var(--bdae-secondary)] animate-spin mx-auto" />
        <p className="text-xs font-semibold text-[var(--bdae-text-secondary)]">
          Fetching User Accounts...
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

  if (users.length === 0) {
    return (
      <div className="bdae-card p-12 text-center space-y-3 border border-[var(--bdae-border)]">
        <Users className="w-10 h-10 text-[var(--bdae-text-secondary)] mx-auto opacity-50" />
        <p className="text-sm font-bold text-[var(--bdae-text-primary)]">No User Accounts Found</p>
        <p className="text-xs text-[var(--bdae-text-secondary)]">Register a user account to get started.</p>
      </div>
    );
  }

  return (
    <div className="bdae-card border border-[var(--bdae-border)] shadow-xl overflow-hidden rounded-2xl">
      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs border-collapse">
          <thead>
            <tr className="border-b border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 text-[11px] font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider">
              <th className="py-3.5 px-4">User Phone (MSISDN)</th>
              <th className="py-3.5 px-4">Global Role</th>
              <th className="py-3.5 px-4">Status</th>
              <th className="py-3.5 px-4">Last Login</th>
              <th className="py-3.5 px-4 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[var(--bdae-border)]">
            {users.map((user) => {
              const formattedLastLogin = user.lastLoginAt
                ? new Date(user.lastLoginAt).toLocaleString('en-US', {
                    year: 'numeric',
                    month: 'short',
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit'
                  })
                : 'Never Logged In';

              const isSuperAdminRole = (user.globalRole || '').toUpperCase().includes('SUPER_ADMIN');

              return (
                <tr 
                  key={user.userId}
                  className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors group"
                >
                  {/* Phone Number */}
                  <td className="py-3.5 px-4">
                    <div className="flex items-center space-x-3">
                      <div 
                        className="w-8 h-8 rounded-lg flex items-center justify-center text-white text-xs font-bold shadow-sm shrink-0"
                        style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
                      >
                        <Phone className="w-4 h-4" />
                      </div>
                      <div>
                        <p className="font-mono font-bold text-[var(--bdae-text-primary)] text-xs">
                          {user.msisdn}
                        </p>
                      </div>
                    </div>
                  </td>

                  {/* Global Role */}
                  <td className="py-3.5 px-4">
                    {isSuperAdminRole ? (
                      <span className="inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-cyan-500/10 text-cyan-600 border border-cyan-500/30">
                        <Crown className="w-3 h-3" />
                        <span>SUPER_ADMIN</span>
                      </span>
                    ) : (
                      <span className="inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-slate-500/10 text-slate-600 border border-slate-500/20">
                        <span>{user.globalRole || 'SACCO_USER'}</span>
                      </span>
                    )}
                  </td>

                  {/* Status Badge */}
                  <td className="py-3.5 px-4">
                    <UserStatusBadge status={user.status} />
                  </td>

                  {/* Last Login Date */}
                  <td className="py-3.5 px-4 text-[11px] text-[var(--bdae-text-secondary)] font-mono">
                    <div className="flex items-center space-x-1">
                      <Calendar className="w-3 h-3 opacity-60" />
                      <span>{formattedLastLogin}</span>
                    </div>
                  </td>

                  {/* Action Buttons */}
                  <td className="py-3.5 px-4 text-right space-x-1.5">
                    {/* Edit Security Parameters */}
                    <button
                      onClick={() => onEdit(user)}
                      className="px-2.5 py-1.5 rounded-xl border border-[var(--bdae-border)] hover:border-[var(--bdae-secondary)] hover:bg-[var(--bdae-secondary)]/10 text-[var(--bdae-secondary)] text-xs font-bold inline-flex items-center gap-1 transition-all shadow-sm"
                      title="Update Mobile Phone & Security Status"
                    >
                      <Edit3 className="w-3.5 h-3.5" />
                      <span>Edit</span>
                    </button>

                    {/* Resend PIN */}
                    <button
                      onClick={() => onResendPin(user.userId)}
                      className="px-2.5 py-1.5 rounded-xl border border-amber-500/30 hover:border-amber-500 bg-amber-500/5 hover:bg-amber-500/10 text-amber-600 dark:text-amber-400 text-xs font-bold inline-flex items-center gap-1 transition-all shadow-sm"
                      title="Resend Initial PIN via SMS"
                    >
                      <KeyRound className="w-3.5 h-3.5" />
                      <span>PIN</span>
                    </button>

                    {/* Assign Role */}
                    <button
                      onClick={() => onAssignRole(user)}
                      className="px-2.5 py-1.5 rounded-xl border border-cyan-500/30 hover:border-cyan-500 bg-cyan-500/5 hover:bg-cyan-500/10 text-cyan-600 dark:text-cyan-400 text-xs font-bold inline-flex items-center gap-1 transition-all shadow-sm"
                      title="Assign Role to User"
                    >
                      <ShieldCheck className="w-3.5 h-3.5" />
                      <span>Role</span>
                    </button>

                    {/* Delete User (SUPER_ADMIN ONLY) */}
                    <PermissionGuard role="SUPER_ADMIN">
                      <button
                        onClick={() => onDelete(user)}
                        className="px-2.5 py-1.5 rounded-xl border border-red-500/30 hover:border-red-500 bg-red-500/10 text-red-600 dark:text-red-400 text-xs font-bold inline-flex items-center gap-1 transition-all shadow-sm"
                        title="Purge User Identity"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                        <span>Purge</span>
                      </button>
                    </PermissionGuard>
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
