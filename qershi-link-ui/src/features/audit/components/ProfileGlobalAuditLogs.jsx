import React, { useState, useEffect } from 'react';
import { RefreshCw, Search, History, Clock, FileText } from 'lucide-react';
import { profileAuditApi } from '../../members/api/profileAuditApi';

export const ProfileGlobalAuditLogs = () => {
    const [logs, setLogs] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [page, setPage] = useState(0);

    const fetchLogs = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const res = await profileAuditApi.getAuditLogs(page, 50);
            setLogs(res || []);
        } catch (err) {
            setError(err?.response?.data?.message || 'Failed to fetch profile audit logs');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchLogs();
    }, [page]);

    // Client-side filtering mechanism based on searchTerm
    const filteredLogs = logs.filter(log => {
        if (!searchTerm) return true;
        const term = searchTerm.toLowerCase();
        return (
            (log.action && log.action.toLowerCase().includes(term)) ||
            (log.userId && log.userId.toLowerCase().includes(term)) ||
            (log.primaryPhone && log.primaryPhone.toLowerCase().includes(term)) ||
            (log.performedByUserId && log.performedByUserId.toLowerCase().includes(term)) ||
            (log.fieldName && log.fieldName.toLowerCase().includes(term))
        );
    });

    return (
        <div className="space-y-6">
            {/* Filter Bar */}
            <div className="bdae-card p-4 border border-[var(--bdae-border)] flex flex-col md:flex-row items-center justify-between gap-4">
                <div className="relative w-full md:w-96">
                    <input
                        type="text"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        placeholder="Search by action, user ID, actor ID, or field..."
                        className="w-full pl-10 pr-4 py-2 rounded-xl border border-[var(--bdae-border)] focus:border-[var(--bdae-secondary)] text-xs bg-transparent outline-none text-[var(--bdae-text-primary)] transition-all font-mono"
                    />
                    <Search className="w-4 h-4 text-[var(--bdae-text-secondary)] absolute left-3.5 top-2.5" />
                </div>
                <div className="flex gap-2">
                    <button onClick={fetchLogs} disabled={isLoading} className="px-3 py-2 rounded-xl border border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-primary)] text-xs font-bold transition-all flex items-center gap-2 shadow-sm">
                        <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} /> Refresh Profiles
                    </button>
                    {/* Basic Pagination (Assuming fixed 50 per page for now) */}
                    <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0} className="px-3 py-2 rounded-xl border border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] hover:bg-black/5 text-xs font-bold shadow-sm transition-all">&laquo; Prev</button>
                    <div className="px-3 py-2 text-xs font-bold text-[var(--bdae-primary)]">Page {page + 1}</div>
                    <button onClick={() => setPage(p => p + 1)} disabled={logs.length < 50} className="px-3 py-2 rounded-xl border border-[var(--bdae-border)] text-[var(--bdae-text-secondary)] hover:bg-black/5 text-xs font-bold shadow-sm transition-all">Next &raquo;</button>
                </div>
            </div>

            {error && (
                <div className="p-3 mb-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 text-xs font-bold">
                    {error}
                </div>
            )}

            {/* Global Timeline/Table view */}
            <div className="bdae-card border border-[var(--bdae-border)] overflow-hidden">
                {isLoading ? (
                    <div className="py-20 text-center"><RefreshCw className="w-6 h-6 animate-spin mx-auto text-[var(--bdae-secondary)]" /></div>
                ) : filteredLogs.length === 0 ? (
                    <div className="py-20 text-center flex flex-col items-center opacity-50">
                        <History className="w-10 h-10 mb-3" />
                        <span className="text-sm font-bold">No profile audit logs found</span>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse">
                            <thead>
                                <tr className="bg-black/5 dark:bg-white/5 border-b border-[var(--bdae-border)] text-[10px] uppercase font-extrabold text-[var(--bdae-text-secondary)] tracking-wider">
                                    <th className="p-4">Action Details</th>
                                    <th className="p-4">Entity (User)</th>
                                    <th className="p-4">Data Changes</th>
                                    <th className="p-4 text-right">Timestamp & Actor</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-[var(--bdae-border)] text-xs">
                                {filteredLogs.map(log => (
                                    <tr key={log.logId} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors group">
                                        <td className="p-4 align-top">
                                            <span className="inline-flex items-center px-2 py-0.5 rounded bg-[var(--bdae-primary)]/10 text-[10px] font-bold text-[var(--bdae-primary)] uppercase">
                                                {log.action}
                                            </span>
                                            {log.fieldName && (
                                                <div className="mt-2 text-[10px] text-[var(--bdae-text-secondary)]">
                                                    Field: <span className="font-bold text-[var(--bdae-text-primary)]">{log.fieldName}</span>
                                                </div>
                                            )}
                                        </td>
                                        <td className="p-4 align-top">
                                            <div className="font-mono text-[11px] text-[var(--bdae-primary)] font-bold mb-1">
                                                {log.primaryPhone ? `📞 ${log.primaryPhone}` : <span className="opacity-50 text-[var(--bdae-text-secondary)] italic">No Phone Linked</span>}
                                            </div>
                                        </td>
                                        <td className="p-4 align-top">
                                            {(log.oldValue || log.newValue) ? (
                                                <div className="space-y-1 w-full max-w-xs">
                                                    {log.oldValue && (
                                                        <div className="p-1 px-2 rounded bg-red-500/10 border border-red-500/20 font-mono break-all line-through opacity-80 text-red-600 text-[10px]">
                                                            - {log.oldValue}
                                                        </div>
                                                    )}
                                                    {log.newValue && (
                                                        <div className="p-1 px-2 rounded bg-emerald-500/10 border border-emerald-500/20 font-mono break-all text-emerald-600 text-[10px]">
                                                            + {log.newValue}
                                                        </div>
                                                    )}
                                                </div>
                                            ) : (
                                                <span className="text-[10px] text-[var(--bdae-text-secondary)] italic">No explicit data tracked</span>
                                            )}
                                        </td>
                                        <td className="p-4 align-top text-right">
                                            <div className="flex flex-col items-end gap-1">
                                                <span className="inline-flex items-center gap-1 text-[11px] font-bold text-[var(--bdae-text-primary)]">
                                                    <Clock className="w-3.5 h-3.5 text-[var(--bdae-secondary)]" />
                                                    {new Date(log.createdAt).toLocaleString()}
                                                </span>
                                                <span className="text-[10px] font-mono text-[var(--bdae-text-secondary)] opacity-70">
                                                    Action by Admin
                                                </span>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
};
