import React from 'react';
import { useNavigate } from 'react-router-dom';
import { KeyRound, Building2, Shield, Phone, Landmark } from 'lucide-react';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { getUserDisplayName, formatRole, maskPhone } from '../../../common/utils/masking';

/**
 * Dashboard Welcome Banner with Professional Identity Presentation & Data Masking
 */
export const DashboardWelcomeHeader = ({ user, onOpenChangePin }) => {
  const navigate = useNavigate();
  const displayName = getUserDisplayName(user);
  const roleTitle = formatRole(user?.globalRole || user?.roles?.[0]);
  const maskedPhone = maskPhone(user?.msisdn);

  return (
    <div
      className="bdae-card p-6 md:p-8 text-white rounded-2xl shadow-xl flex flex-col md:flex-row md:items-center justify-between gap-6"
      style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
    >
      <div className="space-y-2">
        {/* Top Session & Branch Context Meta */}
        <div className="flex flex-wrap items-center gap-2">
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-200 border border-emerald-400/30 text-[11px] font-semibold">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
            Active Session
          </span>
          {user?.saccoId && (
            <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-white/10 text-white/90 border border-white/15 text-[11px] font-mono">
              <Landmark className="w-3 h-3 text-white/70" />
              SACCO ID: {user.saccoId}
            </span>
          )}
        </div>

        {/* Professional Employee Greeting */}
        <h1 className="text-2xl md:text-3xl font-bold tracking-tight">
          Welcome back, {displayName}!
        </h1>

        {/* Clearance Role & PII-Masked Contact Info */}
        <div className="flex flex-wrap items-center gap-3 text-xs opacity-95">
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-lg bg-black/20 backdrop-blur-sm border border-white/20 font-semibold shadow-inner">
            <Shield className="w-3.5 h-3.5 text-amber-300" />
            <span>{roleTitle}</span>
          </span>

          {user?.msisdn && (
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-lg bg-black/20 backdrop-blur-sm border border-white/20 font-mono text-[11px]">
              <Phone className="w-3 h-3 text-white/70" />
              <span>{maskedPhone}</span>
            </span>
          )}
        </div>
      </div>

      {/* Operator Quick Actions */}
      <div className="flex items-center gap-3 self-start md:self-auto flex-shrink-0">
        <button
          onClick={onOpenChangePin}
          className="px-3.5 py-2.5 bg-white/10 hover:bg-white/20 text-white rounded-xl text-xs font-bold flex items-center gap-2 transition-all border border-white/20 shadow-sm"
        >
          <KeyRound className="w-4 h-4" />
          <span>Rotate PIN</span>
        </button>

        <PermissionGuard role="SUPER_ADMIN">
          <button
            onClick={() => navigate('/saccos')}
            className="px-4 py-2.5 bg-white text-black hover:bg-white/90 rounded-xl text-xs font-bold flex items-center gap-2 transition-all shadow-md"
          >
            <Building2 className="w-4 h-4 text-[var(--bdae-primary)]" />
            <span>SACCO Registry</span>
          </button>
        </PermissionGuard>
      </div>
    </div>
  );
};
