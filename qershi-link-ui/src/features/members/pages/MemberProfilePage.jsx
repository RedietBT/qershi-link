import React from 'react';
import { useMemberProfiles } from '../hooks/useMemberProfiles';
import { MemberProfileTable } from '../components/MemberProfileTable';
import { MemberProfileFilterBar } from '../components/MemberProfileFilterBar';
import { CreateProfileModal } from '../components/CreateProfileModal';
import { MemberProfileDetailModal } from '../components/MemberProfileDetailModal';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { Users, UserPlus, RefreshCw, Lock } from 'lucide-react';

export const MemberProfilePage = () => {
    const {
        profiles,
        isLoading,
        error,
        statusFilter,
        setStatusFilter,
        searchTerm,
        setSearchTerm,
        refreshProfiles,
        isCreateOpen,
        setIsCreateOpen,
        viewingProfile,
        setViewingProfile
    } = useMemberProfiles();

    return (
        <PermissionGuard roles={['SUPER_ADMIN', 'SACCO_ADMIN', 'ADMIN', 'BRANCH_MANAGER', 'LOAN_OFFICER']} permissions={['MEMBER_VIEW_BASIC', 'MEMBER_VIEW_FULL']} fallback={
            <div className="p-8 text-center max-w-lg mx-auto space-y-4">
                <div className="w-12 h-12 rounded-full bg-amber-500/10 border border-amber-500/30 text-amber-600 mx-auto flex items-center justify-center">
                    <Lock className="w-6 h-6" />
                </div>
                <h2 className="text-lg font-bold">Access Restricted</h2>
                <p className="text-xs text-[var(--bdae-text-secondary)]">
                    Member Profile Management requires Member View authorization (MEMBER_VIEW_BASIC or Admin role).
                </p>
            </div>
        }>
            <div className="space-y-6 animate-fadeIn max-w-7xl mx-auto">
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-[var(--bdae-border)] pb-4">
                    <div className="flex items-center space-x-3">
                        <div
                            className="w-10 h-10 rounded-xl flex items-center justify-center text-white shadow-md shrink-0"
                            style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
                        >
                            <Users className="w-5 h-5" />
                        </div>
                        <div>
                            <h1 className="text-xl font-extrabold tracking-tight text-[var(--bdae-text-primary)]">
                                Member Profile Management
                            </h1>
                            <p className="text-xs text-[var(--bdae-text-secondary)]">
                                Manage SACCO member onboarding, demographics, and operations.
                            </p>
                        </div>
                    </div>

                    <div className="flex items-center space-x-3">
                        <button
                            onClick={refreshProfiles}
                            disabled={isLoading}
                            className="px-3.5 py-2 rounded-xl border border-[var(--bdae-border)] hover:border-[var(--bdae-secondary)] text-xs font-bold flex items-center gap-2 transition-all shadow-sm"
                        >
                            <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
                            <span>Refresh</span>
                        </button>
                    </div>
                </div>

                <MemberProfileFilterBar
                    searchTerm={searchTerm}
                    setSearchTerm={setSearchTerm}
                    statusFilter={statusFilter}
                    setStatusFilter={setStatusFilter}
                />

                <MemberProfileTable
                    profiles={profiles}
                    isLoading={isLoading}
                    error={error}
                    onViewProfile={(profile) => setViewingProfile(profile)}
                    onRefresh={refreshProfiles}
                />

                {viewingProfile && (
                    <MemberProfileDetailModal
                        profile={viewingProfile}
                        onClose={() => setViewingProfile(null)}
                        onSuccess={() => {
                            refreshProfiles();
                        }}
                    />
                )}
            </div>
        </PermissionGuard>
    );
};
