import { useState, useEffect, useCallback } from 'react';
import { auditApi } from '../api/auditApi';
import { useAuthStore } from '../../../common/store/useAuthStore';

export const useAuditLogs = (initialScope = 'GLOBAL') => {
  const user = useAuthStore((state) => state.user);
  
  const isSuperAdmin =
    user?.globalRole === 'SUPER_ADMIN' ||
    user?.globalRole === 'ROLE_SUPER_ADMIN' ||
    user?.roles?.includes('ROLE_SUPER_ADMIN') ||
    user?.roles?.includes('SUPER_ADMIN');

  // SACCO_ADMIN is restricted to TENANT scope by backend security policies
  const [scope, setScope] = useState(isSuperAdmin ? initialScope : 'TENANT');
  const [logs, setLogs] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const [page, setPage] = useState(0);
  const [size] = useState(50);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL'); // 'ALL' | 'SUCCESS' | 'FAILED'

  const activeScope = isSuperAdmin ? scope : 'TENANT';

  const fetchLogs = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      let data = [];
      if (activeScope === 'TENANT') {
        data = await auditApi.getTenantAuditLogs();
      } else {
        data = await auditApi.getGlobalAuditLogs(page, size);
      }
      setLogs(Array.isArray(data) ? data : []);
      setIsLoading(false);
    } catch (err) {
      setIsLoading(false);
      const msg = err.response?.data?.message || err.message || 'Failed to fetch platform security audit logs.';
      setError(msg);
    }
  }, [activeScope, page, size]);

  useEffect(() => {
    fetchLogs();
  }, [fetchLogs]);

  // Client-side filtering by search term & status
  const filteredLogs = logs.filter((log) => {
    const matchesStatus =
      statusFilter === 'ALL' || (log.status || '').toUpperCase() === statusFilter;

    const term = searchTerm.toLowerCase().trim();
    const matchesSearch =
      !term ||
      (log.action || '').toLowerCase().includes(term) ||
      (log.resourceAffected || '').toLowerCase().includes(term) ||
      (log.userId || '').toLowerCase().includes(term) ||
      (log.saccoId || '').toLowerCase().includes(term) ||
      (log.ipAddress || '').toLowerCase().includes(term) ||
      (log.details || '').toLowerCase().includes(term);

    return matchesStatus && matchesSearch;
  });

  return {
    logs: filteredLogs,
    rawLogsCount: logs.length,
    isLoading,
    error,
    scope: activeScope,
    setScope,
    page,
    setPage,
    searchTerm,
    setSearchTerm,
    statusFilter,
    setStatusFilter,
    refreshLogs: fetchLogs
  };
};
