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
 * Extracts globalRole, permissions, and roles from login API response or JWT token claims.
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
        const token = sessionData.token || sessionData.accessToken || null;
        const jwtClaims = parseJwtClaims(token);

        // Merge response payload properties with JWT payload claims
        const globalRole = sessionData.globalRole || jwtClaims.globalRole || jwtClaims.role || null;
        const permissions = sessionData.permissions || jwtClaims.permissions || [];
        const rawRoles = sessionData.roles || jwtClaims.roles || (globalRole ? [globalRole] : []);
        
        // Normalize roles list (supporting both SUPER_ADMIN and ROLE_SUPER_ADMIN formats)
        let roles = Array.isArray(rawRoles) ? [...rawRoles] : [rawRoles];
        if (globalRole) {
          if (!roles.includes(globalRole)) roles.push(globalRole);
          const prefixed = globalRole.startsWith('ROLE_') ? globalRole : `ROLE_${globalRole}`;
          if (!roles.includes(prefixed)) roles.push(prefixed);
        }

        const userId = sessionData.userId || jwtClaims.userId || jwtClaims.sub;
        const msisdn = sessionData.msisdn || jwtClaims.msisdn || jwtClaims.sub;
        const saccoId = sessionData.saccoId || jwtClaims.saccoId || null;
        const tenantSchema = sessionData.tenantSchema || jwtClaims.tenantSchema || 'master_schema';

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
      storage: createJSONStorage(() => sessionStorage), // Auto-cleared when browser tab closes
    }
  )
);
