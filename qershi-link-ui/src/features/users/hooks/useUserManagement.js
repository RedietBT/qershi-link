import { useState, useEffect, useCallback } from 'react';
import { userApi } from '../api/userApi';

export const useUserManagement = (initialSaccoId = null) => {
  const [users, setUsers] = useState([]);
  const [saccoIdFilter, setSaccoIdFilter] = useState(initialSaccoId);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL'); // 'ALL' | 'ACTIVE' | 'PENDING' | 'LOCKED'

  // Modal Dialog States
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [editingUser, setEditingUser] = useState(null); // User object for PUT
  const [roleAssignUser, setRoleAssignUser] = useState(null); // User object for Role Assignment
  const [deletingUser, setDeletingUser] = useState(null); // User object for DELETE

  const fetchUsers = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await userApi.getUsers(saccoIdFilter);
      setUsers(Array.isArray(data) ? data : []);
      setIsLoading(false);
    } catch (err) {
      setIsLoading(false);
      const msg = err.response?.data?.message || err.message || 'Failed to fetch platform user accounts.';
      setError(msg);
    }
  }, [saccoIdFilter]);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  // Client-side filtering by search term & status
  const filteredUsers = users.filter((user) => {
    const status = (user.status || '').toUpperCase();
    const matchesStatus =
      statusFilter === 'ALL' ||
      (statusFilter === 'ACTIVE' && status === 'ACTIVE') ||
      (statusFilter === 'PENDING' && (status === 'PENDING' || status === 'PASSWORD_CHANGE_REQUIRED')) ||
      (statusFilter === 'LOCKED' && (status === 'LOCKED' || status === 'FROZEN'));

    const term = searchTerm.toLowerCase().trim();
    const matchesSearch =
      !term ||
      (user.msisdn || '').toLowerCase().includes(term) ||
      (user.globalRole || '').toLowerCase().includes(term) ||
      (user.userId || '').toLowerCase().includes(term) ||
      (user.saccoId || '').toLowerCase().includes(term);

    return matchesStatus && matchesSearch;
  });

  return {
    users: filteredUsers,
    rawUsersCount: users.length,
    isLoading,
    error,
    saccoIdFilter,
    setSaccoIdFilter,
    searchTerm,
    setSearchTerm,
    statusFilter,
    setStatusFilter,
    refreshUsers: fetchUsers,
    
    // Modal states & controls
    isCreateOpen,
    setIsCreateOpen,
    editingUser,
    setEditingUser,
    roleAssignUser,
    setRoleAssignUser,
    deletingUser,
    setDeletingUser
  };
};
