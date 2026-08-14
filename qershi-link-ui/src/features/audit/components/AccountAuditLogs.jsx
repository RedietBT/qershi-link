import React, { useState, useEffect } from 'react';
import {
    RefreshCw, Search, History, Clock, Hash, Phone,
    CreditCard, ArrowRight, AlertCircle, ChevronDown, ChevronUp
} from 'lucide-react';
import { accountAuditApi } from '../../accounts/api/accountAuditApi';
import { memberProfileApi } from '../../members/api/memberProfileApi';

// ────────────────────────────────────────────────────────────
// Action badge colour mapping
// ────────────────────────────────────────────────────────────
const ACTION_STYLES = {
    ACCOUNT_OPENED: 'bg-blue-500/10 text-blue-600 border-blue-500/20',
    ACCOUNT_APPROVED: 'bg-emerald-500/10 text-emerald-600 border-emerald-500/20',
    ACCOUNT_CLOSED: 'bg-red-500/10 text-red-600 border-red-500/20',
    FREEZE_UPDATED: 'bg-cyan-500/10 text-cyan-600 border-cyan-500/20',
    LIEN_PLACED: 'bg-amber-500/10 text-amber-600 border-amber-500/20',
    LIEN_RELEASED: 'bg-violet-500/10 text-violet-600 border-violet-500/20',
    STATUS_CHANGED: 'bg-orange-500/10 text-orange-600 border-orange-500/20',
};
const actionStyle = (action) => ACTION_STYLES[action] || 'bg-gray-500/10 text-gray-600 border-gray-500/20';

// ────────────────────────────────────────────────────────────
// Single log row with optional diff expansion
// ────────────────────────────────────────────────────────────
const AuditRow = ({ log, phoneMap }) => {
    const [expanded, setExpanded] = useState(false);
    const hasDiff = log.oldValue || log.newValue;
    // Mask UUID: look up from phone map, otherwise show "System Actor"
    const actorPhone = phoneMap[log.performedByUserId] || null;
    const memberPhone = phoneMap[log.userId] || null;

    return (
        <>
            <tr
                onClick={() => hasDiff && setExpanded(e => !e)}
                className={`hover:bg-black/5 dark:hover:bg-white/5 transition-colors ${hasDiff ? 'cursor-pointer' : ''}`}
            >
                {/* Timestamp */}
                <td className="p-4 align-top whitespace-nowrap">
                    <span className="text-[11px] font-bold text-[var(--bdae-text-secondary)] flex items-center gap-1">
                        <Clock className="w-3 h-3 text-[var(--bdae-secondary)] shrink-0" />
                        {new Date(log.createdAt).toLocaleString('en-ET', {
                            day: '2-digit', month: 'short', year: 'numeric',
                            hour: '2-digit', minute: '2-digit', second: '2-digit'
                        })}
                    </span>
                </td>

                {/* Action */}
                <td className="p-4 align-top">
                    <span className={`inline-flex items-center px-2 py-0.5 rounded-full border text-[9px] font-extrabold uppercase tracking-wide ${actionStyle(log.action)}`}>
                        {log.action?.replace(/_/g, ' ')}
                    </span>
                    {log.fieldName && (
                        <div className="mt-1 text-[10px] text-[var(--bdae-text-secondary)]">
                            Field: <span className="font-bold text-[var(--bdae-text-primary)]">{log.fieldName}</span>
                        </div>
                    )}
                </td>

                {/* Account No */}
                <td className="p-4 align-top">
                    <span className="font-mono font-bold text-xs text-[var(--bdae-primary)] tracking-widest">
                        {log.accountNo || '—'}
                    </span>
                </td>

                {/* Member (no UUID) */}
                <td className="p-4 align-top">
                    <span className="text-xs font-bold flex items-center gap-1 text-[var(--bdae-text-primary)]">
                        {memberPhone
                            ? <><Phone className="w-3 h-3 text-[var(--bdae-secondary)]" /> {memberPhone}</>
                            : <span className="italic text-[var(--bdae-text-secondary)] text-[10px]">Member</span>
                        }
                    </span>
                </td>

                {/* Actor (no UUID) */}
                <td className="p-4 align-top">
                    <span className="text-[11px] font-mono text-[var(--bdae-text-secondary)]">
                        {actorPhone
                            ? <span className="flex items-center gap-1 text-[var(--bdae-text-primary)] font-bold"><Phone className="w-3 h-3" /> {actorPhone}</span>
                            : <span className="italic text-[10px]">Admin / System</span>
                        }
                    </span>
                </td>

                {/* Expand toggle */}
                <td className="p-4 align-top text-right">
                    {hasDiff && (
                        expanded
                            ? <ChevronUp className="w-4 h-4 ml-auto text-[var(--bdae-secondary)]" />
                            : <ChevronDown className="w-4 h-4 ml-auto text-[var(--bdae-text-secondary)]" />
                    )}
                </td>
            </tr>

            {/* Expanded Diff Row */}
            {expanded && hasDiff && (
                <tr className="bg-black/5 dark:bg-white/5">
                    <td colSpan={6} className="px-8 py-3">
                        <div className="flex items-center gap-4 flex-wrap">
                            {log.oldValue && (
                                <div className="flex-1 min-w-[120px] max-w-xs p-2 rounded-xl bg-red-500/10 border border-red-500/20 font-mono text-[10px] text-red-600 break-all">
                                    <span className="font-extrabold block mb-1 uppercase tracking-wide text-[8px]">Before</span>
                                    — {log.oldValue}
                                </div>
                            )}
                            {log.oldValue && log.newValue && (
                                <ArrowRight className="w-4 h-4 text-[var(--bdae-text-secondary)] shrink-0" />
                            )}
                            {log.newValue && (
                                <div className="flex-1 min-w-[120px] max-w-xs p-2 rounded-xl bg-emerald-500/10 border border-emerald-500/20 font-mono text-[10px] text-emerald-600 break-all">
                                    <span className="font-extrabold block mb-1 uppercase tracking-wide text-[8px]">After</span>
                                    + {log.newValue}
                                </div>
                            )}
                        </div>
                    </td>
                </tr>
            )}
        </>
    );
};

