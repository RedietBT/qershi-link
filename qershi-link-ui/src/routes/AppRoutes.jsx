import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { LoginPage } from '../features/auth/pages/LoginPage';
import { SaccoOnboardingPage } from '../features/superadmin/pages/SaccoOnboardingPage';
import { SaccoRegistryPage } from '../features/superadmin/pages/SaccoRegistryPage';
import { ProtectedRoute } from './ProtectedRoute';
import { PermissionRoute } from './PermissionRoute';
import { useAuthStore } from '../common/store/useAuthStore';
import { Layout } from '../common/components/Layout';
import { PermissionGuard } from '../common/components/PermissionGuard';
import { ShieldCheck, CheckCircle2, UserCheck, Building2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

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
              User ID: {user?.userId} | Roles: {user?.roles?.join(', ') || 'ROLE_USER'}
            </p>
          </div>

          <div className="flex items-center gap-3 self-start md:self-auto">
            {/* Show SACCO Registry link ONLY if user holds ROLE_SUPER_ADMIN */}
            {user?.roles?.includes('ROLE_SUPER_ADMIN') && (
              <button
                onClick={() => navigate('/saccos')}
                className="px-4 py-2.5 bg-white text-black hover:bg-white/90 rounded-xl text-xs font-bold flex items-center gap-2 transition-all shadow-md"
              >
                <Building2 className="w-4 h-4 text-[var(--bdae-primary)]" />
                <span>SACCO Registry</span>
              </button>
            )}
          </div>
        </div>

        {/* Security & Permission Matrix - Rendered only for authorized roles */}
        <PermissionGuard role="ROLE_SUPER_ADMIN">
          <div className="bdae-card p-6 space-y-4 border border-[var(--bdae-border)] shadow-xl">
            <h2 className="text-sm font-bold border-b border-[var(--bdae-border)] pb-2 flex items-center gap-2">
              <ShieldCheck className="w-4 h-4 text-[var(--bdae-secondary)]" />
              <span>Super Admin Management Engine</span>
            </h2>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
              <div 
                onClick={() => navigate('/saccos')}
                className="p-4 rounded-xl bdae-surface border border-[var(--bdae-border)] hover:border-[var(--bdae-secondary)] cursor-pointer space-y-1 transition-all"
              >
                <p className="font-bold flex items-center gap-2 text-emerald-500">
                  <CheckCircle2 className="w-4 h-4" /> `/saccos` (SACCO Registry Management)
                </p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)]">
                  List and inspect ecosystem SACCO workspaces (`GET /api/v1/saccos`).
                </p>
              </div>

              <div 
                onClick={() => navigate('/onboard')}
                className="p-4 rounded-xl bdae-surface border border-[var(--bdae-border)] hover:border-[var(--bdae-secondary)] cursor-pointer space-y-1 transition-all"
              >
                <p className="font-bold flex items-center gap-2 text-cyan-500">
                  <UserCheck className="w-4 h-4" /> `/onboard` (SACCO Tenant Onboarding)
                </p>
                <p className="text-[11px] text-[var(--bdae-text-secondary)]">
                  Provision new isolated tenant workspace (`POST /api/v1/sacco/onboard`).
                </p>
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
            <PermissionRoute role="ROLE_SUPER_ADMIN">
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
            <PermissionRoute role="ROLE_SUPER_ADMIN">
              <Layout>
                <SaccoOnboardingPage />
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
