import { useState, useEffect, useCallback } from 'react';
import { kycApi } from '../api/kycApi';

export const useKycQueue = () => {
    const [identifications, setIdentifications] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);
    const [statusFilter, setStatusFilter] = useState('UNVERIFIED'); // Default to Queue view
    const [verifyingDoc, setVerifyingDoc] = useState(null);

    const fetchIdentifications = useCallback(async () => {
        setIsLoading(true);
        setError(null);
        try {
            const res = await kycApi.getAllIdentifications(statusFilter || null);
            setIdentifications(res.data || []);
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to fetch KYC queue.');
        } finally {
            setIsLoading(false);
        }
    }, [statusFilter]);

    useEffect(() => {
        fetchIdentifications();
    }, [fetchIdentifications]);

    return {
        identifications,
        isLoading,
        error,
        statusFilter,
        setStatusFilter,
        refreshQueue: fetchIdentifications,
        verifyingDoc,
        setVerifyingDoc
    };
};