// ────────────────────────────────────────────────────────────
// Main Account Audit Logs Component
// ────────────────────────────────────────────────────────────
export const AccountAuditLogs = () => {
    const [logs, setLogs] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);
    const [page, setPage] = useState(0);

    // Search state
    const [searchMode, setSearchMode] = useState('all');  // 'all' | 'accountNo' | 'phone'
    const [searchInput, setSearchInput] = useState('');
    const [filterText, setFilterText] = useState(''); // client-side action filter

    // Phone-to-UUID reverse lookup map { uuid: phone }
    const [phoneMap, setPhoneMap] = useState({});

    // Fetch phone map for masking UUIDs
    const buildPhoneMap = async () => {
        try {
            const res = await memberProfileApi.getAllProfiles();
            const profiles = res.data || res || [];
            const map = {};
            profiles.forEach(p => {
                if (p.userId && p.address?.primaryPhone) {
                    map[p.userId] = p.address.primaryPhone;
                }
            });
            setPhoneMap(map);
        } catch (_) { /* silently skip — map stays empty */ }
    };

    const fetchLogs = async () => {
        setIsLoading(true); setError(null);
        try {
            let res;
            if (searchMode === 'accountNo' && searchInput.trim()) {
                res = await accountAuditApi.getLogsByAccountNo(searchInput.trim());
                setLogs(Array.isArray(res) ? res : (res.data || []));
            } else {
                res = await accountAuditApi.getAuditLogs(page, 50);
                setLogs(Array.isArray(res) ? res : (res.data || res || []));
            }
        } catch (err) {
            setError(err?.response?.data?.message || 'Failed to load account audit logs.');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        buildPhoneMap();
    }, []);

    useEffect(() => {
        if (searchMode !== 'accountNo') fetchLogs();
    }, [page, searchMode]);

    const handleSearch = () => {
        if (searchMode === 'accountNo') {
            fetchLogs();
        }
    };

    // Client-side filter by action or account number text
    const displayed = logs.filter(log => {
        if (!filterText) return true;
        const t = filterText.toLowerCase();
        return (
            (log.action?.toLowerCase().includes(t)) ||
            (log.accountNo?.toLowerCase().includes(t)) ||
            (log.fieldName?.toLowerCase().includes(t))
        );
    });

    // Stats
    const actionCounts = logs.reduce((acc, l) => {
        acc[l.action] = (acc[l.action] || 0) + 1;
        return acc;
    }, {});

    return (
        <div className="space-y-5">
            {/* Stats Row */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                {[
                    { label: 'Total Events', value: logs.length, color: 'var(--bdae-primary)' },
                    { label: 'Approvals', value: actionCounts['ACCOUNT_APPROVED'] || 0, color: '#10b981' },
                    { label: 'Freezes', value: actionCounts['FREEZE_UPDATED'] || 0, color: '#06b6d4' },
                    { label: 'Liens Placed', value: actionCounts['LIEN_PLACED'] || 0, color: '#f59e0b' },
                ].map(({ label, value, color }) => (
                    <div key={label} className="bdae-card border border-[var(--bdae-border)] rounded-2xl p-4 flex items-center gap-3">
                        <div
                            className="w-9 h-9 rounded-xl flex items-center justify-center text-white text-sm font-extrabold shrink-0 shadow"
                            style={{ background: color }}
                        >
                            {value}
                        </div>
                        <div>
                            <p className="text-[10px] font-bold uppercase tracking-wide text-[var(--bdae-text-secondary)]">{label}</p>
                        </div>
                    </div>
                ))}
            </div>

            {/* Filter / Search Bar */}
            <div className="bdae-card border border-[var(--bdae-border)] rounded-2xl p-4 flex flex-col md:flex-row md:items-center gap-3 flex-wrap">
                {/* Mode Toggle */}
                <div className="flex gap-1.5">
                    {[
                        { key: 'all', label: 'All', icon: History },
                        { key: 'accountNo', label: 'By Account No', icon: Hash },
                    ].map(({ key, label, icon: Icon }) => (
                        <button
                            key={key}
                            onClick={() => { setSearchMode(key); setSearchInput(''); }}
                            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl border text-[10px] font-bold transition-all ${searchMode === key ? 'border-[var(--bdae-primary)] bg-[var(--bdae-primary)]/10 text-[var(--bdae-primary)]' : 'border-[var(--bdae-border)] text-[var(--bdae-text-secondary)]'}`}
                        >
                            <Icon className="w-3 h-3" /> {label}
                        </button>
                    ))}
                </div>

                {/* Search Input — visible when mode ≠ all */}
                {searchMode === 'accountNo' && (
                    <div className="flex gap-2 flex-1">
                        <div className="relative flex-1">
                            <input
                                type="text"
                                value={searchInput}
                                onChange={e => setSearchInput(e.target.value)}
                                onKeyDown={e => e.key === 'Enter' && handleSearch()}
                                placeholder="Enter account number (e.g. 0001-001-101-0000427)"
                                className="w-full pl-9 pr-4 py-2 rounded-xl border border-[var(--bdae-border)] focus:border-[var(--bdae-secondary)] bg-transparent text-xs text-[var(--bdae-text-primary)] outline-none font-mono"
                            />
                            <CreditCard className="w-3.5 h-3.5 text-[var(--bdae-text-secondary)] absolute left-3 top-2.5" />
                        </div>
                        <button
                            onClick={handleSearch}
                            disabled={isLoading || !searchInput.trim()}
                            className="px-4 py-2 rounded-xl text-xs font-bold text-white flex items-center gap-2 disabled:opacity-50"
                            style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
                        >
                            {isLoading ? <RefreshCw className="w-3.5 h-3.5 animate-spin" /> : <Search className="w-3.5 h-3.5" />}
                            Search
                        </button>
                    </div>
                )}

                {/* Action text filter — always visible */}
                <div className="relative flex-1 min-w-[180px]">
                    <input
                        type="text"
                        value={filterText}
                        onChange={e => setFilterText(e.target.value)}
                        placeholder="Filter by action, account no, field..."
                        className="w-full pl-9 pr-4 py-2 rounded-xl border border-[var(--bdae-border)] focus:border-[var(--bdae-secondary)] bg-transparent text-xs text-[var(--bdae-text-primary)] outline-none"
                    />
                    <Search className="w-3.5 h-3.5 text-[var(--bdae-text-secondary)] absolute left-3 top-2.5" />
                </div>

                {/* Refresh + Pagination */}
                <div className="flex gap-2">
                    <button onClick={fetchLogs} disabled={isLoading} className="px-3 py-2 rounded-xl border border-[var(--bdae-border)] text-xs font-bold text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-primary)] flex items-center gap-1.5 transition-all">
                        <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} /> Refresh
                    </button>
                    {searchMode === 'all' && (
                        <>
                            <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0} className="px-3 py-2 rounded-xl border border-[var(--bdae-border)] text-xs font-bold text-[var(--bdae-text-secondary)] hover:bg-black/5 disabled:opacity-40 transition-all">« Prev</button>
                            <div className="px-3 py-2 text-xs font-bold text-[var(--bdae-primary)]">Page {page + 1}</div>
                            <button onClick={() => setPage(p => p + 1)} disabled={logs.length < 50} className="px-3 py-2 rounded-xl border border-[var(--bdae-border)] text-xs font-bold text-[var(--bdae-text-secondary)] hover:bg-black/5 disabled:opacity-40 transition-all">Next »</button>
                        </>
                    )}
                </div>
            </div>

            {error && (
                <div className="flex items-center gap-2 p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 text-xs font-bold">
                    <AlertCircle className="w-4 h-4 shrink-0" /> {error}
                </div>
            )}

            {/* Audit Table */}
            <div className="bdae-card border border-[var(--bdae-border)] rounded-2xl overflow-hidden">
                {isLoading ? (
                    <div className="py-20 text-center"><RefreshCw className="w-6 h-6 animate-spin mx-auto text-[var(--bdae-secondary)]" /></div>
                ) : displayed.length === 0 ? (
                    <div className="py-20 text-center flex flex-col items-center gap-3 opacity-50">
                        <History className="w-10 h-10" />
                        <p className="text-sm font-bold">No account audit events found</p>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse">
                            <thead>
                                <tr className="bg-black/5 dark:bg-white/5 border-b border-[var(--bdae-border)] text-[10px] uppercase font-extrabold text-[var(--bdae-text-secondary)] tracking-wider">
                                    <th className="p-4">Timestamp</th>
                                    <th className="p-4">Action Event</th>
                                    <th className="p-4">Account No.</th>
                                    <th className="p-4">Member</th>
                                    <th className="p-4">Performed By</th>
                                    <th className="p-4 text-right">Details</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-[var(--bdae-border)] text-xs">
                                {displayed.map(log => (
                                    <AuditRow
                                        key={log.logId}
                                        log={log}
                                        phoneMap={phoneMap}
                                    />
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

            {/* Security Notice */}
            <div className="text-[11px] text-[var(--bdae-text-secondary)] border border-[var(--bdae-border)] rounded-xl p-4 leading-relaxed">
                <span className="font-bold text-[var(--bdae-primary)]">🔒 Privacy Notice:</span> Internal user UUIDs are not displayed. Member identities are shown via phone number only. All audit access is gated by the <span className="font-mono font-bold">AUDIT_LOG_VIEW</span> permission.
            </div>
        </div>
    );
};
