import React from 'react';
import { useAuthStore } from '../store/useAuthStore';
import { Building2, LogOut, ShieldCheck, User } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const Navbar = () => {
  const user = useAuthStore((state) => state.user);
  const logout = useAuthStore((state) => state.logout);
  const navigate = useNavigate();

  return (
    <header className="h-16 bdae-surface border-b border-[var(--bdae-border)] px-6 flex items-center justify-between sticky top-0 z-30 transition-colors duration-300">
      {/* Brand & Tenant Indicator */}
      <div className="flex items-center space-x-3 cursor-pointer" onClick={() => navigate('/dashboard')}>
        <div 
          className="w-9 h-9 rounded-xl flex items-center justify-center text-white shadow-md"
          style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
        >
          <Building2 className="w-5 h-5" />
        </div>
        <div>
          <span className="font-extrabold text-sm tracking-tight text-[var(--bdae-text-primary)]">
            Qershi-Link Platform
          </span>
          <span className="text-[10px] block text-[var(--bdae-text-secondary)] font-mono">
            {user?.saccoId || 'Master Schema'}
          </span>
        </div>
      </div>

      {/* Right User Actions */}
      <div className="flex items-center space-x-4">
        {/* User Profile Badge */}
        <div className="flex items-center space-x-2.5 px-3 py-1.5 rounded-xl border border-[var(--bdae-border)] bg-black/5 dark:bg-white/5">
          <div className="w-7 h-7 rounded-full bg-[var(--bdae-primary)] text-white flex items-center justify-center text-xs font-bold">
            <User className="w-4 h-4" />
          </div>
          <div className="text-left">
            <p className="text-xs font-bold text-[var(--bdae-text-primary)] leading-none">{user?.msisdn || 'SACCO User'}</p>
            <p className="text-[10px] text-[var(--bdae-text-secondary)] font-mono">{user?.globalRole || user?.roles?.[0] || 'ROLE_USER'}</p>
          </div>
        </div>

        {/* Logout Button */}
        <button
          onClick={logout}
          className="p-2 text-[var(--bdae-text-secondary)] hover:text-red-500 hover:bg-red-500/10 rounded-xl transition-all"
          title="Sign Out"
        >
          <LogOut className="w-4 h-4" />
        </button>
      </div>
    </header>
  );
};
