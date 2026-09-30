import React from 'react';
import {
  ChevronRight,
  ChevronDown,
  Folder,
  FileText,
  Plus,
  Loader2,
  FolderTree
} from 'lucide-react';
import { formatCurrency } from '../../../common/utils/currency';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';

const TYPE_CONFIG = {
  ASSET: {
    label: 'Asset',
    bg: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20',
    indicator: 'text-emerald-500'
  },
  LIABILITY: {
    label: 'Liability',
    bg: 'bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/20',
    indicator: 'text-amber-500'
  },
  EQUITY: {
    label: 'Equity',
    bg: 'bg-purple-500/10 text-purple-600 dark:text-purple-400 border-purple-500/20',
    indicator: 'text-purple-500'
  },
  REVENUE: {
    label: 'Revenue',
    bg: 'bg-blue-500/10 text-blue-600 dark:text-blue-400 border-blue-500/20',
    indicator: 'text-blue-500'
  },
  EXPENSE: {
    label: 'Expense',
    bg: 'bg-rose-500/10 text-rose-600 dark:text-rose-400 border-rose-500/20',
    indicator: 'text-rose-500'
  }
};

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

  const isMatch =
    searchQuery &&
    (node.glCode.toLowerCase().includes(searchQuery.toLowerCase()) ||
      node.accountName.toLowerCase().includes(searchQuery.toLowerCase()));

  return (
    <>
      <tr
        className={`border-b border-[var(--bdae-border)] hover:bg-black/[0.02] dark:hover:bg-white/[0.02] transition-colors ${
          level === 0 ? 'bg-black/[0.015] dark:bg-white/[0.015] font-semibold' : ''
        } ${isMatch ? 'bg-amber-500/10' : ''}`}
      >
        <td className="py-2.5 px-4">
          <div
            className="flex items-center gap-1.5"
            style={{ paddingLeft: `${level * 22}px` }}
          >
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
            <span className="text-xs text-[var(--bdae-text-primary)] font-medium">
              — {node.accountName}
            </span>
          </div>
        </td>

        <td className="py-2.5 px-3">
          <span
            className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold border ${typeStyle.bg}`}
          >
            {typeStyle.label}
          </span>
        </td>

        <td className="py-2.5 px-3 text-center">
          <span className="font-mono text-[10px] font-bold px-1.5 py-0.5 rounded bg-black/5 dark:bg-white/5 text-[var(--bdae-text-secondary)]">
            {node.normalBalance}
          </span>
        </td>

        <td className="py-2.5 px-4 text-right font-mono text-xs font-bold text-[var(--bdae-text-primary)]">
          {formatCurrency(node.rollupBalance ?? node.currentBalance ?? 0)}
        </td>

        <td className="py-2.5 px-3 text-center">
          <PermissionGuard permissions={[PERMISSIONS.COA_MANAGE]}>
            <button
              type="button"
              onClick={() => onAddSubAccount(node)}
              className="p-1 rounded hover:bg-[var(--bdae-primary)]/10 text-[var(--bdae-primary)] hover:opacity-90 transition-colors"
              title={`Add Sub-Account under ${node.glCode}`}
            >
              <Plus className="w-3.5 h-3.5" />
            </button>
          </PermissionGuard>
        </td>
      </tr>

      {hasChildren &&
        isExpanded &&
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
        ))}
    </>
  );
};

export const CoaTreeTable = ({
  treeData = [],
  loading = false,
  expandedMap = {},
  toggleExpand,
  searchQuery = '',
  onAddSubAccount
}) => {
  return (
    <div className="bdae-card rounded-2xl border border-[var(--bdae-border)] overflow-hidden shadow-sm">
      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs border-collapse">
          <thead>
            <tr className="border-b border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 text-[var(--bdae-text-secondary)]">
              <th className="py-3 px-4 font-semibold">GL Code & Account Hierarchy</th>
              <th className="py-3 px-3 font-semibold">Category</th>
              <th className="py-3 px-3 font-semibold text-center">Normal</th>
              <th className="py-3 px-4 font-semibold text-right">Rolled-Up Balance (ETB)</th>
              <th className="py-3 px-3 font-semibold text-center">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[var(--bdae-border)]">
            {loading ? (
              <tr>
                <td colSpan="5" className="py-16 text-center text-xs text-[var(--bdae-text-secondary)]">
                  <Loader2 className="w-6 h-6 animate-spin mx-auto text-[var(--bdae-primary)] mb-2" />
                  <span>Loading hierarchical chart of accounts tree...</span>
                </td>
              </tr>
            ) : treeData.length === 0 ? (
              <tr>
                <td colSpan="5" className="py-16 text-center text-xs text-[var(--bdae-text-secondary)]">
                  <FolderTree className="w-8 h-8 mx-auto opacity-30 text-[var(--bdae-text-secondary)] mb-2" />
                  <p className="font-bold text-[var(--bdae-text-primary)]">No Chart of Accounts Seeded</p>
                  <p className="text-[11px] opacity-75">Click "New GL Account" above to initialize your general ledger.</p>
                </td>
              </tr>
            ) : (
              treeData.map((node) => (
                <CoaTreeNodeRow
                  key={node.accountId}
                  node={node}
                  level={0}
                  expandedMap={expandedMap}
                  toggleExpand={toggleExpand}
                  searchQuery={searchQuery}
                  onAddSubAccount={onAddSubAccount}
                />
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
