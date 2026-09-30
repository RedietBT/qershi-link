import React, { useState, useEffect, useMemo } from 'react';
import {
  FolderTree, Plus, Search, ChevronRight, ChevronDown, Folder, FileText,
  RefreshCw, Layers, ShieldCheck, AlertCircle, X, CheckCircle, TrendingUp,
  ArrowDownRight, ArrowUpRight, Scale
} from 'lucide-react';
import { accountingApi } from '../api/accountingApi';
import { formatCurrency } from '../../../common/utils/currency';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS, ROLES } from '../../../common/constants/permissions';

const TYPE_CONFIG = {
  ASSET: {
    label: 'Asset',
    bg: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20',
    indicator: 'text-emerald-500',
    normalBalance: 'DEBIT',
  },
  LIABILITY: {
    label: 'Liability',
    bg: 'bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/20',
    indicator: 'text-amber-500',
    normalBalance: 'CREDIT',
  },
  EQUITY: {
    label: 'Equity',
    bg: 'bg-purple-500/10 text-purple-600 dark:text-purple-400 border-purple-500/20',
    indicator: 'text-purple-500',
    normalBalance: 'CREDIT',
  },
  REVENUE: {
    label: 'Revenue',
    bg: 'bg-blue-500/10 text-blue-600 dark:text-blue-400 border-blue-500/20',
    indicator: 'text-blue-500',
    normalBalance: 'CREDIT',
  },
  EXPENSE: {
    label: 'Expense',
    bg: 'bg-rose-500/10 text-rose-600 dark:text-rose-400 border-rose-500/20',
    indicator: 'text-rose-500',
    normalBalance: 'DEBIT',
  },
};

/**
 * Recursive Tree Node Row for Chart of Accounts Tree Table
 */
