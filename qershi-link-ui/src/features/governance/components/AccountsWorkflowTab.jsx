import React, { useState, useEffect } from 'react';
import { CreditCard, Ban, Sliders, Edit3, ShieldCheck, RefreshCw, AlertCircle, PlusCircle, Trash2 } from 'lucide-react';
import { RoleClearanceSelector } from './RoleClearanceSelector';
import { EditProductRuleModal } from './EditProductRuleModal';
import { CreateProductRuleModal } from './CreateProductRuleModal';
import { makerCheckerApi } from '../api/makerCheckerApi';

export const AccountsWorkflowTab = ({ rules, onToggle, onStringChange }) => {
  const [productRules, setProductRules] = useState([]);
  const [isLoadingProducts, setIsLoadingProducts] = useState(true);
  const [editingProduct, setEditingProduct] = useState(null);
  const [isCreatingProduct, setIsCreatingProduct] = useState(false);
  const [productError, setProductError] = useState(null);

  const fetchProductRules = async () => {
    setIsLoadingProducts(true);
    setProductError(null);
    try {
      const data = await makerCheckerApi.getProductRules();
      setProductRules(data || []);
    } catch (err) {
      console.error('Failed to fetch product maker-checker rules:', err);
      setProductError('Failed to load product-specific risk rules.');
    } finally {
      setIsLoadingProducts(false);
    }
  };

  const handleDeleteProduct = async (productCode, productName) => {
    if (!window.confirm(`Are you sure you want to remove risk rules for "${productName}" (${productCode})?`)) {
      return;
    }
    try {
      await makerCheckerApi.deleteProductRule(productCode);
      fetchProductRules();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete product rule');
    }
  };

  useEffect(() => {
    fetchProductRules();
  }, []);

  return (
    <div className="space-y-6 animate-fadeIn">
      {/* Workflow Policy Switch Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* 1. Account Opening */}
        <div className="bdae-card p-6 border border-[var(--bdae-border)] rounded-2xl flex flex-col justify-between space-y-4">
          <div className="flex items-start justify-between">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 rounded-xl bg-blue-500/10 text-blue-600 flex items-center justify-center shrink-0">
                <CreditCard className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-xs font-bold text-[var(--bdae-text-primary)]">
                  Account Opening Dual-Control
                </h3>
                <span
                  className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                    rules.enableAccountOpeningChecker
                      ? 'bg-emerald-500/10 text-emerald-600 border border-emerald-500/20'
                      : 'bg-gray-500/10 text-gray-500 border border-gray-500/20'
                  }`}
                >
                  {rules.enableAccountOpeningChecker ? 'Four-Eyes Active' : 'Direct Activation'}
                </span>
              </div>
            </div>

            <button
              type="button"
              onClick={() => onToggle('enableAccountOpeningChecker')}
              className={`w-11 h-6 flex items-center rounded-full p-1 transition-colors shrink-0 ${
                rules.enableAccountOpeningChecker ? 'bg-[#00CDDB]' : 'bg-gray-300 dark:bg-gray-700'
              }`}
            >
              <div
                className={`bg-white w-4 h-4 rounded-full shadow-md transform transition-transform ${
                  rules.enableAccountOpeningChecker ? 'translate-x-5' : 'translate-x-0'
                }`}
              />
            </button>
          </div>
          <p className="text-[11px] text-[var(--bdae-text-secondary)] leading-relaxed">
            Holds newly opened deposit accounts in <code className="font-mono text-[10px]">PENDING_APPROVAL</code> until verified and authorized by a Branch Manager.
          </p>
        </div>

        {/* 2. Account Freeze & Liens */}
        <div className="bdae-card p-6 border border-[var(--bdae-border)] rounded-2xl flex flex-col justify-between space-y-4">
          <div className="flex items-start justify-between">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 rounded-xl bg-red-500/10 text-red-600 flex items-center justify-center shrink-0">
                <Ban className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-xs font-bold text-[var(--bdae-text-primary)]">
                  Account Freeze & Lien Holds
                </h3>
                <span
                  className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                    rules.enableAccountFreezeChecker
                      ? 'bg-emerald-500/10 text-emerald-600 border border-emerald-500/20'
                      : 'bg-gray-500/10 text-gray-500 border border-gray-500/20'
                  }`}
                >
                  {rules.enableAccountFreezeChecker ? 'Four-Eyes Active' : 'Direct Application'}
                </span>
              </div>
            </div>

            <button
              type="button"
              onClick={() => onToggle('enableAccountFreezeChecker')}
              className={`w-11 h-6 flex items-center rounded-full p-1 transition-colors shrink-0 ${
                rules.enableAccountFreezeChecker ? 'bg-[#00CDDB]' : 'bg-gray-300 dark:bg-gray-700'
              }`}
            >
              <div
                className={`bg-white w-4 h-4 rounded-full shadow-md transform transition-transform ${
                  rules.enableAccountFreezeChecker ? 'translate-x-5' : 'translate-x-0'
                }`}
              />
            </button>
          </div>
          <p className="text-[11px] text-[var(--bdae-text-secondary)] leading-relaxed">
            Requires dual administrative sign-off before executing debit freezes or locking member funds with collateral liens.
          </p>
        </div>
      </div>

      {/* Role Clearance Assignment Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <RoleClearanceSelector
          title="Eligible Account Creation Makers"
          subtitle="Roles permitted to open accounts and initiate freeze requests"
          type="maker"
          selectedRolesString={rules.accountMakerRoles}
          onChange={(val) => onStringChange('accountMakerRoles', val)}
        />

        <RoleClearanceSelector
          title="Eligible Account Authorization Checkers"
          subtitle="Roles permitted to authorize new accounts and approve balance holds"
          type="checker"
          selectedRolesString={rules.accountCheckerRoles}
          onChange={(val) => onStringChange('accountCheckerRoles', val)}
        />
      </div>

      {/* Per-Product Rules Matrix Table */}
      <div className="space-y-3 pt-4 border-t border-[var(--bdae-border)]">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="space-y-0.5">
            <h3 className="text-xs font-bold text-[var(--bdae-text-primary)] flex items-center gap-1.5 uppercase">
              <Sliders className="w-4 h-4 text-[#00CDDB]" />
              Account Product & Type Risk Limits Matrix
            </h3>
            <p className="text-[10px] text-[var(--bdae-text-secondary)]">
              Tailor maximum storing limits, single supervisor thresholds, and daily withdrawal caps per account product (e.g. Student, Women, General).
            </p>
          </div>
          <div className="flex items-center gap-2 self-start sm:self-auto">
            <button
              type="button"
              onClick={() => setIsCreatingProduct(true)}
              className="px-3 py-1.5 rounded-xl text-xs font-bold text-white bg-[#00CDDB] hover:bg-[#00b4c0] flex items-center gap-1.5 shadow-sm transition-all"
            >
              <PlusCircle className="w-3.5 h-3.5" />
              <span>Add Product / Type Rule</span>
            </button>
            <button
              type="button"
              onClick={fetchProductRules}
              className="px-3 py-1.5 rounded-xl border border-[var(--bdae-border)] hover:border-[#00CDDB] text-xs font-bold flex items-center gap-1.5 text-[var(--bdae-text-secondary)] hover:text-[#00CDDB] transition-all"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${isLoadingProducts ? 'animate-spin' : ''}`} />
              <span>Refresh</span>
            </button>
          </div>
        </div>

        {productError && (
          <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-600 text-xs font-bold flex items-center gap-2">
            <AlertCircle className="w-4 h-4" />
            <span>{productError}</span>
          </div>
        )}

        <div className="bdae-card border border-[var(--bdae-border)] shadow-xl overflow-hidden rounded-2xl">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="border-b border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 text-[10px] font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider">
                  <th className="py-3 px-4 font-mono">Code</th>
                  <th className="py-3 px-4">Account Type / Product</th>
                  <th className="py-3 px-4 text-right">Min Operating (ETB)</th>
                  <th className="py-3 px-4 text-right">Max Storing Limit (ETB)</th>
                  <th className="py-3 px-4 text-right">Supervisor Threshold (ETB)</th>
                  <th className="py-3 px-4 text-right">Daily Limit (ETB)</th>
                  <th className="py-3 px-4 text-center">Four-Eyes</th>
                  <th className="py-3 px-4 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[var(--bdae-border)]">
                {productRules.map((p) => (
                  <tr key={p.productCode} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors">
                    <td className="py-3 px-4 font-mono font-bold text-[var(--bdae-primary)]">
                      {p.productCode}
                    </td>
                    <td className="py-3 px-4">
                      <span className="font-bold text-[var(--bdae-text-primary)] block">{p.productName}</span>
                      <span className="inline-block px-1.5 py-0.2 rounded text-[9px] font-mono font-semibold bg-black/5 dark:bg-white/5 text-[var(--bdae-text-secondary)] border border-[var(--bdae-border)]">
                        {p.category || 'SAVINGS'}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right font-mono font-bold text-[var(--bdae-text-primary)]">
                      {(p.minOperatingBalance || 0).toLocaleString()}
                    </td>
                    <td className="py-3 px-4 text-right font-mono font-bold text-[#00CDDB]">
                      {(p.maxBalanceLimit || 0).toLocaleString()}
                    </td>
                    <td className="py-3 px-4 text-right font-mono font-bold text-amber-600 dark:text-amber-400">
                      {(p.singleWithdrawalLimit || 0).toLocaleString()}
                    </td>
                    <td className="py-3 px-4 text-right font-mono font-bold text-[var(--bdae-text-primary)]">
                      {(p.dailyWithdrawalLimit || 0).toLocaleString()}
                    </td>
                    <td className="py-3 px-4 text-center">
                      <span
                        className={`inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold ${
                          p.enableMakerChecker
                            ? 'bg-emerald-500/10 text-emerald-600 border border-emerald-500/20'
                            : 'bg-gray-500/10 text-gray-500 border border-gray-500/20'
                        }`}
                      >
                        {p.enableMakerChecker ? 'Active' : 'Bypassed'}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          onClick={() => setEditingProduct(p)}
                          className="px-2.5 py-1.5 rounded-xl border border-[var(--bdae-border)] hover:border-[#00CDDB] hover:bg-[#00CDDB]/10 text-[#00CDDB] text-xs font-bold inline-flex items-center gap-1 transition-all shadow-sm"
                          title="Configure Product Limits & Maker-Checker"
                        >
                          <Edit3 className="w-3.5 h-3.5" />
                          <span>Edit</span>
                        </button>
                        <button
                          onClick={() => handleDeleteProduct(p.productCode, p.productName)}
                          className="p-1.5 rounded-xl border border-[var(--bdae-border)] hover:border-red-500 hover:bg-red-500/10 text-gray-400 hover:text-red-500 text-xs font-bold inline-flex items-center transition-all shadow-sm"
                          title="Remove Product Risk Rule"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}

                {productRules.length === 0 && !isLoadingProducts && (
                  <tr>
                    <td colSpan="8" className="p-8 text-center text-xs text-[var(--bdae-text-secondary)]">
                      No account type rules configured yet. Click "Add Product / Type Rule" to define limits for Student, Women, or General accounts.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Edit Product Limits Modal */}
      {editingProduct && (
        <EditProductRuleModal
          productRule={editingProduct}
          onClose={() => setEditingProduct(null)}
          onSuccess={fetchProductRules}
        />
      )}

      {/* Create Product Rule Modal */}
      {isCreatingProduct && (
        <CreateProductRuleModal
          existingCodes={productRules.map((p) => p.productCode)}
          onClose={() => setIsCreatingProduct(false)}
          onSuccess={fetchProductRules}
        />
      )}
    </div>
  );
};

