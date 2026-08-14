import React, { useState, useEffect, useRef } from 'react';
import {
    CreditCard, Search, RefreshCw, AlertCircle, Phone, Hash,
    Users, ChevronDown, ChevronRight, UserCircle
} from 'lucide-react';
import { accountLedgerApi } from '../api/accountLedgerApi';
import { memberProfileApi } from '../../members/api/memberProfileApi';
import { depositProductApi } from '../api/depositProductApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { MemberAccountsTab } from '../components/MemberAccountsTab';

// ────────────────────────────────────────────────────────────
// Member display name helper — never shows raw UUIDs
// ────────────────────────────────────────────────────────────
const memberDisplayName = (profile) => {
    const parts = [profile.firstName, profile.middleName, profile.lastName].filter(Boolean);
    return parts.length > 0 ? parts.join(' ') : profile.address?.primaryPhone || 'Unknown Member';
};

const memberInitials = (profile) => {
    const name = memberDisplayName(profile);
    return name.split(' ').map(w => w[0]).slice(0, 2).join('').toUpperCase();
};

// ────────────────────────────────────────────────────────────
// Search Panel: lookup by phone or account number
// ────────────────────────────────────────────────────────────
const AccountSearchPanel = () => {
    const [mode, setMode] = useState('phone'); // 'phone' | 'accountNo'
    const [query, setQuery] = useState('');
    const [results, setResults] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);
    const [searched, setSearched] = useState(false);

    const handleSearch = async () => {
        if (!query.trim()) return;
        setIsLoading(true); setError(null); setSearched(true); setResults([]);
        try {
            let res;
            if (mode === 'phone') {
                res = await accountLedgerApi.getAccountsByPhone(query.trim());
            } else {
                const single = await accountLedgerApi.getAccountByNo(query.trim());
                res = { data: [single.data || single] };
            }
            setResults(res.data || res || []);
        } catch (err) {
            setError(err?.response?.data?.message || 'No accounts found matching this query.');
        } finally {
            setIsLoading(false);
        }
    };

    const STATUS_STYLES = {
        ACTIVE: 'bg-emerald-500/10 text-emerald-600 border-emerald-500/20',
        PENDING_APPROVAL: 'bg-amber-500/10 text-amber-600 border-amber-500/20',
        DORMANT: 'bg-gray-500/10 text-gray-500 border-gray-500/20',
        CLOSED: 'bg-red-500/10 text-red-500 border-red-500/20',
    };

    return (
        <div className="bdae-card border border-[var(--bdae-border)] rounded-2xl p-5 space-y-4">
            <div className="flex items-center gap-2 text-sm font-extrabold text-[var(--bdae-text-primary)]">
                <Search className="w-4 h-4 text-[var(--bdae-secondary)]" />
                Account Lookup
            </div>

            {/* Mode Toggle */}
            <div className="flex gap-2">
                {[
                    { key: 'phone', label: 'By Phone Number', icon: Phone },
                    { key: 'accountNo', label: 'By Account No.', icon: Hash },
                ].map(({ key, label, icon: Icon }) => (
                    <button
                        key={key}
                        onClick={() => { setMode(key); setResults([]); setQuery(''); setSearched(false); setError(null); }}
                        className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-[10px] font-bold border transition-all ${mode === key ? 'border-[var(--bdae-primary)] bg-[var(--bdae-primary)]/10 text-[var(--bdae-primary)]' : 'border-[var(--bdae-border)] text-[var(--bdae-text-secondary)]'}`}
                    >
                        <Icon className="w-3 h-3" /> {label}
                    </button>
                ))}
            </div>

            {/* Search Input */}
            <div className="flex gap-2">
                <div className="relative flex-1">
                    <input
                        type="text"
                        value={query}
                        onChange={e => setQuery(e.target.value)}
                        onKeyDown={e => e.key === 'Enter' && handleSearch()}
                        placeholder={mode === 'phone' ? 'Enter phone number (e.g. 0911234567)' : 'Enter account number (e.g. 0001-001-101-0000427)'}
                        className="w-full pl-9 pr-4 py-2.5 rounded-xl border border-[var(--bdae-border)] focus:border-[var(--bdae-secondary)] bg-transparent text-xs text-[var(--bdae-text-primary)] outline-none font-mono"
                    />
                    {mode === 'phone'
                        ? <Phone className="w-3.5 h-3.5 text-[var(--bdae-text-secondary)] absolute left-3 top-3" />
                        : <Hash className="w-3.5 h-3.5 text-[var(--bdae-text-secondary)] absolute left-3 top-3" />
                    }
                </div>
                <button
                    onClick={handleSearch}
                    disabled={isLoading || !query.trim()}
                    className="px-4 py-2 rounded-xl text-xs font-bold text-white flex items-center gap-2 disabled:opacity-50 transition-all"
                    style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
                >
                    {isLoading ? <RefreshCw className="w-3.5 h-3.5 animate-spin" /> : <Search className="w-3.5 h-3.5" />}
                    Search
                </button>
            </div>

            {/* Results */}
            {error && <div className="flex items-center gap-2 p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 text-xs font-bold"><AlertCircle className="w-4 h-4" /> {error}</div>}

            {searched && !isLoading && !error && results.length === 0 && (
                <div className="text-center py-6 text-xs text-[var(--bdae-text-secondary)] opacity-60">No accounts found.</div>
            )}

            {results.length > 0 && (
                <div className="space-y-2">
                    {results.map(acc => (
                        <div key={acc.accountNo} className="p-3 rounded-xl border border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 space-y-2">
                            <div className="flex items-center justify-between gap-2">
                                <span className="font-mono font-extrabold text-xs text-[var(--bdae-primary)] tracking-widest">{acc.accountNo}</span>
                                <span className={`text-[9px] font-extrabold px-2 py-0.5 rounded-full border ${STATUS_STYLES[acc.accountStatus] || STATUS_STYLES.DORMANT}`}>
                                    {acc.accountStatus}
                                </span>
                            </div>
                            <div className="grid grid-cols-3 gap-2 text-[10px]">
                                <div>
                                    <p className="text-[var(--bdae-text-secondary)]">Product</p>
                                    <p className="font-bold text-[var(--bdae-text-primary)]">{acc.productCode || '—'}</p>
                                </div>
                                <div>
                                    <p className="text-[var(--bdae-text-secondary)]">Ledger Bal.</p>
                                    <p className="font-bold text-[var(--bdae-primary)]">{Number(acc.ledgerBalance || 0).toLocaleString()} ETB</p>
                                </div>
                                <div>
                                    <p className="text-[var(--bdae-text-secondary)]">Branch</p>
                                    <p className="font-bold text-[var(--bdae-text-primary)]">{acc.branchCode || '—'}</p>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

// ────────────────────────────────────────────────────────────
// Member Row with expandable account list
// ────────────────────────────────────────────────────────────
const MemberAccountRow = ({ profile }) => {
    const [isExpanded, setIsExpanded] = useState(false);
    const displayName = memberDisplayName(profile);
    const initials = memberInitials(profile);
    const phone = profile.address?.primaryPhone;

    return (
        <div className="border border-[var(--bdae-border)] rounded-2xl overflow-hidden transition-all">
            {/* Member Header Row */}
            <button
                onClick={() => setIsExpanded(e => !e)}
                className="w-full flex items-center justify-between p-4 hover:bg-black/5 dark:hover:bg-white/5 transition-colors text-left"
            >
                <div className="flex items-center gap-3">
                    <div
                        className="w-9 h-9 rounded-xl flex items-center justify-center text-white text-xs font-extrabold shrink-0"
                        style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
                    >
                        {initials}
                    </div>
                    <div>
                        <p className="text-sm font-bold text-[var(--bdae-text-primary)]">{displayName}</p>
                        <p className="text-[10px] text-[var(--bdae-text-secondary)] flex items-center gap-1">
                            {phone ? <><Phone className="w-3 h-3" /> {phone}</> : <span className="italic">No phone on record</span>}
                        </p>
                    </div>
                </div>
                <div className="flex items-center gap-2">
                    <span className={`text-[9px] font-extrabold px-2 py-0.5 rounded-full border ${profile.status === 'APPROVED' || profile.status === 'ACTIVE'
                            ? 'bg-emerald-500/10 text-emerald-600 border-emerald-500/20'
                            : 'bg-amber-500/10 text-amber-600 border-amber-500/20'
                        }`}>{profile.status || 'PENDING'}</span>
                    {isExpanded
                        ? <ChevronDown className="w-4 h-4 text-[var(--bdae-text-secondary)]" />
                        : <ChevronRight className="w-4 h-4 text-[var(--bdae-text-secondary)]" />
                    }
                </div>
            </button>

            {/* Expanded Account Panel */}
            {isExpanded && (
                <div className="border-t border-[var(--bdae-border)] p-4 bg-black/5 dark:bg-white/5">
                    <MemberAccountsTab userId={profile.userId} />
                </div>
            )}
        </div>
    );
};

// ────────────────────────────────────────────────────────────
// Main Account Management Page
// ────────────────────────────────────────────────────────────
export const AccountManagementPage = () => {
    const [profiles, setProfiles] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');

    const fetchProfiles = async () => {
        setIsLoading(true); setError(null);
        try {
            const res = await memberProfileApi.getAllProfiles();
            setProfiles(res.data || res || []);
        } catch (err) {
            setError(err?.response?.data?.message || 'Failed to load member profiles.');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => { fetchProfiles(); }, []);

    const filtered = profiles.filter(p => {
        if (!searchTerm) return true;
        const t = searchTerm.toLowerCase();
        const name = memberDisplayName(p).toLowerCase();
        const phone = (p.address?.primaryPhone || '').toLowerCase();
        return name.includes(t) || phone.includes(t);
    });

    return (
        <PermissionGuard
            roles={['SACCO_ADMIN', 'ADMIN']}
            permissions={['ACCOUNT_VIEW']}
            fallback={
                <div className="p-8 text-center space-y-3 mt-10">
                    <AlertCircle className="w-10 h-10 mx-auto text-red-500" />
                    <p className="font-bold">Access Restricted — ACCOUNT_VIEW permission required.</p>
                </div>
            }
        >
            <div className="space-y-6 animate-fadeIn">
                {/* Page Header */}
                <div className="flex items-center gap-3 border-b border-[var(--bdae-border)] pb-4">
                    <div
                        className="w-10 h-10 rounded-xl flex items-center justify-center text-white shadow-md shrink-0"
                        style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
                    >
                        <CreditCard className="w-5 h-5" />
                    </div>
                    <div>
                        <h1 className="text-xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
                            Account Management
                        </h1>
                        <p className="text-xs text-[var(--bdae-text-secondary)]">
                            Browse members, open accounts, and perform account operations — no UUIDs shown.
                        </p>
                    </div>
                </div>

                {/* Account Lookup Section */}
                <PermissionGuard roles={['SACCO_ADMIN', 'ADMIN']} permissions={['ACCOUNT_VIEW']}>
                    <AccountSearchPanel />
                </PermissionGuard>

                {/* Member List Section */}
                <div className="space-y-4">
                    <div className="flex items-center justify-between gap-3 flex-wrap">
                        <div className="flex items-center gap-2 text-sm font-extrabold text-[var(--bdae-text-primary)]">
                            <Users className="w-4 h-4 text-[var(--bdae-secondary)]" />
                            Member Roster — Click to expand accounts
                        </div>
                        <div className="flex items-center gap-2">
                            <div className="relative">
                                <input
                                    type="text"
                                    value={searchTerm}
                                    onChange={e => setSearchTerm(e.target.value)}
                                    placeholder="Filter by name or phone..."
                                    className="pl-8 pr-4 py-2 rounded-xl border border-[var(--bdae-border)] focus:border-[var(--bdae-secondary)] bg-transparent text-xs text-[var(--bdae-text-primary)] outline-none w-56"
                                />
                                <Search className="w-3.5 h-3.5 text-[var(--bdae-text-secondary)] absolute left-2.5 top-2.5" />
                            </div>
                            <button onClick={fetchProfiles} disabled={isLoading} className="px-3 py-2 rounded-xl border border-[var(--bdae-border)] text-xs font-bold flex items-center gap-1.5 text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-primary)] transition-all">
                                <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} /> Refresh
                            </button>
                        </div>
                    </div>

                    {error && <div className="flex items-center gap-2 p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 text-xs font-bold"><AlertCircle className="w-4 h-4" /> {error}</div>}

                    {isLoading ? (
                        <div className="py-16 text-center"><RefreshCw className="w-6 h-6 animate-spin mx-auto text-[var(--bdae-secondary)]" /></div>
                    ) : filtered.length === 0 ? (
                        <div className="py-16 text-center opacity-50 space-y-2">
                            <UserCircle className="w-10 h-10 mx-auto" />
                            <p className="text-sm font-bold">No members found</p>
                        </div>
                    ) : (
                        <div className="space-y-2">
                            {filtered.map(profile => (
                                <MemberAccountRow key={profile.userId} profile={profile} />
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </PermissionGuard>
    );
};
