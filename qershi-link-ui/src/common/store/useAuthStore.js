import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

/**
 * Helper to safely parse claims from JWT token string if available.
 */
const parseJwtClaims = (token) => {
  if (!token || typeof token !== 'string' || !token.includes('.')) return {};
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (e) {
    return {};
  }
};

/**
 * Zustand Global Auth & Credential Store for Qershi-Link UI.
 * Unpacks nested userContext { globalRole, permissions, schemaName } returned by backend.
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
        const token = sessionData.accessToken || sessionData.token || null;
        const jwtClaims = parseJwtClaims(token);

        // Extract nested userContext returned by backend LoginResult record
        const ctx = sessionData.userContext || sessionData.user || {};

        const globalRole = ctx.globalRole || sessionData.globalRole || jwtClaims.globalRole || jwtClaims.role || null;
        const permissions = ctx.permissions || sessionData.permissions || jwtClaims.permissions || [];
        const rawRoles = ctx.roles || sessionData.roles || jwtClaims.roles || (globalRole ? [globalRole] : []);

        // Normalize roles list
        let roles = Array.isArray(rawRoles) ? [...rawRoles] : [rawRoles];
        if (globalRole) {
          if (!roles.includes(globalRole)) roles.push(globalRole);
          const prefixed = globalRole.startsWith('ROLE_') ? globalRole : `ROLE_${globalRole}`;
          if (!roles.includes(prefixed)) roles.push(prefixed);
        }

        const userId = ctx.userId || sessionData.userId || jwtClaims.userId || jwtClaims.sub;
        const msisdn = sessionData.msisdn || ctx.msisdn || jwtClaims.msisdn || jwtClaims.sub;
        const saccoId = ctx.saccoId || sessionData.saccoId || jwtClaims.saccoId || null;
        const tenantSchema = ctx.schemaName || sessionData.tenantSchema || jwtClaims.schemaName || jwtClaims.tenantSchema || 'master_schema';

        const userData = {
          userId,
          msisdn,
          globalRole: globalRole || (roles[0] || 'ROLE_USER'),
          roles,
          permissions: Array.isArray(permissions) ? permissions : [],
          saccoId
        };

        set({
          user: userData,
          token: token || null,
          tenantSchema,
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
        if (!user) return false;

        const targetRole = role.startsWith('ROLE_') ? role : `ROLE_${role}`;
        const rawRole = role.startsWith('ROLE_') ? role.replace('ROLE_', '') : role;

        return (
          user.roles?.includes(role) ||
          user.roles?.includes(targetRole) ||
          user.roles?.includes(rawRole) ||
          user.globalRole === role ||
          user.globalRole === targetRole ||
          user.globalRole === rawRole
        );
      },

      hasPermission: (permission) => {
        const { user } = get();
        if (!user) return false;

        // Super Admin override
        if (
          user.globalRole === 'SUPER_ADMIN' ||
          user.globalRole === 'ROLE_SUPER_ADMIN' ||
          user.roles?.includes('ROLE_SUPER_ADMIN') ||
          user.roles?.includes('SUPER_ADMIN')
        ) {
          return true;
        }

        return (
          user.permissions?.includes(permission) ||
          user.roles?.includes(permission)
        );
      },
    }),
    {
      name: 'qershi_auth_session',
      storage: createJSONStorage(() => sessionStorage),
    }
  )
);
