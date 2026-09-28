import React, { useState, useEffect } from 'react';
import {
  Building2, Plus, Search, RefreshCw, CheckCircle2,
  AlertCircle, Edit2, ShieldAlert, Phone, MapPin,
  Vault, DollarSign, X, Check, ArrowRight
} from 'lucide-react';
import { branchApi } from '../api/branchApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';
import { formatCurrency } from '../../../common/utils/currency';

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
          discretionaryLendingLimit: Number(formData.discretionaryLendingLimit) || 0,
          status: formData.status
        });
        setFormSuccess('Branch updated successfully!');
      } else {
        await branchApi.createBranch({
          branchCode: formData.branchCode,
          branchName: formData.branchName,
          region: formData.region,
          address: formData.address,
          contactPhone: formData.contactPhone,
          vaultGlCode: formData.vaultGlCode,
          discretionaryLendingLimit: Number(formData.discretionaryLendingLimit) || 0
        });
        setFormSuccess('New branch created successfully!');
      }

      await loadBranches();
      setTimeout(() => {
        setModalOpen(false);
      }, 1000);
    } catch (err) {
      setFormError(err?.response?.data?.message || 'Operation failed. Please review your inputs.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleStatus = async (branch) => {
    const newStatus = branch.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    try {
      await branchApi.updateBranchStatus(branch.branchId, newStatus);
      await loadBranches();
    } catch (err) {
      alert(err?.response?.data?.message || 'Failed to update branch status.');
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

  const totalBranches = branches.length;
  const activeBranches = branches.filter((b) => b.status === 'ACTIVE').length;
  const totalLendingLimit = branches.reduce(
    (sum, b) => sum + (Number(b.discretionaryLendingLimit) || 0),
    0
  );

  return (
    <PermissionGuard
      roles={['SACCO_ADMIN', 'ADMIN', 'BRANCH_MANAGER', 'TELLER', 'AUDITOR']}
      permissions={[PERMISSIONS.BRANCH_VIEW, PERMISSIONS.ACCOUNT_VIEW]}
      fallback={
        <div className="p-8 text-center max-w-lg mx-auto space-y-4 mt-10">
          <div className="w-14 h-14 rounded-2xl bg-red-500/10 border border-red-500/20 text-red-500 mx-auto flex items-center justify-center">
            <ShieldAlert className="w-7 h-7" />
          </div>
          <h2 className="text-lg font-bold">Access Restricted</h2>
          <p className="text-xs text-[var(--bdae-text-secondary)]">
            Branch Management requires SACCO Administrator or Branch Manager authorization.
          </p>
        </div>
      }
    >
      <div className="space-y-6 animate-fadeIn">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-[var(--bdae-border)] pb-5">
          <div className="flex items-center gap-3">
            <div
              className="w-11 h-11 rounded-2xl flex items-center justify-center text-white shadow-lg shrink-0"
              style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
            >
              <Building2 className="w-6 h-6" />
            </div>
            <div>
              <h1 className="text-xl font-black tracking-tight text-[var(--bdae-text-primary)]">
                Branch Network Management
              </h1>
              <p className="text-xs text-[var(--bdae-text-secondary)]">
                Administer SACCO branch directory, vault GL codes, and local lending limits.
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={loadBranches}
              className="p-2.5 rounded-xl border border-[var(--bdae-border)] hover:bg-[var(--bdae-border)]/20 text-[var(--bdae-text-secondary)] transition-all"
              title="Refresh branches"
            >
              <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            </button>
            <button
              onClick={openCreateModal}
              className="flex items-center gap-2 px-4 py-2.5 rounded-xl font-bold text-xs text-white shadow-md transition-all hover:opacity-95"
              style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
            >
              <Plus className="w-4 h-4" />
              Add Branch
            </button>
          </div>
        </div>

        {/* Metrics Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="bdae-card border border-[var(--bdae-border)] rounded-2xl p-5 flex items-center gap-4">
            <div className="w-12 h-12 rounded-xl bg-blue-500/10 text-blue-500 flex items-center justify-center shrink-0">
              <Building2 className="w-6 h-6" />
            </div>
            <div>
              <div className="text-[11px] font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider">
                Total Branches
              </div>
              <div className="text-2xl font-black text-[var(--bdae-text-primary)] mt-0.5">
                {totalBranches}
              </div>
              <div className="text-[10px] text-emerald-600 font-bold mt-0.5">
                {activeBranches} Operational
              </div>
            </div>
          </div>

          <div className="bdae-card border border-[var(--bdae-border)] rounded-2xl p-5 flex items-center gap-4">
            <div className="w-12 h-12 rounded-xl bg-emerald-500/10 text-emerald-500 flex items-center justify-center shrink-0">
              <Vault className="w-6 h-6" />
            </div>
            <div>
              <div className="text-[11px] font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider">
                Active Vaults
              </div>
              <div className="text-2xl font-black text-[var(--bdae-text-primary)] mt-0.5">
                {activeBranches}
              </div>
              <div className="text-[10px] text-[var(--bdae-text-secondary)] mt-0.5">
                Dedicated GL Ledger Vaults
              </div>
            </div>
          </div>

          <div className="bdae-card border border-[var(--bdae-border)] rounded-2xl p-5 flex items-center gap-4">
            <div className="w-12 h-12 rounded-xl bg-amber-500/10 text-amber-500 flex items-center justify-center shrink-0">
              <DollarSign className="w-6 h-6" />
            </div>
            <div>
              <div className="text-[11px] font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider">
                Total Discretionary Limit
              </div>
              <div className="text-2xl font-black text-[var(--bdae-text-primary)] mt-0.5">
                {formatCurrency(totalLendingLimit)}
              </div>
              <div className="text-[10px] text-[var(--bdae-text-secondary)] mt-0.5">
                Combined Branch Approval Power
              </div>
            </div>
          </div>
        </div>

        {/* Filter and Search Bar */}
        <div className="flex flex-col sm:flex-row items-center justify-between gap-3 bdae-card border border-[var(--bdae-border)] rounded-2xl p-4">
          <div className="relative w-full sm:w-80">
            <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-[var(--bdae-text-secondary)]" />
            <input
              type="text"
              placeholder="Search by code, name, or region..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-2 bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] rounded-xl text-xs text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)]"
            />
          </div>

          <div className="flex items-center gap-2 self-start sm:self-auto">
            {['ALL', 'ACTIVE', 'INACTIVE'].map((st) => (
              <button
                key={st}
                onClick={() => setStatusFilter(st)}
                className={`px-3 py-1.5 rounded-xl text-[10px] font-bold border transition-all ${
                  statusFilter === st
                    ? 'border-[var(--bdae-primary)] bg-[var(--bdae-primary)]/10 text-[var(--bdae-primary)]'
                    : 'border-[var(--bdae-border)] text-[var(--bdae-text-secondary)]'
                }`}
              >
                {st}
              </button>
            ))}
          </div>
        </div>

        {/* Error Notification */}
        {error && (
          <div className="p-4 rounded-xl border border-red-500/20 bg-red-500/10 text-red-500 text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            {error}
          </div>
        )}

        {/* Branch Cards Grid */}
        {loading ? (
          <div className="py-20 text-center">
            <RefreshCw className="w-8 h-8 animate-spin mx-auto text-[var(--bdae-primary)] mb-2" />
            <p className="text-xs text-[var(--bdae-text-secondary)]">Loading branch directory...</p>
          </div>
        ) : filteredBranches.length === 0 ? (
          <div className="bdae-card border border-[var(--bdae-border)] rounded-2xl p-12 text-center space-y-3">
            <Building2 className="w-10 h-10 text-[var(--bdae-text-secondary)] mx-auto opacity-40" />
            <h3 className="text-sm font-bold text-[var(--bdae-text-primary)]">No branches found</h3>
            <p className="text-xs text-[var(--bdae-text-secondary)] max-w-sm mx-auto">
              No branches match your current search criteria. Click "Add Branch" to onboard a new location.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {filteredBranches.map((b) => (
              <div
                key={b.branchId}
                className="bdae-card border border-[var(--bdae-border)] hover:border-[var(--bdae-primary)]/40 rounded-2xl p-5 space-y-4 transition-all shadow-sm flex flex-col justify-between"
              >
                <div className="space-y-3">
                  <div className="flex items-start justify-between gap-2">
                    <div className="flex items-center gap-2">
                      <span className="px-2.5 py-1 rounded-lg bg-[var(--bdae-primary)]/10 text-[var(--bdae-primary)] font-mono font-black text-xs border border-[var(--bdae-primary)]/20">
                        {b.branchCode}
                      </span>
                      <h3 className="font-extrabold text-sm text-[var(--bdae-text-primary)] leading-tight">
                        {b.branchName}
                      </h3>
                    </div>
                    <span
                      className={`px-2 py-0.5 rounded-full text-[9px] font-black border uppercase shrink-0 ${
                        b.status === 'ACTIVE'
                          ? 'bg-emerald-500/10 text-emerald-600 border-emerald-500/20'
                          : 'bg-gray-500/10 text-gray-500 border-gray-500/20'
                      }`}
                    >
                      {b.status}
                    </span>
                  </div>

                  <div className="space-y-1.5 text-xs text-[var(--bdae-text-secondary)] pt-1 border-t border-[var(--bdae-border)]/50">
                    <div className="flex items-center gap-2">
                      <MapPin className="w-3.5 h-3.5 shrink-0 text-[var(--bdae-secondary)]" />
                      <span>{b.region || 'Region Unspecified'}{b.address ? ` • ${b.address}` : ''}</span>
                    </div>
                    {b.contactPhone && (
                      <div className="flex items-center gap-2">
                        <Phone className="w-3.5 h-3.5 shrink-0 text-[var(--bdae-secondary)]" />
                        <span>{b.contactPhone}</span>
                      </div>
                    )}
                    <div className="flex items-center gap-2">
                      <Vault className="w-3.5 h-3.5 shrink-0 text-amber-500" />
                      <span className="font-mono text-[11px]">Vault GL: <b>{b.vaultGlCode || `1010-${b.branchCode}`}</b></span>
                    </div>
                  </div>
                </div>

                <div className="pt-3 border-t border-[var(--bdae-border)] flex items-center justify-between text-xs">
                  <div>
                    <div className="text-[10px] text-[var(--bdae-text-secondary)] font-bold">Lending Limit</div>
                    <div className="font-black text-[var(--bdae-text-primary)]">
                      {formatCurrency(b.discretionaryLendingLimit || 0)}
                    </div>
                  </div>

                  <div className="flex items-center gap-1.5">
                    <button
                      onClick={() => handleToggleStatus(b)}
                      className={`p-1.5 rounded-lg border text-[10px] font-bold transition-all ${
                        b.status === 'ACTIVE'
                          ? 'border-amber-500/20 text-amber-600 hover:bg-amber-500/10'
                          : 'border-emerald-500/20 text-emerald-600 hover:bg-emerald-500/10'
                      }`}
                      title={b.status === 'ACTIVE' ? 'Deactivate Branch' : 'Activate Branch'}
                    >
                      {b.status === 'ACTIVE' ? 'Disable' : 'Enable'}
                    </button>
                    <button
                      onClick={() => openEditModal(b)}
                      className="p-1.5 rounded-lg border border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5 text-[var(--bdae-text-secondary)] transition-all"
                      title="Edit Branch"
                    >
                      <Edit2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Create / Edit Modal */}
        {modalOpen && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fadeIn">
            <div className="bdae-card border border-[var(--bdae-border)] rounded-2xl w-full max-w-lg p-6 space-y-5 bg-[var(--bdae-bg-surface,#18181b)] shadow-2xl">
              <div className="flex items-center justify-between border-b border-[var(--bdae-border)] pb-3">
                <div className="flex items-center gap-2">
                  <Building2 className="w-5 h-5 text-[var(--bdae-primary)]" />
                  <h3 className="font-black text-sm text-[var(--bdae-text-primary)]">
                    {editingBranch ? `Edit Branch: ${editingBranch.branchCode}` : 'Create New SACCO Branch'}
                  </h3>
                </div>
                <button
                  onClick={() => setModalOpen(false)}
                  className="p-1.5 rounded-lg text-[var(--bdae-text-secondary)] hover:bg-black/10 dark:hover:bg-white/10"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              {formError && (
                <div className="p-3 rounded-xl border border-red-500/20 bg-red-500/10 text-red-500 text-xs flex items-center gap-2">
                  <AlertCircle className="w-4 h-4 shrink-0" />
                  {formError}
                </div>
              )}

              {formSuccess && (
                <div className="p-3 rounded-xl border border-emerald-500/20 bg-emerald-500/10 text-emerald-500 text-xs flex items-center gap-2">
                  <CheckCircle2 className="w-4 h-4 shrink-0" />
                  {formSuccess}
                </div>
              )}

              <form onSubmit={handleSubmit} className="space-y-4 text-xs">
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block font-bold text-[var(--bdae-text-secondary)] mb-1">
                      Branch Code *
                    </label>
                    <input
                      type="text"
                      required
                      disabled={!!editingBranch}
                      placeholder="e.g. 002"
                      value={formData.branchCode}
                      onChange={(e) => setFormData({ ...formData, branchCode: e.target.value })}
                      className="w-full px-3 py-2 bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] rounded-xl font-mono text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)] disabled:opacity-50"
                    />
                  </div>

                  <div>
                    <label className="block font-bold text-[var(--bdae-text-secondary)] mb-1">
                      Region / State
                    </label>
                    <input
                      type="text"
                      placeholder="e.g. Addis Ababa"
                      value={formData.region}
                      onChange={(e) => setFormData({ ...formData, region: e.target.value })}
                      className="w-full px-3 py-2 bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] rounded-xl text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)]"
                    />
                  </div>
                </div>

                <div>
                  <label className="block font-bold text-[var(--bdae-text-secondary)] mb-1">
                    Branch Name *
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Bole Medhanealem Branch"
                    value={formData.branchName}
                    onChange={(e) => setFormData({ ...formData, branchName: e.target.value })}
                    className="w-full px-3 py-2 bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] rounded-xl text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)]"
                  />
                </div>

                <div>
                  <label className="block font-bold text-[var(--bdae-text-secondary)] mb-1">
                    Physical Address / Landmark
                  </label>
                  <input
                    type="text"
                    placeholder="e.g. Bole Subcity, Cameroon Street, Near Mall"
                    value={formData.address}
                    onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                    className="w-full px-3 py-2 bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] rounded-xl text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)]"
                  />
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block font-bold text-[var(--bdae-text-secondary)] mb-1">
                      Contact Phone
                    </label>
                    <input
                      type="text"
                      placeholder="e.g. +251 91 100 0000"
                      value={formData.contactPhone}
                      onChange={(e) => setFormData({ ...formData, contactPhone: e.target.value })}
                      className="w-full px-3 py-2 bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] rounded-xl text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)]"
                    />
                  </div>

                  <div>
                    <label className="block font-bold text-[var(--bdae-text-secondary)] mb-1">
                      Vault GL Code
                    </label>
                    <input
                      type="text"
                      placeholder="e.g. 1010-002"
                      value={formData.vaultGlCode}
                      onChange={(e) => setFormData({ ...formData, vaultGlCode: e.target.value })}
                      className="w-full px-3 py-2 bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] rounded-xl font-mono text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)]"
                    />
                  </div>
                </div>

                <div>
                  <label className="block font-bold text-[var(--bdae-text-secondary)] mb-1">
                    Discretionary Lending Limit (ETB)
                  </label>
                  <input
                    type="number"
                    min="0"
                    step="1000"
                    value={formData.discretionaryLendingLimit}
                    onChange={(e) => setFormData({ ...formData, discretionaryLendingLimit: e.target.value })}
                    className="w-full px-3 py-2 bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] rounded-xl text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)]"
                  />
                </div>

                {editingBranch && (
                  <div>
                    <label className="block font-bold text-[var(--bdae-text-secondary)] mb-1">
                      Operational Status
                    </label>
                    <select
                      value={formData.status}
                      onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                      className="w-full px-3 py-2 bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] rounded-xl text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)]"
                    >
                      <option value="ACTIVE">ACTIVE</option>
                      <option value="INACTIVE">INACTIVE</option>
                    </select>
                  </div>
                )}

                <div className="flex items-center justify-end gap-3 pt-4 border-t border-[var(--bdae-border)]">
                  <button
                    type="button"
                    onClick={() => setModalOpen(false)}
                    className="px-4 py-2 rounded-xl border border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] font-bold hover:bg-black/5 dark:hover:bg-white/5"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={submitting}
                    className="px-5 py-2 rounded-xl text-white font-bold shadow-md transition-all hover:opacity-95 disabled:opacity-50 flex items-center gap-2"
                    style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
                  >
                    {submitting && <RefreshCw className="w-3.5 h-3.5 animate-spin" />}
                    {editingBranch ? 'Save Changes' : 'Create Branch'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </PermissionGuard>
  );
};
