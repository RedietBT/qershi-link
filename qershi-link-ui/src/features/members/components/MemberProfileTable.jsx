import React from 'react';
import { Eye, ShieldAlert, RefreshCw, BadgeInfo } from 'lucide-react';
import { PermissionGuard } from '../../../common/components/PermissionGuard';

const formatStatus = (status) => {
    switch (status) {
        case 'PENDING_ONBOARDING': return { text: 'PENDING', class: 'bg-amber-500/10 text-amber-600 border-amber-500/20' };
        case 'ACTIVE': return { text: 'ACTIVE', class: 'bg-emerald-500/10 text-emerald-600 border-emerald-500/20' };
        case 'SUSPENDED': return { text: 'SUSPENDED', class: 'bg-red-500/10 text-red-600 border-red-500/20' };
        case 'CLOSED': return { text: 'CLOSED', class: 'bg-gray-500/10 text-gray-600 border-gray-500/20' };
        default: return { text: status || 'UNKNOWN', class: 'bg-gray-500/10 text-gray-600 border-gray-500/20' };
    }
};

export const MemberProfileTable = ({ profiles = [], isLoading, error, onViewProfile, onRefresh }) => {

    if (isLoading) {
        return (
            <div className="bdae-card p-12 text-center space-y-3 border border-[var(--bdae-border)]">
                <RefreshCw className="w-8 h-8 text-[var(--bdae-secondary)] animate-spin mx-auto" />
                <p className="text-xs font-semibold text-[var(--bdae-text-secondary)]">Loading member profiles...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bdae-card p-8 border border-red-500/30 bg-red-500/5 text-center space-y-3">
                <ShieldAlert className="w-8 h-8 text-red-500 mx-auto" />
                <p className="text-xs font-bold text-red-600 dark:text-red-400">{error}</p>
                <button
                    onClick={onRefresh}
                    className="bdae-btn-primary px-4 py-2 text-xs font-bold rounded-xl inline-flex items-center gap-1.5"
                >
                    <RefreshCw className="w-3.5 h-3.5" /> Retry
                </button>
            </div>
        );
    }

    if (profiles.length === 0) {
        return (
            <div className="bdae-card p-12 text-center space-y-3 border border-[var(--bdae-border)]">
                <BadgeInfo className="w-10 h-10 text-[var(--bdae-text-secondary)] mx-auto opacity-50" />
                <p className="text-sm font-bold text-[var(--bdae-text-primary)]">No Profiles Found</p>
                <p className="text-xs text-[var(--bdae-text-secondary)]">Start by registering a new member.</p>
            </div>
        );
    }

    return (
        <div className="bdae-card border border-[var(--bdae-border)] shadow-xl overflow-hidden rounded-2xl">
            <div className="overflow-x-auto">
                <table className="w-full text-left text-xs border-collapse">
                    <thead>
                        <tr className="border-b border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 text-[11px] font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wider">
                            <th className="py-3.5 px-4 font-mono">Member No</th>
                            <th className="py-3.5 px-4">Full Name</th>
                            <th className="py-3.5 px-4">Gender</th>
                            <th className="py-3.5 px-4">DOB</th>
                            <th className="py-3.5 px-4">Status</th>
                            <th className="py-3.5 px-4 text-right">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-[var(--bdae-border)]">
                        {profiles.map((p) => {
                            const statusConf = formatStatus(p.status);

                            return (
                                <tr key={p.userId} className="hover:bg-black/5 dark:hover:bg-white/5 transition-colors group">
                                    <td className="py-3 px-4 font-mono text-[11px] text-[var(--bdae-primary)] font-bold">
                                        {p.memberNo || 'N/A'}
                                    </td>
                                    <td className="py-3 px-4 font-semibold text-[var(--bdae-text-primary)]">
                                        {p.firstName} {p.middleName} {p.lastName}
                                    </td>
                                    <td className="py-3 px-4 text-[var(--bdae-text-secondary)]">
                                        {p.gender}
                                    </td>
                                    <td className="py-3 px-4 font-mono text-[11px] text-[var(--bdae-text-secondary)]">
                                        {p.dateOfBirth}
                                    </td>
                                    <td className="py-3 px-4">
                                        <span className={`inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold border ${statusConf.class}`}>
                                            {statusConf.text}
                                        </span>
                                    </td>
                                    <td className="py-3 px-4 text-right">
                                        <PermissionGuard authorities={['MEMBER_VIEW_BASIC', 'MEMBER_VIEW_FULL', 'MEMBER_UPDATE', 'MEMBER_APPROVE']} roles={['SUPER_ADMIN', 'SACCO_ADMIN']}>
                                            <button
                                                onClick={() => onViewProfile(p)}
                                                className="p-1.5 rounded-lg border border-[var(--bdae-border)] hover:bg-[var(--bdae-primary)] hover:border-[var(--bdae-primary)] hover:text-white text-[var(--bdae-text-secondary)] transition-all shadow-sm"
                                                title="View Profile Details"
                                            >
                                                <Eye className="w-4 h-4" />
                                            </button>
                                        </PermissionGuard>
                                    </td>
                                </tr>
                            );
                        })}
                    </tbody>
                </table>
            </div>
        </div>
    );
};
