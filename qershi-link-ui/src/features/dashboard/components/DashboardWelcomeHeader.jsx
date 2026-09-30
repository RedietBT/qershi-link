import React from 'react';
import { useNavigate } from 'react-router-dom';
import { KeyRound, Building2 } from 'lucide-react';
import { PermissionGuard } from '../../../common/components/PermissionGuard';

/**
 * Dashboard Welcome Banner with Quick Actions
 */
export const DashboardWelcomeHeader = ({ user, onOpenChangePin }) => {
  const navigate = useNavigate();

  return (
    <div
      className="bdae-card p-6 md:p-8 text-white rounded-2xl shadow-xl flex flex-col md:flex-row md:items-center justify-between gap-4"
      style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
    >
      <div className="space-y-1">
        <span className="px-2.5 py-0.5 rounded-full bg-white/20 text-xs font-semibold">Active Session</span>
        <h1 className="text-2xl font-bold">Welcome, {user?.msisdn || 'SACCO User'}!</h1>
        <p className="text-xs opacity-90 font-mono">
          Role: <span className="font-bold underline">{user?.globalRole || user?.roles?.[0]}</span>
        </p>
      </div>

      <div className="flex items-center gap-3 self-start md:self-auto">
        <button
          onClick={onOpenChangePin}
          className="px-3.5 py-2 bg-white/10 hover:bg-white/20 text-white rounded-xl text-xs font-bold flex items-center gap-2 transition-all border border-white/20 shadow-sm"
        >
          <KeyRound className="w-4 h-4" />
          <span>Rotate Initial PIN</span>
        </button>

        <PermissionGuard role="SUPER_ADMIN">
          <button
            onClick={() => navigate('/saccos')}
            className="px-4 py-2.5 bg-white text-black hover:bg-white/90 rounded-xl text-xs font-bold flex items-center gap-2 transition-all shadow-md"
          >
            <Building2 className="w-4 h-4 text-[var(--bdae-primary)]" />
            <span>SACCO Registry Management</span>
          </button>
        </PermissionGuard>
      </div>
    </div>
  );
};
