import React, { useState, useEffect } from 'react';
import {
  CreditCard,
  Plus,
  RefreshCw,
  AlertCircle,
  CheckCircle,
  Snowflake,
  ShieldAlert,
  ShieldCheck,
  FolderOpen
} from 'lucide-react';
import { accountLedgerApi } from '../api/accountLedgerApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';
import { formatCurrency, formatDateTime } from '../../../common/utils/currency';
import { MaskedDataField } from '../../../common/components/MaskedDataField';
import { OpenAccountModal } from './OpenAccountModal';
import { FreezeAccountModal } from './FreezeAccountModal';
import { LienManagementModal } from './LienManagementModal';
import { ReactivateAccountModal } from './ReactivateAccountModal';

const STATUS_STYLES = {
  ACTIVE: 'bg-emerald-500/10 text-emerald-600 border-emerald-500/20',
  PENDING_APPROVAL: 'bg-amber-500/10 text-amber-600 border-amber-500/20',
  DORMANT: 'bg-rose-500/10 text-rose-600 border-rose-500/20',
  CLOSED: 'bg-red-500/10 text-red-500 border-red-500/20',
  FROZEN: 'bg-blue-500/10 text-blue-600 border-blue-500/20'
};

export const MemberAccountsTab = ({ userId }) => {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Modal States
  const [showOpenModal, setShowOpenModal] = useState(false);
  const [freezeModalAccount, setFreezeModalAccount] = useState(null);
  const [lienModalAccount, setLienModalAccount] = useState(null);
  const [reactivateModalAccount, setReactivateModalAccount] = useState(null);

  const loadAccounts = async () => {
    if (!userId) return;
    setLoading(true);
    setError(null);
    try {
      const res = await accountLedgerApi.getAccountsByUserId(userId);
      setAccounts(res.data || res || []);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load member accounts.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAccounts();
  }, [userId]);

  return (
    <div className="space-y-4">
      {/* Tab Header & Action Bar */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <CreditCard className="w-4 h-4 text-[var(--bdae-primary)]" />
          <span className="text-xs font-bold text-[var(--bdae-text-primary)]">
            Member Accounts ({accounts.length})
          </span>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={loadAccounts}
            disabled={loading}
            className="p-1.5 rounded-xl border border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] hover:bg-black/5"
            title="Reload accounts"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          </button>

          {/* Action button strictly guarded by ACCOUNT_OPEN */}
          <PermissionGuard permissions={[PERMISSIONS.ACCOUNT_OPEN]}>
            <button
              onClick={() => setShowOpenModal(true)}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold text-white shadow-sm hover:opacity-90 transition-all"
              style={{
                background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))`
              }}
            >
              <Plus className="w-3.5 h-3.5" />
              <span>Open Account</span>
            </button>
          </PermissionGuard>
        </div>
      </div>

      {error && (
        <div className="flex items-center gap-2 p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 text-xs font-bold">
          <AlertCircle className="w-4 h-4 shrink-0" /> {error}
        </div>
      )}

      {/* Accounts List */}
      {loading ? (
        <div className="py-12 text-center text-xs text-[var(--bdae-text-secondary)] flex flex-col items-center gap-2">
          <RefreshCw className="w-5 h-5 animate-spin text-[var(--bdae-primary)]" />
          <span>Retrieving accounts...</span>
        </div>
      ) : accounts.length === 0 ? (
        <div className="bdae-card border border-[var(--bdae-border)] rounded-2xl p-10 text-center space-y-2">
          <FolderOpen className="w-8 h-8 mx-auto text-[var(--bdae-text-secondary)]/40" />
          <p className="text-xs font-bold text-[var(--bdae-text-primary)]">
            No Active Core Accounts
          </p>
          <p className="text-[11px] text-[var(--bdae-text-secondary)]">
            Click "Open Account" above to initialize a savings, checking, or term deposit account.
          </p>
        </div>
      ) : (
        <div className="space-y-3">
          {accounts.map((acc) => {
            const isFrozen = acc.freezeStatus && acc.freezeStatus !== 'NONE';
            return (
              <div
                key={acc.accountId || acc.accountNumber}
                className="bdae-card border border-[var(--bdae-border)] rounded-2xl p-4 hover:border-[var(--bdae-secondary)] transition-all space-y-3"
              >
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                  <div className="flex items-center gap-2 flex-wrap">
                    <MaskedDataField
                      value={acc.accountNumber}
                      maskType="accountNumber"
                      allowReveal={true}
                    />
                    <span
                      className={`text-[9px] font-black uppercase px-2 py-0.5 rounded-full border ${
                        STATUS_STYLES[acc.status] ||
                        'bg-gray-500/10 text-gray-500 border-gray-500/20'
                      }`}
                    >
                      {acc.status}
                    </span>
                    {isFrozen && (
                      <span className="text-[9px] font-black uppercase px-2 py-0.5 rounded-full border bg-blue-500/10 text-blue-600 border-blue-500/20 flex items-center gap-1">
                        <Snowflake className="w-2.5 h-2.5" />
                        {acc.freezeStatus}
                      </span>
                    )}
                  </div>

                  <div className="flex items-center gap-2 self-end sm:self-auto">
                    {/* Freeze Action strictly guarded by ACCOUNT_FREEZE */}
                    <PermissionGuard permissions={[PERMISSIONS.ACCOUNT_FREEZE]}>
                      <button
                        type="button"
                        onClick={() => setFreezeModalAccount(acc)}
                        className={`flex items-center gap-1 px-2.5 py-1 rounded-lg text-[10px] font-bold border transition-all ${
                          isFrozen
                            ? 'border-blue-500/30 text-blue-600 bg-blue-500/10'
                            : 'border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] hover:bg-black/5'
                        }`}
                        title="Configure Freeze Status"
                      >
                        <Snowflake className="w-3 h-3" />
                        <span>{isFrozen ? 'Restrictions Active' : 'Freeze'}</span>
                      </button>
                    </PermissionGuard>

                    {/* Lien Holds strictly guarded by LIEN_VIEW or LIEN_CREATE */}
                    <PermissionGuard
                      permissions={[PERMISSIONS.LIEN_VIEW, PERMISSIONS.LIEN_CREATE]}
                    >
                      <button
                        type="button"
                        onClick={() => setLienModalAccount(acc)}
                        className="flex items-center gap-1 px-2.5 py-1 rounded-lg text-[10px] font-bold border border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] hover:bg-black/5 transition-all"
                        title="Inspect or Place Liens"
                      >
                        <ShieldAlert className="w-3 h-3 text-amber-500" />
                        <span>Liens</span>
                      </button>
                    </PermissionGuard>
                    {/* Dormancy KYC Reactivation */}
                    {acc.status === 'DORMANT' && (
                      <PermissionGuard
                        roles={['SUPER_ADMIN', 'ADMIN', 'SACCO_ADMIN', 'BRANCH_MANAGER', 'TELLER', 'CUSTOMER_SERVICE']}
                        permissions={[PERMISSIONS.ACCOUNT_OPEN, PERMISSIONS.ACCOUNT_APPROVE]}
                      >
                        <button
                          type="button"
                          onClick={() => setReactivateModalAccount(acc)}
                          className="flex items-center gap-1 px-2.5 py-1 rounded-lg text-[10px] font-bold border border-rose-500/30 text-rose-600 bg-rose-500/10 hover:bg-rose-500/20 transition-all"
                          title="Initiate or Approve KYC Reactivation"
                        >
                          <ShieldCheck className="w-3 h-3" />
                          <span>{acc.reactivationStatus === 'PENDING_CHECKER_APPROVAL' ? 'Review Reactivation' : 'Reactivate (KYC)'}</span>
                        </button>
                      </PermissionGuard>
                    )}
                  </div>
                </div>

                {/* Dormancy Alert Banner */}
                {acc.status === 'DORMANT' && (
                  <div className="flex items-center justify-between gap-2 p-2.5 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-700 dark:text-rose-300 text-[11px]">
                    <div className="flex items-center gap-2">
                      <ShieldAlert className="w-4 h-4 shrink-0 text-rose-600" />
                      <span>
                        <strong>Account DORMANT (&gt;180 Days Inactivity):</strong> Automated debits and OTC withdrawals are blocked to prevent insider fraud.
                      </span>
                    </div>
                    {acc.reactivationStatus === 'PENDING_CHECKER_APPROVAL' && (
                      <span className="text-[9px] font-bold px-2 py-0.5 rounded-md bg-amber-500/20 text-amber-700 dark:text-amber-300 shrink-0">
                        Pending Four-Eye Approval
                      </span>
                    )}
                  </div>
                )}

                {/* Balances Grid */}
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs pt-2 border-t border-[var(--bdae-border)]">
                  <div>
                    <div className="text-[10px] text-[var(--bdae-text-secondary)]">
                      Available Balance
                    </div>
                    <div className="font-mono text-sm font-black text-emerald-600 dark:text-emerald-400 mt-0.5">
                      {formatCurrency(acc.availableBalance)}
                    </div>
                  </div>

                  <div>
                    <div className="text-[10px] text-[var(--bdae-text-secondary)]">
                      Book Balance
                    </div>
                    <div className="font-mono text-sm font-black text-[var(--bdae-text-primary)] mt-0.5">
                      {formatCurrency(acc.bookBalance)}
                    </div>
                  </div>

                  <div>
                    <div className="text-[10px] text-[var(--bdae-text-secondary)]">
                      Lien Amount
                    </div>
                    <div className="font-mono text-xs font-bold text-amber-500 mt-0.5">
                      {formatCurrency(acc.lienAmount || 0)}
                    </div>
                  </div>

                  <div>
                    <div className="text-[10px] text-[var(--bdae-text-secondary)]">
                      Branch / Product
                    </div>
                    <div className="font-mono text-xs font-bold text-[var(--bdae-text-primary)] mt-0.5">
                      {acc.branchCode || '0001'} / #{acc.productCode || '101'}
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Modular Open Account Modal */}
      <OpenAccountModal
        isOpen={showOpenModal}
        userId={userId}
        onClose={() => setShowOpenModal(false)}
        onOpened={loadAccounts}
      />

      {/* Modular Freeze Account Modal */}
      <FreezeAccountModal
        isOpen={!!freezeModalAccount}
        account={freezeModalAccount}
        onClose={() => setFreezeModalAccount(null)}
        onUpdated={loadAccounts}
      />

      {/* Modular Lien Management Modal */}
      <LienManagementModal
        isOpen={!!lienModalAccount}
        account={lienModalAccount}
        onClose={() => setLienModalAccount(null)}
        onUpdated={loadAccounts}
      />

      {/* Modular KYC Reactivation Modal */}
      <ReactivateAccountModal
        isOpen={!!reactivateModalAccount}
        account={reactivateModalAccount}
        onClose={() => setReactivateModalAccount(null)}
        onUpdated={loadAccounts}
      />
    </div>
  );
};
