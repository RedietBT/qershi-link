import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

/**
 * Zustand Global Auth & Credential Store for Qershi-Link UI.
 * Persists session state in sessionStorage (auto-clears on tab close).
 */
export const useAuthStore = create(
  persist(
    (set, get) => ({
      // --- STATE ---
      user: null,
      token: null,
      tenantSchema: null,
      isAuthenticated: false,

      // --- ACTIONS ---
      login: (sessionData) => {
        const { token, userId, msisdn, roles, saccoId, tenantSchema } = sessionData;
        const userData = { userId, msisdn, roles: roles || [], saccoId };

        set({
          user: userData,
          token: token || null,
          tenantSchema: tenantSchema || 'master_schema',
          isAuthenticated: true,
        });
      },

      logout: () => {
        set({
          user: null,
          token: null,
          tenantSchema: null,
          isAuthenticated: false,
        });
      },

      // --- SECURITY & PERMISSION EVALUATORS ---
      hasRole: (role) => {
        const { user } = get();
        if (!user || !user.roles) return false;
        return user.roles.includes(role);
      },

      hasPermission: (permission) => {
        const { user } = get();
        if (!user || !user.roles) return false;
        return user.roles.includes(permission) || user.roles.includes('ROLE_SUPER_ADMIN');
      },
    }),
    {
      name: 'qershi_auth_session',
      storage: createJSONStorage(() => sessionStorage), // Auto-cleared when browser tab closes
    }
  )
);
