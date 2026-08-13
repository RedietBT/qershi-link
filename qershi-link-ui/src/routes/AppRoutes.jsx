import React from 'react';
import { Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import { LoginPage } from '../features/auth/pages/LoginPage';
import { SaccoOnboardingPage } from '../features/superadmin/pages/SaccoOnboardingPage';
import { SaccoRegistryPage } from '../features/superadmin/pages/SaccoRegistryPage';
import { AuditLogsPage } from '../features/audit/pages/AuditLogsPage';
import { UserManagementPage } from '../features/users/pages/UserManagementPage';
import { ProtectedRoute } from './ProtectedRoute';
import { PermissionRoute } from './PermissionRoute';
import { useAuthStore } from '../common/store/useAuthStore';
import { Layout } from '../common/components/Layout';
import { PermissionGuard } from '../common/components/PermissionGuard';
import { ShieldCheck, Building2, KeyRound, ShieldAlert, Users } from 'lucide-react';

/**
 * Dashboard View Component
 */
function DashboardPage() {
  const user = useAuthStore((state) => state.user);
  const navigate = useNavigate();

  return (
    <Layout>
      <div className="space-y-6 animate-fadeIn">
        {/* Welcome Header */}
        <div 
          className="bdae-card p-6 md:p-8 text-white rounded-2xl shadow-xl flex flex-col md:flex-row md:items-center justify-between gap-4"
          style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
        >
          <div className="space-y-1">
            <span className="px-2.5 py-0.5 rounded-full bg-white/20 text-xs font-semibold">Active Session</span>
            <h1 className="text-2xl font-bold">Welcome, {user?.msisdn || 'SACCO User'}!</h1>
            <p className="text-xs opacity-90 font-mono">
              User ID: {user?.userId || 'N/A'} | Role: <span className="font-bold underline">{user?.globalRole || user?.roles?.[0]}</span>
            </p>
          </div>

          <div className="flex items-center gap-3 self-start md:self-auto">
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

        {/* Super Admin Control Engine Matrix */}
        <PermissionGuard role="SUPER_ADMIN">
          <div className="bdae-card p-6 space-y-4 border border-[var(--bdae-border)] shadow-xl">
            <h2 className="text-sm font-bold border-b border-[var(--bdae-border)] pb-2 flex items-center gap-2">
              <ShieldCheck className="w-4 h-4 text-[var(--bdae-secondary)]" />
              <span>Platform Administrative Control Engines</span>
            </h2>

            <div className="grid grid-cols-1 md:grid-cols-4 gap-4 text-xs">
              {/* Card 1: SACCO Registry Management */}
              <div 
                onClick={() => navigate('/saccos')}
                className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-[var(--bdae-secondary)] cursor-pointer space-y-2 transition-all shadow-sm group"
              >
                <div className="w-9 h-9 rounded-xl bg-emerald-500/10 text-emerald-600 flex items-center justify-center font-bold">
                  <Building2 className="w-5 h-5" />
                </div>
                <div>
                  <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-[var(--bdae-secondary)] transition-colors">
                    SACCO Registry Management
                  </p>
                  <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                    Monitor and manage ecosystem tenant configurations (<code className="font-mono">GET /api/v1/saccos</code>).
                  </p>
                </div>
              </div>

              {/* Card 2: User Account Management */}
              <div 
                onClick={() => navigate('/users')}
                className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-[var(--bdae-secondary)] cursor-pointer space-y-2 transition-all shadow-sm group"
              >
                <div className="w-9 h-9 rounded-xl bg-blue-500/10 text-blue-600 flex items-center justify-center font-bold">
                  <Users className="w-5 h-5" />
                </div>
                <div>
                  <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-[var(--bdae-secondary)] transition-colors">
                    User Account Management
                  </p>
                  <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                    Track and perform CRUD options on identity records (<code className="font-mono">GET /api/v1/users</code>).
                  </p>
                </div>
              </div>

              {/* Card 3: PIN & Credential Operations */}
              <div 
                onClick={() => navigate('/saccos')}
                className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-[var(--bdae-secondary)] cursor-pointer space-y-2 transition-all shadow-sm group"
              >
                <div className="w-9 h-9 rounded-xl bg-amber-500/10 text-amber-600 flex items-center justify-center font-bold">
                  <KeyRound className="w-5 h-5" />
                </div>
                <div>
                  <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-[var(--bdae-secondary)] transition-colors">
                    PIN & Credential Operations
                  </p>
                  <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                    Initial PIN dispatches and SMS resend triggers (<code className="font-mono">POST /api/v1/pin/resend</code>).
                  </p>
                </div>
              </div>

              {/* Card 4: Platform Security Audit Engine */}
              <div 
                onClick={() => navigate('/audit-logs')}
                className="p-5 rounded-2xl bdae-surface border border-[var(--bdae-border)] hover:border-[var(--bdae-secondary)] cursor-pointer space-y-2 transition-all shadow-sm group"
              >
                <div className="w-9 h-9 rounded-xl bg-cyan-500/10 text-cyan-600 flex items-center justify-center font-bold">
                  <ShieldAlert className="w-5 h-5" />
                </div>
                <div>
                  <p className="font-bold text-sm text-[var(--bdae-text-primary)] group-hover:text-[var(--bdae-secondary)] transition-colors">
                    Platform Security Audit Engine
                  </p>
                  <p className="text-[11px] text-[var(--bdae-text-secondary)] mt-1">
                    Inspect system security, login events, and audit logs (<code className="font-mono">GET /api/v1/platform/audit-logs</code>).
                  </p>
                </div>
              </div>
            </div>
          </div>
        </PermissionGuard>
      </div>
    </Layout>
  );
}

/**
 * 404 Page Not Found Component
 */
function NotFoundPage() {
  return (
    <div className="min-h-screen flex items-center justify-center p-6 bg-[var(--bdae-bg)] text-[var(--bdae-text-primary)]">
      <div className="bdae-card p-8 max-w-md w-full text-center space-y-4 shadow-2xl border border-[var(--bdae-border)]">
        <h1 className="text-4xl font-extrabold text-[var(--bdae-secondary)]">404</h1>
        <h2 className="text-lg font-bold">Page Not Found</h2>
        <p className="text-xs text-[var(--bdae-text-secondary)]">
          The page route you are looking for does not exist on Qershi-Link Platform.
        </p>
        <a href="/dashboard" className="bdae-btn-primary block w-full py-2.5 text-xs font-bold rounded-xl">
          Return to Dashboard
        </a>
      </div>
    </div>
  );
}

/**
 * Central Application Routes
 */
export const AppRoutes = () => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  return (
    <Routes>
      {/* Public Route */}
      <Route 
        path="/login" 
        element={
          isAuthenticated ? <Navigate to="/dashboard" replace /> : <LoginPage />
        } 
      />

      {/* Protected Routes */}
      <Route element={<ProtectedRoute />}>
        <Route path="/dashboard" element={<DashboardPage />} />
        
        {/* Protected & Role-Guarded Route: SACCO Registry */}
        <Route 
          path="/saccos" 
          element={
            <PermissionRoute role="SUPER_ADMIN">
              <Layout>
                <SaccoRegistryPage />
              </Layout>
            </PermissionRoute>
          } 
        />

        {/* Protected & Role-Guarded Route: SACCO Onboarding */}
        <Route 
          path="/onboard" 
          element={
            <PermissionRoute role="SUPER_ADMIN">
              <Layout>
                <SaccoOnboardingPage />
              </Layout>
            </PermissionRoute>
          } 
        />

        {/* Protected & Role-Guarded Route: User Account Management */}
        <Route 
          path="/users" 
          element={
            <PermissionRoute role="SUPER_ADMIN">
              <Layout>
                <UserManagementPage />
              </Layout>
            </PermissionRoute>
          } 
        />

        {/* Protected & Role-Guarded Route: Platform Security Audit Logs */}
        <Route 
          path="/audit-logs" 
          element={
            <PermissionRoute role="SUPER_ADMIN">
              <Layout>
                <AuditLogsPage />
              </Layout>
            </PermissionRoute>
          } 
        />
      </Route>

      {/* Default Fallback Redirects */}
      <Route 
        path="/" 
        element={<Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />} 
      />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
};
