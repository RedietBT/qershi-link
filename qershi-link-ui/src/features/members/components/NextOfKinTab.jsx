import React, { useState, useEffect } from 'react';
import { nextOfKinApi } from '../api/nextOfKinApi';
import { PermissionGuard } from '../../../common/components/PermissionGuard';
import { RefreshCw, AlertCircle, CheckCircle, Heart, Plus, Edit3, Trash2, Users, Save } from 'lucide-react';

export const NextOfKinTab = ({ userId }) => {
    const [kins, setKins] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);
    const [successMsg, setSuccessMsg] = useState(null);

    const [isSubmitting, setIsSubmitting] = useState(false);

    // UI State
    const [showForm, setShowForm] = useState(false);
    const [editingKinId, setEditingKinId] = useState(null);

    const [formData, setFormData] = useState({
        fullName: '',
        relationship: '',
        primaryPhone: '',
        idNumber: '',
        physicalAddress: '',
        allocationPercentage: 0
    });

    const fetchNextOfKin = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const res = await nextOfKinApi.getNextOfKinByUserId(userId);
            setKins(res.data || []);
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to fetch Next of Kin beneficiaries');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchNextOfKin();
    }, [userId]);

    const totalAllocated = kins.reduce((sum, kin) => sum + (kin.allocationPercentage || 0), 0);

    const handleOpenCreateForm = () => {
        setFormData({
            fullName: '',
            relationship: '',
            primaryPhone: '',
            idNumber: '',
            physicalAddress: '',
            allocationPercentage: 0
        });
        setEditingKinId(null);
        setShowForm(true);
        setError(null);
        setSuccessMsg(null);
    };

    const handleOpenEditForm = (kin) => {
        setFormData({
            fullName: kin.fullName || '',
            relationship: kin.relationship || '',
            primaryPhone: kin.primaryPhone || '',
            idNumber: kin.idNumber || '',
            physicalAddress: kin.physicalAddress || '',
            allocationPercentage: kin.allocationPercentage || 0
        });
        setEditingKinId(kin.kinId);
        setShowForm(true);
        setError(null);
        setSuccessMsg(null);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);
        setError(null);
        setSuccessMsg(null);

        try {
            if (editingKinId) {
                await nextOfKinApi.updateNextOfKin(editingKinId, formData);
                setSuccessMsg('Beneficiary updated successfully!');
            } else {
                await nextOfKinApi.addNextOfKin(userId, formData);
                setSuccessMsg('Beneficiary added successfully!');
            }
            setShowForm(false);
            fetchNextOfKin();
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to save beneficiary');
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleDelete = async (kinId) => {
        if (!window.confirm("Are you sure you want to remove this nominated beneficiary?")) return;
        setIsSubmitting(true);
        setError(null);
        try {
            await nextOfKinApi.deleteNextOfKin(kinId);
            setSuccessMsg("Beneficiary removed successfully.");
            fetchNextOfKin();
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to delete beneficiary');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="space-y-6">
            {/* Real-time Allocation Progress Bar */}
            <div className="bdae-card p-4 rounded-xl border border-[var(--bdae-border)] bg-black/5 dark:bg-white/5 space-y-2">
                <div className="flex justify-between items-center mb-1">
                    <span className="text-xs font-bold text-[var(--bdae-text-primary)] flex items-center gap-2">
                        <Heart className="w-4 h-4 text-rose-500" /> Payout Allocation Pool
                    </span>
                    <span className={`text-[11px] font-bold ${totalAllocated > 100 ? 'text-red-500' : totalAllocated === 100 ? 'text-emerald-500' : 'text-amber-500'}`}>
                        {totalAllocated.toFixed(2)}% / 100.00%
                    </span>
                </div>
                <div className="w-full bg-black/10 dark:bg-white/10 rounded-full h-2">
                    <div
                        className={`h-2 rounded-full transition-all duration-500 ${totalAllocated > 100 ? 'bg-red-500' : totalAllocated === 100 ? 'bg-emerald-500' : 'bg-rose-500'}`}
                        style={{ width: `${Math.min(totalAllocated, 100)}%` }}
                    />
                </div>
                <p className="text-[10px] text-[var(--bdae-text-secondary)]">Remaining margin: {(100 - totalAllocated).toFixed(2)}%</p>
            </div>

            {error && (
                <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-500 text-xs flex gap-2">
                    <AlertCircle className="w-4 h-4 shrink-0" /><span>{error}</span>
                </div>
            )}
            {successMsg && (
                <div className="p-3 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-600 text-xs flex gap-2">
                    <CheckCircle className="w-4 h-4 shrink-0" /><span>{successMsg}</span>
                </div>
            )}

            <div className="flex justify-between items-center">
                <div className="text-xs font-bold text-[var(--bdae-text-secondary)]">Nominated Beneficiaries</div>
                <PermissionGuard permissions={['NEXT_OF_KIN_MANAGE']}>
                    {!showForm && totalAllocated < 100 && (
                        <button
                            type="button"
                            onClick={handleOpenCreateForm}
                            className="px-3 py-1.5 bg-rose-500/10 hover:bg-rose-500/20 text-rose-500 border border-rose-500/20 text-[11px] font-bold rounded-lg flex items-center gap-1 transition-all"
                        >
                            <Plus className="w-3 h-3" /> Add Beneficiary
                        </button>
                    )}
                </PermissionGuard>
            </div>

            {showForm && (
                <PermissionGuard permissions={['NEXT_OF_KIN_MANAGE']}>
                    <form onSubmit={handleSubmit} className="p-4 border border-[var(--bdae-border)] rounded-xl space-y-4 shadow-sm bg-[var(--bdae-surface)]">
                        <div className="flex items-center gap-2 pb-2 border-b border-[var(--bdae-border)]">
                            <Users className="w-4 h-4 text-[var(--bdae-primary)]" />
                            <h3 className="text-xs font-bold">{editingKinId ? 'Edit Beneficiary' : 'Register New Beneficiary'}</h3>
                        </div>

                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">Full Name</label>
                                <input type="text" required pattern="^[A-Za-z\s\-']{2,150}$" value={formData.fullName} onChange={e => setFormData({ ...formData, fullName: e.target.value })} className="w-full px-3 py-2 text-xs bg-black/5 border border-[var(--bdae-border)] rounded-xl" />
                            </div>
                            <div>
                                <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">Relationship</label>
                                <input type="text" required value={formData.relationship} onChange={e => setFormData({ ...formData, relationship: e.target.value })} className="w-full px-3 py-2 text-xs bg-black/5 border border-[var(--bdae-border)] rounded-xl" />
                            </div>
                            <div>
                                <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">Primary Phone</label>
                                <input type="text" required value={formData.primaryPhone} onChange={e => setFormData({ ...formData, primaryPhone: e.target.value })} className="w-full px-3 py-2 text-xs font-mono bg-black/5 border border-[var(--bdae-border)] rounded-xl" />
                            </div>
                            <div>
                                <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">ID / Passport Number</label>
                                <input type="text" value={formData.idNumber} onChange={e => setFormData({ ...formData, idNumber: e.target.value })} className="w-full px-3 py-2 text-xs font-mono bg-black/5 border border-[var(--bdae-border)] rounded-xl" />
                            </div>
                            <div className="col-span-2">
                                <label className="block text-[11px] font-bold text-[var(--bdae-text-secondary)] mb-1 uppercase">Physical Address</label>
                                <input type="text" required value={formData.physicalAddress} onChange={e => setFormData({ ...formData, physicalAddress: e.target.value })} placeholder="e.g. City, Kebele, House No" className="w-full px-3 py-2 text-xs bg-black/5 border border-[var(--bdae-border)] rounded-xl" />
                            </div>
                            <div className="col-span-2 p-3 bg-red-500/5 border border-red-500/20 rounded-xl flex items-center justify-between">
                                <div>
                                    <label className="block text-[11px] font-bold text-red-600 mb-0.5 uppercase">Payout Allocation (%)</label>
                                    <p className="text-[10px] text-red-600/70">Ensure total doesn't exceed 100% boundary.</p>
                                </div>
                                <input
                                    type="number"
                                    step="0.01"
                                    min="0.01"
                                    max="100"
                                    required
                                    value={formData.allocationPercentage}
                                    onChange={e => setFormData({ ...formData, allocationPercentage: parseFloat(e.target.value) || 0 })}
                                    className="w-24 px-3 py-2 text-xs font-mono font-bold text-right bg-white dark:bg-black border border-red-500/30 rounded-lg outline-none focus:border-red-500 text-red-600"
                                />
                            </div>
                        </div>

                        <div className="flex justify-end gap-2 pt-2">
                            <button type="button" onClick={() => setShowForm(false)} className="px-4 py-2 text-xs font-bold text-[var(--bdae-text-primary)] hover:bg-black/5 rounded-lg border border-[var(--bdae-border)]">
                                Cancel
                            </button>
                            <button type="submit" disabled={isSubmitting} className="bdae-btn-primary px-4 py-2 text-xs font-bold rounded-lg flex items-center gap-2">
                                {isSubmitting ? <RefreshCw className="w-3.5 h-3.5 animate-spin" /> : <Save className="w-3.5 h-3.5" />}
                                {editingKinId ? 'Update Beneficiary' : 'Save Beneficiary'}
                            </button>
                        </div>
                    </form>
                </PermissionGuard>
            )}

            {isLoading ? (
                <div className="text-center py-6">
                    <RefreshCw className="w-5 h-5 animate-spin mx-auto text-[var(--bdae-secondary)]" />
                </div>
            ) : kins.length === 0 && !showForm ? (
                <div className="text-center py-6 text-xs text-[var(--bdae-text-secondary)] border border-[var(--bdae-border)] rounded-xl border-dashed">
                    <Heart className="w-8 h-8 opacity-20 mx-auto mb-2 text-rose-500" />
                    No beneficiaries have been nominated for this member profile yet.
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {kins.map((kin) => (
                        <div key={kin.kinId} className="p-4 border border-[var(--bdae-border)] hover:border-rose-500/30 rounded-xl bg-[var(--bdae-surface)] transition-all flex flex-col justify-between group">
                            <div>
                                <div className="flex justify-between items-start mb-2">
                                    <div>
                                        <h4 className="text-sm font-bold text-[var(--bdae-text-primary)]">
                                            {kin.fullName}
                                        </h4>
                                        <div className="inline-flex items-center px-2 py-0.5 mt-1 rounded bg-black/5 dark:bg-white/5 border border-black/10 text-[10px] font-bold text-[var(--bdae-text-secondary)] uppercase">
                                            {kin.relationship}
                                        </div>
                                    </div>
                                    <div className="bg-rose-500/10 text-rose-600 border border-rose-500/20 px-2.5 py-1 rounded-lg text-xs font-mono font-bold">
                                        {kin.allocationPercentage}%
                                    </div>
                                </div>
                                <div className="space-y-1 mt-4">
                                    <p className="text-[11px] text-[var(--bdae-text-secondary)] font-mono">📱 {kin.primaryPhone}</p>
                                    {kin.idNumber && <p className="text-[11px] text-[var(--bdae-text-secondary)] font-mono">🪪 {kin.idNumber}</p>}
                                    <p className="text-[10px] text-[var(--bdae-text-secondary)] line-clamp-1 opacity-80 mt-1">{kin.physicalAddress}</p>
                                </div>
                            </div>

                            <PermissionGuard permissions={['NEXT_OF_KIN_MANAGE']}>
                                <div className="flex items-center gap-2 mt-4 pt-3 border-t border-[var(--bdae-border)]">
                                    <button
                                        onClick={() => handleOpenEditForm(kin)}
                                        className="flex-1 flex justify-center items-center gap-1.5 px-3 py-1.5 rounded-lg border border-[var(--bdae-border)] text-xs font-bold text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-text-primary)] hover:bg-black/5 transition-colors"
                                    >
                                        <Edit3 className="w-3.5 h-3.5" /> Edit
                                    </button>
                                    <button
                                        onClick={() => handleDelete(kin.kinId)}
                                        className="flex-1 flex justify-center items-center gap-1.5 px-3 py-1.5 rounded-lg border border-red-500/20 text-xs font-bold text-red-500 hover:bg-red-500/10 transition-colors"
                                    >
                                        <Trash2 className="w-3.5 h-3.5" /> Remove
                                    </button>
                                </div>
                            </PermissionGuard>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};
