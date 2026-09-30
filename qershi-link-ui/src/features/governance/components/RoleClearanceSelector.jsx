import React from 'react';
import { ShieldCheck, UserCheck } from 'lucide-react';

const AVAILABLE_ROLES = [
  { id: 'TELLER', label: 'Teller' },
  { id: 'CUSTOMER_SERVICE', label: 'Customer Service' },
  { id: 'LOAN_OFFICER', label: 'Loan Officer' },
  { id: 'BRANCH_MANAGER', label: 'Branch Manager' },
  { id: 'ADMIN', label: 'Admin' },
  { id: 'SACCO_ADMIN', label: 'SACCO Admin' },
  { id: 'AUDITOR', label: 'Auditor' }
];

export const RoleClearanceSelector = ({
  title,
  subtitle,
  type = 'maker', // 'maker' or 'checker'
  selectedRolesString = '',
  onChange
}) => {
  const selectedRoles = selectedRolesString
    ? selectedRolesString.split(',').map((r) => r.trim()).filter(Boolean)
    : [];

  const toggleRole = (roleId) => {
    let updated;
    if (selectedRoles.includes(roleId)) {
      updated = selectedRoles.filter((r) => r !== roleId);
    } else {
      updated = [...selectedRoles, roleId];
    }
    onChange(updated.join(','));
  };

  const isMaker = type === 'maker';

  return (
    <div className="bdae-card p-4 border border-[var(--bdae-border)] rounded-2xl space-y-3">
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-2.5">
          <div
            className={`w-7 h-7 rounded-lg flex items-center justify-center ${
              isMaker
                ? 'bg-blue-500/10 text-blue-600 dark:text-blue-400'
                : 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400'
            }`}
          >
            {isMaker ? <UserCheck className="w-3.5 h-3.5" /> : <ShieldCheck className="w-3.5 h-3.5" />}
          </div>
          <div>
            <h4 className="text-xs font-bold text-[var(--bdae-text-primary)]">{title}</h4>
            <p className="text-[10px] text-[var(--bdae-text-secondary)]">{subtitle}</p>
          </div>
        </div>
        <span className="text-[10px] font-mono text-[var(--bdae-text-secondary)]">
          {selectedRoles.length} Selected
        </span>
      </div>

      <div className="flex flex-wrap gap-1.5 pt-1">
        {AVAILABLE_ROLES.map((role) => {
          const isSelected = selectedRoles.includes(role.id);
          return (
            <button
              key={role.id}
              type="button"
              onClick={() => toggleRole(role.id)}
              className={`px-2.5 py-1 rounded-lg text-[10px] font-bold transition-all border ${
                isSelected
                  ? isMaker
                    ? 'bg-blue-500/15 text-blue-600 dark:text-blue-400 border-blue-500/40 shadow-sm'
                    : 'bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border-emerald-500/40 shadow-sm'
                  : 'bg-black/5 dark:bg-white/5 text-[var(--bdae-text-secondary)] border-[var(--bdae-border)] hover:border-[#00CDDB]/40'
              }`}
            >
              {isSelected ? '✓ ' : '+ '}
              {role.label}
            </button>
          );
        })}
      </div>
    </div>
  );
};
