import React, { createContext, useContext, useState } from 'react';

const AuthContext = createContext(undefined);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('qershi_user_session');
    return savedUser ? JSON.parse(savedUser) : null;
  });

  const login = (sessionData) => {
    const { token, userId, msisdn, roles, saccoId, tenantSchema } = sessionData;
    const userData = { userId, msisdn, roles: roles || [], saccoId, tenantSchema };
    
    setUser(userData);
    if (token) localStorage.setItem('qershi_jwt_token', token);
    if (tenantSchema) localStorage.setItem('qershi_tenant_schema', tenantSchema);
    localStorage.setItem('qershi_user_session', JSON.stringify(userData));
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('qershi_jwt_token');
    localStorage.removeItem('qershi_tenant_schema');
    localStorage.removeItem('qershi_user_session');
  };

  const hasRole = (role) => {
    if (!user || !user.roles) return false;
    return user.roles.includes(role);
  };

  const hasPermission = (permission) => {
    if (!user || !user.roles) return false;
    return user.roles.includes(permission) || user.roles.includes('ROLE_SUPER_ADMIN');
  };

  return (
    <AuthContext.Provider value={{ 
      user, 
      login, 
      logout, 
      hasRole, 
      hasPermission, 
      isAuthenticated: !!user 
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within an AuthProvider');
  return context;
};