const CoaTreeNodeRow = ({
  node,
  level = 0,
  expandedMap,
  toggleExpand,
  searchQuery,
  onAddSubAccount
}) => {
  const isExpanded = !!expandedMap[node.accountId];
  const hasChildren = node.children && node.children.length > 0;
  const typeStyle = TYPE_CONFIG[node.accountType] || TYPE_CONFIG.ASSET;

  // Search match highlight
  const isMatch = searchQuery && (
    node.glCode.toLowerCase().includes(searchQuery.toLowerCase()) ||
    node.accountName.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <>
      <tr
        className={`border-b border-[var(--bdae-border)] hover:bg-black/[0.02] dark:hover:bg-white/[0.02] transition-colors ${
          level === 0 ? 'bg-black/[0.015] dark:bg-white/[0.015] font-semibold' : ''
        } ${isMatch ? 'bg-amber-500/10' : ''}`}
      >
        {/* GL Code & Name with indentation */}
        <td className="py-2.5 px-4">
          <div className="flex items-center gap-1.5" style={{ paddingLeft: `${level * 22}px` }}>
            {hasChildren ? (
              <button
                type="button"
                onClick={() => toggleExpand(node.accountId)}
                className="w-5 h-5 flex items-center justify-center rounded hover:bg-black/5 dark:hover:bg-white/5 text-[var(--bdae-text-secondary)] focus:outline-none"
              >
                {isExpanded ? (
                  <ChevronDown className="w-3.5 h-3.5" />
                ) : (
                  <ChevronRight className="w-3.5 h-3.5" />
                )}
              </button>
            ) : (
              <span className="w-5 inline-block" />
            )}

            {hasChildren ? (
              <Folder className={`w-4 h-4 shrink-0 ${typeStyle.indicator}`} />
            ) : (
              <FileText className="w-3.5 h-3.5 shrink-0 text-[var(--bdae-text-secondary)]/60" />
            )}

            <span className="font-mono text-xs font-bold text-[var(--bdae-text-primary)]">
              {node.glCode}
            </span>
            <span className="text-xs text-[var(--bdae-text-primary)] font-medium truncate max-w-xs md:max-w-md">
              {node.accountName}
            </span>
          </div>
        </td>

        {/* Account Type Badge */}
        <td className="py-2.5 px-3">
          <span
            className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold border ${typeStyle.bg}`}
          >
            {node.accountType}
          </span>
        </td>

        {/* Balance Nature */}
        <td className="py-2.5 px-3">
          <span className="inline-flex items-center gap-1 font-mono text-[11px] text-[var(--bdae-text-secondary)]">
            {node.balanceType === 'DEBIT' ? (
              <ArrowDownRight className="w-3 h-3 text-emerald-500" />
            ) : (
              <ArrowUpRight className="w-3 h-3 text-blue-500" />
            )}
            {node.balanceType}
          </span>
        </td>

        {/* Posting Status */}
        <td className="py-2.5 px-3">
          {node.isPostingAllowed ? (
            <span className="inline-flex items-center gap-1 text-[11px] text-emerald-600 dark:text-emerald-400 font-medium">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
              Direct Posting
            </span>
          ) : (
            <span className="inline-flex items-center gap-1 text-[11px] text-[var(--bdae-text-secondary)]">
              <span className="w-1.5 h-1.5 rounded-full bg-gray-400" />
              Rollup Header
            </span>
          )}
        </td>

        {/* Balance (Formatted ETB) */}
        <td className="py-2.5 px-4 text-right">
          <span
            className={`font-mono text-xs ${
              level === 0 ? 'font-bold text-sm' : 'font-semibold'
            } text-[var(--bdae-text-primary)]`}
          >
            {formatCurrency(node.balance)}
          </span>
        </td>

        {/* Actions (Add Child) */}
        <td className="py-2.5 px-3 text-right">
          <PermissionGuard
            roles={[ROLES.SUPER_ADMIN, ROLES.ADMIN, ROLES.SACCO_ADMIN]}
            permissions={[PERMISSIONS.COA_MANAGE]}
          >
            {!node.isPostingAllowed && (
              <button
                type="button"
                onClick={() => onAddSubAccount(node)}
                title={`Add sub-account under ${node.glCode}`}
                className="p-1 rounded-lg text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-primary)] hover:bg-[var(--bdae-primary)]/10 transition-colors"
              >
                <Plus className="w-3.5 h-3.5" />
              </button>
            )}
          </PermissionGuard>
        </td>
      </tr>

      {/* Recursive Children rendering */}
      {hasChildren && isExpanded && (
        node.children.map((child) => (
          <CoaTreeNodeRow
            key={child.accountId}
            node={child}
            level={level + 1}
            expandedMap={expandedMap}
            toggleExpand={toggleExpand}
            searchQuery={searchQuery}
            onAddSubAccount={onAddSubAccount}
          />
        ))
      )}
    </>
  );
};

/**
 * Modal for Adding a New GL Account
 */
const AddGlAccountModal = ({ onClose, onSuccess, initialParent, flatAccounts }) => {
  const [formData, setFormData] = useState({
    glCode: '',
    accountName: '',
    accountType: initialParent ? initialParent.accountType : 'ASSET',
    parentId: initialParent ? initialParent.accountId : '',
    balanceType: initialParent ? initialParent.balanceType : 'DEBIT',
    isPostingAllowed: true,
    description: '',
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // If parent changes, sync account type & balance nature
  const handleParentChange = (e) => {
    const parentId = e.target.value;
    const parent = flatAccounts.find((a) => a.accountId === parentId);
    if (parent) {
      setFormData((prev) => ({
        ...prev,
        parentId,
        accountType: parent.accountType,
        balanceType: parent.balanceType,
      }));
    } else {
      setFormData((prev) => ({ ...prev, parentId: '' }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    // Validation
    const codePattern = /^[0-9]{4}(-[0-9]{2,4})?$/;
    if (!codePattern.test(formData.glCode.trim())) {
      setError('GL Code must follow format 1000 or 1000-01 (4 digits optionally followed by hyphen and 2-4 digits).');
      return;
    }
    if (!formData.accountName.trim()) {
      setError('Account name is required.');
      return;
    }

    setLoading(true);
    try {
      const payload = {
        glCode: formData.glCode.trim(),
        accountName: formData.accountName.trim(),
        accountType: formData.accountType,
        parentId: formData.parentId || null,
        balanceType: formData.balanceType,
        isPostingAllowed: formData.isPostingAllowed,
        description: formData.description.trim() || null,
      };

      await accountingApi.createGlAccount(payload);
      onSuccess();
      onClose();
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to create GL account.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bdae-card max-w-lg w-full p-6 space-y-4 shadow-2xl border border-[var(--bdae-border)] animate-fadeIn">
        <div className="flex items-center justify-between border-b border-[var(--bdae-border)] pb-3">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-[var(--bdae-primary)]/10 text-[var(--bdae-primary)] flex items-center justify-center font-bold">
              <Plus className="w-4 h-4" />
            </div>
            <div>
              <h2 className="text-base font-bold text-[var(--bdae-text-primary)]">
                Add General Ledger Account
              </h2>
              <p className="text-[11px] text-[var(--bdae-text-secondary)]">
                Define a new account in the institutional Chart of Accounts
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-1 rounded-lg text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)] hover:bg-black/5 dark:hover:bg-white/5"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {error && (
          <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 text-xs flex items-start gap-2">
            <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-3.5 text-xs">
          {/* Parent Account Selector */}
          <div>
            <label className="block font-semibold mb-1 text-[var(--bdae-text-primary)]">
              Parent Account / Header Category
            </label>
            <select
              value={formData.parentId}
              onChange={handleParentChange}
              className="w-full py-2 px-3 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)]"
            >
              <option value="">-- No Parent (Root Category) --</option>
              {flatAccounts
                .filter((a) => !a.isPostingAllowed)
                .map((a) => (
                  <option key={a.accountId} value={a.accountId}>
                    {a.glCode} - {a.accountName} ({a.accountType})
                  </option>
                ))}
            </select>
          </div>

          <div className="grid grid-cols-2 gap-3">
            {/* GL Code */}
            <div>
              <label className="block font-semibold mb-1 text-[var(--bdae-text-primary)]">
                GL Code <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                placeholder="e.g. 1010-02"
                value={formData.glCode}
                onChange={(e) => setFormData({ ...formData, glCode: e.target.value })}
                className="w-full py-2 px-3 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] font-mono text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)]"
                required
              />
            </div>

            {/* Account Type */}
            <div>
              <label className="block font-semibold mb-1 text-[var(--bdae-text-primary)]">
                Account Pillar <span className="text-red-500">*</span>
              </label>
              <select
                value={formData.accountType}
                onChange={(e) => setFormData({ ...formData, accountType: e.target.value })}
                disabled={!!formData.parentId}
                className="w-full py-2 px-3 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)] disabled:opacity-60"
              >
                <option value="ASSET">ASSET</option>
                <option value="LIABILITY">LIABILITY</option>
                <option value="EQUITY">EQUITY</option>
                <option value="REVENUE">REVENUE</option>
                <option value="EXPENSE">EXPENSE</option>
              </select>
            </div>
          </div>

          {/* Account Name */}
          <div>
            <label className="block font-semibold mb-1 text-[var(--bdae-text-primary)]">
              Account Name <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              placeholder="e.g. Branch Vault Cash - Hawassa"
              value={formData.accountName}
              onChange={(e) => setFormData({ ...formData, accountName: e.target.value })}
              className="w-full py-2 px-3 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)]"
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            {/* Balance Nature */}
            <div>
              <label className="block font-semibold mb-1 text-[var(--bdae-text-primary)]">
                Normal Balance
              </label>
              <select
                value={formData.balanceType}
                onChange={(e) => setFormData({ ...formData, balanceType: e.target.value })}
                className="w-full py-2 px-3 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)]"
              >
                <option value="DEBIT">DEBIT (+ increases balance)</option>
                <option value="CREDIT">CREDIT (+ increases balance)</option>
              </select>
            </div>

            {/* Posting Allowed Checkbox */}
            <div className="flex items-center pt-5">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={formData.isPostingAllowed}
                  onChange={(e) => setFormData({ ...formData, isPostingAllowed: e.target.checked })}
                  className="rounded border-[var(--bdae-border)] text-[var(--bdae-primary)] focus:ring-0"
                />
                <span className="font-semibold text-[var(--bdae-text-primary)]">
                  Allow Direct Posting
                </span>
              </label>
            </div>
          </div>

          {/* Description */}
          <div>
            <label className="block font-semibold mb-1 text-[var(--bdae-text-primary)]">
              Description / Audit Purpose
            </label>
            <textarea
              rows={2}
              placeholder="Optional notes or standard regulatory reference"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="w-full py-2 px-3 rounded-xl border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)] resize-none"
            />
          </div>

          <div className="flex justify-end gap-2.5 pt-3 border-t border-[var(--bdae-border)]">
            <button
              type="button"
              onClick={onClose}
              disabled={loading}
              className="px-4 py-2 rounded-xl border border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] hover:bg-black/5 dark:hover:bg-white/5 font-semibold"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="bdae-btn-primary px-5 py-2 rounded-xl text-white font-bold flex items-center gap-2 disabled:opacity-50"
            >
              {loading && <RefreshCw className="w-3.5 h-3.5 animate-spin" />}
              <span>Save GL Account</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

/**
 * Main ChartOfAccountsPage Component
 */
export const ChartOfAccountsPage = () => {
  const [treeData, setTreeData] = useState([]);
  const [flatData, setFlatData] = useState([]);
  const [expandedMap, setExpandedMap] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [selectedParent, setSelectedParent] = useState(null);

  // Load COA data from backend
  const fetchCoa = async () => {
    setLoading(true);
    setError(null);
    try {
      const [tree, flat] = await Promise.all([
        accountingApi.getCoaTree(),
        accountingApi.getCoaFlat(),
      ]);
      setTreeData(tree || []);
      setFlatData(flat || []);

      // Default expand level 0 root folders
      const initialExpanded = {};
      (tree || []).forEach((root) => {
        initialExpanded[root.accountId] = true;
      });
      setExpandedMap(initialExpanded);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load Chart of Accounts.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCoa();
  }, []);

  // Toggle single node expansion
  const toggleExpand = (accountId) => {
    setExpandedMap((prev) => ({
      ...prev,
      [accountId]: !prev[accountId],
    }));
  };

  // Expand all / Collapse all helpers
  const handleExpandAll = () => {
    const all = {};
    const recurse = (nodes) => {
      nodes.forEach((n) => {
        all[n.accountId] = true;
        if (n.children) recurse(n.children);
      });
    };
    recurse(treeData);
    setExpandedMap(all);
  };

  const handleCollapseAll = () => {
    setExpandedMap({});
  };

  // Auto-expand nodes when searching
  useEffect(() => {
    if (!searchQuery.trim()) return;
    const matchingExpanded = { ...expandedMap };
    const checkMatch = (nodes) => {
      let anyMatch = false;
      nodes.forEach((node) => {
        const matchesThis =
          node.glCode.toLowerCase().includes(searchQuery.toLowerCase()) ||
          node.accountName.toLowerCase().includes(searchQuery.toLowerCase());
        const childMatch = node.children && checkMatch(node.children);
        if (matchesThis || childMatch) {
          matchingExpanded[node.accountId] = true;
          anyMatch = true;
        }
      });
      return anyMatch;
    };
    checkMatch(treeData);
    setExpandedMap(matchingExpanded);
  }, [searchQuery]);

  // Aggregate Root Pillar Balances for Executive Metric Cards
  const summaryCards = useMemo(() => {
    const totals = { ASSET: 0, LIABILITY: 0, EQUITY: 0, REVENUE: 0, EXPENSE: 0 };
    treeData.forEach((root) => {
      if (totals[root.accountType] !== undefined) {
        totals[root.accountType] += root.balance || 0;
      }
    });
    return totals;
  }, [treeData]);

  return (
    <div className="space-y-6 animate-fadeIn pb-12">
      {/* Top Banner Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-[var(--bdae-border)] pb-4">
        <div className="flex items-center gap-3">
          <div
            className="w-10 h-10 rounded-xl flex items-center justify-center text-white shadow-md shrink-0"
            style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
          >
            <FolderTree className="w-5 h-5" />
          </div>
          <div>
            <h1 className="text-xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
              Dynamic Chart of Accounts (COA)
            </h1>
            <p className="text-xs text-[var(--bdae-text-secondary)]">
              Multi-tiered General Ledger hierarchy with real-time balance aggregation across the 5 pillars.
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2.5">
          <button
            type="button"
            onClick={fetchCoa}
            disabled={loading}
            title="Reload Chart of Accounts"
            className="p-2.5 rounded-xl border border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>

          {/* Add GL Account Button (Gated to Admin / Manage) */}
          <PermissionGuard
            roles={[ROLES.SUPER_ADMIN, ROLES.ADMIN, ROLES.SACCO_ADMIN]}
            permissions={[PERMISSIONS.COA_MANAGE]}
            fallback={
              <div className="px-3 py-1.5 rounded-xl border border-[var(--bdae-border)] text-[11px] text-[var(--bdae-text-secondary)] font-semibold flex items-center gap-1.5 bg-black/5 dark:bg-white/5">
                <ShieldCheck className="w-3.5 h-3.5 text-emerald-500" />
                <span>Auditor View-Only</span>
              </div>
            }
          >
            <button
              type="button"
              onClick={() => {
                setSelectedParent(null);
                setIsAddModalOpen(true);
              }}
              className="bdae-btn-primary px-4 py-2 rounded-xl text-white text-xs font-bold flex items-center gap-1.5 shadow-md"
            >
              <Plus className="w-4 h-4" />
              <span>Add GL Account</span>
            </button>
          </PermissionGuard>
        </div>
      </div>

      {/* 5-Pillar Executive Summary Metric Deck */}
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3.5">
        {/* Assets Card */}
        <div className="bdae-card p-4 rounded-2xl border border-emerald-500/20 shadow-sm space-y-1">
          <div className="flex items-center justify-between">
            <span className="text-[10px] font-bold uppercase tracking-wider text-emerald-600 dark:text-emerald-400">
              1000 · Assets
            </span>
            <span className="w-2 h-2 rounded-full bg-emerald-500" />
          </div>
          <p className="text-base font-extrabold text-[var(--bdae-text-primary)] font-mono">
            {formatCurrency(summaryCards.ASSET)}
          </p>
          <span className="text-[10px] text-[var(--bdae-text-secondary)]">Normal: Debit</span>
        </div>

        {/* Liabilities Card */}
        <div className="bdae-card p-4 rounded-2xl border border-amber-500/20 shadow-sm space-y-1">
          <div className="flex items-center justify-between">
            <span className="text-[10px] font-bold uppercase tracking-wider text-amber-600 dark:text-amber-400">
              2000 · Liabilities
            </span>
            <span className="w-2 h-2 rounded-full bg-amber-500" />
          </div>
          <p className="text-base font-extrabold text-[var(--bdae-text-primary)] font-mono">
            {formatCurrency(summaryCards.LIABILITY)}
          </p>
          <span className="text-[10px] text-[var(--bdae-text-secondary)]">Normal: Credit</span>
        </div>

        {/* Equity Card */}
        <div className="bdae-card p-4 rounded-2xl border border-purple-500/20 shadow-sm space-y-1">
          <div className="flex items-center justify-between">
            <span className="text-[10px] font-bold uppercase tracking-wider text-purple-600 dark:text-purple-400">
              3000 · Equity
            </span>
            <span className="w-2 h-2 rounded-full bg-purple-500" />
          </div>
          <p className="text-base font-extrabold text-[var(--bdae-text-primary)] font-mono">
            {formatCurrency(summaryCards.EQUITY)}
          </p>
          <span className="text-[10px] text-[var(--bdae-text-secondary)]">Normal: Credit</span>
        </div>

        {/* Revenue Card */}
        <div className="bdae-card p-4 rounded-2xl border border-blue-500/20 shadow-sm space-y-1">
          <div className="flex items-center justify-between">
            <span className="text-[10px] font-bold uppercase tracking-wider text-blue-600 dark:text-blue-400">
              4000 · Revenue
            </span>
            <span className="w-2 h-2 rounded-full bg-blue-500" />
          </div>
          <p className="text-base font-extrabold text-[var(--bdae-text-primary)] font-mono">
            {formatCurrency(summaryCards.REVENUE)}
          </p>
          <span className="text-[10px] text-[var(--bdae-text-secondary)]">Normal: Credit</span>
        </div>

        {/* Expenses Card */}
        <div className="bdae-card p-4 rounded-2xl border border-rose-500/20 shadow-sm space-y-1 col-span-2 sm:col-span-1">
          <div className="flex items-center justify-between">
            <span className="text-[10px] font-bold uppercase tracking-wider text-rose-600 dark:text-rose-400">
              5000 · Expenses
            </span>
            <span className="w-2 h-2 rounded-full bg-rose-500" />
          </div>
          <p className="text-base font-extrabold text-[var(--bdae-text-primary)] font-mono">
            {formatCurrency(summaryCards.EXPENSE)}
          </p>
          <span className="text-[10px] text-[var(--bdae-text-secondary)]">Normal: Debit</span>
        </div>
      </div>

      {/* Controls Bar: Search & Expand Helpers */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-3 bdae-card p-3 rounded-2xl border border-[var(--bdae-border)]">
        <div className="relative w-full sm:w-96">
          <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-[var(--bdae-text-secondary)]" />
          <input
            type="text"
            placeholder="Filter accounts by GL code or name..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-9 pr-4 py-2 rounded-xl text-xs border border-[var(--bdae-border)] bg-[var(--bdae-surface)] text-[var(--bdae-text-primary)] focus:outline-none focus:border-[var(--bdae-primary)]"
          />
          {searchQuery && (
            <button
              type="button"
              onClick={() => setSearchQuery('')}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          )}
        </div>

        <div className="flex items-center gap-2 self-end sm:self-auto">
          <button
            type="button"
            onClick={handleExpandAll}
            className="px-3 py-1.5 rounded-xl border border-[var(--bdae-border)] text-[11px] font-semibold text-[var(--bdae-text-secondary)] hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
          >
            Expand All
          </button>
          <button
            type="button"
            onClick={handleCollapseAll}
            className="px-3 py-1.5 rounded-xl border border-[var(--bdae-border)] text-[11px] font-semibold text-[var(--bdae-text-secondary)] hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
          >
            Collapse All
          </button>
        </div>
      </div>

      {/* Main Interactive Tree Table */}
      <div className="bdae-card rounded-2xl border border-[var(--bdae-border)] overflow-hidden shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-black/5 dark:bg-white/5 border-b border-[var(--bdae-border)] font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider text-[10px]">
              <tr>
                <th className="py-3 px-4">Account Code & Description</th>
                <th className="py-3 px-3">Pillar</th>
                <th className="py-3 px-3">Nature</th>
                <th className="py-3 px-3">Posting</th>
                <th className="py-3 px-4 text-right">Aggregated Balance</th>
                <th className="py-3 px-3 text-right">Action</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-[var(--bdae-text-secondary)]">
                    <RefreshCw className="w-6 h-6 animate-spin mx-auto mb-2 text-[var(--bdae-primary)]" />
                    <span>Loading General Ledger Tree...</span>
                  </td>
                </tr>
              ) : error ? (
                <tr>
                  <td colSpan={6} className="py-8 text-center text-red-500">
                    <AlertCircle className="w-6 h-6 mx-auto mb-2" />
                    <span>{error}</span>
                  </td>
                </tr>
              ) : treeData.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-8 text-center text-[var(--bdae-text-secondary)]">
                    No General Ledger accounts found.
                  </td>
                </tr>
              ) : (
                treeData.map((rootNode) => (
                  <CoaTreeNodeRow
                    key={rootNode.accountId}
                    node={rootNode}
                    level={0}
                    expandedMap={expandedMap}
                    toggleExpand={toggleExpand}
                    searchQuery={searchQuery}
                    onAddSubAccount={(node) => {
                      setSelectedParent(node);
                      setIsAddModalOpen(true);
                    }}
                  />
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Add GL Account Modal */}
      {isAddModalOpen && (
        <AddGlAccountModal
          onClose={() => {
            setIsAddModalOpen(false);
            setSelectedParent(null);
          }}
          onSuccess={fetchCoa}
          initialParent={selectedParent}
          flatAccounts={flatData}
        />
      )}
    </div>
  );
};

export default ChartOfAccountsPage;
