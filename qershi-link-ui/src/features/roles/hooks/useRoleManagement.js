import { useState, useEffect, useCallback } from 'react';
import { roleApi } from '../api/roleApi';

export const useRoleManagement = () => {
  const [roles, setRoles] = useState([]);
  const [permissions, setPermissions] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const [searchTerm, setSearchTerm] = useState('');
  const [typeFilter, setTypeFilter] = useState('ALL'); // 'ALL' | 'SYSTEM' | 'CUSTOM'

  // Modal States
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [editingRole, setEditingRole] = useState(null); // Role object for PUT
  const [deletingRole, setDeletingRole] = useState(null); // Role object for DELETE

  const fetchRolesData = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [rolesData, permsData] = await Promise.all([
        roleApi.getRoles(),
        roleApi.getPermissions()
      ]);

      setRoles(Array.isArray(rolesData) ? rolesData : []);
      setPermissions(Array.isArray(permsData) ? permsData : []);
      setIsLoading(false);
    } catch (err) {
      setIsLoading(false);
      const msg = err.response?.data?.message || err.message || 'Failed to fetch RBAC role definitions.';
      setError(msg);
    }
  }, []);

  useEffect(() => {
    fetchRolesData();
  }, [fetchRolesData]);

  // Client-side filtering by search term and type
  const filteredRoles = roles.filter((role) => {
    const isSystem = Boolean(role.isSystemDefined || role.systemDefined);
    const matchesType =
      typeFilter === 'ALL' ||
      (typeFilter === 'SYSTEM' && isSystem) ||
      (typeFilter === 'CUSTOM' && !isSystem);

    const term = searchTerm.toLowerCase().trim();
    const matchesSearch =
      !term ||
      (role.roleName || '').toLowerCase().includes(term) ||
      (role.description || '').toLowerCase().includes(term);

    return matchesType && matchesSearch;
  });

  return {
    roles: filteredRoles,
    rawRoles: roles,
    permissions,
    isLoading,
    error,
    searchTerm,
    setSearchTerm,
    typeFilter,
    setTypeFilter,
    refreshRoles: fetchRolesData,

    // Modal states & controls
    isCreateOpen,
    setIsCreateOpen,
    editingRole,
    setEditingRole,
    deletingRole,
    setDeletingRole
  };
};
