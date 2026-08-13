import React from 'react';
import { Users, CheckCircle2, Clock, Crown } from 'lucide-react';

export const UserStatsBar = ({ users = [] }) => {
  const totalCount = users.length;
  const activeCount = users.filter(u => (u.status || '').toUpperCase() === 'ACTIVE').length;
  const pendingCount = users.filter(u => {
    const s = (u.status || '').toUpperCase();
    return s === 'PENDING' || s === 'PASSWORD_CHANGE_REQUIRED';
  }).length;
  const superAdminCount = users.filter(u => {
    const r = (u.globalRole || '').toUpperCase();
    return r === 'SUPER_ADMIN' || r === 'ROLE_SUPER_ADMIN';
  }).length;

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      {/* Total Accounts */}
      <div className="bdae-card p-5 flex items-center space-x-4 border border-[var(--bdae-border)] shadow-md">
        <div 
          className="w-12 h-12 rounded-2xl flex items-center justify-center text-white shadow-md shrink-0"
          style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
        >
          <Users className="w-6 h-6" />
        </div>
        <div>
          <p className="text-xs text-[var(--bdae-text-secondary)] font-semibold uppercase tracking-wider">
            Total Users
          </p>
          <p className="text-2xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
            {totalCount}
          </p>
        </div>
      </div>

      {/* Active Users */}
      <div className="bdae-card p-5 flex items-center space-x-4 border border-[var(--bdae-border)] shadow-md">
        <div className="w-12 h-12 rounded-2xl bg-emerald-500/10 text-emerald-600 border border-emerald-500/30 flex items-center justify-center shrink-0">
          <CheckCircle2 className="w-6 h-6" />
        </div>
        <div>
          <p className="text-xs text-[var(--bdae-text-secondary)] font-semibold uppercase tracking-wider">
            Active Accounts
          </p>
          <p className="text-2xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
            {activeCount}
          </p>
        </div>
      </div>

      {/* Pending Accounts */}
      <div className="bdae-card p-5 flex items-center space-x-4 border border-[var(--bdae-border)] shadow-md">
        <div className="w-12 h-12 rounded-2xl bg-amber-500/10 text-amber-600 border border-amber-500/30 flex items-center justify-center shrink-0">
          <Clock className="w-6 h-6" />
        </div>
        <div>
          <p className="text-xs text-[var(--bdae-text-secondary)] font-semibold uppercase tracking-wider">
            Pending Initial PIN
          </p>
          <p className="text-2xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
            {pendingCount}
          </p>
        </div>
      </div>

      {/* Super Admins */}
      <div className="bdae-card p-5 flex items-center space-x-4 border border-[var(--bdae-border)] shadow-md">
        <div className="w-12 h-12 rounded-2xl bg-cyan-500/10 text-cyan-600 border border-cyan-500/30 flex items-center justify-center shrink-0">
          <Crown className="w-6 h-6" />
        </div>
        <div>
          <p className="text-xs text-[var(--bdae-text-secondary)] font-semibold uppercase tracking-wider">
            Super Admins
          </p>
          <p className="text-2xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
            {superAdminCount}
          </p>
        </div>
      </div>
    </div>
  );
};
