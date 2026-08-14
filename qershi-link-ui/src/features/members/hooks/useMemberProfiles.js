import { useState, useCallback, useEffect } from 'react';
import { memberProfileApi } from '../api/memberProfileApi';

export const useMemberProfiles = () => {
    const [profiles, setProfiles] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    const [statusFilter, setStatusFilter] = useState('');
    const [searchTerm, setSearchTerm] = useState('');

    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const [viewingProfile, setViewingProfile] = useState(null);

    const fetchProfiles = useCallback(async () => {
        setIsLoading(true);
        setError(null);
        try {
            const res = await memberProfileApi.getAllProfiles(statusFilter || null);
            if (res.success && res.data) {
                setProfiles(res.data);
            } else {
                setProfiles([]);
            }
        } catch (err) {
            console.error('Failed to fetch profiles:', err);
            setError(err.response?.data?.message || 'Failed to load member profiles.');
        } finally {
            setIsLoading(false);
        }
    }, [statusFilter]);

    useEffect(() => {
        fetchProfiles();
    }, [fetchProfiles]);

    const filteredProfiles = profiles.filter(p => {
        if (!searchTerm) return true;
        const lowerSearch = searchTerm.toLowerCase();
        return (
            p.memberNo?.toLowerCase().includes(lowerSearch) ||
            p.firstName?.toLowerCase().includes(lowerSearch) ||
            p.lastName?.toLowerCase().includes(lowerSearch)
        );
    });

    return {
        profiles: filteredProfiles,
        isLoading,
        error,
        statusFilter,
        setStatusFilter,
        searchTerm,
        setSearchTerm,
        refreshProfiles: fetchProfiles,
        isCreateOpen,
        setIsCreateOpen,
        viewingProfile,
        setViewingProfile
    };
};
