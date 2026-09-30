import React, { useState, useEffect } from 'react';
import {
  CreditCard,
  RefreshCw,
  Users,
  ChevronDown,
  UserCircle
} from 'lucide-react';
import { memberProfileApi } from '../../members/api/memberProfileApi';
import { MemberAccountsTab } from '../components/MemberAccountsTab';
import { AccountSearchPanel } from '../components/AccountSearchPanel';

// Member display name helper
const memberDisplayName = (profile) => {
  const parts = [profile.firstName, profile.middleName, profile.lastName].filter(Boolean);
  return parts.length > 0 ? parts.join(' ') : profile.address?.primaryPhone || 'Unknown Member';
};

const memberInitials = (profile) => {
  const name = memberDisplayName(profile);
  return name.split(' ').map((w) => w[0]).slice(0, 2).join('').toUpperCase();
};

export const AccountManagementPage = () => {
  const [members, setMembers] = useState([]);
  const [selectedMember, setSelectedMember] = useState(null);
  const [loadingMembers, setLoadingMembers] = useState(true);
  const [searchMember, setSearchMember] = useState('');

  const loadMembers = async () => {
    setLoadingMembers(true);
    try {
      const res = await memberProfileApi.getAllMembers();
      const list = res.data || res || [];
      setMembers(list);
      if (list.length > 0 && !selectedMember) {
        setSelectedMember(list[0]);
      }
    } catch {
      setMembers([]);
    } finally {
      setLoadingMembers(false);
    }
  };

  useEffect(() => {
    loadMembers();
  }, []);

  const handleSelectMemberById = (userId) => {
    const found = members.find((m) => m.userId === userId || m.memberId === userId);
    if (found) {
      setSelectedMember(found);
    } else {
      setSelectedMember({ userId, firstName: 'Member', lastName: userId.slice(0, 8) });
    }
  };

  const filteredMembers = members.filter((m) => {
    const q = searchMember.toLowerCase();
    const name = memberDisplayName(m).toLowerCase();
    const phone = (m.address?.primaryPhone || '').toLowerCase();
    return name.includes(q) || phone.includes(q);
  });

  return (
    <div className="space-y-6 animate-fadeIn pb-12">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-[var(--bdae-border)] pb-4">
        <div>
          <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-[var(--bdae-text-secondary)]">
            <CreditCard className="w-4 h-4 text-[var(--bdae-primary)]" />
            <span>Retail & Corporate Banking</span>
          </div>
          <h1 className="text-2xl font-black tracking-tight text-[var(--bdae-text-primary)]">
            Member Account Portfolios
          </h1>
          <p className="text-xs text-[var(--bdae-text-secondary)]">
            Open core deposit accounts, inspect real-time available and ledger balances, and enforce legal freeze or lien holds.
          </p>
        </div>

        <button
          onClick={loadMembers}
          disabled={loadingMembers}
          className="p-2.5 rounded-xl border border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)] hover:bg-black/5 self-start sm:self-auto transition-all"
          title="Reload member directory"
        >
          <RefreshCw className={`w-4 h-4 ${loadingMembers ? 'animate-spin' : ''}`} />
        </button>
      </div>

      {/* Modular Search Panel */}
      <AccountSearchPanel onSelectMember={handleSelectMemberById} />

      {/* Split: Member Selector & Accounts View */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left: Member Directory */}
        <div className="bdae-card border border-[var(--bdae-border)] rounded-2xl p-4 space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">
              <Users className="w-3.5 h-3.5 text-[var(--bdae-primary)]" />
              Member Directory ({members.length})
            </div>
          </div>

          <input
            type="text"
            value={searchMember}
            onChange={(e) => setSearchMember(e.target.value)}
            placeholder="Filter by name or phone..."
            className="w-full px-3 py-2 rounded-xl border border-[var(--bdae-border)] bg-transparent text-xs text-[var(--bdae-text-primary)] outline-none"
          />

          <div className="space-y-1 max-h-[550px] overflow-y-auto pr-1">
            {loadingMembers ? (
              <div className="py-8 text-center text-xs text-[var(--bdae-text-secondary)]">
                <RefreshCw className="w-4 h-4 animate-spin mx-auto text-[var(--bdae-primary)] mb-1" />
                Loading members...
              </div>
            ) : filteredMembers.length === 0 ? (
              <div className="py-8 text-center text-xs text-[var(--bdae-text-secondary)]">
                No members found.
              </div>
            ) : (
              filteredMembers.map((m) => {
                const isSelected =
                  selectedMember &&
                  (selectedMember.userId === m.userId || selectedMember.memberId === m.memberId);
                return (
                  <button
                    key={m.userId || m.memberId}
                    onClick={() => setSelectedMember(m)}
                    className={`w-full p-3 rounded-xl border text-left transition-all flex items-center gap-3 ${
                      isSelected
                        ? 'border-[var(--bdae-primary)] bg-[var(--bdae-primary)]/10 text-[var(--bdae-primary)]'
                        : 'border-[var(--bdae-border)] hover:bg-black/5 dark:hover:bg-white/5'
                    }`}
                  >
                    <div
                      className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold shrink-0 ${
                        isSelected
                          ? 'bg-[var(--bdae-primary)] text-white'
                          : 'bg-black/5 dark:bg-white/5 text-[var(--bdae-text-secondary)]'
                      }`}
                    >
                      {memberInitials(m)}
                    </div>
                    <div className="min-w-0 flex-1">
                      <div className="text-xs font-bold text-[var(--bdae-text-primary)] truncate">
                        {memberDisplayName(m)}
                      </div>
                      <div className="text-[10px] text-[var(--bdae-text-secondary)] font-mono truncate">
                        {m.address?.primaryPhone || m.userId?.slice(0, 12)}
                      </div>
                    </div>
                  </button>
                );
              })
            )}
          </div>
        </div>

        {/* Right: Selected Member's Accounts Tab */}
        <div className="lg:col-span-2 space-y-4">
          {selectedMember ? (
            <div className="bdae-card border border-[var(--bdae-border)] rounded-2xl p-6 space-y-6">
              {/* Member Profile Banner */}
              <div className="flex items-center gap-4 pb-4 border-b border-[var(--bdae-border)]">
                <div
                  className="w-12 h-12 rounded-2xl flex items-center justify-center text-white text-base font-extrabold shadow-md shrink-0"
                  style={{
                    background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))`
                  }}
                >
                  {memberInitials(selectedMember)}
                </div>
                <div className="flex-1 min-w-0">
                  <h2 className="text-base font-extrabold text-[var(--bdae-text-primary)] truncate">
                    {memberDisplayName(selectedMember)}
                  </h2>
                  <div className="flex items-center gap-3 text-[11px] text-[var(--bdae-text-secondary)] mt-0.5 flex-wrap">
                    {selectedMember.address?.primaryPhone && (
                      <span>Phone: <b>{selectedMember.address.primaryPhone}</b></span>
                    )}
                    {selectedMember.kycStatus && (
                      <span className="px-2 py-0.5 rounded-full text-[9px] font-black uppercase bg-emerald-500/10 text-emerald-600 border border-emerald-500/20">
                        KYC: {selectedMember.kycStatus}
                      </span>
                    )}
                  </div>
                </div>
              </div>

              {/* Member Accounts Tab */}
              <MemberAccountsTab userId={selectedMember.userId || selectedMember.memberId} />
            </div>
          ) : (
            <div className="bdae-card border border-[var(--bdae-border)] rounded-2xl p-16 text-center space-y-3">
              <UserCircle className="w-10 h-10 mx-auto text-[var(--bdae-text-secondary)]/40" />
              <p className="text-sm font-bold text-[var(--bdae-text-primary)]">
                No Member Selected
              </p>
              <p className="text-xs text-[var(--bdae-text-secondary)]">
                Choose a member from the directory on the left or use the account lookup above to inspect accounts.
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
