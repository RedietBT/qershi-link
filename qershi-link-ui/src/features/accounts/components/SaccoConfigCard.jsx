import React, { useState, useEffect } from 'react';
import { Building2, Save, RefreshCw, AlertCircle, CheckCircle, Info, Settings } from 'lucide-react';
import { accountConfigApi } from '../api/accountConfigApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';

/**
 * SaccoConfigCard
 *
 * Displays and edits the tenant SACCO Code configuration.
 * Read access: ACCOUNT_VIEW, SACCO_ADMIN, ADMIN
 * Write access: ROLE_MANAGE, SACCO_ADMIN, ADMIN
 */
export const SaccoConfigCard = () => {
    const [config, setConfig] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isSaving, setIsSaving] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    // Form state
    const [saccoCode, setSaccoCode] = useState('');
    const [saccoName, setSaccoName] = useState('');

    const fetchConfig = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const res = await accountConfigApi.getSaccoConfig();
            const data = res.data || res;
            setConfig(data);
            setSaccoCode(data.saccoCode || '');
            setSaccoName(data.saccoName || '');
        } catch (err) {
            setError(err?.response?.data?.message || 'Failed to load SACCO configuration.');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchConfig();
    }, []);

    const handleSave = async () => {
        setIsSaving(true);
        setError(null);
        setSuccess(null);
        try {
            const payload = { saccoCode: saccoCode.trim(), saccoName: saccoName.trim() };
            // Use POST if not yet configured, PUT to update existing
            const res = config?.saccoCode
                ? await accountConfigApi.updateSaccoConfig(payload)
                : await accountConfigApi.setSaccoConfig(payload);

            const updated = res.data || res;
            setConfig(updated);
            setSuccess('SACCO configuration saved successfully!');
            setTimeout(() => setSuccess(null), 4000);
        } catch (err) {
            setError(err?.response?.data?.message || 'Failed to save SACCO configuration.');
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <div className="bdae-card border border-[var(--bdae-border)] rounded-2xl overflow-hidden shadow-xl">
            {/* Card Header */}
            <div
                className="p-5 flex items-center justify-between"
                style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
            >
                <div className="flex items-center gap-3 text-white">
                    <div className="w-9 h-9 rounded-xl bg-white/20 flex items-center justify-center border border-white/30">
                        <Building2 className="w-5 h-5" />
                    </div>
                    <div>
                        <h2 className="text-sm font-extrabold">SACCO Code Configuration</h2>
                        <p className="text-[11px] opacity-80">
                            Tenant SACCO code used for ISO Luhn account number generation.
                        </p>
                    </div>
                </div>
                <button onClick={fetchConfig} disabled={isLoading} className="text-white/70 hover:text-white transition-colors">
                    <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
                </button>
            </div>

            {/* Body */}
            <div className="p-6 space-y-5">
                {/* Current Config Info Badge */}
                {config?.saccoCode && (
                    <div className="flex items-center gap-2 p-3 rounded-xl bg-[var(--bdae-primary)]/10 border border-[var(--bdae-primary)]/20 text-xs">
                        <Info className="w-4 h-4 text-[var(--bdae-primary)] shrink-0" />
                        <span className="text-[var(--bdae-text-secondary)]">
                            Active Code: <span className="font-extrabold font-mono text-[var(--bdae-primary)]">{config.saccoCode}</span>
                            {'  '}| Branch Code: <span className="font-extrabold font-mono text-[var(--bdae-primary)]">{config.branchCode || '0001'}</span>
                        </span>
                    </div>
                )}

                {/* Error */}
                {error && (
                    <div className="flex items-center gap-2 p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-600 text-xs font-bold">
                        <AlertCircle className="w-4 h-4 shrink-0" /> {error}
                    </div>
                )}

                {/* Success */}
                {success && (
                    <div className="flex items-center gap-2 p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-600 text-xs font-bold">
                        <CheckCircle className="w-4 h-4 shrink-0" /> {success}
                    </div>
                )}

                {/* Form Fields */}
                {isLoading ? (
                    <div className="py-10 text-center">
                        <RefreshCw className="w-6 h-6 animate-spin mx-auto text-[var(--bdae-secondary)]" />
                    </div>
                ) : (
                    <>
                        {/* SACCO Name */}
                        <div className="space-y-1.5">
                            <label className="text-xs font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wide">
                                SACCO Name
                            </label>
                            <input
                                type="text"
                                value={saccoName}
                                onChange={(e) => setSaccoName(e.target.value)}
                                placeholder="e.g. Awach Saving And Credit Cooperative"
                                className="w-full px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] focus:border-[var(--bdae-secondary)] bg-transparent text-xs text-[var(--bdae-text-primary)] outline-none transition-all"
                            />
                        </div>

                        {/* SACCO Code */}
                        <div className="space-y-1.5">
                            <label className="text-xs font-bold text-[var(--bdae-text-secondary)] uppercase tracking-wide">
                                SACCO Code <span className="text-red-500">*</span>
                            </label>
                            <input
                                type="text"
                                value={saccoCode}
                                onChange={(e) => setSaccoCode(e.target.value.toUpperCase())}
                                placeholder="e.g. 0001"
                                maxLength={10}
                                className="w-full px-4 py-2.5 rounded-xl border border-[var(--bdae-border)] focus:border-[var(--bdae-secondary)] bg-transparent text-xs font-mono text-[var(--bdae-text-primary)] outline-none transition-all tracking-widest"
                            />
                            <p className="text-[10px] text-[var(--bdae-text-secondary)]">
                                This code is permanently embedded into every generated account number (e.g. <span className="font-mono">0001</span>-001-101-XXXXXXX).
                            </p>
                        </div>

                        {/* Save Button — guarded by ROLE_MANAGE or SACCO_ADMIN */}
                        <PermissionGuard
                            roles={['SACCO_ADMIN', 'ADMIN']}
                            permissions={['ROLE_MANAGE']}
                            fallback={
                                <div className="flex items-center gap-2 p-3 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-600 text-xs">
                                    <AlertCircle className="w-4 h-4 shrink-0" />
                                    You have read-only access to SACCO configuration.
                                </div>
                            }
                        >
                            <button
                                onClick={handleSave}
                                disabled={isSaving || !saccoCode.trim()}
                                className="w-full flex items-center justify-center gap-2 py-2.5 rounded-xl text-xs font-bold text-white transition-all shadow-md disabled:opacity-50 disabled:cursor-not-allowed"
                                style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
                            >
                                {isSaving ? (
                                    <><RefreshCw className="w-4 h-4 animate-spin" /> Saving...</>
                                ) : (
                                    <><Save className="w-4 h-4" /> Save SACCO Configuration</>
                                )}
                            </button>
                        </PermissionGuard>
                    </>
                )}
            </div>
        </div>
    );
};
