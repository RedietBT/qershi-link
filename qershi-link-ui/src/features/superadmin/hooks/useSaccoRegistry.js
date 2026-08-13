import { useState, useEffect, useCallback } from 'react';
import { saccoApi } from '../api/saccoApi';

export const useSaccoRegistry = () => {
  const [saccos, setSaccos] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // Single SACCO inspection state
  const [selectedSacco, setSelectedSacco] = useState(null);
  const [isDetailLoading, setIsDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState(null);

  const fetchSaccos = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await saccoApi.getSaccos();
      setSaccos(Array.isArray(data) ? data : []);
      setIsLoading(false);
    } catch (err) {
      setIsLoading(false);
      const message = err.response?.data?.message || err.message || 'Failed to load SACCO registry.';
      setError(message);
    }
  }, []);

  const fetchSaccoDetails = async (saccoId) => {
    setIsDetailLoading(true);
    setDetailError(null);
    try {
      const data = await saccoApi.getSaccoById(saccoId);
      setSelectedSacco(data);
      setIsDetailLoading(false);
    } catch (err) {
      setIsDetailLoading(false);
      const message = err.response?.data?.message || err.message || 'Failed to load SACCO details.';
      setDetailError(message);
    }
  };

  const closeDetailModal = () => {
    setSelectedSacco(null);
    setDetailError(null);
  };

  useEffect(() => {
    fetchSaccos();
  }, [fetchSaccos]);

  return {
    saccos,
    isLoading,
    error,
    refreshSaccos: fetchSaccos,
    selectedSacco,
    isDetailLoading,
    detailError,
    fetchSaccoDetails,
    closeDetailModal
  };
};
