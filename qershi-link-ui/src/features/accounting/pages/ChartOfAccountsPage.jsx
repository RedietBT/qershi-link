import React, { useState, useEffect } from 'react';
import {
  FolderTree,
  Plus,
  Search,
  RefreshCw,
  Layers,
  AlertCircle,
  CheckCircle2
} from 'lucide-react';
import { accountingApi } from '../api/accountingApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';
import { CoaPillarsDeck } from '../components/CoaPillarsDeck';
import { CoaTreeTable } from '../components/CoaTreeTable';
import { CoaFlatTable } from '../components/CoaFlatTable';
import { CreateGlAccountModal } from '../components/CreateGlAccountModal';

export const ChartOfAccountsPage = () => {
  const [treeData, setTreeData] = useState([]);
  const [flatAccounts, setFlatAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionSuccess, setActionSuccess] = useState(null);

  // Search & View Controls
  const [searchQuery, setSearchQuery] = useState('');
  const [viewMode, setViewMode] = useState('TREE'); // 'TREE' | 'FLAT'
  const [expandedMap, setExpandedMap] = useState({});

  // Modal State
  const [modalOpen, setModalOpen] = useState(false);
  const [parentAccount, setParentAccount] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [modalError, setModalError] = useState(null);

  const [formData, setFormData] = useState({
    glCode: '',
    accountName: '',
    accountType: 'ASSET',
    parentGlCode: null,
    normalBalance: 'DEBIT',
    isReconciliationAccount: false
  });

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [treeRes, flatRes] = await Promise.all([
        accountingApi.getCoaTree(),
        accountingApi.getCoaFlat()
      ]);
      setTreeData(treeRes || []);
      setFlatAccounts(flatRes || []);

      // Auto-expand root level accounts by default
      const initialMap = {};
      (treeRes || []).forEach((root) => {
        initialMap[root.accountId] = true;
      });
      setExpandedMap(initialMap);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load Chart of Accounts.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const toggleExpand = (accountId) => {
    setExpandedMap((prev) => ({
      ...prev,
      [accountId]: !prev[accountId]
    }));
  };

  const expandAll = () => {
    const newMap = {};
    const recurse = (nodes) => {
      nodes.forEach((n) => {
        newMap[n.accountId] = true;
        if (n.children && n.children.length > 0) recurse(n.children);
      });
    };
    recurse(treeData);
    setExpandedMap(newMap);
  };

  const collapseAll = () => {
    setExpandedMap({});
  };

  const openCreateModal = (parent = null) => {
    setParentAccount(parent);
    setFormData({
      glCode: '',
      accountName: '',
      accountType: parent ? parent.accountType : 'ASSET',
      parentGlCode: parent ? parent.glCode : null,
      normalBalance: parent ? parent.normalBalance : 'DEBIT',
      isReconciliationAccount: false
    });
    setModalError(null);
    setModalOpen(true);
  };

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setModalError(null);

    try {
      const payload = {
        glCode: formData.glCode.trim(),
        accountName: formData.accountName.trim(),
        accountType: formData.accountType,
        parentGlCode: formData.parentGlCode ? formData.parentGlCode.trim() : null,
        normalBalance: formData.normalBalance,
        isReconciliationAccount: formData.isReconciliationAccount
      };

      await accountingApi.createGlAccount(payload);
      setActionSuccess(`GL Account [${payload.glCode}] created successfully.`);
      setModalOpen(false);
      await loadData();
    } catch (err) {
      setModalError(err?.response?.data?.message || 'Failed to create GL account.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-6 animate-fadeIn pb-12">
      {/* ── Page Header ── */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-[var(--bdae-border)] pb-4">
        <div>
          <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
            <FolderTree className="w-4 h-4 text-[var(--bdae-primary)]" />
            <span>General Ledger & Financial Accounting</span>
          </div>
          <h1 className="text-2xl font-black tracking-tight text-[var(--bdae-text-primary)]">
            Chart of Accounts (COA) Hierarchy
          </h1>
          <p className="text-xs text-[var(--bdae-text-secondary)]">
            Hierarchical multi-tier General Ledger mapping statutory assets, liabilities, equity, revenues, and operating expenses.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={loadData}
            disabled={loading}
            className="px-3 py-2 rounded-xl text-xs font-semibold border border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5 text-[var(--bdae-text-primary)] flex items-center gap-1.5 transition-all"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            <span>Refresh</span>
          </button>

          {/* Action button strictly guarded by COA_MANAGE */}
          <PermissionGuard permissions={[PERMISSIONS.COA_MANAGE]}>
            <button
              onClick={() => openCreateModal(null)}
              className="px-4 py-2 rounded-xl text-xs font-bold bg-[var(--bdae-primary)] hover:opacity-90 text-white flex items-center gap-2 shadow-lg shadow-[var(--bdae-primary)]/20 transition-all"
            >
              <Plus className="w-4 h-4" />
              <span>New GL Account</span>
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

      {actionSuccess && (
        <div className="p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-600 dark:text-emerald-400 text-xs flex items-center gap-2.5 animate-fadeIn">
          <CheckCircle2 className="w-4 h-4 shrink-0" />
          <span>{actionSuccess}</span>
        </div>
      )}

      {/* ── 1. Modular 5 Pillars Metric Deck ── */}
      <CoaPillarsDeck treeData={treeData} />

      {/* ── 2. Search & View Mode Toolbar ── */}
      <div className="bdae-card p-4 rounded-2xl border border-[var(--bdae-border)] flex flex-col sm:flex-row items-center justify-between gap-4">
        <div className="relative w-full sm:w-80">
          <Search className="w-4 h-4 text-[var(--bdae-text-secondary)] absolute left-3 top-2.5" />
          <input
            type="text"
            placeholder="Search GL code or account title..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="bdae-input text-xs pl-9 w-full font-mono"
          />
        </div>

        <div className="flex items-center gap-2 self-start sm:self-auto">
          {viewMode === 'TREE' && (
            <>
              <button
                type="button"
                onClick={expandAll}
                className="px-3 py-1.5 rounded-lg text-xs font-semibold text-[var(--bdae-text-secondary)] hover:bg-black/5 dark:hover:bg-white/5 transition-all"
              >
                Expand All
              </button>
              <button
                type="button"
                onClick={collapseAll}
                className="px-3 py-1.5 rounded-lg text-xs font-semibold text-[var(--bdae-text-secondary)] hover:bg-black/5 dark:hover:bg-white/5 transition-all"
              >
                Collapse All
              </button>
            </>
          )}

          <div className="flex items-center p-1 rounded-xl bg-black/5 dark:bg-white/5 border border-[var(--bdae-border)] text-xs">
            <button
              onClick={() => setViewMode('TREE')}
              className={`px-3 py-1 rounded-lg font-bold transition-all ${
                viewMode === 'TREE'
                  ? 'bg-[var(--bdae-primary)] text-white shadow-sm'
                  : 'text-[var(--bdae-text-secondary)]'
              }`}
            >
              Hierarchical Tree
            </button>
            <button
              onClick={() => setViewMode('FLAT')}
              className={`px-3 py-1 rounded-lg font-bold transition-all ${
                viewMode === 'FLAT'
                  ? 'bg-[var(--bdae-primary)] text-white shadow-sm'
                  : 'text-[var(--bdae-text-secondary)]'
              }`}
            >
              Flat Table ({flatAccounts.length})
            </button>
          </div>
        </div>
      </div>

      {/* ── 3. Modular Table Display ── */}
      {viewMode === 'TREE' ? (
        <CoaTreeTable
          treeData={treeData}
          loading={loading}
          expandedMap={expandedMap}
          toggleExpand={toggleExpand}
          searchQuery={searchQuery}
          onAddSubAccount={openCreateModal}
        />
      ) : (
        <CoaFlatTable
          flatAccounts={flatAccounts}
          loading={loading}
          searchQuery={searchQuery}
        />
      )}

      {/* ── 4. Modular Create GL Account Modal ── */}
      <CreateGlAccountModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        parentAccount={parentAccount}
        formData={formData}
        setFormData={setFormData}
        flatAccounts={flatAccounts}
        isSubmitting={isSubmitting}
        error={modalError}
        onSubmit={handleCreateSubmit}
      />
    </div>
  );
};
