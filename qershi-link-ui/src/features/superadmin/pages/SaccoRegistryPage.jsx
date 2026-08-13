import React, { useState } from 'react';
import { useSaccoRegistry } from '../hooks/useSaccoRegistry';
import { SaccoStatsBar } from '../components/SaccoStatsBar';
import { SaccoTenantTable } from '../components/SaccoTenantTable';
import { SaccoDetailModal } from '../components/SaccoDetailModal';
import { PinResendModal } from '../components/PinResendModal';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { Building2, Plus, RefreshCw, Lock, KeyRound } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const SaccoRegistryPage = () => {
  const {
    saccos,
    isLoading,
    error,
    refreshSaccos,
    selectedSacco,
    isDetailLoading,
    detailError,
    fetchSaccoDetails,
    closeDetailModal
  } = useSaccoRegistry();

  const navigate = useNavigate();

  // Pin Resend Modal State
  const [isPinModalOpen, setIsPinModalOpen] = useState(false);
  const [pinModalMode, setPinModalMode] = useState('phone'); // 'phone' | 'userId'
  const [pinModalTarget, setPinModalTarget] = useState('');

  const handleOpenPinModalByPhone = () => {
    setPinModalMode('phone');
    setPinModalTarget('');
    setIsPinModalOpen(true);
  };

  const handleOpenPinModalByUserId = (userId) => {
    setPinModalMode('userId');
    setPinModalTarget(userId);
    setIsPinModalOpen(true);
  };

  return (
    <PermissionGuard role="ROLE_SUPER_ADMIN" fallback={
      <div className="p-8 text-center max-w-lg mx-auto space-y-4">
        <div className="w-12 h-12 rounded-full bg-amber-500/10 border border-amber-500/30 text-amber-600 mx-auto flex items-center justify-center">
          <Lock className="w-6 h-6" />
        </div>
        <h2 className="text-lg font-bold">Access Restricted</h2>
        <p className="text-xs text-[var(--bdae-text-secondary)]">
          SACCO Registry Management requires Super Admin authorization (<code className="font-mono bg-black/10 dark:bg-white/10 px-1 py-0.5 rounded">ROLE_SUPER_ADMIN</code>).
        </p>
      </div>
    }>
      <div className="space-y-6 animate-fadeIn max-w-7xl mx-auto">
        {/* Header */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-[var(--bdae-border)] pb-4">
          <div className="flex items-center space-x-3">
            <div 
              className="w-10 h-10 rounded-xl flex items-center justify-center text-white shadow-md shrink-0"
              style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
            >
              <Building2 className="w-5 h-5" />
            </div>
            <div>
              <h1 className="text-xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
                SACCO Registry Management
              </h1>
              <p className="text-xs text-[var(--bdae-text-secondary)]">
                Monitor and inspect ecosystem tenant configurations (<code className="font-mono">GET /api/v1/saccos</code>).
              </p>
            </div>
          </div>

          <div className="flex items-center space-x-2.5">
            {/* Refresh Button */}
            <button
              onClick={refreshSaccos}
              disabled={isLoading}
              className="px-3.5 py-2 rounded-xl border border-[var(--bdae-border)] hover:border-[var(--bdae-secondary)] text-xs font-bold flex items-center gap-2 transition-all shadow-sm"
            >
              <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
              <span>Refresh</span>
            </button>

            {/* Onboard New SACCO Button */}
            <button
              onClick={() => navigate('/onboard')}
              className="bdae-btn-primary px-4 py-2 text-xs font-bold rounded-xl flex items-center gap-2 shadow-md"
            >
              <Plus className="w-4 h-4" />
              <span>Onboard New SACCO</span>
            </button>

            {/* Red Dot Position: Resend PIN by Phone Button */}
            <button
              onClick={handleOpenPinModalByPhone}
              className="px-3.5 py-2 rounded-xl border border-red-500/40 bg-red-500/10 hover:bg-red-500/20 text-red-600 dark:text-red-400 text-xs font-bold flex items-center gap-2 transition-all shadow-md"
              title="Resend Initial PIN via SMS by Phone Number (POST /api/v1/pin/resend)"
            >
              <KeyRound className="w-4 h-4" />
              <span>Resend PIN</span>
            </button>
          </div>
        </div>

        {/* Stats Bar */}
        <SaccoStatsBar saccos={saccos} />

        {/* Tenant Table */}
        <SaccoTenantTable 
          saccos={saccos} 
          isLoading={isLoading} 
          error={error} 
          onInspect={fetchSaccoDetails}
          onResendPin={handleOpenPinModalByUserId}
          onRefresh={refreshSaccos}
        />

        {/* Single SACCO Detail Modal */}
        <SaccoDetailModal 
          sacco={selectedSacco} 
          isLoading={isDetailLoading} 
          error={detailError} 
          onClose={closeDetailModal} 
        />

        {/* PIN Operations Resend Modal */}
        {isPinModalOpen && (
          <PinResendModal
            initialMode={pinModalMode}
            initialTarget={pinModalTarget}
            onClose={() => setIsPinModalOpen(false)}
          />
        )}

      </div>
    </PermissionGuard>
  );
};
