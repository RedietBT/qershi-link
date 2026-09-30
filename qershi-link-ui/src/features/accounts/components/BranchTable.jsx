import React from 'react';
import {
  Building2,
  Vault,
  Edit2,
  CheckCircle2,
  AlertCircle,
  MapPin,
  Phone,
  DollarSign
} from 'lucide-react';
import { formatCurrency } from '../../../common/utils/currency';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { PERMISSIONS } from '../../../common/constants/permissions';
import { MaskedDataField } from '../../../common/components/MaskedDataField';

export const BranchTable = ({ branches = [], onEditBranch }) => {
  return (
    <div className="bdae-card rounded-2xl border border-[var(--bdae-border)] overflow-hidden shadow-sm">
      <div className="p-4 border-b border-[var(--bdae-border)] flex items-center justify-between">
        <h2 className="text-xs font-bold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
          Registered Branch Directory ({branches.length} Locations)
        </h2>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs border-collapse">
          <thead>
            <tr className="border-b border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 text-[var(--bdae-text-secondary)]">
              <th className="py-3 px-4 font-semibold">Code</th>
              <th className="py-3 px-4 font-semibold">Branch Name</th>
              <th className="py-3 px-4 font-semibold">Region / Address</th>
              <th className="py-3 px-4 font-semibold">Contact Phone</th>
              <th className="py-3 px-4 font-semibold">Vault GL</th>
              <th className="py-3 px-4 font-semibold text-right">Lending Limit</th>
              <th className="py-3 px-4 font-semibold text-center">Status</th>
              <th className="py-3 px-4 font-semibold text-center">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[var(--bdae-border)]">
            {branches.map((b) => (
              <tr
                key={b.branchId}
                className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
              >
                <td className="py-3.5 px-4 font-mono font-bold text-[var(--bdae-primary)]">
                  {b.branchCode}
                </td>
                <td className="py-3.5 px-4">
                  <div className="font-bold text-[var(--bdae-text-primary)] flex items-center gap-1.5">
                    <Building2 className="w-3.5 h-3.5 text-[var(--bdae-secondary)]" />
                    <span>{b.branchName}</span>
                    {b.isCentralVault && (
                      <span className="px-1.5 py-0.5 rounded text-[9px] font-black uppercase bg-amber-500/10 text-amber-600 border border-amber-500/20">
                        HQ Vault
                      </span>
                    )}
                  </div>
                </td>
                <td className="py-3.5 px-4 text-[var(--bdae-text-secondary)]">
                  <div className="flex items-center gap-1">
                    <MapPin className="w-3 h-3 text-[var(--bdae-text-secondary)]" />
                    <span>
                      {b.region ? `${b.region}, ` : ''}
                      {b.address || '—'}
                    </span>
                  </div>
                </td>
                <td className="py-3.5 px-4">
                  <div className="flex items-center gap-1.5">
                    <Phone className="w-3 h-3 text-[var(--bdae-text-secondary)]" />
                    <MaskedDataField
                      value={b.contactPhone}
                      maskType="phone"
                      allowReveal={true}
                    />
                  </div>
                </td>
                <td className="py-3.5 px-4 font-mono text-[var(--bdae-text-primary)]">
                  <div className="flex items-center gap-1">
                    <Vault className="w-3 h-3 text-[var(--bdae-secondary)]" />
                    <span>{b.vaultGlCode || '1011-0001'}</span>
                  </div>
                </td>
                <td className="py-3.5 px-4 text-right font-mono font-bold text-[var(--bdae-text-primary)]">
                  {formatCurrency(b.discretionaryLendingLimit)}
                </td>
                <td className="py-3.5 px-4 text-center">
                  <span
                    className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold ${
                      b.status === 'ACTIVE'
                        ? 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400'
                        : 'bg-red-500/10 text-red-600 dark:text-red-400'
                    }`}
                  >
                    {b.status === 'ACTIVE' ? (
                      <CheckCircle2 className="w-3 h-3" />
                    ) : (
                      <AlertCircle className="w-3 h-3" />
                    )}
                    {b.status}
                  </span>
                </td>
                <td className="py-3.5 px-4 text-center">
                  <PermissionGuard
                    permissions={[PERMISSIONS.BRANCH_MANAGE]}
                    fallback={
                      <span className="text-[10px] text-[var(--bdae-text-secondary)] opacity-50">
                        View Only
                      </span>
                    }
                  >
                    <button
                      type="button"
                      onClick={() => onEditBranch(b)}
                      className="px-2.5 py-1 rounded-lg bg-black/5 dark:bg-white/5 hover:bg-[var(--bdae-primary)] hover:text-white text-[var(--bdae-text-primary)] font-bold text-[11px] transition-all flex items-center gap-1 mx-auto"
                      title="Edit Branch Configuration"
                    >
                      <Edit2 className="w-3 h-3" />
                      <span>Configure</span>
                    </button>
                  </PermissionGuard>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
