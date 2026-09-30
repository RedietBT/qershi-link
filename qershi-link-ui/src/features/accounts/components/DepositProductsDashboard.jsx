import React, { useState, useEffect } from 'react';
import {
  PackagePlus,
  RefreshCw,
  AlertCircle,
  TrendingUp,
  FolderOpen
} from 'lucide-react';
import { depositProductApi } from '../api/depositProductApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';
import { CreateProductModal } from './CreateProductModal';
import { DepositProductCard } from './DepositProductCard';

const CATEGORIES = ['SAVINGS', 'FIXED_DEPOSIT', 'CURRENT', 'SHARES', 'RECURRING'];

export const DepositProductsDashboard = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [showModal, setShowModal] = useState(false);

  const loadProducts = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await depositProductApi.getAllProducts();
      setProducts(res.data || res || []);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load deposit products catalog.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProducts();
  }, []);

  const filtered =
    selectedCategory === 'ALL'
      ? products
      : products.filter((p) => p.category === selectedCategory);

  return (
    <div className="space-y-6">
      {/* Action / Filter Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        {/* Category Pills */}
        <div className="flex flex-wrap gap-2 text-xs">
          <button
            onClick={() => setSelectedCategory('ALL')}
            className={`px-3 py-1.5 rounded-xl font-bold transition-all ${
              selectedCategory === 'ALL'
                ? 'bg-[var(--bdae-primary)] text-white shadow-sm'
                : 'bg-black/5 dark:bg-white/5 text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]'
            }`}
          >
            All Products ({products.length})
          </button>
          {CATEGORIES.map((cat) => {
            const count = products.filter((p) => p.category === cat).length;
            return (
              <button
                key={cat}
                onClick={() => setSelectedCategory(cat)}
                className={`px-3 py-1.5 rounded-xl font-bold transition-all ${
                  selectedCategory === cat
                    ? 'bg-[var(--bdae-primary)] text-white shadow-sm'
                    : 'bg-black/5 dark:bg-white/5 text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)]'
                }`}
              >
                {cat} ({count})
              </button>
            );
          })}
        </div>

        {/* CTA Buttons strictly guarded with PRODUCT_CREATE */}
        <div className="flex items-center gap-2 self-start sm:self-auto">
          <button
            onClick={loadProducts}
            disabled={loading}
            className="p-2.5 rounded-xl border border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)] hover:bg-black/5 transition-all"
            title="Reload Products"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>

          <PermissionGuard permissions={[PERMISSIONS.PRODUCT_CREATE]}>
            <button
              onClick={() => setShowModal(true)}
              className="px-4 py-2.5 rounded-xl text-xs font-bold text-white flex items-center gap-2 shadow-md transition-all hover:opacity-90"
              style={{
                background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))`
              }}
            >
              <PackagePlus className="w-4 h-4" />
              <span>New Deposit Product</span>
            </button>
          </PermissionGuard>
        </div>
      </div>

      {/* Error Alert */}
      {error && (
        <div className="flex items-center gap-3 p-4 rounded-2xl bg-red-500/10 border border-red-500/20 text-red-600 text-xs font-bold">
          <AlertCircle className="w-5 h-5 shrink-0" /> {error}
        </div>
      )}

      {/* Product Grid */}
      {loading ? (
        <div className="p-16 text-center text-xs text-[var(--bdae-text-secondary)] flex flex-col items-center gap-3">
          <RefreshCw className="w-6 h-6 animate-spin text-[var(--bdae-primary)]" />
          <span>Loading catalog...</span>
        </div>
      ) : filtered.length === 0 ? (
        <div className="bdae-card border border-[var(--bdae-border)] rounded-2xl p-16 text-center space-y-3">
          <FolderOpen className="w-10 h-10 mx-auto text-[var(--bdae-text-secondary)]/40" />
          <p className="text-sm font-bold text-[var(--bdae-text-primary)]">
            No Deposit Products Found
          </p>
          <p className="text-xs text-[var(--bdae-text-secondary)] max-w-sm mx-auto">
            {selectedCategory === 'ALL'
              ? 'Click "New Deposit Product" above to configure your first savings, term deposit, or share account product.'
              : `No products registered under category "${selectedCategory}".`}
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtered.map((prod) => (
            <DepositProductCard key={prod.productId || prod.productCode} product={prod} />
          ))}
        </div>
      )}

      {/* Modular Create Product Modal */}
      <CreateProductModal
        isOpen={showModal}
        onClose={() => setShowModal(false)}
        onCreated={loadProducts}
      />
    </div>
  );
};
