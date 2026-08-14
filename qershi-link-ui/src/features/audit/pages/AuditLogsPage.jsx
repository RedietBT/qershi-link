import React, { useState } from 'react';
import { useAuditLogs } from '../hooks/useAuditLogs';
import { AuditLogStatsBar } from '../components/AuditLogStatsBar';
import { AuditLogFilterBar } from '../components/AuditLogFilterBar';
import { AuditLogTable } from '../components/AuditLogTable';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { ShieldAlert, RefreshCw, Lock, Globe, Building2, UserCircle, Key } from 'lucide-react';
import { ProfileGlobalAuditLogs } from '../components/ProfileGlobalAuditLogs';

export const AuditLogsPage = () => {
  const {
    logs,
    isLoading,
    error,
    scope,
    setScope,
    searchTerm,
    setSearchTerm,
    statusFilter,
    setStatusFilter,
    refreshLogs
  } = useAuditLogs('GLOBAL');

  const [activeTab, setActiveTab] = useState('auth');

  return (
    <PermissionGuard roles={['SUPER_ADMIN', 'SACCO_ADMIN']} fallback={
      <div className="p-8 text-center max-w-lg mx-auto space-y-4">
        <div className="w-12 h-12 rounded-full bg-amber-500/10 border border-amber-500/30 text-amber-600 mx-auto flex items-center justify-center">
          <Lock className="w-6 h-6" />
        </div>
        <h2 className="text-lg font-bold">Access Restricted</h2>
        <p className="text-xs text-[var(--bdae-text-secondary)]">
          Security Audit Engine requires Super Admin or SACCO Admin authorization.
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
              <ShieldAlert className="w-5 h-5" />
            </div>
            <div>
              <h1 className="text-xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
                Security Audit Engine
              </h1>
              <p className="text-xs text-[var(--bdae-text-secondary)]">
                Inspect system security, login events, and tenant audit logs.
              </p>
            </div>
          </div>

          <div className="flex items-center space-x-3">
            {/* Scope Toggle: SUPER_ADMIN Only */}
            <PermissionGuard role="SUPER_ADMIN" fallback={
              <span className="inline-flex items-center space-x-1.5 px-3 py-1.5 rounded-xl border border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 text-xs font-bold text-[var(--bdae-secondary)]">
                <Building2 className="w-3.5 h-3.5" />
                <span>Tenant Audit Scope</span>
              </span>
            }>
              <div className="grid grid-cols-2 gap-1 p-1 bg-black/5 dark:bg-white/5 rounded-xl border border-[var(--bdae-border)]">
                <button
                  onClick={() => setScope('GLOBAL')}
                  className={`px-3 py-1.5 rounded-lg text-xs font-bold flex items-center gap-1.5 transition-all ${scope === 'GLOBAL'
                      ? 'bg-[var(--bdae-primary)] text-white shadow-sm'
                      : 'text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]'
                    }`}
                >
                  <Globe className="w-3.5 h-3.5" />
                  <span>Global Logs</span>
                </button>

                <button
                  onClick={() => setScope('TENANT')}
                  className={`px-3 py-1.5 rounded-lg text-xs font-bold flex items-center gap-1.5 transition-all ${scope === 'TENANT'
                      ? 'bg-[var(--bdae-primary)] text-white shadow-sm'
                      : 'text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]'
                    }`}
                >
                  <Building2 className="w-3.5 h-3.5" />
                  <span>Tenant Scope</span>
                </button>
              </div>
            </PermissionGuard>

            {/* Refresh Button */}
            <button
              onClick={refreshLogs}
              disabled={isLoading}
              className="px-3.5 py-2 rounded-xl border border-[var(--bdae-border)] hover:border-[var(--bdae-secondary)] text-xs font-bold flex items-center gap-2 transition-all shadow-sm"
            >
              <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
              <span>Refresh</span>
            </button>
          </div>
        </div>

        {/* Audit Domain Tabs */}
        <div className="flex items-center gap-1 p-1 bg-black/5 dark:bg-white/5 rounded-xl border border-[var(--bdae-border)] w-fit mb-4">
          <button
            onClick={() => setActiveTab('auth')}
            className={`px-6 py-2 rounded-lg text-xs font-bold flex items-center gap-2 transition-all ${activeTab === 'auth'
                ? 'bg-[var(--bdae-primary)] text-white shadow-md'
                : 'text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)] hover:bg-black/5 dark:hover:bg-white/5'
              }`}
          >
            <Key className="w-4 h-4" /> System & Auth Audits
          </button>

          <button
            onClick={() => setActiveTab('profile')}
            className={`px-6 py-2 rounded-lg text-xs font-bold flex items-center gap-2 transition-all ${activeTab === 'profile'
                ? 'bg-[var(--bdae-primary)] text-white shadow-md'
                : 'text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)] hover:bg-black/5 dark:hover:bg-white/5'
              }`}
          >
            <UserCircle className="w-4 h-4" /> Profile Audits
          </button>
        </div>

        {/* Dynamic Tab Body */}
        {activeTab === 'auth' && (
          <div className="space-y-6 animate-fadeIn">
            {/* Stats Bar */}
            <AuditLogStatsBar logs={logs} />

            {/* Filter Bar */}
            <AuditLogFilterBar
              searchTerm={searchTerm}
              setSearchTerm={setSearchTerm}
              statusFilter={statusFilter}
              setStatusFilter={setStatusFilter}
            />

            {/* Audit Log Table */}
            <AuditLogTable
              logs={logs}
              isLoading={isLoading}
              error={error}
              onRefresh={refreshLogs}
            />
          </div>
        )}

        {activeTab === 'profile' && (
          <div className="animate-fadeIn">
            <PermissionGuard permissions={['AUDIT_LOG_VIEW']} fallback={
              <div className="text-center p-8 bg-red-500/10 border border-red-500/20 rounded-xl text-red-600 text-sm font-bold">
                You lack the AUDIT_LOG_VIEW permission required to view profile modification streams.
              </div>
            }>
              <ProfileGlobalAuditLogs />
            </PermissionGuard>
          </div>
        )}

      </div>
    </PermissionGuard>
  );
};
