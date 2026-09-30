import React, { useState, useEffect } from 'react';
import {
  Building2,
  Plus,
  RefreshCw,
  AlertCircle
} from 'lucide-react';
import { branchApi } from '../api/branchApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';
import { BranchMetricsDeck } from '../components/BranchMetricsDeck';
import { BranchSearchFilterBar } from '../components/BranchSearchFilterBar';
import { BranchTable } from '../components/BranchTable';
import { BranchFormModal } from '../components/BranchFormModal';

export const BranchManagementPage = () => {
  const [branches, setBranches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');

  // Modal State
  const [modalOpen, setModalOpen] = useState(false);
  const [editingBranch, setEditingBranch] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState(null);
  const [formSuccess, setFormSuccess] = useState(null);

  // Form Fields
  const [formData, setFormData] = useState({
    branchCode: '',
    branchName: '',
    region: '',
    address: '',
    contactPhone: '',
    vaultGlCode: '',
    discretionaryLendingLimit: '100000',
    status: 'ACTIVE'
  });

  const loadBranches = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await branchApi.getAllBranches();
      setBranches(res.data || []);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load branch directory.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadBranches();
  }, []);

  const openCreateModal = () => {
    setEditingBranch(null);
    setFormData({
      branchCode: '',
      branchName: '',
      region: '',
      address: '',
      contactPhone: '',
      vaultGlCode: '',
      discretionaryLendingLimit: '100000',
      status: 'ACTIVE'
    });
    setFormError(null);
    setFormSuccess(null);
    setModalOpen(true);
  };

  const openEditModal = (branch) => {
    setEditingBranch(branch);
    setFormData({
      branchCode: branch.branchCode || '',
      branchName: branch.branchName || '',
      region: branch.region || '',
      address: branch.address || '',
      contactPhone: branch.contactPhone || '',
      vaultGlCode: branch.vaultGlCode || '',
      discretionaryLendingLimit: branch.discretionaryLendingLimit?.toString() || '100000',
      status: branch.status || 'ACTIVE'
    });
    setFormError(null);
    setFormSuccess(null);
    setModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setFormError(null);
    setFormSuccess(null);

    try {
      if (editingBranch) {
        await branchApi.updateBranch(editingBranch.branchId, {
          branchName: formData.branchName,
          region: formData.region,
          address: formData.address,
          contactPhone: formData.contactPhone,
          vaultGlCode: formData.vaultGlCode,
          discretionaryLendingLimit: parseFloat(formData.discretionaryLendingLimit),
          status: formData.status
        });
        setFormSuccess(`Branch [${formData.branchCode}] updated successfully.`);
      } else {
        await branchApi.createBranch({
          branchCode: formData.branchCode,
          branchName: formData.branchName,
          region: formData.region,
          address: formData.address,
          contactPhone: formData.contactPhone,
          vaultGlCode: formData.vaultGlCode,
          discretionaryLendingLimit: parseFloat(formData.discretionaryLendingLimit)
        });
        setFormSuccess(`Branch [${formData.branchCode}] onboarded successfully.`);
      }

      await loadBranches();
      setTimeout(() => {
        setModalOpen(false);
      }, 1200);
    } catch (err) {
      setFormError(err?.response?.data?.message || 'Operation failed. Please review inputs.');
    } finally {
      setSubmitting(false);
    }
  };

  const filteredBranches = branches.filter((b) => {
    const matchesSearch =
      b.branchName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      b.branchCode?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      b.region?.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesStatus = statusFilter === 'ALL' || b.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  return (
    <div className="space-y-6 animate-fadeIn pb-12">
      {/* ── Page Header ── */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-[var(--bdae-border)] pb-4">
        <div>
          <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
            <Building2 className="w-4 h-4 text-[var(--bdae-primary)]" />
            <span>Core Banking Infrastructure</span>
          </div>
          <h1 className="text-2xl font-black tracking-tight text-[var(--bdae-text-primary)]">
            Branch Network Administration
          </h1>
          <p className="text-xs text-[var(--bdae-text-secondary)]">
            Configure regional SACCO branches, assign dedicated cash vaults, and establish discretionary credit caps.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={loadBranches}
            disabled={loading}
            className="px-3 py-2 rounded-xl text-xs font-semibold border border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5 text-[var(--bdae-text-primary)] flex items-center gap-1.5 transition-all"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            <span>Refresh</span>
          </button>

          {/* Action button strictly guarded by BRANCH_MANAGE */}
          <PermissionGuard permissions={[PERMISSIONS.BRANCH_MANAGE]}>
            <button
              onClick={openCreateModal}
              className="px-4 py-2 rounded-xl text-xs font-bold bg-[var(--bdae-primary)] hover:opacity-90 text-white flex items-center gap-2 shadow-lg shadow-[var(--bdae-primary)]/20 transition-all"
            >
              <Plus className="w-4 h-4" />
              <span>Onboard New Branch</span>
            </button>
          </PermissionGuard>
        </div>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 text-xs flex items-center gap-2.5">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* ── 1. Modular Branch Metrics Deck ── */}
      <BranchMetricsDeck branches={branches} />

      {/* ── 2. Modular Filter & Search Bar ── */}
      <BranchSearchFilterBar
        searchQuery={searchQuery}
        setSearchQuery={setSearchQuery}
        statusFilter={statusFilter}
        setStatusFilter={setStatusFilter}
      />

      {/* ── 3. Modular Branch Table ── */}
      <BranchTable
        branches={filteredBranches}
        onEditBranch={openEditModal}
      />

      {/* ── 4. Modular Branch Form Modal ── */}
      <BranchFormModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        editingBranch={editingBranch}
        formData={formData}
        setFormData={setFormData}
        submitting={submitting}
        formError={formError}
        formSuccess={formSuccess}
        onSubmit={handleSubmit}
      />
    </div>
  );
};
